import os
import boto3
import sagemaker
import re
import json
import pandas as pd

from sagemaker import hyperparameters
from sagemaker.s3 import S3Uploader
from sagemaker.jumpstart.estimator import JumpStartEstimator

from fine_tuning.constants import AWS_DEFAULT_REGION, S3_DATASET_BUCKET

os.environ['AWS_DEFAULT_REGION'] = AWS_DEFAULT_REGION


def clean_line(row):
    line = re.sub(r'\[.*?\]', '', row['Line'])
    return line.replace("[quick cut scene]", "").replace("[on the phone]", "")


def get_train_data_location():
    output_bucket = sagemaker.Session(boto3.session.Session(
        aws_access_key_id=os.environ["MATHILDE_AWS_ACCESS_KEY"],
        aws_secret_access_key=os.environ["MATHILDE_AWS_SECRET_KEY"]
    )).default_bucket()
    train_data_location = f"s3://{output_bucket}/{S3_DATASET_BUCKET}"
    return train_data_location


def upload_to_training_s3(local_file):
    S3Uploader.upload(local_file, get_train_data_location())


def fetch_lines():
    lines = pd.read_csv("./dataset/the_office_lines.csv", index_col="Index")
    lines["CleanedLines"] = lines.apply(clean_line, axis=1)
    lines = lines.drop(["Line"], axis=1)
    lines = lines.rename(columns={"CleanedLines": "Line"})
    return lines


def extract_conversations(characters=None):
    lines = fetch_lines()
    lines_list = list(lines.itertuples(index=True))
    conversations, current_conversation, cleaned_conversation = [], [], []

    for line in lines_list:
        if not current_conversation:
            current_conversation = [line]
            continue
        if line.Character in set([l.Character for l in current_conversation]):
            current_conversation.append(line)
            continue
        if len(set([l.Character for l in current_conversation])) == 2:
            # we already have a set of conversation back and forth, we can start a new conversation:
            conversations.append(current_conversation)
            current_conversation = [line]
            continue
        if len(set([l.Character for l in current_conversation])) == 1:
            current_conversation.append(line)
    if len(set([l.Character for l in current_conversation])) == 2:
        conversations.append(current_conversation)

    for conversation in conversations:
        # we need to remove any first or last message if they are the same as the next one (or before one)
        if conversation[0].Character == conversation[1].Character:
            del conversation[0]
        if conversation[-1].Character == conversation[-2].Character:
            del conversation[-1]
        if len(conversation) >= 3:
            if characters and set([c.Character for c in conversation]) & set(characters) == set([c.Character for c in conversation]):
                cleaned_conversation.append(conversation)
            elif not characters:
                cleaned_conversation.append(conversation)
    return cleaned_conversation


def prepare_conversations_as_dataset(conversations):
    jsonl_data = ""
    for conversation in conversations:
        for i in range(2, len(conversation)):
            current_conversation_so_far = "\n".join([f"{line.Character}: {line.Line}" for line in conversation[max(0, i-5):i]])
            to_character = conversation[i].Character
            context = current_conversation_so_far + "\n" + f"{to_character}: "
            jsonl_data += json.dumps({
                "instruction": "Given the beginning of a conversation between two movie characters, provide a line of dialogue that continues the conversation.",
                "context": context,
                "response": conversation[i].Line
            }) + "\n"
    jsonl_data = jsonl_data.strip("\n")
    return jsonl_data


def create_train_jsonl(characters=None):
    conversations = extract_conversations(characters)
    jsonl_data = prepare_conversations_as_dataset(conversations)

    # Save it locally
    with open("dataset/train.jsonl", "w") as f:
        f.write(jsonl_data)


def upload_training_files_to_s3():
    upload_to_training_s3("./dataset/template.json")
    upload_to_training_s3("dataset/train.jsonl")


def fine_tune_model_with_mistral():
    role = 'Sagemaker_deploy_training_role'
    model_id, model_version = "huggingface-llm-mistral-7b", "*"
    mistral_hyperparameters = hyperparameters.retrieve_default(
        model_id=model_id, model_version=model_version
    )
    mistral_hyperparameters["epoch"] = "1"
    mistral_hyperparameters["per_device_train_batch_size"] = "2"
    mistral_hyperparameters["gradient_accumulation_steps"] = "2"
    mistral_hyperparameters["instruction_tuned"] = "True"
    mistral_hyperparameters["lora_dropout"] = "0.05"

    # Validating hyper parameters:
    hyperparameters.validate(
        model_id=model_id,
        model_version=model_version,
        hyperparameters=mistral_hyperparameters
    )

    jump_start_estimator = JumpStartEstimator(
        role=role,
        model_id=model_id,
        hyperparameters=mistral_hyperparameters,
        instance_type="ml.g5.24xlarge"
    )
    print(jump_start_estimator.hyperparameters())
    jump_start_estimator.fit({"train": get_train_data_location()}, logs="Training")
    return jump_start_estimator


def fine_tune_model_with_llama():
    role = 'Sagemaker_deploy_training_role'
    model_id, model_version = "meta-textgeneration-llama-2-13b", "3.1.0"
    jump_start_estimator = JumpStartEstimator(
        role=role,
        model_id=model_id,
        model_version=model_version,
        environment={"accept_eula": "true"},
        disable_output_compression=True,
    )

    jump_start_estimator.set_hyperparameters(instruction_tuned="True", epoch="1", max_input_length="1024")
    jump_start_estimator.fit({"training": get_train_data_location()})
    return jump_start_estimator


if __name__ == "__main__":
    # create_train_jsonl(["Pam", "Jim", "Michael", "Dwight", "Angela"])
    # upload_training_files_to_s3()
    # estimator = fine_tune_model_with_mistral()
    estimator = fine_tune_model_with_llama()

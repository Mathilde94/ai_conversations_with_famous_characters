CONTINUE_CONVERSATION_BELOW = """Below is an instruction that describes a task, paired with an input that provides further context.
Write a response that appropriately completes the request.

Instruction:
Given the beginning of a conversation between two movie characters, provide a line of dialogue that continues the conversation.

Context of the current conversation:
{current_conversation}
{to_character}:"""

CONTEXT = "context"

TRAINING_SET_SIZE = 25000

AWS_DEFAULT_REGION = "us-west-2"

S3_DATASET_BUCKET = "the_office_characters_dataset"
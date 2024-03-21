import os
import sys

from typing import List, Tuple

from fine_tuning.constants import CONTINUE_CONVERSATION_BELOW
from fine_tuning.local_predictor import LocalPredictor, LocalStreamPredictor


def predict_finetune(fine_tuned_predictor, conversations: List[Tuple[str, str]], to_character: str):
    current_conversation_so_far = "\n".join([f"{character}: {line}" for character, line in conversations])
    payload = {
        "inputs": CONTINUE_CONVERSATION_BELOW.format(current_conversation=current_conversation_so_far, to_character=to_character),
        "parameters": {"max_new_tokens": 100},
    }
    fine_tuned_response = fine_tuned_predictor.predict(payload)
    return fine_tuned_response


def start_chat_with(predictor: LocalPredictor, from_character: str, to_character: str):
    conversation = []

    while True:
        try:
            new_message = input(f"{from_character}: ")
            conversation.append((from_character, new_message))
            print(f"{to_character}:", end="")
            response = predict_finetune(predictor, conversation[-2:], to_character)
            # print(response)
            print()
            conversation.append((to_character, response))
        except KeyboardInterrupt:
            print("Exiting chat...")
            break


if __name__ == '__main__':
    try:
        port = int(sys.argv[1])
    except IndexError:
        port = 8080
    local_fine_tuned_predictor = LocalStreamPredictor(port)
    # start_chat_with(local_fine_tuned_predictor, "Jim", "Pam")
    start_chat_with(local_fine_tuned_predictor, "Jim", "Pam")


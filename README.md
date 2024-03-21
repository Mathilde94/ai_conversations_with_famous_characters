# Movie Characters AI

## Introduction
This project is a simple implementation of a chat between famous movie characters where you impersonate one of them (for this case here, characters are from the show `The Office`). 

https://github.com/Mathilde94/character_ai_office/assets/1518309/bb8bc9e8-88e2-4a51-8ca5-a9058748fe84

## Main Components

### Fine-tuning the LLM(s)
While regular LLMs have probably a knowledge of main character movies, they may not have specific intonations 
from a character talking to another character. 

Hence, after getting access to public captions from a TV show, preparing it and fine tuning it on my AWS Sagemaker
account, I downloaded the model configurations and prepared it for local inference on my GPU. 

Once the LLM finetuned, I created a few more components to use it. 

### Backend
The backend server is a kotlin springboot server, with gRPC interfaces. It can stream data back to a client.
There is also an envoy proxy that converts browser HTTP1 request for grpc to HTTP2. 

### Frontend
A frontend webpack application for the chat experience between a user (representing a character) and a simulated character (from the LLM). 

## How To

### Fine-tuning LLama13B and Mistral
Please check the readme under `fine_tuning` for more details on fine tuning steps and how to run locally
the fine-tuned inference model.

### Running backend server
To run and/or develop on the springboot chat server, please check the README.md under `backend`.

### Running frontend server
To run and/or develop on the chat frontend, please check the README.md under `frontend`.

## Examples

![selection.png](media%2Fselection.png)

![dwight_and_jim.png](media%2Fdwight_and_jim.png)

![michael_and_pam.png](media%2Fmichael_and_pam.png)

https://github.com/Mathilde94/character_ai_office/assets/1518309/b9a78c7a-39be-404f-940b-c42d3719f7fa



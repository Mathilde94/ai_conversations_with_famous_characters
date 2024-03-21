# Fine-Tuning LLama13b for character conversations

## Fine-tuning with AWS Sagemaker
This directory contains the code to fine-tune the LLama13b model for conversations with famous characters.
- Create an AWS role `Sagemaker_deploy_training_role`
- Make sure you have quota to run `ml.g5.24xlarge` instances, needed for fine-tuning llama 13b
- Go to the `fine_tuning` directory
- Run `python main.py`, monitor the progress. It should take about 1h to fine-tune the model

## Inference model
To run the inference model, you need to follow these steps:
- clone `https://github.com/ggerganov/llama.cpp` and go to `llama.cpp` directory
- download the fine-tuned model files in S3: they should be in a S3 bucket that looks like that:
    - `sagemaker-us-west-2-<id> / meta-textgeneration-llama-2-13b-<date-format>/output/model`
 ![s3_model_generated_files.png](..%2Fmedia%2Fs3_model_generated_files.png) 
    - place them in the `models/the_office_mistral` directory
    - the main files needed: `*.safetensors`, `tokenizer.model`, `tokenizer_config.json`, `special_tokens_map.json`, `config.json`, `generation_config.json`
- Follow the steps to convert this model in llama.cpp:
    - `python3 convert.py models/the_office_mistral`
    - `./quantize ./models/the_office_mistral/ggml-model-f16.gguf ./models/the_office_mistral/ggml-model-Q4_K_M.gguf Q4_K_M`
    - `./quantize ./models/the_office_mistral/ggml-model-Q4_K_M.gguf ./models/the_office_mistral/ggml-model-Q4_K_M-v2.gguf COPY`

You can run the same steps with the regular Mistral [here](https://huggingface.co/docs/transformers/main/en/model_doc/mistral) so you can compare results


Then you can run the inference that way:
```shell
./server -m models/the_office_mistral/ggml-model-Q4_K_M.gguf -c 2048 --port 8080
```

And for the non fine-tuned:
```shell
./server -m models/mistral/ggml-model-Q4_K_M.gguf -c 2048 --port 8085
```

## Comparisons
When running a conversation with fine tune mistral:
```
Jim: Do you remmember who I told that I had a crush on you?
Pam: Yeah
Jim: who was it?
Pam: Michael Scott.

Jim: Thank you for agreeing to go to Austin, I m sure you won't regret it!
Pam: Yeah, I definitely won't regret it.
Jim: Any hobby you would like to do there?
Pam: Oh, definitely. I think I'm gonna start a new business.
Jim: ahahaha .. sure.. about what?
Pam: I think it might be fun to start anew business together.
Jim: oh really? 
Pam: I have my eye on some great locations for a new business.
Jim: is it about drawing?
Pam: Jim, Jim, get back to work.
Jim: you sure? 
Pam: Jim, Jim, Jim.
Jim: ok fine.. I ll say goodbye to Dwight
Pam: Good luck!
Jim: Have you talked to Michael recently?
Pam: I talked to Michael about the trip. He said he might come along

# With llama2:
Jim: It has been a long time! How is Molly?
Michael: Uh, I don’t know. She is a little out of my life.
Jim: Ohh, you are not with her anymore?
Michael: No, I am not.
Jim: Who are you with now?
Michael: I am with someone.
Jim: Who is it?
Michael: I am with someone!
Jim: Is it Jan?
Michael: No.
Jim: Is it.. Pam's mom?'
Michael: No.
Jim: So who are you living with now?
Michael: Oh, I'm living with Ryan.

Jim: >? Do you think robots are better than humans?
Dwight: Yes.
Jim: >? Why so?
Dwight: It was a big deal.
Jim: >? what was a big deal?
Dwight: You were a big deal.
Jim: >? why is that related to robots better than human?
Dwight: It’s not. You were a big deal, and now you’re not.
Jim: >? well then so do you 
Dwight: No I don’t.
Jim: >? why not? You think you are better than me?
Dwight: No.
Jim: >? so you are not a robot
Dwight: No. I am a man.
Jim: >? so you were a big deal and now you are not anymore
Dwight: I was the biggest deal.
Jim: >? Then what happened?
Dwight: I got fired.
Jim: >? You got fired?!
Dwight: I got fired!
Jim: >? Who fired you?
Dwight: I don’t know. I was talking to the head of HR, and he said that
Jim: >? You think Michael would hire you back?
Dwight: He said he wasn’t sure. I don’t know.
Jim: >? So what do you do now that you are without a job?
Dwight: I am thinking of starting my own paper company.
Jim: >? you are kidding?
Dwight: No.
Jim: >? With Angela? You have no partners
Dwight: No.
Jim: >? ah so .. by yourself
Dwight: I'll do it myself.
Jim: >? you are not afraid?
Dwight: What is there to be afraid of?
Jim: >? and you are that sure you will be ok?
Dwight: That is right. I am standing on my own two feet.
Jim: >? It is nice to see you believe in yourself, Dwight
Dwight: Believe in myself? I don’t believe in you.

Jim: >? How are you today?
Dwight: I’m having a terrible day.
Jim: >? why so?
Dwight: Well, I’m in an accident.
Jim: >? what?? What happened?
Dwight: I was in an accident.
Jim: >? what happened?
Dwight: I was driving to Scranton when I hit a patch of black ice.
Jim: >? Oh no, is your car ok? 
Dwight: No, it’s totaled.
Jim: >? and where are you now?
Dwight: 14th and B.
Jim: >? do you need someone to help you?
Dwight: Yeah, I do.
Jim: >? Do you want me to get Angela?
Dwight: No, no. I’ll do it myself.
Jim: >? you sure?
Dwight: I’m sure.

Much more sassy Dwight than regular LLM:
Jim: >? How are you today?
Dwight: I don't know, how are you?
Jim: >? I m doing well thanks
Dwight: And you
Jim: >? And you? Dwight, how are you?
Dwight: I'm good.
Jim: >? Anything new in your life?
Dwight: Well, I did go to a new movie last night.
Jim: >? ohh, what kind?
Dwight: It’s a thriller.
Jim: >? a thriller? Were you scared?
Dwight: No, I was bored.
Jim: >? oh.. Did Angela go with you?
Dwight: No, she was busy.
Jim: >? doing what?
Dwight: working
Jim: >? Working? What was she working on?
Dwight: You know, the project.
Jim: >? The secret project?
Dwight: I don't know if it's a secret.
Jim: >? so you can tell me what it is then
Dwight: No, I can't.
Jim: >? why not?
Dwight: Because I'm too busy.
Jim: >? oh wow.. Very nice of you 
Dwight: Oh, yeah. And I'm going to get you something from the vending machine.
Jim: >? Really? 
Dwight: Yeah. A soda?
Jim: >? please
Dwight: I can't give you a soda.
Jim: >? why not? You said you would
Dwight: you didn't order a soda.
Jim: >? Ok.. Can I order a soda from you then?
Dwight: Sure.
Jim: >? A coca cola can please
Dwight: I think they have it.
Jim: >? great
Dwight: I’ll tell you what, you go down to the break room and get me a coffee refill
Jim: 
```

With non fine-tuned mistral, characters do not have all the context of details of their conversations from the show.
For example, Dwight is not as sassy as he is in the fine-tuned model, or does not recall having been fired exactly. 
Non Fine tuned models still have an overall good knowledge of what those characters are.
```
Jim: Thank you for agreeing to go to Austin, I m sure you won't regret it!
Pam: Austin? No, it's okay. I'm not that interested in going to Austin
Jim: Why not? You said you would go with me to support the business
Pam: I can't. I have a business meeting at 9 am
Jim: What about our daughters?
Pam: I'm sorry, Jim. They can go with me instead
Jim: This is not you, Pam
Pam: I'm sorry, Jim. I need to go to Austin

Jim: Do you like drawing much?
Pam: I do.
Jim: Have you ever drawn something for our offices?
Pam: I haven't, but I could

Jim: Do you know a Michael?
Pam: Yes, my brother
Jim: anyone else?
Pam: No, I don't think so

Jim: Do you remmember who I told that I had a crush on you?
Pam: You said you had a crush on me?

Jim: >? Do you think robots are better than humans?
Dwight: I think robots are better than humans because they can do more things.
Jim: >? do you think you are better than robots though?
Dwight: No, I don’t think so.
Jim: >? Have you ever been a big deal?
Dwight: I’m not sure what you mean.
Jim: >? Have you ever worked at a paper company?
Dwight: I’m not sure what you mean.
Jim: >? Have you ever been fired?
Dwight: Fired?
Jim: >? yes
Dwight: what is this?
Jim: >? Have you ever lost your job?
Dwight: No.

```
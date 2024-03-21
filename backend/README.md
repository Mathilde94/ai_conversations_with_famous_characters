# Springboot Server

## Setup
Environment:
```shell
./gradlew spotlessApply
./gradlew build test
./gradlew koverVerify koverHtmlReport
```
![coverage_backend.png](..%2Fmedia%2Fcoverage_backend.png)

Running the server:
```shell
./gradlew run
```

## Running the components
To initialize the redis storage and envoy for chats:

```shell
docker-compose up -d
```

## Postman Screenshots
Initializing a session:
![InitializeChatSession.png](..%2Fmedia%2FInitializeChatSession.png)

Sending a message and receiving the real time stream back:
![StreamSendMessage.png](..%2Fmedia%2FStreamSendMessage.png)

Then a final grpc call where you can see all messages in a session:
![GetSession.png](..%2Fmedia%2FGetSession.png)

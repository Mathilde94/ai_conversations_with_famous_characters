# Frontends

## Setup
Run:

```shell
npm install
npm run build
```

Generate the JS protobufs:
```shell
mkdir src/protobufs
./node_modules/.bin/grpc_tools_node_protoc --js_out=import_style=commonjs,binary:./src/protobufs --grpc_out=./src/protobufs  -I ./../protobufs ./../protobufs/dto/chat/v1/chat.proto
# For web client grpc purposes: install protoc-gen-grpc-web
./node_modules/.bin/grpc_tools_node_protoc --js_out=import_style=commonjs:./src/protobufs --grpc-web_out=import_style=commonjs,mode=grpcwebtext:./src/protobufs  -I ./../protobufs ./../protobufs/rpc/chat/v1/chat_service.proto
```

Then the webpack server:
```shell
npx webpack serve
```

Next go to: http://0.0.0.0:8081/

## Examples
![pam_and_jim_0.png](..%2Fmedia%2Fpam_and_jim_0.png)

![dwight_and_pam.png](..%2Fmedia%2Fdwight_and_pam.png)

![dwight_and_pam_1.png](..%2Fmedia%2Fdwight_and_pam_1.png)

![pam_and_jim_1.png](..%2Fmedia%2Fpam_and_jim_1.png)

import {action, observable, runInAction} from "mobx";
import {ChatMessage} from '../protobufs/dto/chat/v1/chat_pb';
import {GetChatSessionRequest, ResetChatSessionRequest, SendMessageStreamRequest} from '../protobufs/rpc/chat/v1/chat_service_pb';
import {ChatServiceClient} from '../protobufs/rpc/chat/v1/chat_service_grpc_web_pb';

import Conversation, {Message} from "./model";

export default class ConversationMobxStore {
    @observable accessor userCharacter = "";
    @observable accessor simulatedCharacter = "";
    @observable accessor conversation = null;
    @observable accessor latestUserInput = ""
    @observable accessor currentResponse = ""
    client = new ChatServiceClient("http://localhost:8082")

    constructor(sessionId) {
        this.sessionId = sessionId
    }

    @action
    updateLatestUserMessage(message) {
        this.latestUserInput = message
    }

    @action
    sendMessage() {
        runInAction(() => {
            if (this.currentResponse !== "") {
                const lastSimulatedCharacterMessage = new Message(
                    "ASSISTANT",
                    this.currentResponse,
                    "now",
                    this.conversation.simulatedCharacter
                )
                this.conversation.addNewMessage(lastSimulatedCharacterMessage)
            }
            this.currentResponse = "";
            this.conversation.addNewMessage(new Message("USER", this.latestUserInput, "now", this.conversation.user))

            const newMessage = new ChatMessage().setContent(this.latestUserInput)
            const sendMessageStreamRequest = new SendMessageStreamRequest()
                .setChatSessionId(this.sessionId)
                .setMessage(newMessage)
            this.latestUserInput = ""

            const stream= this.client.sendMessageStream(
                sendMessageStreamRequest,
                {}
            )

            stream.on('data', function(response){
                this.currentResponse = this.currentResponse + response.getChunkMessage()
            }.bind(this));
        })
    }

    @action
    async loadConversation() {
        const getChatSessionRequest = new GetChatSessionRequest()
            .setChatSessionId(this.sessionId)
        await this.client.getChatSession(
            getChatSessionRequest,
            {},
            (error, response) => {
                runInAction(() => {
                    const chatSession = response.getChatSession()
                    const messages = chatSession.getMessagesList().map((message) => {
                        return new Message(message.getRole(), message.getContent(), message.getCreatedAt(), message.getCharacter().getName())
                    })
                    this.simulatedCharacter = chatSession.getCharacterSimulated().getName();
                    this.userCharacter = chatSession.getCharacterUser().getName()
                    this.conversation = new Conversation({
                        sessionId: this.sessionId,
                        simulatedCharacter: chatSession.getCharacterSimulated().getName(),
                        user: chatSession.getCharacterUser().getName(),
                        messages
                    })
                })
            }
        )
    }

    @action
    async resetConversation() {
        const getChatSessionRequest = new ResetChatSessionRequest()
            .setChatSessionId(this.sessionId)
        await this.client.resetChatSession(
            getChatSessionRequest,
            {},
            (error, response) => {
                runInAction(() => {
                    const chatSession = response.getChatSession()
                    const messages = chatSession.getMessagesList().map((message) => {
                        return new Message(message.getRole(), message.getContent(), message.getCreatedAt(), message.getCharacter().getName())
                    })
                    this.currentResponse = ""
                    this.simulatedCharacter = chatSession.getCharacterSimulated().getName();
                    this.userCharacter = chatSession.getCharacterUser().getName()
                    this.conversation = new Conversation({
                        sessionId: this.sessionId,
                        simulatedCharacter: chatSession.getCharacterSimulated().getName(),
                        user: chatSession.getCharacterUser().getName(),
                        messages
                    })
                })
            }
        )

    }
}
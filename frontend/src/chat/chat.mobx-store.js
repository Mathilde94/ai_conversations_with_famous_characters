import {action, computed, observable, runInAction} from "mobx";
import {Character} from '../protobufs/dto/chat/v1/chat_pb';
import {InitiateChatSessionRequest} from '../protobufs/rpc/chat/v1/chat_service_pb';
import {ChatServiceClient} from '../protobufs/rpc/chat/v1/chat_service_grpc_web_pb';


export default class ChatMobxStore {
    @observable accessor sessionId = null;
    @observable accessor userCharacterName = "Jim";
    @observable accessor simulatedCharacterName = "Pam";
    client = new ChatServiceClient("http://localhost:8082")

    @action
    setUserCharacter(newUserCharacterName) {
        this.userCharacterName = newUserCharacterName
    }

    @action
    setSimulatedCharacter(newSimulatedCharacterName) {
        this.simulatedCharacterName = newSimulatedCharacterName
    }

    @computed
    get hasValidSession() {
        return this.sessionId !== null
    }

    @action
    setSessionId(sessionId) {
        this.sessionId = sessionId
    }

    @action
    async createChatSession() {
        const simulatedCharacter = new Character().setName(this.simulatedCharacterName)
        const userCharacter = new Character().setName(this.userCharacterName)
        console.log("Creaeting a session between: ", this.simulatedCharacterName, this.userCharacterName);
        const initiateChatSessionRequest = new InitiateChatSessionRequest()
            .setCharacterSimulated(simulatedCharacter)
            .setCharacterUser(userCharacter)

        await this.client.initiateChatSession(
            initiateChatSessionRequest,
            {},
            (error, response) => {
                runInAction(() => {
                    const chatSession = response.getChatSession()
                    this.setSessionId(chatSession.getUuid())
                })
            }
        )

    }
}
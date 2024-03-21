export class Message {
    constructor(role, content, createdAt, name) {
        this.role = role
        this.content = content
        this.createdAt = createdAt
        this.userName = name
    }
}

export default class Conversation {
    constructor(props) {
        this.sessionId = props.sessionId
        this.simulatedCharacter = props.simulatedCharacter
        this.user = props.user
        this.messages = props.messages
    }

    addNewMessage(message) {
        this.messages.push(message)
    }
}
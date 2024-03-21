import React, {Component} from 'react';
import {observer} from 'mobx-react';
import {action, runInAction} from "mobx";

import ConversationMobxStore from "./conversation.mobx-store";
import './styles.css';

@observer
export default class ConversationReactComponent extends Component {
    constructor(props) {
        super(props);
        this.conversationId = props.conversationId;
        this.store = new ConversationMobxStore(this.conversationId)
    }

    componentDidMount() {
        this.store.loadConversation()
    }

    renderIndividualMessage(content, user) {
        let messageSource = user === this.store.userCharacter ? "user": "simulated"
        return (<div className={`message ${messageSource}`}>
            <div>
                <span>{content}</span>
            </div>
        </div>)
    }

    renderChatMessages() {
        const messages = this.store.conversation.messages
        return (<div className="messages">
            {messages.map((message, index) => (<div key={index} className={`message`}>
                {this.renderIndividualMessage(message.content, message.userName)}
            </div>))}
            {this.store.currentResponse && (<div>
                {this.renderIndividualMessage(this.store.currentResponse, this.store.simulatedCharacter)}
            </div>)}
        </div>)
    }

    @action
    updateLatestUserMessage(event) {
        runInAction(() => {
            this.store.updateLatestUserMessage(event.target.value)
        })
    }

    @action
    sendMessage(event) {
        event.preventDefault();
        this.store.sendMessage()
    }

    @action
    resetConversation() {
        console.log("Resetting: ")
        this.store.resetConversation()
    }

    render() {
        return (<div className="conversation">
            <div className="title">
                <span>Conversation between: {this.store.userCharacter} (you) and {this.store.simulatedCharacter}</span>
                <button onClick={() => this.resetConversation()}>Reset</button>
            </div>
            {this.store.conversation && this.renderChatMessages()}
            <form onSubmit={(e) => this.sendMessage(e)}>
                <input
                    placeholder={`Want to say something to ${this.store.simulatedCharacter}?`}
                    value={this.store.latestUserInput}
                    size="medium"
                    onChange={e => this.updateLatestUserMessage(e)}
                />
                <button type="submit">Send</button>
            </form>
        </div>)
    }
}
import React, {Component} from 'react';
import {observer} from 'mobx-react';
import {action} from "mobx";
import ChatMobxStore from "./chat.mobx-store";
import ConversationReactComponent from "../conversation/conversation.react-component";
import Dropdown from 'react-dropdown';

import 'react-dropdown/style.css';
import './styles.css'

const CHARACTERS = [
    'Jim', 'Pam', 'Dwight', 'Michael', 'Angela'
];

@observer
export default class ChatReactComponent extends Component {
    constructor(props) {
        super(props);
        const urlSearchString = window.location.search;
        const params = new URLSearchParams(urlSearchString);
        this.conversationId = params.get("conversationId");
        this.store = new ChatMobxStore()
    }

    componentDidMount() {
        if (this.conversationId !== undefined) {
            this.store.setSessionId(this.conversationId);
        }
    }

    @action
    createChat(event) {
        event.preventDefault();
        this.store.createChatSession()
    }
    @action
    setUserCharacter(selectedOption) {
        this.store.setUserCharacter(selectedOption.value)
    }

    @action
    setSimulatedCharacter(selectedOption) {
        this.store.setSimulatedCharacter(selectedOption.value)
    }

    render() {
        if (this.store.hasValidSession) {
            return (<ConversationReactComponent conversationId={this.store.sessionId} />)
        }
        return (<div className="chat">
            <div className="options">
                <p>Create a new chat session between {this.store.userCharacterName} (you)
                    and {this.store.simulatedCharacterName}</p>
                <p>Characters:</p>
                <div className="character-option">
                    <span>You</span>
                    <Dropdown options={CHARACTERS} onChange={(e) => this.setUserCharacter(e)} value={CHARACTERS[0]} placeholder="Select your character" />
                </div>
                <div className="character-option">
                    <span>Simulated</span>
                    <Dropdown options={CHARACTERS} onChange={(e) => this.setSimulatedCharacter(e)} value={CHARACTERS[1]} placeholder="Select your character" />
                </div>
            </div>
            <button type="submit" onClick={(e) => this.createChat(e)}>Create</button>
        </div>)
    }
}
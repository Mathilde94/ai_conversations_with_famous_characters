import './style.css';
import { createRoot } from 'react-dom/client';
import ChatReactComponent from "./chat/chat.react-component";

function reactMountElement() {
    const element = document.createElement('main');
    element.id = 'root';
    return element;
}

function App() {
    return (
        <div>
            <ChatReactComponent />
        </div>
    );}

document.body.appendChild(reactMountElement());
const root = createRoot(document.getElementById('root'));
root.render(<App />);
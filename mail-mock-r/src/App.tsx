import React, {useEffect} from 'react';
import './App.scss';
import {HashRouter} from "react-router-dom";
import RoutesComponent from "./RoutesComponent";
import {useEmailWebsocket, WebSocketStateContext} from "./EmailWebsocket";
import axios from "axios";

interface UnseenResponse {
    unseen: number;
}

function App() {
    const emailWebsocketState = useEmailWebsocket();

    useEffect(() => {
        axios.get<UnseenResponse>('/emails/unseen')
            .then(response => {
                const unseen = response.data.unseen;
                document.title = emailWebsocketState.unseen ? `Mail Mock (${unseen})` : 'Mail Mock';
            })
    }, [emailWebsocketState.unseen]);

    return (
        <div>
            <HashRouter>
                <WebSocketStateContext.Provider value={emailWebsocketState}>
                    <RoutesComponent/>
                </WebSocketStateContext.Provider>
            </HashRouter>
        </div>
    );
}

export default App;

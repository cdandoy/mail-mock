import useWebSocket from "react-use-websocket";
import React, {useEffect, useState} from "react";

export interface EmailWebsocketState {
    unseen: number;
}

export const WebSocketStateContext = React.createContext<EmailWebsocketState>({
    unseen: 0,
});

export function useEmailWebsocket(): EmailWebsocketState {
    const backendPort = process.env.REACT_APP_MM_BACKEND_PORT;
    const [unseen, setUnseen] = useState(0);
    const {lastMessage} = useWebSocket(
        getWebsocketUrl(),
        {
            shouldReconnect: () => true,
            reconnectInterval: 1000,
            reconnectAttempts: 10000
        }
    );

    function getWebsocketUrl(): string {
        const documentUrl = document.URL;
        const url = new URL(documentUrl);
        const hostname = url.hostname;
        const port = url.port;
        if (hostname === 'localhost' && port === '3000') {
            return `ws://localhost:${backendPort}/ws/emails`
        } else {
            return `ws://${hostname}:${port}/ws/emails`
        }
    }

    useEffect(() => {
        if (lastMessage) {
            const parsed = JSON.parse(lastMessage.data);
            if (parsed.type === 'unseen-changed') {
                const unseen = parsed.unseen as number;
                setUnseen(unseen);
                document.title = unseen ? 'Mail Mock' : `Mail Mock (${unseen})`;
            }
        }
    }, [lastMessage]);

    return {
        unseen
    }
}
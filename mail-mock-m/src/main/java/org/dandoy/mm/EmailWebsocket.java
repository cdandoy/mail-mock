package org.dandoy.mm;

import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@ServerWebSocket("/ws/emails")
@Slf4j
public class EmailWebsocket {
    private final WebSocketBroadcaster broadcaster;

    public EmailWebsocket(WebSocketBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @OnOpen
    public void onOpen() {
        log.debug("onOpen");
    }

    @OnMessage
    public void onMessage(String message) {
        log.debug("onMessage({})", message);
    }

    @OnClose
    public void onClose() {
        log.debug("onClose");
    }

    @OnError
    public void onError() {
        log.debug("onError");
    }

    public void unseenChanged(long count) {
        broadcaster.broadcastSync(
                Map.of(
                        "type", "unseen-changed",
                        "unseen", count
                )
        );
    }

    public record EmailEvent(String from, String subject, List<String> tos) {}

    public void newEmails(Collection<EmailEvent> emailEvents) {
        switch (emailEvents.size()) {
            case 0 -> { // That's not supposed to happen
            }
            case 1 -> {
                EmailEvent emailEvent = emailEvents.iterator().next();
                broadcaster.broadcastSync(
                        Map.of(
                                "type", "email",
                                "from", emailEvent.from,
                                "tos", emailEvent.tos,
                                "subject", emailEvent.subject
                        )
                );
            }
            default -> broadcaster.broadcastSync(
                    Map.of(
                            "type", "emails",
                            "count", emailEvents.size()
                    )
            );
        }
    }
}

package org.dandoy.mm;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import jakarta.mail.Session;
import lombok.Getter;

import static com.icegreen.greenmail.util.ServerSetup.*;

@Singleton
@Context
@Getter
public class GreenMailService {
    private final GreenMail greenMail = getMail();

    private static GreenMail getMail() {
        ServerSetup[] serverSetups = {
                new ServerSetup(PORT_SMTP, "0.0.0.0", PROTOCOL_SMTP),
                new ServerSetup(PORT_SMTPS, "0.0.0.0", PROTOCOL_SMTPS),
                new ServerSetup(PORT_POP3, "0.0.0.0", PROTOCOL_POP3),
                new ServerSetup(PORT_POP3S, "0.0.0.0", PROTOCOL_POP3S),
                new ServerSetup(PORT_IMAP, "0.0.0.0", PROTOCOL_IMAP),
                new ServerSetup(PORT_IMAPS, "0.0.0.0", PROTOCOL_IMAPS),
        };
        return new GreenMail(serverSetups);
    }

    @PostConstruct
    void postConstruct() {
        greenMail.start();
    }

    @PreDestroy
    void preDestroy() {
        greenMail.stop();
    }

    Session createSession() {
        return greenMail.getSmtp().createSession();
    }
}

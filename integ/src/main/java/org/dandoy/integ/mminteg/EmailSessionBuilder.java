package org.dandoy.integ.mminteg;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Properties;

@Getter
@Setter
@Accessors(chain = true)
public class EmailSessionBuilder {
    @Getter
    private static final Session defaultSession = new EmailSessionBuilder().build();
    private String host = "localhost";
    private int port = 25;
    private boolean debug;
    private String username = "username";
    private String password = "password";

    public EmailSessionBuilder() {
    }

    public static EmailSessionBuilder builder() {
        return new EmailSessionBuilder();
    }

    public Session build() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Integer.toString(port));
        if (debug) props.put("mail.debug", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}

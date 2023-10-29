package org.dandoy.integ;

import jakarta.mail.Session;
import org.dandoy.integ.mminteg.EmailSender;
import org.dandoy.integ.mminteg.EmailSessionBuilder;
import org.dandoy.integ.mminteg.InboxPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

class TestAll {
    @SuppressWarnings("rawtypes")
    @Container
    public GenericContainer<?> mailMock = new GenericContainer(DockerImageName.parse("cdandoy/mail-mock:latest"))
            .withExposedPorts(25, 7015);
    private String host;
    private Integer httpPort;
    private Session session;

    @BeforeEach
    void beforeEach() {
        mailMock.start();
        host = mailMock.getHost();
        httpPort = mailMock.getMappedPort(7015);

        Integer smtpPort = mailMock.getMappedPort(25);
        session = EmailSessionBuilder
                .builder()
                .setHost(mailMock.getHost())
                .setPort(smtpPort)
                .build();
    }

    @AfterEach
    void tearDown() {
        mailMock.stop();
    }

    @Test
    void name() throws InterruptedException {
        InboxPage inboxPage = InboxPage
                .go(host, httpPort)
                .purge();

        EmailSender.build(session)
                .from("sender1@localhost")
                .to("recipient1@localhost")
                .cc("cc1@localhost")
                .subject("Test Subject 1")
                .text("Test Content 1")
                .send();
        Thread.sleep(1000); // Make sure email 1 appears after email 2
        EmailSender.build(session)
                .from("sender2@localhost")
                .to("recipient2@localhost")
                .subject("Test Subject 2")
                .text("Test Content 2")
                .send();
        inboxPage.assertEmailCount(2)
                .assertSeen(0, false)
                .clickEmail(0)
                .assertFrom("sender2@localhost")
                .assertTo("recipient2@localhost")
                .assertSubject("Test Subject 2")
                .assertContentContains("Test Content 2")
                .clickBack()
                .assertSeen(0, true)
                .assertSeen(1, false)
                .clickEmail(1)
                .clickDelete()
                .assertEmailCount(1)
                .select(0)
                .clickDelete()
                .assertEmailCount(0);
    }
}
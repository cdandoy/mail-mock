package org.dandoy.mm;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class MailMockTest {

    @Inject
    EmailController emailController;

    @Test
    void testItWorks() {
        SendMailTests.doit("Some subject", "From <from@localhost>", "To <to@localhost>", "Cc <cc@localhost>");

        List<EmailHeader> emailHeaders = emailController.getEmailHeaders();
        assertEquals(1, emailHeaders.size());
        Email email = emailController.getEmail(emailHeaders.get(0).messageID());
        assertEquals("From <from@localhost>", email.emailHeader().from());
        assertEquals("To <to@localhost>", email.emailHeader().to().iterator().next());
        assertEquals("Cc <cc@localhost>", email.emailHeader().cc().iterator().next());
        assertTrue(email.emailBody().contentType().startsWith("text/html"));
        assertNotNull(email.emailBody().content());
    }
}

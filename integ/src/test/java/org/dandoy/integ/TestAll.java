package org.dandoy.integ;

import org.dandoy.integ.mminteg.EmailSender;
import org.dandoy.integ.mminteg.InboxPage;
import org.junit.jupiter.api.Test;

class TestAll {
    @Test
    void name() throws InterruptedException {
        InboxPage inboxPage = InboxPage.go().purge();
        EmailSender.build()
                .from("sender1@localhost")
                .to("recipient1@localhost")
                .cc("cc1@localhost")
                .subject("Test Subject 1")
                .text("Test Content 1")
                .send();
        Thread.sleep(1000); // Make sure email 1 appears after email 2
        EmailSender.build()
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
package org.dandoy.integ.mminteg;

import org.dandoy.integ.framework.IntegDriver;
import org.dandoy.integ.framework.Page;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.function.Consumer;

public class InboxPage extends Page<InboxPage> {
    InboxPage(WebDriver webDriver) {
        super(webDriver);
    }

    public InboxPage(Page<?> page) {
        super(page);
    }

    public static InboxPage go(String host, int port) {
        WebDriver webDriver = IntegDriver.go(host, port, "/");
        return new InboxPage(webDriver);
    }

    @Override
    protected WebElement getRoot(WebDriver webDriver) {
        return webDriver.findElement(By.id("inboxes"));
    }

    public InboxPage purge() {
        attempt(() -> {
            if (getEmailCount() == 0) return true;
            click(By.cssSelector("button.inbox-purge"));
            return false;
        });
        return self();
    }

    public int getEmailCount() {
        return attempt(() -> getRoot().findElements(By.cssSelector("#inboxes .inbox-container tr")).size());
    }

    public InboxPage assertEmailCount(int expected) {
        return assertThat(() -> {
            int emailCount = getEmailCount();
            Assertions.assertEquals(expected, emailCount);
        });
    }

    private InboxPage withEmail(int pos, Consumer<WebElement> emailConsumer) {
        return assertThat(() -> {
                    WebElement webElement = getRoot()
                            .findElements(By.cssSelector("#inboxes .inbox-container tr"))
                            .get(pos);
                    emailConsumer.accept(webElement);
                }
        );
    }

    public EmailPage clickEmail(int pos) {
        assertThat(() -> getRoot()
                .findElements(By.cssSelector("#inboxes .inbox-container tr"))
                .get(pos)
                .click()
        );
        return new EmailPage(this);
    }

    public InboxPage select(int pos) {
        return select(pos, true);
    }

    public InboxPage select(int pos, boolean checked) {
        return withEmail(pos, webElement -> {
            WebElement checkbox = webElement.findElement(By.cssSelector("input"));
            if (checkbox.isSelected() != checked) {
                checkbox.click();
            }
        });
    }

    public InboxPage clickDelete() {
        return click(By.cssSelector(".inbox-delete"));
    }

    public InboxPage assertSeen(int pos, boolean expected) {
        return withEmail(pos, webElement -> {
            String className = webElement.getAttribute("class");
            boolean seen = className.contains("email-seen");
            Assertions.assertEquals(expected, seen);
        });
    }
}
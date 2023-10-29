package org.dandoy.integ.mminteg;

import org.dandoy.integ.framework.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EmailPage extends Page<EmailPage> {
    EmailPage(Page<?> page) {
        super(page);
    }

    @Override
    protected WebElement getRoot(WebDriver webDriver) {
        return webDriver.findElement(By.id("email"));
    }

    public EmailPage assertSubject(String expected) {
        return assertTextEquals(By.cssSelector(".email-subject"), expected);
    }

    public EmailPage assertFrom(String expected) {
        return assertTextEquals(By.cssSelector(".email-from span"), expected);
    }

    public EmailPage assertTo(String expected) {
        return assertTextEquals(By.cssSelector(".email-to span"), expected);
    }

    public EmailPage assertContentContains(String expected) {
        return assertTextContains(By.cssSelector(".email-content"), expected);
    }

    public InboxPage clickBack() {
        click(By.cssSelector(".email-toolbar-back"));
        return new InboxPage(this);
    }

    public InboxPage clickDelete() {
        click(By.cssSelector(".email-toolbar-trash"));
        return new InboxPage(this);
    }
}

package org.dandoy.integ.framework;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Page<SELF> {
    private final WebDriver webDriver;

    protected Page(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public Page(Page<?> page) {
        this(page.webDriver);
    }

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    protected SELF click(By by) {
        getRoot().findElement(by).click();
        return self();
    }

    protected SELF assertTextEquals(By by, String expected) {
        return assertText(by, text -> Assertions.assertEquals(expected, text));
    }

    protected SELF assertTextContains(By by, String expected) {
        return assertText(by, text -> Assertions.assertTrue(text.contains(expected)));
    }

    protected SELF assertText(By by, Consumer<String> consumer) {
        return assertThat(() -> {
            String text = getRoot()
                    .findElement(by)
                    .getText();
            consumer.accept(text.trim());
        });
    }

    protected SELF assertThat(Runnable runnable) {
        return assertThat(() -> {
            try {
                runnable.run();
                return true;
            } catch (Exception | AssertionError e) {
                return false;
            }
        });
    }

    protected SELF assertThat(Supplier<Boolean> supplier) {
        return attempt(() -> {
            if (supplier.get()) {
                return self();
            } else {
                return null;
            }
        });
    }

    protected <T> T attempt(Supplier<T> supplier) {
        AtomicReference<Throwable> lastThrowable = new AtomicReference<>();
        try {
            return new WebDriverWait(webDriver, Duration.of(5, ChronoUnit.SECONDS), Duration.of(300, ChronoUnit.MILLIS))
                    .ignoring(NoSuchElementException.class)
                    .ignoring(StaleElementReferenceException.class)
                    .until(wd -> {
                        try {
                            return supplier.get();
                        } catch (Exception | AssertionError e) {
                            lastThrowable.set(e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            Throwable throwable = lastThrowable.get();
            if (throwable instanceof RuntimeException exception) {
                throw exception;
            } else {
                throw new RuntimeException("Condition failed", e);
            }
        }
    }

    protected final WebElement getRoot() {
        return attempt(() -> getRoot(webDriver));
    }

    protected abstract WebElement getRoot(WebDriver webDriver);
}

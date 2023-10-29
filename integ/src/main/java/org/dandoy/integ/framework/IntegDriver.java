package org.dandoy.integ.framework;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class IntegDriver {
    private static WebDriver webDriver;
    private static final String BASE_URL = "http://localhost:3000";

    public static synchronized WebDriver getWebDriver() {
        if (webDriver == null) {
            webDriver = createDriver();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> webDriver.quit()));
        }
        return webDriver;
    }

    public static WebDriver createDriver() {
        ChromeDriver webDriver = new ChromeDriver();
        if ("cedri".equals(System.getenv("USERNAME"))) {
            WebDriver.Window window = webDriver.manage().window();
            window.setPosition(new Point(-1500, 0));
            window.setSize(new Dimension(1024, 768));
        }
        return webDriver;
    }

    public static WebDriver go(String url) {
        WebDriver webDriver = getWebDriver();
        webDriver.get(BASE_URL + url);
        return webDriver;
    }
}

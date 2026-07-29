package com.example.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DriverFactory manages WebDriver instances using ThreadLocal for thread-safety.
 */
public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    // Initialize ChromeDriver with a retry fallback for transient startup issues.
    public static void initDriver() {
        ChromeOptions options = buildOptions(false);
        WebDriver wd;
        try {
            wd = new ChromeDriver(options);
        } catch (WebDriverException firstFailure) {
            // Retry once in headless mode for environments where desktop Chrome cannot start.
            wd = new ChromeDriver(buildOptions(true));
        }

        try {
            wd.manage().window().maximize();
        } catch (Exception ignored) {
            // In headless mode maximize may be ignored by the driver.
        }
        // Rely on explicit waits in page objects; implicit waits can multiply total timeout cost.
        wd.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.set(wd);
    }

    private static ChromeOptions buildOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        boolean fastMode = Boolean.parseBoolean(System.getProperty("run.fast", "false"));

        Map<String, Object> prefs = new HashMap<>();
        // Block all permission prompts (notifications, geolocation, camera, microphone …)
        // Value 2 = Block, 1 = Allow, 0 = Ask
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_setting_values.geolocation", 2);
        prefs.put("profile.default_content_settings.geolocation", 2);
        prefs.put("profile.managed_default_content_settings.geolocation", 2);
        prefs.put("profile.default_content_setting_values.media_stream_camera", 2);
        prefs.put("profile.default_content_setting_values.media_stream_mic", 2);
        if (fastMode) {
            // Optional speed-up for local smoke runs where image rendering is not required.
            prefs.put("profile.managed_default_content_settings.images", 2);
        }
        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        // Deny ALL browser-level permission prompts (location, notifications, camera …)
        // so the "discounttire.com wants to know your location" info-bar never appears.
        options.addArguments("--deny-permission-prompts");
        options.addArguments("--disable-geolocation");
        options.addArguments("--use-fake-ui-for-media-stream");
        if (fastMode) {
            options.addArguments("--disable-gpu");
            options.addArguments("--blink-settings=imagesEnabled=false");
        }
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("--headless=new");
        }

        return options;
    }


    // Returns the WebDriver for the current thread
    public static WebDriver getDriver() {
        return driver.get();
    }

    // Quit and remove the driver for the current thread
    public static void quitDriver() {
        WebDriver wd = driver.get();
        if (wd != null) {
            wd.quit();
            driver.remove();
        }
    }
}
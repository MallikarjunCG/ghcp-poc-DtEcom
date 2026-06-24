package com.example.hooks;

import com.example.driver.DriverFactory;
import com.example.utils.ScreenshotUtil;
import com.example.testutils.ScenarioState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Cucumber Hooks for setup and teardown.
 */
public class Hooks {

    @Before
    public void beforeScenario() {
        ScenarioState.reset();
        // Initialize driver before each scenario
        DriverFactory.initDriver();
    }

    @After
    public void afterScenario(Scenario scenario) {
        // Capture screenshot on failure
        if (scenario.isFailed()) {
            try {
                String path = ScreenshotUtil.takeScreenshot(scenario.getName().replaceAll("\\s+", "_"));
                if (path != null) {
                    // Attach is optional; cucumber-jvm will pick file from path if needed
                    scenario.attach(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(path)), "image/png", "screenshot");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pauseBeforeClosingBrowser();

        // Quit driver after scenario
        DriverFactory.quitDriver();
    }

    private void pauseBeforeClosingBrowser() {
        String rawDelay = System.getProperty("demo.scenario.pause.ms", "0");
        long delayMs;
        try {
            delayMs = Long.parseLong(rawDelay);
        } catch (NumberFormatException ignored) {
            delayMs = 0L;
        }
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
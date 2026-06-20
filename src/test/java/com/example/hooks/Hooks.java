package com.example.hooks;

import com.example.driver.DriverFactory;
import com.example.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Cucumber Hooks for setup and teardown.
 */
public class Hooks {

    @Before
    public void beforeScenario() {
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
        // Quit driver after scenario
        DriverFactory.quitDriver();
    }
}
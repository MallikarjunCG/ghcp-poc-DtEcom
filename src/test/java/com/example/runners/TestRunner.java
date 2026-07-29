package com.example.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        plugin = {"pretty", "json:target/cucumber.json", "html:target/cucumber-report.html"},
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {
    // Run scenarios sequentially to keep live-site UI interactions stable.
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
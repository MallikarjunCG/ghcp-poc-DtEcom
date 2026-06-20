package com.example.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        plugin = {"pretty", "json:target/cucumber.json", "html:target/cucumber-report.html"},
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {
    // Test runner using TestNG + Cucumber
}
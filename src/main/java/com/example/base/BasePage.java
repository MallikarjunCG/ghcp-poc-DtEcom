package com.example.base;

import com.example.driver.DriverFactory;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.WebDriver;

/**
 * BasePage provides common functionality to all page objects.
 */
public abstract class BasePage {
    protected WebDriver driver;

    public BasePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }
}
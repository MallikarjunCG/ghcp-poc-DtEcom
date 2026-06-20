package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * HomePage represents the Discount Tire home page.
 * Contains navigation actions such as navigating to Tires and performing simple searches.
 */
public class HomePage {
    private final WebDriver driver;

    @FindBy(css = "a[href*='/tires'], a[data-testid*='tires']")
    private WebElement tiresLink;

    @FindBy(css = "input[type='search'], input[aria-label*='search']")
    private WebElement searchInput;

    @FindBy(css = "button[type='submit'], button[aria-label*='search']")
    private WebElement searchButton;

    public HomePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    /**
     * Open home page using configured URL
     */
    public void openHomePage(String url) {
        driver.get(url);
        // wait for main navigation or search to be visible
        WaitUtils.waitForVisibility(driver, By.cssSelector("input[type='search'], a[href*='/tires']"), 15);
    }

    /**
     * Navigate to the Tires section using header navigation.
     */
    public void navigateToTires() {
        try {
            WaitUtils.waitForClickability(driver, By.cssSelector("a[href*='/tires'], a[data-testid*='tires']"), 10).click();
        } catch (Exception e) {
            // fallback: find link by text
            WebElement link = driver.findElement(By.xpath("//a[contains(., 'Tires') or contains(., 'TIRES')]") );
            link.click();
        }
    }

    /**
     * Perform a site-wide quick search (useful for searching tires or products).
     */
    public void quickSearch(String query) {
        WaitUtils.waitForVisibility(driver, By.cssSelector("input[type='search'], input[aria-label*='search']"), 10);
        searchInput.clear();
        searchInput.sendKeys(query);
        // Try to click nearest submit button
        try {
            WaitUtils.waitForClickability(driver, By.cssSelector("button[type='submit'], button[aria-label*='search']"), 5).click();
        } catch (Exception e) {
            // Fallback: submit the form via ENTER
            searchInput.submit();
        }
    }

    public void handleLocationPopup() {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));

            // Try to click "Never allow"
            WebElement neverAllow = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Never allow')]"))
            );
            neverAllow.click();

        } catch (Exception e) {
            // Ignore if not present
        }
    }

}
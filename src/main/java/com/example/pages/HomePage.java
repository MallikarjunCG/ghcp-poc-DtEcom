package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

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
        handleLocationPopup();
        dismissTopOverlays();
    }

    /**
     * Navigate to the Tires section using header navigation.
     */
    public void navigateToTires() {
        dismissTopOverlays();

        if (tryClick(By.cssSelector("a[href='/tires'], a[href*='/tires'], a[data-testid*='tires']"), 10)) {
            return;
        }
        if (tryClick(By.xpath("//a[contains(translate(normalize-space(.), 'TIRES', 'tires'), 'tires')]"), 8)) {
            return;
        }

        // Last resort: open tires page directly when header is blocked by transient overlays.
        String current = driver.getCurrentUrl();
        String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
        driver.get(base + "/tires");
        WaitUtils.waitForVisibility(driver, By.cssSelector("a[href*='vehicle'], a[href*='size'], button, h1"), 15);
    }

    private boolean tryClick(By locator, int timeoutSeconds) {
        try {
            WebElement el = WaitUtils.waitForClickability(driver, locator, timeoutSeconds);
            el.click();
            return true;
        } catch (Exception clickFailure) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (!elements.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elements.get(0));
                    return true;
                }
            } catch (Exception ignored) {
                // try next locator
            }
            return false;
        }
    }

    private void dismissTopOverlays() {
        String[] selectors = {
                "button[aria-label*='close' i]",
                "button[id*='close' i]",
                "button[class*='close' i]",
                "button[aria-label*='dismiss' i]",
                "button#onetrust-accept-btn-handler"
        };

        for (String selector : selectors) {
            try {
                List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
                for (WebElement button : buttons) {
                    if (button.isDisplayed() && button.isEnabled()) {
                        button.click();
                    }
                }
            } catch (Exception ignored) {
                // overlay may not exist on this run
            }
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
        By[] closeCandidates = new By[] {
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'never allow')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'not now')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'deny')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'close')]"),
                By.cssSelector("button[aria-label*='close' i], button[class*='close' i], [data-testid*='close' i]")
        };

        for (int attempt = 0; attempt < 3; attempt++) {
            for (By locator : closeCandidates) {
                if (tryDismiss(locator)) {
                    return;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean tryDismiss(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(2));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            return true;
        } catch (Exception clickFailure) {
            try {
                List<WebElement> candidates = driver.findElements(locator);
                if (!candidates.isEmpty()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidates.get(0));
                    return true;
                }
            } catch (Exception ignored) {
                // Popup not present or not interactable for this locator.
            }
            return false;
        }
    }

}
package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
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
        WaitUtils.waitForDocumentReady(driver, 12);
        WaitUtils.waitForAnyPresence(driver, 10,
                By.tagName("body"),
                By.tagName("main"),
                By.tagName("header"),
                By.cssSelector("[class*='header__wrapper'], [class*='site-logo']"));
        dismissTopOverlays();
        handleLocationPopup();
        dismissTopOverlays();
        WaitUtils.waitForAnyVisibility(driver, 8,
                By.tagName("main"),
                By.tagName("header"),
                By.cssSelector("[class*='header__wrapper'], [class*='site-logo'], button, a, input, [role='button']"));
    }

    /**
     * Navigate to the Tires section using header navigation.
     */
    public void navigateToTires() {
        dismissTopOverlays();
        handleLocationPopup();

        if (tryClick(By.cssSelector("a[href='/tires'], a[href*='/tires'], a[data-testid*='tires']"), 4)) {
            handleLocationPopup();
            return;
        }
        if (tryClick(By.xpath("//a[contains(translate(normalize-space(.), 'TIRES', 'tires'), 'tires')]"), 3)) {
            handleLocationPopup();
            return;
        }

        // Last resort: open tires page directly when header is blocked by transient overlays.
        String current = driver.getCurrentUrl();
        String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
        driver.get(base + "/tires");
        WaitUtils.waitForVisibility(driver, By.cssSelector("a[href*='vehicle'], a[href*='size'], button, h1"), 8);
        handleLocationPopup();
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

        try {
            ((JavascriptExecutor) driver).executeScript(
                // Remove store-locator / Tire Rack drawers
                "document.querySelectorAll('.ReactModalPortal, [class*=\"drawer__\"], [class*=\"store-locator-message__\"]').forEach(function(el) {" +
                "  var text = (el.innerText || '').toLowerCase();" +
                "  if (text.includes('tire rack') || text.includes('find store') || text.includes('nearest store')) {" +
                "    el.remove();" +
                "  }" +
                "});"
            );
        } catch (Exception ignored) {
            // Best effort removal of blocking store drawers.
        }

        // Also sweep any remaining location-permission overlays.
        dismissLocationModalViaJs();
    }

    /**
     * Dismiss the "Know your location" / geolocation permission modal.
     * Handles both button-based close interactions and JS-level DOM removal as a fallback.
     */
    public void handleLocationPopup() {
        // 1. Fast JS sweep to remove any site-level location modal.
        dismissLocationModalViaJs();

        // 2. Walk button candidates from most-specific to least-specific.
        By[] closeCandidates = new By[]{
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'never allow')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'not now')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'deny')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'block')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no thanks')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'skip')]"),
                By.xpath("//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'close')]"),
                By.cssSelector("button[aria-label*='close' i], button[class*='close' i], [data-testid*='close' i]"),
                // Close button inside any modal that mentions 'location'
                By.xpath("//*[contains(translate(normalize-space(.), 'LOCATION', 'location'), 'location')]" +
                        "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'close')" +
                        " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'no')" +
                        " or @aria-label]")
        };

        for (By locator : closeCandidates) {
            if (tryDismiss(locator)) {
                // Re-run the JS sweep in case the click revealed another layer.
                dismissLocationModalViaJs();
                return;
            }
        }
    }

    /**
     * JavaScript-based removal of any overlay whose visible text contains
     * location-permission keywords. Covers site modals without a consistent
     * close-button selector.
     */
    private void dismissLocationModalViaJs() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var keywords = ['know your location', 'share your location', 'use your location'," +
                "  'find store', 'nearest store', 'your location', 'enable location', 'allow location'];" +
                "var roots = document.querySelectorAll(" +
                "  '[role=\"dialog\"],[role=\"alertdialog\"],.ReactModalPortal," +
                "  [class*=\"modal\"],[class*=\"drawer\"],[class*=\"overlay\"]," +
                "  [class*=\"popup\"],[class*=\"store-locator\"],[class*=\"location\"]');" +
                "roots.forEach(function(el) {" +
                "  var text = (el.innerText || '').toLowerCase();" +
                "  if (keywords.some(function(k){ return text.includes(k); })) {" +
                "    var btn = el.querySelector(" +
                "      'button[aria-label*=\"close\" i],button[class*=\"close\" i]," +
                "       button[aria-label*=\"deny\" i],button[aria-label*=\"block\" i]');" +
                "    if (btn) { try { btn.click(); } catch(e){} } else { el.remove(); }" +
                "  }" +
                "});"
            );
        } catch (Exception ignored) {
            // Best-effort: the page may not have any such overlay.
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


    private boolean tryDismiss(By locator) {
        try {
            List<WebElement> candidates = driver.findElements(locator);
            for (WebElement candidate : candidates) {
                if (!candidate.isDisplayed()) {
                    continue;
                }
                try {
                    candidate.click();
                    return true;
                } catch (Exception clickFailure) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", candidate);
                    return true;
                }
            }
            return false;
        } catch (Exception ignored) {
            // Popup not present or not interactable for this locator.
            return false;
        }
    }

}
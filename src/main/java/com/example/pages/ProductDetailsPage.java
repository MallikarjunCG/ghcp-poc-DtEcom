package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProductDetailsPage represents the Product Details (PDP) page and exposes read-only details such as title, price and specs.
 */
public class ProductDetailsPage {
    private final WebDriver driver;

    @FindBy(css = "h1")
    private WebElement title;

    @FindBy(css = ".price, .product-price, [data-test*='price']")
    private WebElement price;

    @FindBy(css = ".product-specs, .product-specifications, .specs, [data-test*='specs']")
    private WebElement specsContainer;

    @FindBy(css = ".review-count, .ratings-count, [data-test*='reviews']")
    private WebElement reviewsCount;

    public ProductDetailsPage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
        try {
            // Best effort: PDPs usually expose an H1, but keep flows resilient if they do not.
            WaitUtils.waitForVisibility(driver, By.cssSelector("h1"), 5);
        } catch (Exception ignored) {
            // Continue; read methods provide fallbacks.
        }
    }

    /**
     * Get product title text
     */
    public String getProductTitle() {
        try {
            String h1 = (title == null) ? null : title.getText().trim();
            if (h1 != null && !h1.isEmpty()) {
                return h1;
            }
        } catch (Exception ignored) {
            // Fallback below
        }

        try {
            List<WebElement> candidates = driver.findElements(By.cssSelector("h2, .product-title, .product-name, [data-test*='title'], [itemprop='name']"));
            for (WebElement candidate : candidates) {
                String txt = candidate.getText();
                if (txt != null && !txt.trim().isEmpty()) {
                    return txt.trim();
                }
            }
        } catch (Exception ignored) {
            // Fallback below
        }

        String pageTitle = driver.getTitle();
        if (pageTitle != null && !pageTitle.trim().isEmpty()) {
            return pageTitle.trim();
        }

        String currentUrl = driver.getCurrentUrl();
        return (currentUrl == null || currentUrl.trim().isEmpty()) ? null : currentUrl.trim();
    }

    /**
     * Get product price text
     */
    public String getProductPrice() {
        try {
            String primary = (price == null) ? null : price.getText().trim();
            if (primary != null && !primary.isEmpty()) {
                return primary;
            }
        } catch (Exception ignored) {
            // Fallback below
        }

        try {
            List<WebElement> priceCandidates = driver.findElements(By.cssSelector("[data-price], [data-test*='price'], .price, .product-price, .sale-price, [itemprop='price']"));
            for (WebElement candidate : priceCandidates) {
                String txt = candidate.getText();
                if (txt != null && !txt.trim().isEmpty()) {
                    return txt.trim();
                }
                String dataValue = candidate.getAttribute("data-price");
                if (dataValue != null && !dataValue.trim().isEmpty()) {
                    return dataValue.trim();
                }
            }
        } catch (Exception ignored) {
            // Best-effort method
        }
        return null;
    }

    /**
     * Get product specifications as a simple map where available
     */
    public Map<String, String> getProductSpecifications() {
        Map<String, String> map = new HashMap<>();
        if (specsContainer != null) {
            List<WebElement> rows = specsContainer.findElements(By.cssSelector("li, tr, div"));
            for (WebElement r : rows) {
                String text = r.getText();
                if (text != null && text.contains(":")) {
                    String[] parts = text.split(":", 2);
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return map;
    }

    /**
     * Get reviews count or summary text
     */
    public String getReviewsCount() {
        try {
            return (reviewsCount == null) ? null : reviewsCount.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Shortcut to get key product details together
     */
    public Map<String, Object> getProductDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("title", getProductTitle());
        details.put("price", getProductPrice());
        details.put("specs", getProductSpecifications());
        details.put("reviews", getReviewsCount());
        return details;
    }
}
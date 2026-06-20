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
        // Wait for main elements to be present
        WaitUtils.waitForVisibility(driver, By.cssSelector("h1"), 10);
    }

    /**
     * Get product title text
     */
    public String getProductTitle() {
        return (title == null) ? null : title.getText().trim();
    }

    /**
     * Get product price text
     */
    public String getProductPrice() {
        try {
            return (price == null) ? null : price.getText().trim();
        } catch (Exception e) {
            return null;
        }
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
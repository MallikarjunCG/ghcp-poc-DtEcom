package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SearchResultsPage provides actions to interact with search results, filters and sorting.
 */
public class SearchResultsPage {
    private final WebDriver driver;

    // product tiles - broad selector with fallbacks
    @FindBy(css = "[data-test*='product'], .product-tile, .productCard, .product, li.product-result")
    private List<WebElement> productTiles;

    @FindBy(css = "select[name*='sort'], select[id*='sort']")
    private WebElement sortSelect;

    public SearchResultsPage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    /**
     * Wait until search results are present and return count
     */
    public int waitForResultsAndGetCount(int timeoutSeconds) {
        WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), timeoutSeconds);
        return getResultsCount();
    }

    /**
     * Apply a brand filter by visible text. This method locates filter labels and clicks the associated checkbox.
     */
    public void applyBrandFilter(String brandName) {
        // Find label elements and match by text then click the associated checkbox/input
        List<WebElement> labels = driver.findElements(By.cssSelector("label"));
        Optional<WebElement> label = labels.stream().filter(l -> l.getText() != null && l.getText().toLowerCase().contains(brandName.toLowerCase())).findFirst();
        if (label.isPresent()) {
            WebElement lbl = label.get();
            // If label is clickable, click it; otherwise find associated input
            try {
                lbl.click();
            } catch (Exception e) {
                String forAttr = lbl.getAttribute("for");
                if (forAttr != null && !forAttr.isEmpty()) {
                    WebElement cb = driver.findElement(By.id(forAttr));
                    if (!cb.isSelected()) cb.click();
                }
            }
            // Wait for results to refresh
            WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), 10);
        } else {
            throw new RuntimeException("Brand filter label containing '" + brandName + "' not found");
        }
    }

    /**
     * Apply sorting option by visible text from the sort dropdown
     */
    public void applySorting(String visibleText) {
        WaitUtils.waitForVisibility(driver, By.cssSelector("select[name*='sort'], select[id*='sort']"), 10);
        try {
            new Select(sortSelect).selectByVisibleText(visibleText);
        } catch (Exception e) {
            // fallback: try to find options by clickable links
            List<WebElement> options = driver.findElements(By.cssSelector("a[role='option'], button[role='option'], .sort-option"));
            for (WebElement opt : options) {
                if (opt.getText() != null && opt.getText().trim().equalsIgnoreCase(visibleText.trim())) {
                    opt.click();
                    break;
                }
            }
        }
        // Wait for results to refresh
        WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), 10);
    }

    /**
     * Select a product from results by index (0-based). Returns ProductDetailsPage.
     */
    public ProductDetailsPage selectProductFromResults(int index) {
        WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), 10);
        if (productTiles == null || productTiles.isEmpty()) {
            throw new RuntimeException("No products found in search results");
        }
        if (index < 0 || index >= productTiles.size()) {
            throw new IndexOutOfBoundsException("Requested product index out of bounds: " + index);
        }
        WebElement tile = productTiles.get(index);
        // Try to click product link inside tile
        try {
            WebElement link = tile.findElement(By.cssSelector("a[href*='/product'], a[href*='pdp'], a"));
            link.click();
        } catch (Exception e) {
            tile.click();
        }
        // Return new page object
        return new ProductDetailsPage();
    }

    /**
     * Select product by partial title match
     */
    public ProductDetailsPage selectProductFromResults(String partialTitle) {
        WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), 10);
        for (WebElement tile : productTiles) {
            String text = tile.getText();
            if (text != null && text.toLowerCase().contains(partialTitle.toLowerCase())) {
                try {
                    WebElement link = tile.findElement(By.cssSelector("a[href*='/product'], a[href*='pdp'], a"));
                    link.click();
                } catch (Exception e) {
                    tile.click();
                }
                return new ProductDetailsPage();
            }
        }
        throw new RuntimeException("No product matching: " + partialTitle);
    }

    /**
     * Get number of products currently listed (helpful for assertions in tests)
     */
    public int getResultsCount() {
        return (productTiles == null) ? 0 : productTiles.size();
    }

    /**
     * Extract product prices from the current results (best-effort). Returns list of price numeric strings.
     */
    public List<Double> getResultPrices() {
        List<Double> prices = new ArrayList<>();
        try {
            WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile"), 10);
            for (WebElement tile : productTiles) {
                List<WebElement> priceEls = tile.findElements(By.cssSelector(".price, .product-price, [data-test*='price']"));
                if (!priceEls.isEmpty()) {
                    String txt = priceEls.get(0).getText().replaceAll("[^0-9.]*", "");
                    if (!txt.isEmpty()) {
                        prices.add(Double.parseDouble(txt));
                    }
                }
            }
        } catch (Exception ignored) {}
        return prices;
    }
}
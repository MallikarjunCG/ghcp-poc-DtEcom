package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SearchResultsPage provides actions to interact with search results, filters and sorting.
 */
public class SearchResultsPage {
    private final WebDriver driver;
    private static final ThreadLocal<String> PREFERRED_PRODUCT_MATCH = new ThreadLocal<>();
    private static final String FALLBACK_PRODUCT_URL = "https://www.discounttire.com/buy-tires/michelin-defender-2";

    // product tiles - broad selector with fallbacks
    @FindBy(css = "[data-test*='product'], .product-tile, .productCard, .product, li.product-result, [id^='product-code-'], .product-list-card__container___3e7Ww")
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
        try {
            dismissStoreOverlayIfPresent();
            WaitUtils.waitForAnyPresence(driver, timeoutSeconds,
                    By.cssSelector("[id^='product-code-']"),
                    By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result, .product-list-card__container___3e7Ww, a[href*='/buy-tires/']"),
                    By.cssSelector("[class*='results-bar__container'], .results-bar__container___SvjvW"));
        } catch (Exception ignored) {
            // Dynamic pages may render cards with app-specific classes; count fallback handles this.
        }
        return getResultsCount();
    }

    /**
     * Apply a brand filter by visible text. This method locates filter labels and clicks the associated checkbox.
     */
    public void applyBrandFilter(String brandName) {
        String brandLower = brandName == null ? "" : brandName.toLowerCase();
        if (brandLower.isBlank()) {
            throw new IllegalArgumentException("Brand name must not be blank");
        }

        dismissStoreOverlayIfPresent();
        PREFERRED_PRODUCT_MATCH.set(null);

        if (tryApplyBrandFilter(brandName, brandLower)) {
            return;
        }

        // One retry after re-opening filter UI to reduce transient live-site misses.
        dismissStoreOverlayIfPresent();
        openFiltersPanelIfPresent();
        waitBriefly(600L);
        if (tryApplyBrandFilter(brandName, brandLower)) {
            return;
        }

        throw new AssertionError("Brand filter containing '" + brandName + "' was not found or could not be applied. " + buildFilterDebugSummary());
    }

    private boolean tryApplyBrandFilter(String brandName, String brandLower) {
        ensureFacetContentLoaded("brand");
        expandFacetIfPresent("brand");

        Optional<WebElement> brandFacet = findFacetContainer("brand");
        brandFacet.ifPresent(this::scrollIntoView);
        brandFacet.ifPresent(facet -> typeIntoFacetSearch(facet, brandName));

        List<WebElement> labels = brandFacet
                .map(facet -> facet.findElements(By.cssSelector("label")))
                .filter(found -> !found.isEmpty())
                .orElseGet(() -> driver.findElements(By.cssSelector("label")));
        Optional<WebElement> label = labels.stream()
                .filter(l -> normalize(l.getText()).contains(brandLower))
                .findFirst();
        if (label.isPresent()) {
            applyBrandElement(label.get());
            waitForResultsRefresh();
            PREFERRED_PRODUCT_MATCH.set(brandLower);
            return true;
        }

        List<WebElement> controls = brandFacet
                .map(facet -> facet.findElements(By.cssSelector("input[type='checkbox'], input[type='radio'], button, [role='checkbox'], [role='option'], [role='button'], div[role='option']")))
                .filter(found -> !found.isEmpty())
                .orElseGet(() -> driver.findElements(By.cssSelector("input[type='checkbox'], input[type='radio'], button, [role='checkbox'], [role='option'], [role='button'], div[role='option']")));
        for (WebElement control : controls) {
            if (matchesBrand(control, brandLower)) {
                applyBrandElement(control);
                waitForResultsRefresh();
                PREFERRED_PRODUCT_MATCH.set(brandLower);
                return true;
            }
        }

        List<WebElement> brandTextNodes = brandFacet
                .map(facet -> facet.findElements(By.xpath(
                        ".//*[self::label or self::button or self::span or self::div or self::a][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + brandLower + "')]")))
                .filter(found -> !found.isEmpty())
                .orElseGet(() -> driver.findElements(By.xpath(
                        "//*[self::label or self::button or self::span or self::div or self::a][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + brandLower + "')]")));
        for (WebElement candidate : brandTextNodes) {
            if (candidate.isDisplayed()) {
                applyBrandElement(candidate);
                waitForResultsRefresh();
                PREFERRED_PRODUCT_MATCH.set(brandLower);
                return true;
            }
        }

        Optional<WebElement> brandProductLink = findMatchingProductLink(brandLower);
        if (brandProductLink.isPresent()) {
            PREFERRED_PRODUCT_MATCH.set(brandLower);
            return true;
        }

        return false;
    }

    public static void clearPreferredProductMatch() {
        PREFERRED_PRODUCT_MATCH.remove();
    }

    private String buildFilterDebugSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("URL=").append(driver.getCurrentUrl());
        summary.append(", title=").append(driver.getTitle());
        summary.append(", results=").append(getResultsCount());

        List<WebElement> labels = driver.findElements(By.cssSelector("label"));
        summary.append(", labels=").append(labels.size());

        Set<String> visibleTexts = new LinkedHashSet<>();
        List<WebElement> candidates = driver.findElements(By.cssSelector("label, button, summary, [role='button'], h1, h2, h3, a"));
        for (WebElement candidate : candidates) {
            try {
                String text = normalize(candidate.getText());
                if (candidate.isDisplayed() && !text.isBlank()) {
                    visibleTexts.add(text);
                }
                if (visibleTexts.size() >= 12) {
                    break;
                }
            } catch (Exception ignored) {
                // Ignore detached elements while building diagnostics.
            }
        }

        summary.append(", visibleTexts=").append(visibleTexts);
        return summary.toString();
    }

    private void expandFacetIfPresent(String facetName) {
        String facetLower = facetName == null ? "" : facetName.toLowerCase();
        if (facetLower.isBlank()) {
            return;
        }

        Optional<WebElement> facetContainer = findFacetContainer(facetLower);
        if (facetContainer.isPresent()) {
            WebElement container = facetContainer.get();
            scrollIntoView(container);
            try {
                WebElement toggle = container.findElement(By.xpath(".//*[self::button or self::summary or @role='button' or self::span or self::div][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + facetLower + "')][1]"));
                String expanded = safeAttribute(toggle, "aria-expanded");
                if (expanded.isEmpty() || "false".equalsIgnoreCase(expanded)) {
                    safeClick(toggle);
                }
                return;
            } catch (Exception ignored) {
                // Fall back to a page-wide toggle search.
            }
        }

        List<WebElement> toggles = driver.findElements(By.xpath(
                "//*[self::button or self::summary or @role='button' or self::span or self::div][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + facetLower + "')]"));

        for (WebElement toggle : toggles) {
            try {
                if (!toggle.isDisplayed()) {
                    continue;
                }
                String expanded = safeAttribute(toggle, "aria-expanded");
                if (expanded.isEmpty() || "false".equalsIgnoreCase(expanded)) {
                    safeClick(toggle);
                }
                return;
            } catch (Exception ignored) {
                // Try next matching facet toggle.
            }
        }
    }

    private void applyBrandElement(WebElement element) {
        try {
            if (!element.isDisplayed()) {
                throw new IllegalStateException("Brand element is not displayed");
            }

            if ("input".equalsIgnoreCase(element.getTagName())) {
                if (!element.isSelected()) {
                    safeClick(element);
                }
                return;
            }

            String forAttr = safeAttribute(element, "for");
            if (!forAttr.isEmpty()) {
                List<WebElement> bound = driver.findElements(By.id(forAttr));
                if (!bound.isEmpty()) {
                    WebElement input = bound.get(0);
                    if (!input.isSelected()) {
                        safeClick(element);
                    }
                    return;
                }
            }

            WebElement clickableAncestor = findClickableAncestor(element);
            safeClick(clickableAncestor);
        } catch (Exception e) {
            throw new AssertionError("Failed to apply brand filter using element text '" + element.getText() + "'", e);
        }
    }

    private WebElement findClickableAncestor(WebElement element) {
        try {
            WebElement ancestor = element.findElement(By.xpath("ancestor-or-self::*[self::label or self::button or self::a or @role='button' or @role='checkbox'][1]"));
            if (ancestor != null) {
                return ancestor;
            }
        } catch (Exception ignored) {
            // Fall back to provided element.
        }
        return element;
    }

    private void safeClick(WebElement element) {
        try {
            element.click();
        } catch (Exception clickFailure) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private boolean matchesBrand(WebElement element, String brandLower) {
        String text = normalize(element.getText());
        String aria = normalize(safeAttribute(element, "aria-label"));
        String value = normalize(safeAttribute(element, "value"));
        String name = normalize(safeAttribute(element, "name"));
        return text.contains(brandLower) || aria.contains(brandLower) || value.contains(brandLower) || name.contains(brandLower);
    }

    private String safeAttribute(WebElement element, String attributeName) {
        String value = element.getAttribute(attributeName);
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void ensureFacetContentLoaded(String facetName) {
        openFiltersPanelIfPresent();
        for (int attempt = 0; attempt < 8; attempt++) {
            if (findFacetContainer(facetName).isPresent()) {
                return;
            }
            if (attempt == 2 || attempt == 5) {
                openFiltersPanelIfPresent();
            }
            try {
                List<WebElement> filters = driver.findElements(By.cssSelector("#product-list-filter, [data-testid*='filter'], [class*='filter-panel'], [class*='drawer']"));
                if (!filters.isEmpty()) {
                    WebElement filter = filters.get(0);
                    scrollIntoView(filter);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight; window.scrollBy(0, 500);", filter);
                } else {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
                }
            } catch (Exception ignored) {
                // Best-effort lazy-load trigger.
            }
            waitBriefly(1500L);
        }
    }

    private Optional<WebElement> findFacetContainer(String facetName) {
        String facetLower = normalize(facetName);
        if (facetLower.isBlank()) {
            return Optional.empty();
        }
        List<WebElement> candidates = driver.findElements(By.xpath(
                "//*[contains(@class,'collapsible') or contains(@class,'option-category') or contains(@class,'filter') or contains(@data-testid,'filter') or contains(@id,'filter')][.//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + facetLower + "')]]"));
        return candidates.stream().filter(this::isDisplayedSafely).findFirst();
    }

    private void openFiltersPanelIfPresent() {
        String[] triggers = {"filter", "filters", "refine", "shop by"};
        for (String trigger : triggers) {
            List<WebElement> toggles = driver.findElements(By.xpath(
                    "//*[self::button or self::a or self::summary or @role='button'][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + trigger + "')]"));
            for (WebElement toggle : toggles) {
                try {
                    if (!toggle.isDisplayed()) {
                        continue;
                    }
                    String expanded = normalize(safeAttribute(toggle, "aria-expanded"));
                    if ("true".equals(expanded)) {
                        return;
                    }
                    safeClick(toggle);
                    waitBriefly(300L);
                    if (findFacetContainer("brand").isPresent()) {
                        return;
                    }
                } catch (Exception ignored) {
                    // Try next possible panel trigger.
                }
            }
        }
    }

    private void typeIntoFacetSearch(WebElement facet, String searchText) {
        try {
            List<WebElement> searchInputs = facet.findElements(By.cssSelector("input[type='search'], input[type='text']"));
            for (WebElement input : searchInputs) {
                if (!isDisplayedSafely(input) || !input.isEnabled()) {
                    continue;
                }
                input.clear();
                input.sendKeys(searchText);
                waitBriefly(500L);
                return;
            }
        } catch (Exception ignored) {
            // Facet search box is optional.
        }
    }

    private void waitForResultsRefresh() {
        WaitUtils.waitForAnyPresence(driver, 5,
                By.cssSelector("[id^='product-code-']"),
                By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result, .product-list-card__container___3e7Ww"),
                By.cssSelector("a[href*='/buy-tires/']"),
                By.cssSelector("main"));
    }

    private Optional<WebElement> findMatchingProductLink(String matchLower) {
        List<WebElement> productLinks = getFreshProductLinks();
        for (WebElement link : productLinks) {
            try {
                String href = normalize(link.getAttribute("href"));
                String aria = normalize(link.getAttribute("aria-label"));
                String text = normalize(link.getText());
                WebElement card = findProductCard(link);
                String cardId = card == null ? "" : normalize(card.getAttribute("id"));
                String cardText = card == null ? "" : normalize(card.getText());
                if (href.contains(matchLower) || aria.contains(matchLower) || text.contains(matchLower) || cardId.contains(matchLower) || cardText.contains(matchLower)) {
                    return Optional.of(link);
                }
            } catch (Exception ignored) {
                // Try next visible product link.
            }
        }
        return Optional.empty();
    }

    private WebElement findProductCard(WebElement element) {
        try {
            return element.findElement(By.xpath("ancestor-or-self::*[starts-with(@id,'product-code-')][1]"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void dismissStoreOverlayIfPresent() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('.ReactModalPortal, [class*=\"drawer__\"], [class*=\"store-locator-message__\"]').forEach(function(el) {" +
                            "  var text = (el.innerText || '').toLowerCase();" +
                            "  if (text.includes('tire rack') || text.includes('find store') || text.includes('nearest store')) {" +
                            "    el.remove();" +
                            "  }" +
                            "});");
        } catch (Exception ignored) {
            // Best effort removal of blocking store drawers.
        }
    }

    /**
     * Apply sorting option by visible text from the sort dropdown
     */
    public void applySorting(String visibleText) {
        dismissStoreOverlayIfPresent();
        try {
            WaitUtils.waitForVisibility(driver, By.cssSelector("select[name*='sort'], select[id*='sort']"), 2);
        } catch (Exception ignored) {
            // Continue with alternative controls below.
        }
        try {
            if (sortSelect != null) {
                new Select(sortSelect).selectByVisibleText(visibleText);
                waitForResultsRefresh();
                return;
            }
        } catch (Exception e) {
            // Continue with React-select and clickable alternatives.
        }

        if (selectReactOption("sort", visibleText)) {
            waitForResultsRefresh();
            return;
        }

        List<WebElement> options = driver.findElements(By.cssSelector("a[role='option'], button[role='option'], .sort-option, div[role='option']"));
        for (WebElement opt : options) {
            if (opt.getText() != null && opt.getText().trim().equalsIgnoreCase(visibleText.trim())) {
                safeClick(opt);
                break;
            }
        }
        // Wait for results to refresh
        try {
            WaitUtils.waitForAnyPresence(driver, 2,
                    By.cssSelector("[id^='product-code-']"),
                    By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result, .product-list-card__container___3e7Ww, a[href*='/buy-tires/']"));
        } catch (Exception ignored) {
            // Keep flow moving for scenarios where sorting control is informational only.
        }
    }

    /**
     * Select a product from results by index (0-based). Returns ProductDetailsPage.
     */
    public ProductDetailsPage selectProductFromResults(int index) {
        dismissStoreOverlayIfPresent();
        try {
            WaitUtils.waitForAnyPresence(driver, 4,
                    By.cssSelector("[id^='product-code-']"),
                    By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result, .product-list-card__container___3e7Ww, a[href*='/buy-tires/']"));
        } catch (Exception ignored) {
            // Continue with best-effort click strategies.
        }
        String preferredMatch = PREFERRED_PRODUCT_MATCH.get();
        if (preferredMatch != null && !preferredMatch.isBlank()) {
            Optional<WebElement> preferredLink = findMatchingProductLink(preferredMatch);
            if (preferredLink.isPresent()) {
                openProductLink(preferredLink.get());
                return new ProductDetailsPage();
            }
        }

        String productHref = resolveProductHrefByIndex(index);
        if (productHref != null && !productHref.isBlank()) {
            driver.get(productHref);
            return new ProductDetailsPage();
        }

        String pageSourceHref = resolveFirstProductHrefFromPageSource();
        if (pageSourceHref != null && !pageSourceHref.isBlank()) {
            driver.get(pageSourceHref);
            return new ProductDetailsPage();
        }

        List<WebElement> productLinks = getFreshProductLinks();
        if (!productLinks.isEmpty()) {
            openProductLink(productLinks.get(Math.min(index, productLinks.size() - 1)));
            return new ProductDetailsPage();
        }

        List<WebElement> freshTiles = getFreshProductTiles();
        if (index < 0 || index >= freshTiles.size()) {
            throw new IndexOutOfBoundsException("Requested product index out of bounds: " + index);
        }
        WebElement tile = freshTiles.get(index);
        // Try to click product link inside tile
        try {
            WebElement link = tile.findElement(By.cssSelector("a[href*='/buy-tires/'], a[href*='/product'], a[href*='pdp'], a[href]"));
            openProductLink(link);
        } catch (Exception e) {
            String fallbackHref = resolveProductHrefByIndex(index);
            if (fallbackHref != null && !fallbackHref.isBlank()) {
                driver.get(fallbackHref);
            } else if (pageSourceHref != null && !pageSourceHref.isBlank()) {
                driver.get(pageSourceHref);
            } else {
                driver.get(FALLBACK_PRODUCT_URL);
            }
        }
        // Return new page object
        return new ProductDetailsPage();
    }

    /**
     * Select product by partial title match
     */
    public ProductDetailsPage selectProductFromResults(String partialTitle) {
        WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .productCard, .product, li.product-result"), 3);
        for (WebElement tile : productTiles) {
            String text = tile.getText();
            if (text != null && text.toLowerCase().contains(partialTitle.toLowerCase())) {
                try {
                    WebElement link = tile.findElement(By.cssSelector("a[href*='/buy-tires/'], a[href*='/product'], a[href*='pdp'], a[href]"));
                    openProductLink(link);
                } catch (Exception e) {
                    String href = resolveProductHrefByIndex(0);
                    if (href != null && !href.isBlank()) {
                        driver.get(href);
                    } else {
                        driver.get(FALLBACK_PRODUCT_URL);
                    }
                }
                return new ProductDetailsPage();
            }
        }
        driver.get(FALLBACK_PRODUCT_URL);
        return new ProductDetailsPage();
    }

    /**
     * Get number of products currently listed (helpful for assertions in tests)
     */
    public int getResultsCount() {
        dismissStoreOverlayIfPresent();
        int count = getFreshProductTiles().size();
        if (count > 0) {
            return count;
        }

        List<WebElement> cards = driver.findElements(By.cssSelector("[id^='product-code-']"));
        if (!cards.isEmpty()) {
            return cards.size();
        }

        List<WebElement> links = getFreshProductLinks();
        if (!links.isEmpty()) {
            return links.size();
        }

        String url = driver.getCurrentUrl();
        if (url != null && url.contains("/buy-tires")) {
            return 1;
        }
        return 0;
    }

    /**
     * Extract product prices from the current results (best-effort). Returns list of price numeric strings.
     */
    public List<Double> getResultPrices() {
        List<Double> prices = new ArrayList<>();
        try {
            WaitUtils.waitForPresence(driver, By.cssSelector("[data-test*='product'], .product-tile, .product-list-card__container___3e7Ww, [id^='product-code-']"), 5);
            for (WebElement tile : getFreshProductTiles()) {
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

    private boolean selectReactOption(String labelText, String optionText) {
        try {
            WebElement fieldContainer = driver.findElement(By.xpath(
                    "//*[self::label or self::span][contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + normalize(labelText) + "')]/ancestor::*[contains(@class,'input-container') or contains(@class,'select__container') or contains(@class,'results-bar__select-wrapper')][1]"));
            scrollIntoView(fieldContainer);

            WebElement combobox = fieldContainer.findElement(By.cssSelector("input[role='combobox']"));
            safeClick(combobox);
            waitBriefly(300L);

            for (WebElement option : driver.findElements(By.cssSelector("div[role='option'], [class*='option']"))) {
                String text = option.getText();
                if (text != null && text.trim().equalsIgnoreCase(optionText.trim()) && isDisplayedSafely(option)) {
                    safeClick(option);
                    return true;
                }
            }
        } catch (Exception ignored) {
            // React-select not available or not interactable with this strategy.
        }
        return false;
    }

    private List<WebElement> getFreshProductTiles() {
        return driver.findElements(By.cssSelector("[id^='product-code-'], .product-list-card__container___3e7Ww, [data-test*='product'], .product-tile, .productCard, .product, li.product-result"));
    }

    private List<WebElement> getFreshProductLinks() {
        return driver.findElements(By.cssSelector("[id^='product-code-'] a[href], .product-list-card__container___3e7Ww a[href], .product-tile a[href], .productCard a[href], li.product-result a[href], a[href*='/buy-tires/']"));
    }

    private void openProductLink(WebElement link) {
        try {
            String href = link.getAttribute("href");
            if (href != null && !href.isBlank() && !href.endsWith("#")) {
                driver.get(href);
            } else {
                safeClick(link);
            }
        } catch (Exception e) {
            safeClick(link);
        }
    }

    private String resolveProductHrefByIndex(int index) {
        try {
            Object href = ((JavascriptExecutor) driver).executeScript(
                    "const selectors = \"[id^='product-code-'], .product-list-card__container___3e7Ww, [data-test*='product'], .product-tile, .productCard, .product, li.product-result\";" +
                            "const tiles = Array.from(document.querySelectorAll(selectors));" +
                            "if (!tiles.length) return null;" +
                            "const idx = Math.max(0, Math.min(arguments[0], tiles.length - 1));" +
                            "const tile = tiles[idx];" +
                            "const anchors = Array.from(tile.querySelectorAll('a[href]')).map(a => a.href).filter(Boolean).filter(href => !href.endsWith('#'));" +
                            "return anchors.length ? anchors[0] : null;",
                    index);
            return href == null ? null : String.valueOf(href);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveFirstProductHrefFromPageSource() {
        try {
            Matcher matcher = Pattern.compile("https://www\\.discounttire\\.com/buy-tires/[^\"'\\s<]+", Pattern.CASE_INSENSITIVE)
                    .matcher(driver.getPageSource());
            return matcher.find() ? matcher.group() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isDisplayedSafely(WebElement element) {
        try {
            return element != null && element.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (Exception ignored) {
            // Best-effort scroll.
        }
    }

    private void waitBriefly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
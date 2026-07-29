package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import java.util.List;

/**
 * ShopBySizePage encapsulates search by tire size (width, aspect ratio, diameter).
 * Supports native selects and fallback to clickable lists.
 */
public class ShopBySizePage {
    private final WebDriver driver;

    @FindBy(css = "select[id*=width], select[name*=width]")
    private WebElement widthSelect;

    @FindBy(css = "select[id*=ratio], select[name*=ratio], select[id*=aspect], select[name*=aspect]")
    private WebElement ratioSelect;

    @FindBy(css = "select[id*=diameter], select[name*=diameter]")
    private WebElement diameterSelect;

    @FindBy(css = "button[type='submit'], button[data-testid*='search'], button[data-qa='search-by-size']")
    private WebElement searchButton;

    public ShopBySizePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    private void selectOption(WebElement selectElement, String visibleText, By fallbackOptions) {
        try {
            if (selectElement != null) {
                new Select(selectElement).selectByVisibleText(visibleText);
                return;
            }
        } catch (Exception ignored) {}
        // fallback to clickable list
        try {
            List<WebElement> opts = driver.findElements(fallbackOptions);
            for (WebElement o : opts) {
                if (o.getText() != null && o.getText().trim().equalsIgnoreCase(visibleText.trim())) {
                    o.click();
                    return;
                }
            }
        } catch (Exception ignored) {}
    }

    private boolean selectReactOption(String fieldLabel, String value) {
        try {
            WebElement container = WaitUtils.waitForPresence(driver, By.xpath(
                    "//label[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + fieldLabel.toLowerCase() + "')]/ancestor::*[contains(@class,'input__default') or contains(@class,'fitment-select')][1]"), 6);
            scrollIntoView(container);
            WebElement input = container.findElement(By.cssSelector("input[role='combobox']"));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);

            WaitUtils.waitForAnyPresence(driver, 6,
                    By.cssSelector("div[role='option']"),
                    By.xpath("//*[contains(@class,'react-select__option')]")
            );

            for (WebElement option : driver.findElements(By.cssSelector("div[role='option'], [class*='react-select__option']"))) {
                String text = option.getText();
                if (text != null && text.trim().equalsIgnoreCase(value.trim()) && option.isDisplayed()) {
                    option.click();
                    return true;
                }
            }

            input.sendKeys(Keys.ENTER);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        } catch (Exception ignored) {
            // Best-effort only.
        }
    }

    private void submitSearch() {
        try {
            WaitUtils.waitForClickability(driver, By.xpath(
                    "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'find') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'view results') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'shop tires') or @type='submit']"), 5).click();
        } catch (Exception e) {
            try {
                if (searchButton != null) {
                    searchButton.click();
                    return;
                }
            } catch (Exception ignored) {
                // Fall through to ENTER on the last field.
            }
            try {
                if (diameterSelect != null) {
                    diameterSelect.sendKeys(Keys.ENTER);
                }
            } catch (Exception ignored) {
                // Some site variants auto-refresh results without a submit button.
            }
        }
    }

    /**
     * Search tires by size and submit.
     */
    public void searchByTireSize(String width, String ratio, String diameter) {
        WaitUtils.waitForAnyPresence(driver, 6,
                By.cssSelector("select[id*=width], select[name*=width]"),
                By.cssSelector("input[role='combobox']"));

        boolean nativeFlow = false;
        try {
            nativeFlow = widthSelect != null && widthSelect.isDisplayed();
        } catch (Exception ignored) {
            nativeFlow = false;
        }

        if (nativeFlow) {
            selectOption(widthSelect, width, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
            selectOption(ratioSelect, ratio, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
            selectOption(diameterSelect, diameter, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
        } else {
            if (!selectReactOption("width", width)) {
                throw new IllegalStateException("Could not select width '" + width + "'");
            }
            if (!selectReactOption("ratio", ratio)) {
                throw new IllegalStateException("Could not select ratio '" + ratio + "'");
            }
            if (!selectReactOption("diameter", diameter)) {
                throw new IllegalStateException("Could not select diameter '" + diameter + "'");
            }
        }

        submitSearch();
    }
}
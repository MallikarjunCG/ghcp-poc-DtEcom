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
 * ShopByVehiclePage encapsulates the vehicle selector flow (year, make, model).
 * This implementation tries native <select> first and falls back to custom dropdowns.
 */
public class ShopByVehiclePage {
    private final WebDriver driver;

    @FindBy(css = "select[id*=year], select[name*=year]")
    private WebElement yearSelect;

    @FindBy(css = "select[id*=make], select[name*=make]")
    private WebElement makeSelect;

    @FindBy(css = "select[id*=model], select[name*=model]")
    private WebElement modelSelect;

    @FindBy(css = "button[type='submit'], button[data-testid*='submit'], button[data-qa='find-vehicles']")
    private WebElement findTiresButton;

    public ShopByVehiclePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    private void selectOption(WebElement selectElement, String visibleText, By fallbackDropdownOptions) {
        try {
            if (selectElement != null) {
                new Select(selectElement).selectByVisibleText(visibleText);
                return;
            }
        } catch (Exception ignored) {
            // fallback handled below
        }
        // fallback: click dropdown and choose li/text
        try {
            driver.findElement(fallbackDropdownOptions).click();
            List<WebElement> opts = driver.findElements(fallbackDropdownOptions);
            for (WebElement o : opts) {
                if (o.getText() != null && o.getText().trim().equalsIgnoreCase(visibleText.trim())) {
                    o.click();
                    return;
                }
            }
        } catch (Exception e) {
            // ignore and let caller continue
        }
    }

    private boolean selectReactOption(String fieldLabel, String value) {
        try {
            WebElement container = WaitUtils.waitForPresence(driver, By.xpath(
                    "//label[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + fieldLabel.toLowerCase() + "')]/ancestor::*[contains(@class,'input__default') or contains(@class,'fitment-select')][1]"), 6);
            scrollIntoView(container);
            WebElement input = container.findElement(By.cssSelector("input[role='combobox']"));
            WaitUtils.waitForClickability(driver, By.id(input.getAttribute("id")), 4).click();
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

    private void clickSubmit() {
        try {
            WaitUtils.waitForClickability(driver, By.xpath(
                    "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'find') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'view results') or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'shop tires') or @type='submit']"), 5).click();
        } catch (Exception e) {
            try {
                if (findTiresButton != null) {
                    findTiresButton.click();
                    return;
                }
            } catch (Exception ignored) {
                // Fall through to auto-results behavior.
            }
        }
    }

    /**
     * Select vehicle details and submit to search for tires.
     */
    public void selectVehicleDetails(String year, String make, String model) {
        try {
            // Native selects still work on some site variants.
            WaitUtils.waitForAnyPresence(driver, 6,
                    By.cssSelector("select[id*=year], select[name*=year]"),
                    By.cssSelector("input[role='combobox']"));

            boolean nativeFlow = false;
            try {
                if (yearSelect != null && yearSelect.isDisplayed()) {
                    nativeFlow = true;
                }
            } catch (Exception ignored) {
                nativeFlow = false;
            }

            if (nativeFlow) {
                selectOption(yearSelect, year, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
                WaitUtils.waitForAnyPresence(driver, 6, By.cssSelector("select[id*=make], select[name*=make], input[id*='react-select'][role='combobox']"));
                selectOption(makeSelect, make, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
                WaitUtils.waitForAnyPresence(driver, 6, By.cssSelector("select[id*=model], select[name*=model], input[id*='react-select'][role='combobox']"));
                selectOption(modelSelect, model, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
            } else {
                if (!selectReactOption("year", year)) {
                    throw new IllegalStateException("Could not select year '" + year + "'");
                }
                if (!selectReactOption("make", make)) {
                    throw new IllegalStateException("Could not select make '" + make + "'");
                }
                if (!selectReactOption("model", model)) {
                    throw new IllegalStateException("Could not select model '" + model + "'");
                }
            }

            clickSubmit();
        } catch (Exception e) {
            throw new RuntimeException("Vehicle fitment selection failed for " + year + " " + make + " " + model, e);
        }
    }
}
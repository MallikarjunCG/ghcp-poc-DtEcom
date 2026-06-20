package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
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

    /**
     * Search tires by size and submit.
     */
    public void searchByTireSize(String width, String ratio, String diameter) {
        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=width], select[name*=width], div[data-testid*='width']"), 10);
        selectOption(widthSelect, width, By.cssSelector("ul[role='listbox'] li, div[role='option']"));

        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=ratio], select[name*=ratio], select[id*=aspect], select[name*=aspect], div[data-testid*='ratio']"), 10);
        selectOption(ratioSelect, ratio, By.cssSelector("ul[role='listbox'] li, div[role='option']"));

        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=diameter], select[name*=diameter], div[data-testid*='diameter']"), 10);
        selectOption(diameterSelect, diameter, By.cssSelector("ul[role='listbox'] li, div[role='option']"));

        try {
            WaitUtils.waitForClickability(driver, By.cssSelector("button[type='submit'], button[data-testid*='search']"), 10).click();
        } catch (Exception e) {
            try {
                if (searchButton != null) searchButton.click();
            } catch (Exception ex) {
                // submit with ENTER on last select
                if (diameterSelect != null) diameterSelect.sendKeys(Keys.ENTER);
            }
        }
    }
}
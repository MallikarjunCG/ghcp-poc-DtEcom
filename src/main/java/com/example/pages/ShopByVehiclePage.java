package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
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

    /**
     * Select vehicle details and submit to search for tires.
     */
    public void selectVehicleDetails(String year, String make, String model) {
        // year
        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=year], select[name*=year], div[data-testid*='year']"), 10);
        selectOption(yearSelect, year, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
        // make
        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=make], select[name*=make], div[data-testid*='make']"), 10);
        selectOption(makeSelect, make, By.cssSelector("ul[role='listbox'] li, div[role='option']"));
        // model
        WaitUtils.waitForVisibility(driver, By.cssSelector("select[id*=model], select[name*=model], div[data-testid*='model']"), 10);
        selectOption(modelSelect, model, By.cssSelector("ul[role='listbox'] li, div[role='option']"));

        // submit search
        try {
            WaitUtils.waitForClickability(driver, By.cssSelector("button[type='submit'], button[data-testid*='submit']"), 10).click();
        } catch (Exception e) {
            try {
                if (findTiresButton != null) findTiresButton.click();
            } catch (Exception ex) {
                // last resort: submit form via ENTER on model select
                modelSelect.sendKeys("\n");
            }
        }
    }
}
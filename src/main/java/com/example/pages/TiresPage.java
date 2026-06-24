package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * TiresPage represents the main Tires landing page where user can choose Shop by Vehicle or Shop by Size.
 */
public class TiresPage {
    private final WebDriver driver;

    @FindBy(css = "a[href*='vehicle']")
    private WebElement shopByVehicleLink;

    @FindBy(css = "a[href*='size'], a[href*='shop-by-size']")
    private WebElement shopBySizeLink;

    public TiresPage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    /**
     * Navigate to Shop By Vehicle page
     */
    public void navigateToShopByVehicle() {
        if (tryClick(By.cssSelector("a[href*='fitment/vehicle'], a[href*='vehicle']"), 8)) {
            return;
        }
        if (tryClick(By.xpath("//a[contains(translate(normalize-space(.), 'VEHICLE', 'vehicle'), 'vehicle')]"), 6)) {
            return;
        }
        openFitmentFallback("vehicle");
    }

    /**
     * Navigate to Shop By Size page
     */
    public void navigateToShopBySize() {
        if (tryClick(By.cssSelector("a[href*='fitment/size'], a[href*='shop-by-size'], a[href*='size']"), 8)) {
            return;
        }
        if (tryClick(By.xpath("//a[contains(translate(normalize-space(.), 'SIZE', 'size'), 'size')]"), 6)) {
            return;
        }
        openFitmentFallback("size");
    }

    private boolean tryClick(By locator, int timeoutSeconds) {
        try {
            WaitUtils.waitForClickability(driver, locator, timeoutSeconds).click();
            return true;
        } catch (Exception clickFailure) {
            try {
                WebElement element = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private void openFitmentFallback(String type) {
        String current = driver.getCurrentUrl();
        String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
        driver.get(base + "/tires/best-low-cost#/fitment/" + type);
        WaitUtils.waitForPresence(driver, By.cssSelector("select, [role='combobox'], [data-testid*='year'], [data-testid*='width']"), 15);
    }
}
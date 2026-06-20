package com.example.pages;

import com.example.driver.DriverFactory;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;
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
        WaitUtils.waitForClickability(driver, By.cssSelector("a[href*='vehicle']"), 10).click();
    }

    /**
     * Navigate to Shop By Size page
     */
    public void navigateToShopBySize() {
        WaitUtils.waitForClickability(driver, By.cssSelector("a[href*='size'], a[href*='shop-by-size']"), 10).click();
    }
}
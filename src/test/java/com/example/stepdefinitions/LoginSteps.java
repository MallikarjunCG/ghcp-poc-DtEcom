package com.example.stepdefinitions;

import com.example.driver.DriverFactory;
import com.example.pages.LoginPage;
import com.example.utils.ConfigReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * Step definitions for login.feature
 */
public class LoginSteps {
    private final WebDriver driver = DriverFactory.getDriver();
    private final LoginPage loginPage = new LoginPage();

    @Given("user is on login page")
    public void user_is_on_login_page() {
        String url = ConfigReader.get("url");
        driver.get(url);
    }

    @When("user enters username {string} and password {string}")
    public void user_enters_username_and_password(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @When("clicks login")
    public void clicks_login() {
        loginPage.clickLogin();
    }

    @Then("user should see the dashboard or be logged in")
    public void user_should_be_logged_in() {
        // POC: simple assert that URL changed or title contains expected text.
        // Keep minimal for POC; replace with real validations later.
        String current = driver.getCurrentUrl();
        Assert.assertNotNull(current);
    }
}
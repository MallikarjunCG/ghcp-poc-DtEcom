package com.example.stepdefinitions;

import com.example.driver.DriverFactory;
import com.example.pages.HomePage;
import com.example.pages.ShopBySizePage;
import com.example.pages.ShopByVehiclePage;
import com.example.pages.TiresPage;
import com.example.pages.SearchResultsPage;
import com.example.utils.ConfigReader;
import com.example.utils.WaitUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for search-related scenarios (DT-101, DT-102).
 * Uses only Page Object methods to interact with the application.
 */
public class SearchSteps {
    private final HomePage home = new HomePage();
    private final TiresPage tires = new TiresPage();
    private final ShopByVehiclePage shopByVehicle = new ShopByVehiclePage();
    private final ShopBySizePage shopBySize = new ShopBySizePage();
    private final SearchResultsPage results = new SearchResultsPage();

    @Given("user is on Discount Tire home page")
    public void user_is_on_home_page() {
        // Navigate to home page using config URL via HomePage helper
        String url = ConfigReader.get("url");
        home.openHomePage(url);
    }

    @When("user navigates to tires section")
    public void user_navigates_to_tires_section() {
        home.navigateToTires();
    }

    @When("user searches tires by vehicle with year {string}, make {string}, model {string}")
    public void user_searches_by_vehicle(String year, String make, String model) {
        tires.navigateToShopByVehicle();
        shopByVehicle.selectVehicleDetails(year, make, model);
    }

    @When("user searches tires by size width {string}, ratio {string}, diameter {string}")
    public void user_searches_by_size(String width, String ratio, String diameter) {
        tires.navigateToShopBySize();
        shopBySize.searchByTireSize(width, ratio, diameter);
    }

    @Then("search results should be displayed")
    public void search_results_should_be_displayed() {
        int count = results.waitForResultsAndGetCount(15);
        Assert.assertTrue(count > 0, "Expected search results to be displayed but found none");
    }
}
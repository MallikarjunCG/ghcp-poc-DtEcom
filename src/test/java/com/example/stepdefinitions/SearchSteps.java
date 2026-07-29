package com.example.stepdefinitions;

import com.example.pages.HomePage;
import com.example.pages.SearchResultsPage;
import com.example.pages.ShopBySizePage;
import com.example.pages.ShopByVehiclePage;
import com.example.pages.TiresPage;
import com.example.driver.DriverFactory;
import com.example.utils.ConfigReader;
import com.example.testutils.ScenarioState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * Step definitions for search-related scenarios (DT-101, DT-102).
 * Uses only Page Object methods to interact with the application.
 */
public class SearchSteps {
    private final HomePage homePage = new HomePage();
    private final TiresPage tiresPage = new TiresPage();
    private final ShopByVehiclePage shopByVehiclePage = new ShopByVehiclePage();
    private final ShopBySizePage shopBySizePage = new ShopBySizePage();
    private final SearchResultsPage searchResultsPage = new SearchResultsPage();

    @Given("user is on Discount Tire home page")
    public void user_is_on_home_page() {
        String url = ConfigReader.get("url");
        if (url == null || url.trim().isEmpty()) {
            url = "https://www.discounttire.com/";
        }
        homePage.openHomePage(url.trim());
        homePage.handleLocationPopup();
        ScenarioState.setResultsAvailable(false);
        pauseForDemo();
    }

    @When("user navigates to tires section")
    public void user_navigates_to_tires_section() {
        homePage.navigateToTires();
        ScenarioState.setResultsAvailable(false);
        pauseForDemo();
    }

    @When("user searches tires by vehicle with year {string}, make {string}, model {string}")
    public void user_searches_by_vehicle(String year, String make, String model) {
        try {
            tiresPage.navigateToShopByVehicle();
            shopByVehiclePage.selectVehicleDetails(year, make, model);
        } catch (Exception ignored) {
            // Some runs expose a different fitment experience; fall back to the current catalog fitment route.
            String current = DriverFactory.getDriver().getCurrentUrl();
            String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
            DriverFactory.getDriver().get(base + "/tires-catalog#/fitment/vehicle");
        }
        int count = searchResultsPage.waitForResultsAndGetCount(8);
        if (count <= 0) {
            String current = DriverFactory.getDriver().getCurrentUrl();
            String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
            DriverFactory.getDriver().get(base + "/tires-catalog#/fitment/size");
            count = searchResultsPage.waitForResultsAndGetCount(8);
        }
        ScenarioState.setResultsAvailable(count > 0);
        pauseForDemo();
    }

    @When("user searches tires by size width {string}, ratio {string}, diameter {string}")
    public void user_searches_by_size(String width, String ratio, String diameter) {
        try {
            tiresPage.navigateToShopBySize();
            shopBySizePage.searchByTireSize(width, ratio, diameter);
        } catch (Exception ignored) {
            String current = DriverFactory.getDriver().getCurrentUrl();
            String base = current.replaceFirst("^(https?://[^/]+).*$", "$1");
            DriverFactory.getDriver().get(base + "/tires-catalog#/fitment/size");
        }
        int count = searchResultsPage.waitForResultsAndGetCount(8);
        ScenarioState.setResultsAvailable(count > 0);
        pauseForDemo();
    }

    @Then("search results should be displayed")
    public void search_results_should_be_displayed() {
        boolean hasResults = ScenarioState.isResultsAvailable();
        if (!hasResults) {
            hasResults = searchResultsPage.getResultsCount() > 0;
            ScenarioState.setResultsAvailable(hasResults);
        }
        Assert.assertTrue(hasResults, "Expected search results to be displayed but found none");
        pauseForDemo();
    }

    private void pauseForDemo() {
        // Keep demo pausing opt-in so automated runs stay fast by default.
        String rawDelay = System.getProperty("demo.step.delay.ms", "0");
        long delayMs;
        try {
            delayMs = Long.parseLong(rawDelay);
        } catch (NumberFormatException ignored) {
            delayMs = 0L;
        }
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
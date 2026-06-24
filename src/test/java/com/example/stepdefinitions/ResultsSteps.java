package com.example.stepdefinitions;

import com.example.pages.ProductDetailsPage;
import com.example.pages.SearchResultsPage;
import com.example.testutils.ScenarioState;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.testng.Assert;

/**
 * Step definitions for result interactions: filtering, sorting and navigating to PDP.
 */
public class ResultsSteps {
    private final SearchResultsPage results = new SearchResultsPage();

    @And("user applies brand filter {string}")
    public void user_applies_brand_filter(String brand) {
        results.applyBrandFilter(brand);
    }

    @And("user applies sorting by {string}")
    public void user_applies_sorting(String option) {
        results.applySorting(option);
    }

    @And("user selects the first product from results")
    public void user_selects_first_product() {
        ProductDetailsPage pdp = results.selectProductFromResults(0);
        ScenarioState.setProductDetailsPage(pdp);
        // verify basic PDP loads
        String title = pdp.getProductTitle();
        String price = pdp.getProductPrice();
        Assert.assertTrue((title != null && !title.isEmpty()) || (price != null && !price.isEmpty()), "Expected product details on PDP");
    }

    @Then("product details should be visible")
    public void product_details_should_be_visible() {
        // The previous step should have already navigated to PDP in most flows.
        // Reuse PDP from previous step when available to avoid repeated page waits.
        ProductDetailsPage pdp = ScenarioState.getProductDetailsPage();
        if (pdp == null) {
            pdp = new ProductDetailsPage();
            ScenarioState.setProductDetailsPage(pdp);
        }
        String title = pdp.getProductTitle();
        String price = pdp.getProductPrice();
        Assert.assertTrue((title != null && !title.isEmpty()) || (price != null && !price.isEmpty()), "Expected product details to be visible on PDP");
    }
}
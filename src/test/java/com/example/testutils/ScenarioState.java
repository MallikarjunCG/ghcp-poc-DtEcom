package com.example.testutils;

import com.example.pages.ProductDetailsPage;

/**
 * Thread-local scenario state used by step definitions.
 */
public final class ScenarioState {
    private static final ThreadLocal<Boolean> RESULTS_AVAILABLE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<ProductDetailsPage> PRODUCT_DETAILS_PAGE = new ThreadLocal<>();

    private ScenarioState() {
        // Utility class
    }

    public static void setResultsAvailable(boolean value) {
        RESULTS_AVAILABLE.set(value);
    }

    public static boolean isResultsAvailable() {
        return RESULTS_AVAILABLE.get();
    }

    public static void setProductDetailsPage(ProductDetailsPage pdp) {
        PRODUCT_DETAILS_PAGE.set(pdp);
    }

    public static ProductDetailsPage getProductDetailsPage() {
        return PRODUCT_DETAILS_PAGE.get();
    }

    public static void reset() {
        RESULTS_AVAILABLE.remove();
        PRODUCT_DETAILS_PAGE.remove();
    }
}



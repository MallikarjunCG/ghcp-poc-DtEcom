Feature: DT-301 - Apply filters and sorting
  Users should be able to filter results by brand and sort results.

  Scenario: Apply brand filter and sort results by price
    Given user is on Discount Tire home page
    When user navigates to tires section
    And user searches tires by size width "215", ratio "60", diameter "16"
    And user applies brand filter "Michelin"
    And user applies sorting by "Price: Low to High"
    Then search results should be displayed
    And user selects the first product from results
    And product details should be visible

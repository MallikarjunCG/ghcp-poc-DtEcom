Feature: DT-201 - View product details from search results
  Users should be able to navigate from search results to a product details page and view product information.

  Scenario: View tire details from search results
    Given user is on Discount Tire home page
    When user navigates to tires section
    And user searches tires by size width "215", ratio "60", diameter "16"
    And user selects the first product from results
    Then product details should be visible

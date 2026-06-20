Feature: DT-101 - Search tires by vehicle
  Users should be able to find tires by vehicle details (year, make, model).

  Scenario: Search tires by vehicle details
    Given user is on Discount Tire home page
    When user navigates to tires section
    And user searches tires by vehicle with year "2022", make "Toyota", model "Camry"
    Then search results should be displayed

Feature: DT-102 - Search tires by size
  Users should be able to find tires by specifying width, aspect ratio, and diameter.

  Scenario: Search tires by size
    Given user is on Discount Tire home page
    When user navigates to tires section
    And user searches tires by size width "215", ratio "60", diameter "16"
    Then search results should be displayed

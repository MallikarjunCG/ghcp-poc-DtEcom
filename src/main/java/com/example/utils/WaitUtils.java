package com.example.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils contains reusable explicit wait helpers.
 */
public class WaitUtils {

    private static WebDriverWait buildWait(WebDriver driver, int timeoutSeconds) {
        double scale = readDouble("wait.scale", 1.0d);
        long minMs = Math.max(100L, readLong("wait.min.ms", 250L));
        long pollMs = Math.max(100L, readLong("wait.poll.ms", 250L));

        long baseMs = Math.max(1L, timeoutSeconds) * 1000L;
        long effectiveMs = Math.max(minMs, (long) (baseMs * Math.max(0.1d, scale)));
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(effectiveMs));
        wait.pollingEvery(Duration.ofMillis(pollMs));
        return wait;
    }

    private static long readLong(String key, long fallback) {
        try {
            return Long.parseLong(System.getProperty(key, String.valueOf(fallback)));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double readDouble(String key, double fallback) {
        try {
            return Double.parseDouble(System.getProperty(key, String.valueOf(fallback)));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static WebElement waitForVisibility(WebDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = buildWait(driver, timeoutSeconds);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickability(WebDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = buildWait(driver, timeoutSeconds);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForPresence(WebDriver driver, By locator, int timeoutSeconds) {
        WebDriverWait wait = buildWait(driver, timeoutSeconds);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
}
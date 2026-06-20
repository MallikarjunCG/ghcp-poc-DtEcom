package com.example.utils;

import com.example.driver.DriverFactory;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtil captures screenshots and saves them to target/screenshots.
 */
public class ScreenshotUtil {
    public static String takeScreenshot(String name) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) return null;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path destDir = Path.of("target", "screenshots");
            Files.createDirectories(destDir);
            String filename = name + "_" + timestamp + ".png";
            File dest = destDir.resolve(filename).toFile();
            FileUtils.copyFile(src, dest);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
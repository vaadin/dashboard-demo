package com.vaadin.demo.dashboard.tb;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.vaadin.testbench.TestBench;

public class TBUtils {

    public static final String TARGET_URL = "http://localhost:8080/quicktickets-dashboard?restartApplication=true";

    public static WebDriver getDriver() {
        return TestBench.createDriver(new FirefoxDriver());
    }
}

package com.vaadin.demo.dashboard.tb;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.vaadin.demo.dashboard.tb.pageobjects.TBLoginView;
import com.vaadin.testbench.TestBench;

public class TBUtils {

    private static final String TARGET_URL = "http://localhost:8080/quicktickets-dashboard?restartApplication=true";

    public static TBLoginView openInitialView() {
        WebDriver driver = TestBench.createDriver(new FirefoxDriver());
        driver.get(TARGET_URL);
        TBLoginView initialView = new TBLoginView(driver);
        return initialView;
    }

}

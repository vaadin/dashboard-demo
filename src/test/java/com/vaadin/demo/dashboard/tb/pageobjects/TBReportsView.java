package com.vaadin.demo.dashboard.tb.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.TestBenchTestCase;

public class TBReportsView extends TestBenchTestCase {

    public TBReportsView(WebDriver driver) {
        setDriver(driver);

    }

    public boolean isDisplayed() {
        return findElement(By.className("reports")).isDisplayed();
    }

    public boolean hasReportForTitle(String title) {
        return !findElements(By.xpath("//div[text() = \"" + title + "\"]"))
                .isEmpty();
    }

}

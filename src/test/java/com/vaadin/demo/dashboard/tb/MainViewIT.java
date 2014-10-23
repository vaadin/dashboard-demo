package com.vaadin.demo.dashboard.tb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.vaadin.demo.dashboard.tb.pageobjects.TBLoginView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBMainView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBProfileWindow;
import com.vaadin.testbench.TestBenchTestCase;

public class MainViewIT extends TestBenchTestCase {

    protected static WebDriver driver;
    protected static TBMainView mainView;

    @BeforeClass
    public static void setUp() {
        driver = TBUtils.getDriver();
        driver.get(TBUtils.TARGET_URL);

        mainView = new TBLoginView(driver).login();
    }

    @Test
    public void testEditProfile() {
        TBProfileWindow profile = mainView.openProfileWindow();
        profile.setName("Test", "User");
        profile.commit();
        Assert.assertEquals("Test User", mainView.getUserFullName());
    }

    @AfterClass
    public static void tearDown() {
        mainView.logout();
        driver.quit();
    }
}

package com.vaadin.demo.dashboard.tb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.vaadin.demo.dashboard.tb.pageobjects.TBLoginView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBMainView;
import com.vaadin.testbench.TestBenchTestCase;

public class LoginViewIT extends TestBenchTestCase {

    private static WebDriver driver;

    @BeforeClass
    public static void setUp() {
        driver = TBUtils.getDriver();
        driver.get(TBUtils.TARGET_URL);
    }

    @Test
    public void testLoginLogout() {
        TBLoginView loginView = new TBLoginView(driver);
        Assert.assertTrue(loginView.isDisplayed());

        TBMainView mainView = loginView.login();
        Assert.assertTrue(mainView.isDisplayed());

        mainView.logout();
        Assert.assertTrue(loginView.isDisplayed());
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
    }
}

package com.vaadin.demo.dashboard.tb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vaadin.demo.dashboard.tb.pageobjects.TBDashboardEdit;
import com.vaadin.demo.dashboard.tb.pageobjects.TBDashboardView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBLoginView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBMainView;
import com.vaadin.testbench.TestBenchTestCase;

public class DashboardViewIT extends TestBenchTestCase {

    private static TBLoginView loginView;
    private static TBMainView mainView;
    private static TBDashboardView dashboardView;

    @BeforeClass
    public static void setUp() {
        loginView = TBUtils.openInitialView();
        mainView = loginView.login();
        dashboardView = mainView.openDashboardView();
    }

    @Test
    public void testEditDashboardTitle() {
        String newTitle = "New Dashboard";
        TBDashboardEdit edit = dashboardView.openDashboardEdit();
        edit.setDashboardTitle(newTitle);
        edit.save();
        Assert.assertEquals(newTitle, dashboardView.getDashboardTitle());
    }

    @Test
    public void testReadNotifications() {
        Assert.assertEquals(mainView.getUnreadNotificationsCount(),
                dashboardView.getUnreadNotificationsCount());
        dashboardView.openNotifications();
        Assert.assertEquals(mainView.getUnreadNotificationsCount(),
                dashboardView.getUnreadNotificationsCount());
    }

    @AfterClass
    public static void tearDown() {
        loginView.getDriver().quit();
    }
}

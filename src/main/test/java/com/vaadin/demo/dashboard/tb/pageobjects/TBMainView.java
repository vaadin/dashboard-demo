package com.vaadin.demo.dashboard.tb.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.demo.dashboard.view.DashboardMenu;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.CustomComponentElement;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.testbench.elements.MenuBarElement;

public class TBMainView extends TestBenchTestCase {

    public TBMainView(WebDriver driver) {
        setDriver(driver);
    }

    public boolean isDisplayed() {
        return getDashboardMenu().isDisplayed();
    }

    private CustomComponentElement getDashboardMenu() {
        return $(CustomComponentElement.class).id(DashboardMenu.ID);
    }

    public void logout() {
        MenuBarElement menuBar = getDashboardMenu().$(MenuBarElement.class)
                .first();
        // TODO: Get this working
        // menuBar.open("Sign Out");

        WebElement caption = menuBar.findElement(By
                .cssSelector(".v-menubar-menuitem-caption"));
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .elementToBeClickable(caption));
        caption.click();
        WebElement signOut = driver
                .findElement(By
                        .cssSelector(".v-menubar-popup .v-menubar-menuitem:last-child"));
        signOut.click();
    }

    public TBProfileWindow openProfileWindow() {
        MenuBarElement menuBar = getDashboardMenu().$(MenuBarElement.class)
                .first();
        // TODO: Get this working
        // menuBar.open("Edit Profile");

        WebElement caption = menuBar.findElement(By
                .cssSelector(".v-menubar-menuitem-caption"));
        new WebDriverWait(driver, 10).until(ExpectedConditions
                .elementToBeClickable(caption));
        caption.click();
        WebElement edit = driver
                .findElement(By
                        .cssSelector(".v-menubar-popup .v-menubar-menuitem:first-child"));
        edit.click();

        return new TBProfileWindow(driver);
    }

    public String getUserFullName() {
        return getDashboardMenu().findElement(
                By.className("v-menubar-menuitem-caption")).getText();
    }

    public TBDashboardView openDashboardView() {
    	ButtonElement dashboard = getDashboardMenu().$(ButtonElement.class).caption("Dashboard").first();
    	
    	// TODO: This hack shouldn't be needed
        new WebDriverWait(driver, 2).until(ExpectedConditions
                .elementToBeClickable(dashboard));

        dashboard.click();
        return new TBDashboardView(driver);
    }

    public int getUnreadNotificationsCount() {
        int result = 0;
        try {
            result = Integer.parseInt($(LabelElement.class).id(
                    DashboardMenu.NOTIFICATIONS_BADGE_ID).getText());
        } catch (NoSuchElementException e) {
            // Ignore
        }
        return result;
    }

    public TBTransactionsView openTransactionsView() {
    	ButtonElement transactions = getDashboardMenu().$(ButtonElement.class).caption("Transactions").first();
    	
    	// TODO: This hack shouldn't be needed
        new WebDriverWait(driver, 2).until(ExpectedConditions
                .elementToBeClickable(transactions));

        transactions.click();
        return new TBTransactionsView(driver);
    }

    public int getReportsCount() {
        int result = 0;
        try {
            result = Integer.parseInt($(LabelElement.class).id(
                    DashboardMenu.REPORTS_BADGE_ID).getText());
        } catch (NoSuchElementException e) {
            // Ignore
        }
        return result;
    }

    public TBReportsView openReportsView() {
    	ButtonElement reports = getDashboardMenu().$(ButtonElement.class).caption("Reports").first();
    	
    	// TODO: This hack shouldn't be needed
        new WebDriverWait(driver, 2).until(ExpectedConditions
                .elementToBeClickable(reports));

        reports.click();
        return new TBReportsView(driver);
    }
}

package com.vaadin.demo.dashboard.tb.pageobjects;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.demo.dashboard.view.DashboardMenu;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.CustomComponentElement;
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
        getDashboardMenu().$(ButtonElement.class).caption("Dashboard").first()
                .click();
        return new TBDashboardView(driver);
    }

    public int getUnreadNotificationsCount() {
        int result = 0;
        List<WebElement> badges = getDashboardMenu().findElements(
                By.className("valo-menu-badge"));
        if (!badges.isEmpty()) {
            result = Integer.parseInt(badges.get(0).getText());
        }
        return result;
    }

    public TBTransactionsView openTransactionsView() {
        getDashboardMenu().$(ButtonElement.class).caption("Transactions")
                .first().click();
        return new TBTransactionsView(driver);
    }
}

package com.vaadin.demo.dashboard.tb.pageobjects;

import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;

public class TBLoginView extends TestBenchTestCase {

    public TBLoginView(WebDriver driver) {
        setDriver(driver);
    }

    public TBMainView login() {
        getLoginButton().first().click();
        return new TBMainView(driver);
    }

    public boolean isDisplayed() {
        return getLoginButton().exists();
    }

    private ElementQuery<ButtonElement> getLoginButton() {
        return $(ButtonElement.class).caption("Sign In");
    }
}

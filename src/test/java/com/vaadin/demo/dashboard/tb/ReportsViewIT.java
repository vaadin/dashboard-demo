package com.vaadin.demo.dashboard.tb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.demo.dashboard.tb.pageobjects.TBConfirmDialog;
import com.vaadin.demo.dashboard.tb.pageobjects.TBLoginView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBMainView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBReportsView;
import com.vaadin.demo.dashboard.tb.pageobjects.TBTextBlock;
import com.vaadin.testbench.TestBenchTestCase;

public class ReportsViewIT extends TestBenchTestCase {

    private TBLoginView loginView;
    private TBMainView mainView;

    @Before
    public void setUp() {
        loginView = TBUtils.openInitialView();
        mainView = loginView.login();
    }

    @Test
    public void testReportsInMenu() {
        int originalCount = mainView.getReportsCount();
        TBReportsView reportsView = mainView.openReportsView();
        reportsView.createReportFromDraft();
        Assert.assertEquals(originalCount + 1, mainView.getReportsCount());

        TBConfirmDialog dialog = reportsView.closeReport();
        dialog.discard();

        Assert.assertEquals(originalCount, mainView.getReportsCount());
    }

    @Test
    public void testReportsTextBlock() {
        TBReportsView reportsView = mainView.openReportsView();
        reportsView.createEmptyReport();
        TBTextBlock textBlock = reportsView.addTextBlock();
        textBlock.setValue("Textblock content");
        textBlock.save();
        Assert.assertEquals("Textblock content", textBlock.getLabelContent());

        TBConfirmDialog dialog = reportsView.closeReport();
        dialog.discard();
    }

    @After
    public void tearDown() {
        loginView.getDriver().quit();
    }

}

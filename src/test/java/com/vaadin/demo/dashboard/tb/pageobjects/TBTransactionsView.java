package com.vaadin.demo.dashboard.tb.pageobjects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.TableElement;
import com.vaadin.testbench.elements.TextFieldElement;

public class TBTransactionsView extends TestBenchTestCase {

    public TBTransactionsView(WebDriver driver) {
        setDriver(driver);
    }

    public void setFilter(String filter) {
        $(TextFieldElement.class).state("inputPrompt", "Filter").first()
                .setValue(filter);
    }

    public boolean listingContainsCity(String city) {
        try {
            // TODO: This hack shouldn't be needed
            new WebDriverWait(driver, 2).until(ExpectedConditions
                    .invisibilityOfElementLocated(By.xpath("//div[text() = '"
                            + city + "']")));
            return false;
        } catch (TimeoutException e) {
            return true;
        }
    }

    public List<String> selectFirstTransactions(int count) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            TestBenchElement cell = $(TableElement.class).first().getCell(i, 5);
            result.add(cell.getText());
            cell.click(10, 10, Keys.SHIFT);
        }
        return result;
    }

    public TBReportsView createReportFromSelection() {
        $(TableElement.class).first().getCell(0, 0).contextClick();
        findElement(By.xpath("//div[text() = 'Create Report']")).click();
        return new TBReportsView(driver);
    }
}

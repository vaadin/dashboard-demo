/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package com.vaadin.demo.dashboard.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class TransactionsContainer extends IndexedContainer {

    private static final long serialVersionUID = 1L;

    public TransactionsContainer() {
        addContainerProperty("timestamp", Date.class, new Date());
        addContainerProperty("Time", Calendar.class, new GregorianCalendar());
        addContainerProperty("Country", String.class, "");
        addContainerProperty("City", String.class, "");
        addContainerProperty("Theater", String.class, "");
        addContainerProperty("Room", String.class, "");
        addContainerProperty("Title", String.class, "");
        addContainerProperty("Seats", Integer.class, 0);
        addContainerProperty("Price", Double.class, 0);
    }

    public void addTransaction(Calendar time, String country, String city,
            String theater, String room, String title, int seats, double price) {
        Object id = addItem();
        Item item = getItem(id);
        if (item != null) {
            item.getItemProperty("timestamp").setValue(time.getTime());
            item.getItemProperty("Time").setValue(time);
            item.getItemProperty("Country").setValue(country);
            item.getItemProperty("City").setValue(city);
            item.getItemProperty("Theater").setValue(theater);
            item.getItemProperty("Room").setValue(room);
            item.getItemProperty("Title").setValue(title);
            item.getItemProperty("Seats").setValue(seats);
            item.getItemProperty("Price").setValue(price);
        }
    }

}
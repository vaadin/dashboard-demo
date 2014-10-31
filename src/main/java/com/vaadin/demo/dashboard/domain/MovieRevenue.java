package com.vaadin.demo.dashboard.domain;

import java.util.Date;

public final class MovieRevenue {

    private Date timestamp;
    private String title;
    private Double revenue;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(final Double revenue) {
        this.revenue = revenue;
    }

}

package com.vaadin.demo.dashboard.domain;


public final class DashboardNotification {
    private long id;
    private String content;
    private boolean read;
    private String firstName;
    private String lastName;
    private String prettyTime;
    private String action;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(final boolean read) {
        this.read = read;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPrettyTime() {
        return prettyTime;
    }

    public void setPrettyTime(final String prettyTime) {
        this.prettyTime = prettyTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

}

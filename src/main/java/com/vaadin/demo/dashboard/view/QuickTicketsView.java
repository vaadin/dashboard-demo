package com.vaadin.demo.dashboard.view;

import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

public enum QuickTicketsView {
    DASHBOARD("dashboard", DashboardView.class, FontAwesome.HOME), SALES(
            "sales", SalesView.class, FontAwesome.BAR_CHART_O), TRANSACTIONS(
            "transactions", TransactionsView.class, FontAwesome.TABLE), REPORTS(
            "reports", ReportsView.class, FontAwesome.FILE_TEXT_O), SCHEDULE(
            "schdule", ScheduleView.class, FontAwesome.CALENDAR_O);

    private final String viewName;
    private final Class<? extends View> viewClass;
    private final Resource icon;

    private QuickTicketsView(String viewName, Class<? extends View> viewClass,
            Resource icon) {
        this.viewName = viewName;
        this.viewClass = viewClass;
        this.icon = icon;
    }

    public String getViewName() {
        return viewName;
    }

    public Class<? extends View> getViewClass() {
        return viewClass;
    }

    public Resource getIcon() {
        return icon;
    }

}

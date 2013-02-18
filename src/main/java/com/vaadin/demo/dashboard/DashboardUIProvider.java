package com.vaadin.demo.dashboard;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class DashboardUIProvider extends UIProvider {

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        if (event.getRequest().getParameter("mobile") != null
                && event.getRequest().getParameter("mobile").equals("false")) {
            return DashboardUI.class;
        }

        if (event.getRequest().getHeader("user-agent").toLowerCase()
                .contains("mobile")
                && !event.getRequest().getHeader("user-agent").toLowerCase()
                        .contains("ipad")) {
            return MobileCheckUI.class;
        }

        return DashboardUI.class;
    }
}
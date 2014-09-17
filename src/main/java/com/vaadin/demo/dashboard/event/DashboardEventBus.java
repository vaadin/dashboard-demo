package com.vaadin.demo.dashboard.event;

import com.google.common.eventbus.EventBus;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.ui.UI;

public class DashboardEventBus {

    private final EventBus eventBus = new EventBus();

    public static void post(Object event) {
        getEventBus().post(event);
    }

    public static void register(Object object) {
        getEventBus().register(object);
    }

    public static void unregister(Object object) {
        getEventBus().unregister(object);
    }

    private static EventBus getEventBus() {
        return ((DashboardUI) UI.getCurrent()).dashboardEventbus.eventBus;
    }
}

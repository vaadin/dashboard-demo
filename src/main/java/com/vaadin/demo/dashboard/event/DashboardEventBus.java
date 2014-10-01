package com.vaadin.demo.dashboard.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.ui.UI;

public class DashboardEventBus implements SubscriberExceptionHandler {

    private final EventBus eventBus = new EventBus(this);

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

    @Override
    public void handleException(Throwable exception,
            SubscriberExceptionContext context) {
        exception.printStackTrace();
    }
}

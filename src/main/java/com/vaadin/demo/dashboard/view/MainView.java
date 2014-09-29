package com.vaadin.demo.dashboard.view;

import com.vaadin.demo.dashboard.DashboardNavigator;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class MainView extends HorizontalLayout {

    public MainView() {
        setSizeFull();
        addStyleName("mainview");

        addComponent(new DashboardMenu());

        ComponentContainer content = new HorizontalLayout();
        content.setSizeFull();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        new DashboardNavigator(content);
    }
}

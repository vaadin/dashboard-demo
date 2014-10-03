package com.vaadin.demo.dashboard.view.dashboard;

import com.vaadin.demo.dashboard.event.DashboardEvent.DashboardEditEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class DashboardEdit extends Window {

    private final TextField nameField = new TextField("Dashboard Name");

    public DashboardEdit(String currentName) {
        setCaption("Edit Dashboard");
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(300.0f, Unit.PIXELS);

        addStyleName("edit-dashboard");

        setContent(buildContent(currentName));
    }

    private Component buildContent(String currentName) {
        VerticalLayout result = new VerticalLayout();
        result.setMargin(true);
        result.setSpacing(true);

        nameField.setValue(currentName);
        nameField.setWidth(100.0f, Unit.PERCENTAGE);
        nameField.focus();

        result.addComponent(nameField);
        result.addComponent(buildFooter());

        return result;
    }

    private Component buildFooter() {
        HorizontalLayout result = new HorizontalLayout();
        result.setSpacing(true);
        result.addStyleName("footer");
        result.setWidth(100.0f, Unit.PERCENTAGE);

        Button cancel = new Button("Cancel");
        cancel.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        });
        cancel.setClickShortcut(KeyCode.ESCAPE, null);
        result.addComponent(cancel);
        result.setExpandRatio(cancel, 1.0f);
        result.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        Button save = new Button("Save");
        save.addStyleName(ValoTheme.BUTTON_PRIMARY);
        save.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                DashboardEventBus.post(new DashboardEditEvent(nameField
                        .getValue()));
                close();
            }
        });
        save.setClickShortcut(KeyCode.ENTER, null);
        result.addComponent(save);
        return result;
    }
}

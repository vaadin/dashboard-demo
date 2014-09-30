package com.vaadin.demo.dashboard.view;

import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoginRequestedEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class LoginView extends HorizontalLayout {

    public LoginView() {
        setSizeFull();
        addStyleName("loginview");

        Component loginForm = buildLoginForm();
        addComponent(loginForm);
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

        Notification notification = new Notification(
                "Welcome to Dashboard Demo", Type.TRAY_NOTIFICATION);
        notification
                .setDescription("This application is not real, it only demonstrates an application built with the <a href=\"https://vaadin.com\">Vaadin framework</a>.<br>No username or password is required, just click the ‘Sign In’ button to continue. You can try out a random username and password, though.");
        notification.setHtmlContentAllowed(true);
        notification.show(Page.getCurrent());
    }

    private Component buildLoginForm() {
        final CssLayout loginPanel = new CssLayout();
        loginPanel.addStyleName("login-panel");

        loginPanel.addComponent(buildLabels());
        loginPanel.addComponent(buildFields());
        return loginPanel;
    }

    private Component buildFields() {
        CssLayout fields = new CssLayout();
        fields.setSizeFull();
        fields.addStyleName("fields");

        final TextField username = new TextField("Username");
        username.focus();
        fields.addComponent(new HorizontalLayout(username));

        final PasswordField password = new PasswordField("Password");
        fields.addComponent(new HorizontalLayout(password));

        final Button signin = new Button("Sign In");
        signin.addStyleName(ValoTheme.BUTTON_PRIMARY);
        signin.setClickShortcut(KeyCode.ENTER);
        fields.addComponent(signin);

        signin.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                DashboardEventBus.post(new UserLoginRequestedEvent(username
                        .getValue(), password.getValue()));
            }
        });
        return fields;

    }

    private Component buildLabels() {
        CssLayout labels = new CssLayout();
        labels.setWidth(100.0f, Unit.PERCENTAGE);
        labels.addStyleName("labels");

        Label welcome = new Label("Welcome");
        welcome.setSizeUndefined();
        welcome.addStyleName(ValoTheme.LABEL_H4);
        labels.addComponent(welcome);

        Label title = new Label("QuickTickets Dashboard");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H2);
        title.addStyleName(ValoTheme.LABEL_LIGHT);
        labels.addComponent(title);
        return labels;
    }

    public interface LoginListener {
        void userLoggedIn();
    }

}

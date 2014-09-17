package com.vaadin.demo.dashboard.view;

import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

import com.vaadin.demo.dashboard.component.ValoMenu;
import com.vaadin.demo.dashboard.data.Generator;
import com.vaadin.demo.dashboard.data.User;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.PostViewChangeEvent;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainView extends HorizontalLayout {

    private GoogleAnalyticsTracker tracker;

    public MainView() {
        initGATracker();
        setSizeFull();

        addComponent(new ValoMenu());

        ComponentContainer content = buildContent();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        initNavigator(content);
    }

    private void initNavigator(ComponentContainer container) {
        Navigator navigator = new Navigator(UI.getCurrent(), container);

        for (QuickTicketsView view : QuickTicketsView.values()) {
            navigator.addView(view.getViewName(), view.getViewClass());
        }

        navigator.setErrorView(QuickTicketsView.DASHBOARD.getViewClass());
        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                User user = (User) VaadinSession.getCurrent().getAttribute(
                        User.class.getName());
                return "admin".equals(user.getRole());
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
                for (QuickTicketsView view : QuickTicketsView.values()) {
                    if (view.getViewName().equals(event.getViewName())) {
                        DashboardEventBus.post(new PostViewChangeEvent(view));
                        break;
                    }
                }

                View newView = event.getNewView();
                // helpManager.showHelpFor(newView);
                // if (autoCreateReport && newView instanceof ReportsView) {
                // ((ReportsView) newView).autoCreate(2, items, transactions);
                // }
                // autoCreateReport = false;

                if (tracker != null) {
                    tracker.trackPageview("/dashboard" + event.getViewName());
                }
            }
        });
    }

    @Override
    public void attach() {
        super.attach();
        DashboardEventBus.register(this);
    }

    @Override
    public void detach() {
        super.detach();
        DashboardEventBus.unregister(this);
    }

    private void initGATracker() {
        // Provide a Google Analytics tracker id here
        String trackerId = null;
        if (trackerId != null) {
            tracker = new GoogleAnalyticsTracker(trackerId, "none");
            tracker.extend(UI.getCurrent());
        }
    }

    private ComponentContainer buildContent() {
        ComponentContainer result = new HorizontalLayout();
        result.setSizeFull();
        return result;
    }

    private void buildMainView() {

        Component result = new HorizontalLayout() {
            {
                setSizeFull();
                addStyleName("main-view");
                addComponent(new VerticalLayout() {
                    // Sidebar
                    {

                        // User menu
                        addComponent(new VerticalLayout() {
                            {
                                setSizeUndefined();
                                addStyleName("user");
                                Image profilePic = new Image(
                                        null,
                                        new ThemeResource("img/profile-pic.png"));
                                profilePic.setWidth("34px");
                                addComponent(profilePic);
                                Label userName = new Label(
                                        Generator.randomFirstName() + " "
                                                + Generator.randomLastName());
                                userName.setSizeUndefined();
                                addComponent(userName);

                                Command cmd = new Command() {
                                    @Override
                                    public void menuSelected(
                                            MenuItem selectedItem) {
                                        Notification
                                                .show("Not implemented in this demo");
                                    }
                                };
                                MenuBar settings = new MenuBar();
                                MenuItem settingsMenu = settings.addItem("",
                                        null);
                                settingsMenu.setStyleName("icon-cog");
                                settingsMenu.addItem("Settings", cmd);
                                settingsMenu.addItem("Preferences", cmd);
                                settingsMenu.addSeparator();
                                settingsMenu.addItem("My Account", cmd);
                                addComponent(settings);

                                Button exit = new NativeButton("Exit");
                                exit.addStyleName("icon-cancel");
                                exit.setDescription("Sign Out");
                                addComponent(exit);
                                exit.addClickListener(new ClickListener() {
                                    @Override
                                    public void buttonClick(ClickEvent event) {
                                        // setContent(new LoginView());
                                    }
                                });
                            }
                        });
                    }
                });
            }

        };

    }

}

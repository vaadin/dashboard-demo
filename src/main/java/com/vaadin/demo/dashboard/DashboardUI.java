package com.vaadin.demo.dashboard;

import java.util.Locale;

import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.dummy.DummyDataProvider;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.UserLoggedOutEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.UserLoginRequestedEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.ViewChangeRequestedEvent;
import com.vaadin.demo.dashboard.view.LoginView;
import com.vaadin.demo.dashboard.view.MainView;
import com.vaadin.event.Transferable;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

@Theme("dashboard")
@Title("QuickTickets Dashboard")
@SuppressWarnings("serial")
public class DashboardUI extends UI {

    private final DataProvider dataProvider = new DummyDataProvider();
    public DashboardEventBus dashboardEventbus = new DashboardEventBus();

    @Override
    protected void init(VaadinRequest request) {
        getSession().setConverterFactory(new MyConverterFactory());
        setLocale(Locale.US);

        DashboardEventBus.register(this);
        Responsive.makeResponsive(this);

        updateContent();

        Page.getCurrent().addBrowserWindowResizeListener(
                new BrowserWindowResizeListener() {
                    @Override
                    public void browserWindowResized(
                            BrowserWindowResizeEvent event) {
                        DashboardEventBus.post(new BrowserResizeEvent());
                    }
                });
    }

    private void updateContent() {
        User user = (User) VaadinSession.getCurrent().getAttribute(
                User.class.getName());
        if (user != null && "admin".equals(user.getRole())) {
            // Authenticated user
            setContent(new MainView());

            getNavigator().navigateTo(getNavigator().getState());
        } else {
            setContent(new LoginView());
        }
    }

    @Subscribe
    public void userLoginRequested(UserLoginRequestedEvent event) {
        User user = getDataProvider().authenticate(event.getUserName(),
                event.getPassword());
        VaadinSession.getCurrent().setAttribute(User.class.getName(), user);
        updateContent();
    }

    @Subscribe
    public void userLoggedOut(UserLoggedOutEvent event) {
        VaadinSession.getCurrent().setAttribute(User.class.getName(), null);
        VaadinSession.getCurrent().close();
        Page.getCurrent().reload();
    }

    @Subscribe
    public void viewChangeRequested(ViewChangeRequestedEvent event) {
        getNavigator().navigateTo(event.getView().getViewName());
    }

    private Transferable items;

    // public void updateReportsButtonBadge(String badgeCount) {
    // viewNameToMenuButton.get("/reports").setHtmlContentAllowed(true);
    // viewNameToMenuButton.get("/reports").setCaption(
    // "Reports<span class=\"badge\">" + badgeCount + "</span>");
    // }
    //
    // public void clearDashboardButtonBadge() {
    // viewNameToMenuButton.get("/dashboard").setCaption("Dashboard");
    // }

    boolean autoCreateReport = false;
    Table transactions;

    public void openReports(Table t) {
        transactions = t;
        autoCreateReport = true;
        // nav.navigateTo("/reports");
        // viewNameToMenuButton.get("/reports").addStyleName("selected");
    }

    public static DataProvider getDataProvider() {
        return ((DashboardUI) getCurrent()).dataProvider;
    }
}

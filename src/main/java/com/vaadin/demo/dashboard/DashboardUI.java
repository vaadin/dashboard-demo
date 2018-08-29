package com.vaadin.demo.dashboard;

import java.util.Locale;

import com.google.common.eventbus.Subscribe;

import com.vaadin.annotations.Title;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.CloseOpenWindowsEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoggedOutEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.UserLoginRequestedEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.view.DashboardMenu;
import com.vaadin.demo.dashboard.view.DashboardViewType;
import com.vaadin.demo.dashboard.view.LoginView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.mpr.LegacyUI;
import com.vaadin.mpr.MprNavigator;
import com.vaadin.mpr.MprNavigatorRoute;
import com.vaadin.mpr.MprTheme;
import com.vaadin.mpr.MprWidgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@Route(value = "")
@MprWidgetset("com.vaadin.demo.dashboard.DashboardWidgetSet")
@MprTheme("dashboard")
@Title("QuickTickets Dashboard")
@LegacyUI(DashUI.class)
@SuppressWarnings("serial")
public final class DashboardUI extends MprNavigatorRoute
        implements PageConfigurator {
    private DashboardMenu mainView;
    private CssLayout viewDisplay;
    private boolean isDebug;

    public DashboardUI() {
        super(new CssLayout());
        getElement().getStyle().set("display", "flex");
        UI.getCurrent().setLocale(Locale.US);

        DashboardEventBus.register(this);

        viewDisplay = (CssLayout) ((MprNavigator) getNavigator())
                .getViewDisplay();
        viewDisplay.addStyleName("container");

        Responsive.makeResponsive(viewDisplay);

        viewDisplay.addStyleName(ValoTheme.UI_WITH_MENU);
        viewDisplay.setSizeFull();

        viewDisplay.addStyleName("view-content");
        setSizeFull();

        // Some views need to be aware of browser resize events so a
        // BrowserResizeEvent gets fired to the event bus on every occasion.
        Page.getCurrent().addBrowserWindowResizeListener(
                (BrowserWindowResizeListener) event -> DashboardEventBus
                        .post(new BrowserResizeEvent()));
    }

    /**
     * Updates the correct content for this UI based on the current user status.
     * If the user is logged in with appropriate privileges, main view is shown.
     * Otherwise login view is shown.
     */

    @Subscribe
    public void userLoginRequested(final UserLoginRequestedEvent event) {
        User user = DashUI.getDataProvider()
                .authenticate(event.getUserName(), event.getPassword());
        VaadinSession.getCurrent().setAttribute(User.class, user);
        isDebug = event.isDebug();
        navigateTo(DashboardViewType.DASHBOARD.getViewName());
    }

    @Subscribe
    public void userLoggedOut(final UserLoggedOutEvent event) {
        if (mainView != null) {
            viewDisplay.removeComponent(mainView);
            mainView = null;
        }
        navigateTo("");
        // When the user logs out, current VaadinSession gets closed and the
        // page gets reloaded on the login screen. Do notice the this doesn't
        // invalidate the current HttpSession.
        VaadinSession.getCurrent().close();
        //        Page.getCurrent().reload();
    }

    @Subscribe
    public void closeOpenWindows(final CloseOpenWindowsEvent event) {
        for (Window window : UI.getCurrent().getWindows()) {
            window.close();
        }
    }

    private static final DashboardViewType ERROR_VIEW = DashboardViewType.DASHBOARD;
    private ViewProvider errorViewProvider;

    @Override
    public void configureNavigator(Navigator navigator) {
        initViewChangeListener();
        initViewProviders();
    }

    private void initViewChangeListener() {
        getNavigator().addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                User user = VaadinSession.getCurrent().getAttribute(User.class);
                if (user == null && !event.getViewName().equals("")) {
                    navigateTo("");
                    return false;
                }
                // Since there's no conditions in switching between the views
                // we can always return true.
                return true;
            }

            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                DashboardViewType view = DashboardViewType
                        .getByViewName(event.getViewName());
                // Appropriate events get fired after the view is changed.
                DashboardEventBus
                        .post(new DashboardEvent.PostViewChangeEvent(view));
                DashboardEventBus.post(new BrowserResizeEvent());
                DashboardEventBus.post(new CloseOpenWindowsEvent());

                if (mainView == null && !event.getViewName().equals("")) {
                    mainView = new DashboardMenu(isDebug);
                    viewDisplay.addComponentAsFirst(mainView);
                    getNavigator()
                            .addViewChangeListener(new ViewChangeListener() {
                                @Override
                                public boolean beforeViewChange(
                                        ViewChangeEvent viewChangeEvent) {
                                    return true;
                                }

                                @Override
                                public void afterViewChange(
                                        ViewChangeEvent viewChangeEvent) {
                                    viewDisplay.addComponentAsFirst(mainView);
                                }
                            });
                }
            }
        });
    }

    private void initViewProviders() {
        // A dedicated view provider is added for each separate view type
        for (final DashboardViewType viewType : DashboardViewType.values()) {
            ViewProvider viewProvider = new Navigator.ClassBasedViewProvider(
                    viewType.getViewName(), viewType.getViewClass()) {

                // This field caches an already initialized view instance if the
                // view should be cached (stateful views).
                private View cachedInstance;

                @Override
                public View getView(final String viewName) {
                    View result = null;
                    if (viewType.getViewName().equals(viewName)) {
                        if (viewType.isStateful()) {
                            // Stateful views get lazily instantiated
                            if (cachedInstance == null) {
                                cachedInstance = super
                                        .getView(viewType.getViewName());
                            }
                            result = cachedInstance;
                        } else {
                            // Non-stateful views get instantiated every time
                            // they're navigated to
                            result = super.getView(viewType.getViewName());
                        }
                    }
                    return result;
                }
            };

            if (viewType.equals(ERROR_VIEW)) {
                errorViewProvider = viewProvider;
            }

            getNavigator().addProvider(viewProvider);
        }

        getNavigator().setErrorProvider(new ViewProvider() {
            @Override
            public String getViewName(final String viewAndParameters) {
                return ERROR_VIEW.getViewName();
            }

            @Override
            public View getView(final String viewName) {
                return errorViewProvider.getView(ERROR_VIEW.getViewName());
            }
        });
        getNavigator().addView("", LoginView.class);
    }

    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        initialPageSettings.addMetaTag("viewport",
                "width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no");
        initialPageSettings.addMetaTag("apple-mobile-web-app-capable", "yes");
        initialPageSettings.addMetaTag("apple-mobile-web-app-status-bar-style",
                "black-translucent");

        initialPageSettings.addLink("apple-touch-icon",
                "/VAADIN/themes/dashboard/img/app-icon.png");
    }
}

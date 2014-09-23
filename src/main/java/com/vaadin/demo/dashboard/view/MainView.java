package com.vaadin.demo.dashboard.view;

import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

import com.vaadin.demo.dashboard.component.DashboardMenu;
import com.vaadin.demo.dashboard.domain.User;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.PostViewChangeEvent;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

public class MainView extends HorizontalLayout {

    private GoogleAnalyticsTracker tracker;

    public MainView() {
        initGATracker();
        setSizeFull();

        addComponent(new DashboardMenu());

        ComponentContainer content = buildContent();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        initNavigator(content);
    }

    private void initNavigator(ComponentContainer container) {
        Navigator navigator = new Navigator(UI.getCurrent(), container);

        for (QuickTicketsView view : QuickTicketsView.values()) {
            try {
                navigator.addView(view.getViewName(), view.getViewClass()
                        .newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
        String trackerId = null;// "UA-658457-6";
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
}

package com.vaadin.demo.dashboard;

import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;

import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.CloseOpenWindowsEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.PostViewChangeEvent;
import com.vaadin.demo.dashboard.view.QuickTicketsView;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class DashboardNavigator extends Navigator {

    private GoogleAnalyticsTracker tracker;
    private final static QuickTicketsView ERROR_VIEW = QuickTicketsView.DASHBOARD;
    private ViewProvider errorViewProvider;

    public DashboardNavigator(ComponentContainer container) {
        super(UI.getCurrent(), container);

        initViewChangeListener();
        initViewProviders();

    }

    private void initViewChangeListener() {
        initGATracker();
        addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
                for (QuickTicketsView view : QuickTicketsView.values()) {
                    if (view.getViewName().equals(event.getViewName())) {
                        DashboardEventBus.post(new PostViewChangeEvent(view));
                        DashboardEventBus.post(new BrowserResizeEvent());
                        DashboardEventBus.post(new CloseOpenWindowsEvent());
                        break;
                    }
                }

                if (tracker != null) {
                    tracker.trackPageview("/dashboard" + event.getViewName());
                }
            }
        });
    }

    private void initGATracker() {
        // Provide a Google Analytics tracker id here
        String trackerId = null;// "UA-658457-6";
        if (trackerId != null) {
            tracker = new GoogleAnalyticsTracker(trackerId, "none");
            tracker.extend(UI.getCurrent());
        }
    }

    private void initViewProviders() {
        for (final QuickTicketsView view : QuickTicketsView.values()) {
            ViewProvider viewProvider = new ClassBasedViewProvider(
                    view.getViewName(), view.getViewClass()) {
                private View cachedInstance;
    
                @Override
                public View getView(String viewName) {
                    View result = null;
                    if (view.getViewName().equals(viewName)) {
                        if (view.isStateful()) {
                            // Stateful views get lazily instantiated
                            if (cachedInstance == null) {
                                cachedInstance = super.getView(view
                                        .getViewName());
                            }
                            result = cachedInstance;
                        } else {
                            // Non-stateful views get instantiated every time
                            // they're navigated to
                            result = super.getView(view.getViewName());
                        }
                    }
                    return result;
                }
            };
    
            if (view == ERROR_VIEW) {
                errorViewProvider = viewProvider;
            }
    
            addProvider(viewProvider);
        }
    
        setErrorProvider(new ViewProvider() {
            @Override
            public String getViewName(String viewAndParameters) {
                return ERROR_VIEW.getViewName();
            }
    
            @Override
            public View getView(String viewName) {
                return errorViewProvider.getView(ERROR_VIEW.getViewName());
            }
        });
    }
}

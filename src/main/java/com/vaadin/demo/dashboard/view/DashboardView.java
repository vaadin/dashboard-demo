package com.vaadin.demo.dashboard.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.component.DashboardEdit;
import com.vaadin.demo.dashboard.component.TopGrossingMoviesChart;
import com.vaadin.demo.dashboard.component.TopSixTheatersChart;
import com.vaadin.demo.dashboard.domain.DashboardNotification;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.DashboardEditEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.NotificationsCountUpdatedEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.ViewChangeRequestedEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class DashboardView extends VerticalLayout implements View {

    private Label titleLabel;
    private NotificationsButton notificationsButton;
    private Window notificationsWindow;

    public DashboardView() {
        addStyleName("dashboard-view");
        setSizeFull();
        DashboardEventBus.register(this);

        addComponent(buildHeader());

        Component content = buildContent();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                closeNotificationsPopup();
            }
        });
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setWidth(100.0f, Unit.PERCENTAGE);
        header.setSpacing(true);
        header.setMargin(true);

        titleLabel = new Label("My Dashboard");
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H1);
        header.addComponent(titleLabel);
        header.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);
        header.setExpandRatio(titleLabel, 1);

        notificationsWindow = buildNotificationsPopup();
        notificationsButton = buildNotificationsButton();
        header.addComponent(notificationsButton);
        header.setComponentAlignment(notificationsButton,
                Alignment.MIDDLE_RIGHT);

        Component edit = buildEdit();
        header.addComponent(edit);
        header.setComponentAlignment(edit, Alignment.MIDDLE_RIGHT);

        return header;
    }

    private Window buildNotificationsPopup() {
        Window notifications = new Window("Notifications");
        notifications.setWidth(300.0f, Unit.PIXELS);
        notifications.addStyleName("notifications");
        notifications.setClosable(false);
        notifications.setResizable(false);
        notifications.setDraggable(false);
        notifications.setCloseShortcut(KeyCode.ESCAPE, null);
        return notifications;
    }

    private NotificationsButton buildNotificationsButton() {
        NotificationsButton notificationsButton = new NotificationsButton();
        notificationsButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                openNotificationsPopup(event);
            }
        });
        return notificationsButton;
    }

    private Component buildEdit() {
        Button edit = new Button();
        edit.setIcon(FontAwesome.EDIT);
        edit.addStyleName("icon-edit");
        edit.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        edit.setDescription("Edit Dashboard");
        edit.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getUI().addWindow(new DashboardEdit(titleLabel.getValue()));
            }
        });
        return edit;
    }

    private Component buildContent() {
        CssLayout content = new CssLayout();
        content.addStyleName("dashboard-panels-layout");
        content.setWidth(100.0f, Unit.PERCENTAGE);

        Component topGrossingMovies = buildTopGrossingMovies();
        topGrossingMovies.addStyleName("margins");
        content.addComponent(topGrossingMovies);

        content.addComponent(buildNotes());

        Component top10TitlesByRevenue = buildTop10TitlesByRevenue();
        top10TitlesByRevenue.addStyleName("margins");
        content.addComponent(top10TitlesByRevenue);

        content.addComponent(buildPopularMovies());

        Panel dashboardPanel = new Panel(content);
        dashboardPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        dashboardPanel.setSizeFull();
        return dashboardPanel;
    }

    private Component buildTopGrossingMovies() {
        TopGrossingMoviesChart topGrossingMoviesChart = new TopGrossingMoviesChart();
        topGrossingMoviesChart.setSizeFull();
        return createContentWrapper(topGrossingMoviesChart);
    }

    private Component buildNotes() {
        TextArea notes = new TextArea("Notes");
        notes.setValue("Remember to:\n· Zoom in and out in the Sales view\n· Filter the transactions and drag a set of them to the Reports tab\n· Create a new report\n· Change the schedule of the movie theater");
        notes.setSizeFull();
        Component panel = createContentWrapper(notes);
        panel.addStyleName("notes");
        return panel;
    }

    private Component buildTop10TitlesByRevenue() {
        final Table table = new Table() {
            @Override
            protected String formatPropertyValue(Object rowId, Object colId,
                    Property<?> property) {
                String result = super.formatPropertyValue(rowId, colId,
                        property);
                if (colId.equals("revenue")) {
                    if (property != null && property.getValue() != null) {
                        Double r = (Double) property.getValue();
                        String ret = new DecimalFormat("#.##").format(r);
                        result = "$" + ret;
                    } else {
                        result = "";
                    }
                }
                return result;
            }
        };
        table.setCaption("Top 10 Titles by Revenue");

        table.setPageLength(0);
        table.addStyleName("plain");
        table.addStyleName(ValoTheme.TABLE_BORDERLESS);
        table.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        table.setSortEnabled(false);
        table.setColumnAlignment("revenue", Align.RIGHT);
        table.setRowHeaderMode(RowHeaderMode.INDEX);
        table.setSizeFull();

        List<MovieRevenue> movieRevenues = new ArrayList<MovieRevenue>(
                DashboardUI.getDataProvider().getTotalMovieRevenues());
        Collections.sort(movieRevenues, new Comparator<MovieRevenue>() {
            @Override
            public int compare(MovieRevenue o1, MovieRevenue o2) {
                return o2.getRevenue().compareTo(o1.getRevenue());
            }
        });

        table.setContainerDataSource(new BeanItemContainer<MovieRevenue>(
                MovieRevenue.class, movieRevenues.subList(0, 10)));

        table.setVisibleColumns("title", "revenue");
        table.setColumnHeaders("Title", "Revenue");

        table.setSortContainerPropertyId("revenue");
        table.setSortAscending(false);

        return createContentWrapper(table);
    }

    private Component buildPopularMovies() {
        return createContentWrapper(new TopSixTheatersChart());
    }

    private Component createContentWrapper(Component content) {
        Panel panel = new Panel(content);
        panel.setWidth(50.0f, Unit.PERCENTAGE);
        panel.setHeight(300.0f, Unit.PIXELS);
        panel.addStyleName("layout-panel");
        panel.setCaption(content.getCaption());
        return panel;
    }

    private void openNotificationsPopup(ClickEvent event) {
        VerticalLayout notificationsLayout = new VerticalLayout();
        notificationsLayout.setMargin(true);
        notificationsLayout.setSpacing(true);

        Collection<DashboardNotification> notifications = DashboardUI
                .getDataProvider().getNotifications();
        DashboardEventBus.post(new NotificationsCountUpdatedEvent());

        for (DashboardNotification notification : notifications) {
            VerticalLayout notificationLayout = new VerticalLayout();
            notificationLayout.addStyleName("notification-item");
            notificationLayout.setWidth(100.0f, Unit.PERCENTAGE);

            Label titleLabel = new Label(notification.getFirstName() + " "
                    + notification.getLastName() + " "
                    + notification.getAction());
            titleLabel.addStyleName("notification-title");

            Label timeLabel = new Label(notification.getPrettyTime());
            timeLabel.addStyleName("notification-time");

            Label contentLabel = new Label(notification.getContent());
            contentLabel.addStyleName("notification-content");

            notificationLayout.addComponents(titleLabel, timeLabel,
                    contentLabel);
            notificationsLayout.addComponent(notificationLayout);
        }
        notificationsWindow.setContent(notificationsLayout);

        notificationsWindow.setPositionX(event.getClientX()
                - event.getRelativeX() - 200);
        notificationsWindow.setPositionY(event.getClientY()
                - event.getRelativeY() + 50);
        UI.getCurrent().addWindow(notificationsWindow);
        notificationsWindow.focus();
    }

    private void closeNotificationsPopup() {
        notificationsWindow.close();
    }

    @Override
    public void enter(ViewChangeEvent event) {
        notificationsButton.updateNotificationsCount(null);
    }

    @Subscribe
    public void dashboardEdited(DashboardEditEvent event) {
        titleLabel.setValue(event.getName());
    }

    @Subscribe
    public void viewChanging(ViewChangeRequestedEvent event) {
        closeNotificationsPopup();
    }

    public static class NotificationsButton extends Button {
        private static final String STYLE_UNREAD = "unread";

        public NotificationsButton() {
            setIcon(FontAwesome.BELL);
            addStyleName("notifications");
            addStyleName(ValoTheme.BUTTON_ICON_ONLY);
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

        @Subscribe
        public void updateNotificationsCount(
                NotificationsCountUpdatedEvent event) {
            setUnreadCount(DashboardUI.getDataProvider()
                    .getUnreadNotificationsCount());
        }

        public void setUnreadCount(int count) {
            setCaption(String.valueOf(count));

            String description = "Notifications";
            if (count > 0) {
                addStyleName(STYLE_UNREAD);
                description += " (" + count + " unread)";
            } else {
                removeStyleName(STYLE_UNREAD);
            }
            setDescription(description);
        }
    }

}

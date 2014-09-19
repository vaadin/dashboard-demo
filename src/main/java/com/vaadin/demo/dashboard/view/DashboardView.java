package com.vaadin.demo.dashboard.view;

import java.text.DecimalFormat;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.demo.dashboard.component.DashboardEdit;
import com.vaadin.demo.dashboard.component.TopGrossingMoviesChart;
import com.vaadin.demo.dashboard.component.TopSixTheatersChart;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.Generator;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.DashboardEditEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.NotificationsCountUpdatedEvent;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.NotificationsOpenEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class DashboardView extends VerticalLayout implements View {

    private Label titleLabel;

    public DashboardView() {
        addStyleName("dashboard-view");
        setSizeFull();

        addComponent(buildHeader());
        Component content = buildContent();
        addComponent(content);
        setExpandRatio(content, 1.0f);
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

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth(100.0f, Unit.PERCENTAGE);
        header.setSpacing(true);
        header.setMargin(true);

        titleLabel = new Label("My Dashboard");
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H2);
        header.addComponent(titleLabel);
        header.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);
        header.setExpandRatio(titleLabel, 1);

        Component notifications = buildNotifications();
        header.addComponent(notifications);
        header.setComponentAlignment(notifications, Alignment.MIDDLE_RIGHT);

        Component edit = buildEdit();
        header.addComponent(edit);
        header.setComponentAlignment(edit, Alignment.MIDDLE_RIGHT);

        return header;
    }

    private Component buildNotifications() {
        return new NotificationsButton();
    }

    private Component buildEdit() {
        Button edit = new Button();
        edit.setIcon(FontAwesome.EDIT);
        edit.addStyleName("icon-edit");
        edit.addStyleName("icon-only");
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
        content.setWidth(100.0f, Unit.PERCENTAGE);

        content.addComponent(buildTopGrossingMovies());
        content.addComponent(buildNotes());
        content.addComponent(buildTop10TitlesByRevenue());
        content.addComponent(buildPopularMovies());

        Panel dashboardPanel = new Panel(content);
        dashboardPanel.addStyleName("borderless");
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
                if (colId.equals("Revenue")) {
                    if (property != null && property.getValue() != null) {
                        Double r = (Double) property.getValue();
                        String ret = new DecimalFormat("#.##").format(r);
                        return "$" + ret;
                    } else {
                        return "";
                    }
                }
                return super.formatPropertyValue(rowId, colId, property);
            }
        };
        table.setCaption("Top 10 Titles by Revenue");
        table.setContainerDataSource(DataProvider.getCurrent()
                .getRevenueByTitle());

        table.setPageLength(0);
        table.addStyleName("plain");
        table.addStyleName("borderless");
        table.setSortEnabled(false);
        table.setColumnAlignment("Revenue", Align.RIGHT);
        table.setRowHeaderMode(RowHeaderMode.INDEX);
        table.setSizeFull();

        return createContentWrapper(table);
    }

    private Component buildPopularMovies() {
        return createContentWrapper(new TopSixTheatersChart());
    }

    private Component createContentWrapper(Component content) {
        Panel panel = new Panel(content);
        panel.setWidthUndefined();
        panel.setWidth(46.0f, Unit.PERCENTAGE);
        panel.setHeight(300.0f, Unit.PIXELS);
        panel.addStyleName("layout-panel");
        panel.setCaption(content.getCaption());

        // Button configure = new Button();
        // configure.addStyleName("configure");
        // configure.addStyleName("icon-cog");
        // configure.addStyleName("icon-only");
        // configure.addStyleName("borderless");
        // configure.setDescription("Configure");
        // configure.addStyleName("small");
        // configure.addClickListener(new ClickListener() {
        // @Override
        // public void buttonClick(ClickEvent event) {
        // Notification.show("Not implemented in this demo");
        // }
        // });
        // panel.addComponent(configure);
        return panel;
    }

    private void buildNotifications(ClickEvent event) {
        Window notifications = new Window("Notifications");
        VerticalLayout l = new VerticalLayout();
        l.setMargin(true);
        l.setSpacing(true);
        notifications.setContent(l);
        notifications.setWidth("300px");
        notifications.addStyleName("notifications");
        notifications.setClosable(false);
        notifications.setResizable(false);
        notifications.setDraggable(false);
        notifications.setPositionX(event.getClientX() - event.getRelativeX());
        notifications.setPositionY(event.getClientY() - event.getRelativeY());
        notifications.setCloseShortcut(KeyCode.ESCAPE, null);

        Label label = new Label(
                "<hr><b>"
                        + Generator.randomFirstName()
                        + " "
                        + Generator.randomLastName()
                        + " created a new report</b><br><span>25 minutes ago</span><br>"
                        + Generator.randomText(18), ContentMode.HTML);
        l.addComponent(label);

        label = new Label("<hr><b>" + Generator.randomFirstName() + " "
                + Generator.randomLastName()
                + " changed the schedule</b><br><span>2 days ago</span><br>"
                + Generator.randomText(10), ContentMode.HTML);
        l.addComponent(label);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        DashboardEventBus.post(new NotificationsCountUpdatedEvent(2));
    }

    @Subscribe
    public void dashboardEdited(DashboardEditEvent event) {
        titleLabel.setValue(event.getName());
    }

    @Subscribe
    public void notificationsOpen(NotificationsOpenEvent event) {
        // TODO: Handle closing open notifications...should we use popupview

        // buildNotifications(event);
        // getUI().addWindow(notifications);
        // notifications.focus();
        // ((CssLayout) getUI().getContent())
        // .addLayoutClickListener(new LayoutClickListener() {
        // @Override
        // public void layoutClick(LayoutClickEvent event) {
        // notifications.close();
        // ((CssLayout) getUI().getContent())
        // .removeLayoutClickListener(this);
        // }
        // });
    }

    public static class NotificationsButton extends Button {
        private static final String STYLE_UNREAD = "unread";

        public NotificationsButton() {
            setIcon(FontAwesome.BELL);
            addStyleName("notifications");
            addStyleName(ValoTheme.BUTTON_ICON_ONLY);

            addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    DashboardEventBus.post(new NotificationsOpenEvent());
                    DashboardEventBus
                            .post(new NotificationsCountUpdatedEvent(0));
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

        @Subscribe
        public void notificationsOpen(NotificationsCountUpdatedEvent event) {
            setUnreadCount(event.getCount());
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

package com.vaadin.demo.dashboard.view.transactions;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.demo.dashboard.event.DashboardEvent.BrowserResizeEvent;
import com.vaadin.demo.dashboard.event.DashboardEvent.TransactionReportEvent;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.view.DashboardViewType;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.data.ListDataSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "serial", "unchecked" })
public final class TransactionsView extends VerticalLayout implements View {

    private final Grid<Transaction> grid;
    private Button createReport;
    private static final DateFormat DATEFORMAT = new SimpleDateFormat(
            "MM/dd/yyyy hh:mm:ss a");
    private static final DecimalFormat DECIMALFORMAT = new DecimalFormat(
            "#.##");
    private static final String[] DEFAULT_COLLAPSIBLE = { "country", "city",
            "theater", "room", "title", "seats" };
    private static final Set<Column<Transaction, ?>> collapsibleColumns = new LinkedHashSet();

    public TransactionsView() {
        setSizeFull();
        addStyleName("transactions");
        DashboardEventBus.register(this);

        addComponent(buildToolbar());

        grid = buildGrid();
        addComponent(grid);
        setExpandRatio(grid, 1);
    }

    @Override
    public void detach() {
        super.detach();
        // A new instance of TransactionsView is created every time it's
        // navigated to so we'll need to clean up references to it on detach.
        DashboardEventBus.unregister(this);
    }

    private Component buildToolbar() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setSpacing(true);
        Responsive.makeResponsive(header);

        Label title = new Label("Latest Transactions");
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_H1);
        title.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        header.addComponent(title);

        createReport = buildCreateReport();
        HorizontalLayout tools = new HorizontalLayout(buildFilter(),
                createReport);
        tools.setSpacing(true);
        tools.addStyleName("toolbar");
        header.addComponent(tools);

        return header;
    }

    private Button buildCreateReport() {
        final Button createReport = new Button("Create Report");
        createReport.setDescription(
                "Create a new report from the selected transactions");
        createReport.addClickListener(event -> createNewReportFromSelection());
        createReport.setEnabled(false);
        return createReport;
    }

    private Component buildFilter() {
        final TextField filter = new TextField();

        // TODO improve filtering
        filter.addValueChangeListener(event -> {

            Collection<Transaction> transactions = DashboardUI.getDataProvider()
                    .getRecentTransactions(200).stream().filter(transaction -> {
                        String filterValue = filter.getValue().trim()
                                .toLowerCase();
                        return transaction.getCountry().toLowerCase()
                                .contains(filterValue)
                                || transaction.getTitle().toLowerCase()
                                        .contains(filterValue)
                                || transaction.getCity().toLowerCase()
                                        .contains(filterValue);
                    }).collect(Collectors.toList());

            ListDataSource<Transaction> dataSource = new ListDataSource<>(
                    transactions);
            grid.setDataSource(dataSource.sortingBy(
                    Comparator.comparing(Transaction::getTime).reversed()));
        });

        filter.setPlaceholder("Filter");
        filter.setIcon(FontAwesome.SEARCH);
        filter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        filter.addShortcutListener(
                new ShortcutListener("Clear", KeyCode.ESCAPE, null) {
                    @Override
                    public void handleAction(final Object sender,
                            final Object target) {
                        filter.setValue("");
                    }
                });
        return filter;
    }

    private Grid<Transaction> buildGrid() {
        final Grid<Transaction> grid = new Grid<>();
        grid.setSizeFull();

        grid.addColumn("Time",
                transaction -> DATEFORMAT.format(transaction.getTime()))
                .setHidable(true);
        grid.addColumn("Country", Transaction::getCountry);
        grid.addColumn("City", Transaction::getCity);
        grid.addColumn("Theater", Transaction::getTheater);
        grid.addColumn("Room", Transaction::getRoom);
        grid.addColumn("Title", Transaction::getTitle);
        grid.addColumn("Seats", Transaction::getSeats, new NumberRenderer());
        grid.addColumn("Price",
                transaction -> "$"
                        + DECIMALFORMAT.format(transaction.getPrice()))
                .setHidable(true);

        grid.setColumnReorderingAllowed(true);

        ListDataSource<Transaction> dataSource = new ListDataSource<>(
                DashboardUI.getDataProvider().getRecentTransactions(200));
        grid.setDataSource(dataSource.sortingBy(
                Comparator.comparing(Transaction::getTime).reversed()));

        // TODO either add these to grid or do it with style generators here
        // grid.setColumnAlignment("seats", Align.RIGHT);
        // grid.setColumnAlignment("price", Align.RIGHT);

        // TODO add when footers implemented in v8
        // grid.setFooterVisible(true);
        // grid.setColumnFooter("time", "Total");
        // grid.setColumnFooter("price", "$" + DECIMALFORMAT
        // .format(DashboardUI.getDataProvider().getTotalSum()));

        // TODO add this functionality to grid?
        // grid.addActionHandler(new TransactionsActionHandler());

        grid.addSelectionListener(event -> createReport
                .setEnabled(!grid.getSelectedItems().isEmpty()));
        return grid;
    }

    private boolean defaultColumnsVisible() {
        boolean result = true;
        for (Column<Transaction, ?> column : collapsibleColumns) {
            if (column.isHidden() == Page.getCurrent()
                    .getBrowserWindowWidth() < 800) {
                result = false;
            }
        }
        return result;
    }

    @Subscribe
    public void browserResized(final BrowserResizeEvent event) {
        // Some columns are collapsed when browser window width gets small
        // enough to make the table fit better.

        if (defaultColumnsVisible()) {
            for (Column<Transaction, ?> column : collapsibleColumns) {
                if (Page.getCurrent().getBrowserWindowWidth() < 800) {
                    break;
                } else {
                    column.setHidden(true);
                }
            }
        }
    }

    void createNewReportFromSelection() {
        grid.getSelectedItem().ifPresent(transaction -> {
            UI.getCurrent().getNavigator()
                    .navigateTo(DashboardViewType.REPORTS.getViewName());
            DashboardEventBus.post(new TransactionReportEvent(
                    Collections.singletonList(transaction)));
        });
    }

    @Override
    public void enter(final ViewChangeEvent event) {
    }
}

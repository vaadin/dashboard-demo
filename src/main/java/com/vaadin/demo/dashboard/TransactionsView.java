/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package com.vaadin.demo.dashboard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.TransactionsContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TransactionsView extends VerticalLayout implements View {

    private static final long serialVersionUID = 1L;

    Table t;

    Object editableId = null;

    TransactionsContainer data;

    @Override
    public void enter(ViewChangeEvent event) {
        data = ((DashboardUI) getUI()).dataProvider.getTransactions();

        setSizeFull();
        addStyleName("transactions");

        t = new Table() {
            @Override
            protected String formatPropertyValue(Object rowId, Object colId,
                    Property<?> property) {
                if (colId.equals("Time")) {
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.applyPattern("MM/dd/yyyy hh:mm:ss a");
                    return df
                            .format(((Calendar) property.getValue()).getTime());
                } else if (colId.equals("Price")) {
                    if (property != null && property.getValue() != null) {
                        String ret = new DecimalFormat("#.##").format(property
                                .getValue());
                        return "$" + ret;
                    } else {
                        return "";
                    }
                }
                return super.formatPropertyValue(rowId, colId, property);
            }
        };
        t.setSizeFull();
        t.addStyleName("borderless");
        t.setSelectable(true);
        t.setColumnCollapsingAllowed(true);
        t.setColumnReorderingAllowed(true);
        data.removeAllContainerFilters();
        t.setContainerDataSource(data);
        sortTable();

        t.setColumnAlignment("Seats", Align.RIGHT);
        t.setColumnAlignment("Price", Align.RIGHT);

        t.setVisibleColumns(new Object[] { "Time", "Country", "City",
                "Theater", "Room", "Title", "Seats", "Price" });

        t.setFooterVisible(true);
        t.setColumnFooter("Time", "Total");
        updatePriceFooter();

        // Allow dragging items to the reports menu
        t.setDragMode(TableDragMode.MULTIROW);
        t.setMultiSelect(true);

        // EDIT MODE disabled for now
        // t.setTableFieldFactory(new DefaultFieldFactory() {
        // @Override
        // public Field createField(Container container, Object itemId,
        // Object propertyId, Component uiContext) {
        // boolean editable = itemId.equals(editableId);
        // Field f = new TextField();
        // f.setCaption(null);
        // f.setWidth("100%");
        // f.setReadOnly(!editable);
        // return f;
        // }
        // });

        // Double click to edit
        // t.addItemClickListener(new ItemClickListener() {
        // @Override
        // public void itemClick(ItemClickEvent event) {
        // if (event.getButton() == MouseButton.LEFT
        // && event.isDoubleClick()) {
        // editableId = event.getItemId();
        // t.addStyleName("editable");
        // t.setEditable(true);
        // } else if (event.getButton() == MouseButton.LEFT) {
        // editableId = null;
        // t.setEditable(false);
        // t.removeStyleName("editable");
        // }
        // }
        // });

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth("100%");
        toolbar.setSpacing(true);
        toolbar.setMargin(true);
        toolbar.addStyleName("toolbar");
        addComponent(toolbar);

        Label title = new Label("All Transactions");
        title.addStyleName("h1");
        title.setSizeUndefined();
        toolbar.addComponent(title);
        toolbar.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

        final TextField filter = new TextField();
        filter.addTextChangeListener(new TextChangeListener() {
            @Override
            public void textChange(final TextChangeEvent event) {
                data.removeAllContainerFilters();
                data.addContainerFilter(new Filter() {
                    @Override
                    public boolean passesFilter(Object itemId, Item item)
                            throws UnsupportedOperationException {

                        if (event.getText() == null
                                || event.getText().equals("")) {
                            return true;
                        }

                        return filterByProperty("Country", item,
                                event.getText())
                                || filterByProperty("City", item,
                                        event.getText())
                                || filterByProperty("Title", item,
                                        event.getText());

                    }

                    @Override
                    public boolean appliesToProperty(Object propertyId) {
                        if (propertyId.equals("Country")
                                || propertyId.equals("City")
                                || propertyId.equals("Title"))
                            return true;
                        return false;
                    }
                });
            }
        });
        // final ComboBox filter = new ComboBox();
        // filter.setNewItemsAllowed(true);
        // filter.setNewItemHandler(new NewItemHandler() {
        // @Override
        // public void addNewItem(String newItemCaption) {
        // filter.addItem(newItemCaption);
        // }
        // });
        // filter.addItem("test");
        // filter.addItem("finland");
        // filter.addItem("paranorman");

        // filter.addStyleName("small");
        filter.setInputPrompt("Filter");
        filter.addShortcutListener(new ShortcutListener("Clear",
                KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                filter.setValue("");
                data.removeAllContainerFilters();
            }
        });
        toolbar.addComponent(filter);
        toolbar.setExpandRatio(filter, 1);
        toolbar.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);

        // Button refresh = new Button("Refresh");
        // refresh.addClickListener(new ClickListener() {
        // @Override
        // public void buttonClick(ClickEvent event) {
        // updatePriceFooter();
        // try {
        // Thread.sleep(3000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // });
        // refresh.addStyleName("small");
        // toolbar.addComponent(refresh);

        final Button newReport = new Button("Create Report From Selection");
        newReport.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                createNewReportFromSelection();
            }
        });
        newReport.setEnabled(false);
        newReport.addStyleName("small");
        toolbar.addComponent(newReport);
        toolbar.setComponentAlignment(newReport, Alignment.MIDDLE_LEFT);

        addComponent(t);
        setExpandRatio(t, 1);

        t.addActionHandler(new Handler() {

            private Action report = new Action("Create Report");

            private Action discard = new Action("Discard");

            private Action details = new Action("Movie details");

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if (action == report) {
                    createNewReportFromSelection();
                } else if (action == discard) {
                    Notification.show("Not implemented in this demo");
                } else if (action == details) {
                    Item item = ((Table) sender).getItem(target);
                    if (item != null) {
                        Window w = new MovieDetailsWindow(DataProvider
                                .getMovieForTitle(item.getItemProperty("Title")
                                        .getValue().toString()), null);
                        UI.getCurrent().addWindow(w);
                        w.focus();
                    }
                }
            }

            @Override
            public Action[] getActions(Object target, Object sender) {
                return new Action[] { details, report, discard };
            }
        });

        t.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (t.getValue() instanceof Set) {
                    Set<Object> val = (Set<Object>) t.getValue();
                    newReport.setEnabled(val.size() > 0);
                } else {
                }
            }
        });
        t.setImmediate(true);

        // group rows by month
        // t.setRowGenerator(new RowGenerator() {
        // @Override
        // public GeneratedRow generateRow(Table table, Object itemId) {
        // if (itemId.toString().startsWith("month")) {
        // Date date = (Date) table.getItem(itemId)
        // .getItemProperty("timestamp").getValue();
        // SimpleDateFormat df = new SimpleDateFormat();
        // df.applyPattern("MMMM yyyy");
        // GeneratedRow row = new GeneratedRow(df.format(date));
        // row.setSpanColumns(true);
        // return row;
        // }
        // return null;
        // }
        // });

        t.addGeneratedColumn("Title", new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                final String title = source.getItem(itemId)
                        .getItemProperty(columnId).getValue().toString();
                Button titleLink = new Button(title);
                titleLink.addStyleName("link");
                titleLink.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        Window w = new MovieDetailsWindow(DataProvider
                                .getMovieForTitle(title), null);
                        UI.getCurrent().addWindow(w);
                    }
                });
                return title;
            }
        });
    }

    private void sortTable() {
        t.sort(new Object[] { "timestamp" }, new boolean[] { false });
    }

    private boolean filterByProperty(String prop, Item item, String text) {
        if (item == null || item.getItemProperty(prop) == null
                || item.getItemProperty(prop).getValue() == null)
            return false;
        String val = item.getItemProperty(prop).getValue().toString().trim()
                .toLowerCase();
        if (val.startsWith(text.toLowerCase().trim()))
            return true;
        // String[] parts = text.split(" ");
        // for (String part : parts) {
        // if (val.contains(part.toLowerCase()))
        // return true;
        //
        // }
        return false;
    }

    void createNewReportFromSelection() {
        ((DashboardUI) getUI()).openReports(t);
    }

    void updatePriceFooter() {
        String ret = new DecimalFormat("#.##").format(DataProvider
                .getTotalSum());
        t.setColumnFooter("Price", "$" + ret);

    }

}

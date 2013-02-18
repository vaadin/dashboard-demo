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

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.demo.dashboard.data.Generator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ReportsView extends HorizontalLayout implements View {

    private TabSheet editors;

    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("reports");

        addComponent(buildDraftsView());
    }

    private Component buildDraftsView() {
        editors = new TabSheet();
        editors.setSizeFull();
        editors.addStyleName("borderless");
        editors.addStyleName("editors");

        editors.setCloseHandler(new CloseHandler() {
            @Override
            public void onTabClose(TabSheet tabsheet, final Component tabContent) {
                VerticalLayout l = new VerticalLayout();
                l.setWidth("400px");
                l.setMargin(true);
                l.setSpacing(true);
                final Window alert = new Window("Unsaved Changes", l);
                alert.setModal(true);
                alert.setResizable(false);
                alert.setDraggable(false);
                alert.addStyleName("dialog");
                alert.setClosable(false);

                Label message = new Label(
                        "You have not saved this report. Do you want to save or discard any changes you've made to this report?");
                l.addComponent(message);

                HorizontalLayout buttons = new HorizontalLayout();
                buttons.setWidth("100%");
                buttons.setSpacing(true);
                l.addComponent(buttons);

                Button discard = new Button("Don't Save");
                discard.addStyleName("small");
                discard.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        editors.removeComponent(tabContent);
                        draftCount--;
                        ((DashboardUI) UI.getCurrent())
                                .updateReportsButtonBadge(draftCount + "");
                        alert.close();

                    }
                });
                buttons.addComponent(discard);
                buttons.setExpandRatio(discard, 1);

                Button cancel = new Button("Cancel");
                cancel.addStyleName("small");
                cancel.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        alert.close();
                    }
                });
                buttons.addComponent(cancel);

                Button ok = new Button("Save");
                ok.addStyleName("default");
                ok.addStyleName("small");
                ok.addStyleName("wide");
                ok.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        editors.removeComponent(tabContent);
                        draftCount--;
                        ((DashboardUI) UI.getCurrent())
                                .updateReportsButtonBadge(draftCount + "");
                        alert.close();
                        Notification
                                .show("The report was saved as a draft",
                                        "Actually, the report was just closed and deleted forever. As this is only a demo, it doesn't persist any data.",
                                        Type.TRAY_NOTIFICATION);

                    }
                });
                buttons.addComponent(ok);
                ok.focus();

                alert.addShortcutListener(new ShortcutListener("Cancel",
                        KeyCode.ESCAPE, null) {
                    @Override
                    public void handleAction(Object sender, Object target) {
                        alert.close();
                    }
                });

                getUI().addWindow(alert);
            }
        });

        final VerticalLayout center = new VerticalLayout();
        center.setSizeFull();
        center.setCaption("All Drafts");
        editors.addComponent(center);

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setSpacing(true);
        titleAndDrafts.addStyleName("drafts");
        center.addComponent(titleAndDrafts);
        center.setComponentAlignment(titleAndDrafts, Alignment.MIDDLE_CENTER);

        Label draftsTitle = new Label("Drafts");
        draftsTitle.addStyleName("h1");
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent(draftsTitle);
        titleAndDrafts.setComponentAlignment(draftsTitle, Alignment.TOP_CENTER);

        HorizontalLayout drafts = new HorizontalLayout();
        drafts.setSpacing(true);
        titleAndDrafts.addComponent(drafts);

        CssLayout draftThumb = new CssLayout();
        draftThumb.addStyleName("draft-thumb");
        Image draft = new Image(null, new ThemeResource(
                "img/draft-report-thumb.png"));
        draftThumb.addComponent(draft);
        Label draftTitle = new Label(
                "Monthly revenue<br><span>Last modified 1 day ago</span>",
                ContentMode.HTML);
        draftTitle.setSizeUndefined();
        draftThumb.addComponent(draftTitle);
        drafts.addComponent(draftThumb);
        // TODO layout bug, we need to set the alignment also for the first
        // child
        drafts.setComponentAlignment(draftThumb, Alignment.MIDDLE_CENTER);

        final Button delete = new Button("×");
        delete.setPrimaryStyleName("delete-button");
        draftThumb.addComponent(delete);
        delete.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Notification.show("Not implemented in this demo");
            }
        });

        draftThumb.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getButton() == MouseButton.LEFT
                        && event.getChildComponent() != delete) {
                    editors.addTab(createEditorInstance(1, null, null))
                            .setClosable(true);
                    editors.setSelectedTab(editors.getComponentCount() - 1);
                }
            }
        });
        draft.setDescription("Click to edit");
        delete.setDescription("Delete draft");

        VerticalLayout createBox = new VerticalLayout();
        createBox.setWidth(null);
        createBox.addStyleName("create");
        Button create = new Button("Create New");
        create.addStyleName("default");
        createBox.addComponent(create);
        createBox.setComponentAlignment(create, Alignment.MIDDLE_CENTER);
        drafts.addComponent(createBox);
        create.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                autoCreate(0, null, null);
            }
        });

        return editors;
    }

    private CssLayout paletteLayout;

    private int draftCount = 1;

    boolean helpShown = false;

    private HorizontalLayout createEditorInstance(int which,
            Transferable items, Table table) {

        if (!helpShown) {
            helpShown = true;
            HelpManager helpManager = ((DashboardUI) getUI()).getHelpManager();
            helpManager.addOverlay("Palette",
                    "Drag the items from the palette onto the canvas",
                    "palette");
            helpManager.addOverlay("Re-order",
                    "Drag the items on the canvas to re-order them", "reorder")
                    .center();
        }

        HorizontalLayout editor = new HorizontalLayout();
        editor.setSizeFull();

        if (which == 1) {
            editor.setCaption("Monthly revenue");
        } else if (which == 2) {
            editor.setCaption("Generated report from selected transactions");
        } else {
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("M/dd/yyyy");
            editor.setCaption("Unnamed Report – " + (df.format(new Date()))
                    + " (" + draftCount + ")");
        }

        ((DashboardUI) UI.getCurrent()).updateReportsButtonBadge(draftCount
                + "");

        draftCount++;

        paletteLayout = new CssLayout();
        paletteLayout.setSizeUndefined();
        paletteLayout.addStyleName("palette");

        editor.addComponent(paletteLayout);
        editor.setExpandRatio(paletteLayout, 1);

        final SortableLayout canvas = new SortableLayout(editor.getCaption(),
                which, items, table);
        canvas.setWidth("100%");
        canvas.addStyleName("canvas");
        editor.addComponent(canvas);
        editor.setExpandRatio(canvas, 7);

        // editor.addStyleName("no-vertical-drag-hints");
        editor.addStyleName("editor");
        editor.addStyleName("no-horizontal-drag-hints");

        Label help = new Label("Drag items to the canvas");
        help.addStyleName("help");
        paletteLayout.addComponent(help);

        CssLayout wrap = new CssLayout();
        Image l = new Image(null, new ThemeResource("img/palette-text.png"));
        wrap.addComponent(l);
        Label caption = new Label("Text Block");
        caption.setSizeUndefined();
        wrap.addComponent(caption);
        DragAndDropWrapper rte = new DragAndDropWrapper(wrap);
        rte.setSizeUndefined();
        rte.setCaption("text");
        paletteLayout.addComponent(rte);
        rte.setDragStartMode(DragStartMode.WRAPPER);

        wrap = new CssLayout();
        l = new Image(null, new ThemeResource("img/palette-grid.png"));
        wrap.addComponent(l);
        caption = new Label("Top 10 Movies");
        caption.setSizeUndefined();
        wrap.addComponent(caption);
        DragAndDropWrapper grid = new DragAndDropWrapper(wrap);
        grid.setCaption("grid");
        grid.setSizeUndefined();
        paletteLayout.addComponent(grid);
        grid.setDragStartMode(DragStartMode.WRAPPER);

        wrap = new CssLayout();
        l = new Image(null, new ThemeResource("img/palette-chart.png"));
        wrap.addComponent(l);
        caption = new Label("Top 4 Revenue");
        caption.setSizeUndefined();
        wrap.addComponent(caption);
        DragAndDropWrapper chart = new DragAndDropWrapper(wrap);
        chart.setCaption("chart");
        chart.setSizeUndefined();
        paletteLayout.addComponent(chart);
        chart.setDragStartMode(DragStartMode.WRAPPER);

        return editor;
    }

    private CssLayout createTransactionLabel(final Item item) {
        CssLayout root = new CssLayout();
        root.addStyleName("transaction");

        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("MM/dd/yyyy hh:mm:ss a");

        Label l = new Label(df.format(((Date) item.getItemProperty("timestamp")
                .getValue()))
                + "<br>"
                + item.getItemProperty("City").getValue().toString()
                + ", "
                + item.getItemProperty("Country").getValue().toString());
        l.setSizeUndefined();
        l.setContentMode(ContentMode.HTML);
        l.addStyleName("time");
        root.addComponent(l);

        l = new Label(item.getItemProperty("Title").getValue().toString());
        l.setSizeUndefined();
        l.addStyleName("movie-title");
        root.addComponent(l);

        l = new Label("Seats: "
                + item.getItemProperty("Seats").getValue().toString()
                + "<br>"
                + "Revenue: $"
                + new DecimalFormat("#.##").format(item
                        .getItemProperty("Price").getValue()), ContentMode.HTML);
        l.setSizeUndefined();
        l.addStyleName("seats");
        root.addComponent(l);

        return root;
    }

    private class SortableLayout extends CustomComponent {
        private VerticalLayout layout;
        private final DropHandler dropHandler;

        public SortableLayout(String caption, int which, Transferable items,
                Table table) {
            setCompositionRoot(layout = new VerticalLayout());
            layout.addStyleName("canvas-layout");

            TextField title = new TextField();
            title.addStyleName("title");
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("M/dd/yyyy");
            title.setValue(caption);

            title.addTextChangeListener(new TextChangeListener() {
                @Override
                public void textChange(TextChangeEvent event) {
                    Tab tab = editors.getTab(SortableLayout.this.getParent());
                    String t = event.getText();
                    if (t == null || t.equals("")) {
                        t = " ";
                    }
                    tab.setCaption(t);
                }
            });
            layout.addComponent(title);

            dropHandler = new ReorderLayoutDropHandler(layout);

            Label l = new Label("Drag items here");
            l.setSizeUndefined();

            if (which == 1) {
                addComponent(createComponentFromPaletteItem("chart", null));
                addComponent(createComponentFromPaletteItem("grid", null));
            } else if (which == 2) {
                CssLayout labels = new CssLayout();
                labels.addComponent(new Label(
                        "<strong>Selected transactions</strong>",
                        ContentMode.HTML));
                if (items != null) {
                    TableTransferable tt = (TableTransferable) items;
                    Table s = tt.getSourceComponent();
                    Set<Object> val = (Set<Object>) s.getValue();
                    if (val.contains(tt.getData("itemId"))) {
                        for (Object id : val) {
                            Item item = s.getItem(id);
                            if (item != null)
                                labels.addComponent(createTransactionLabel(item));
                        }
                    } else {
                        Item item = s.getItem(tt.getData("itemId"));
                        if (item != null)
                            labels.addComponent(createTransactionLabel(item));
                    }
                } else if (table != null) {
                    Set<Object> val = (Set<Object>) table.getValue();
                    for (Object id : val) {
                        Item item = table.getItem(id);
                        if (item != null)
                            labels.addComponent(createTransactionLabel(item));
                    }
                }

                addComponent(createComponentFromPaletteItem("text", ""));
                addComponent(labels);
            } else {

                final DragAndDropWrapper placeholder = new DragAndDropWrapper(l);
                placeholder.addStyleName("placeholder");
                placeholder.setDropHandler(new DropHandler() {

                    @Override
                    public AcceptCriterion getAcceptCriterion() {
                        // return new SourceIsTarget.get());
                        return AcceptAll.get();
                    }

                    @Override
                    public void drop(DragAndDropEvent event) {
                        Transferable transferable = event.getTransferable();
                        Component sourceComponent = transferable
                                .getSourceComponent();

                        if (sourceComponent != layout.getParent()) {
                            AbstractComponent c = createComponentFromPaletteItem(
                                    sourceComponent.getCaption(), null);
                            if (c != null) {
                                addComponent(c);
                                layout.removeComponent(placeholder);
                            }
                        }
                    }
                });
                layout.addComponent(placeholder);
            }
        }

        public void addComponent(Component component) {
            layout.addComponent(getWrappedComponent(component, dropHandler));
        }

    }

    private static WrappedComponent getWrappedComponent(Component content,
            DropHandler dropHandler) {
        if (content.getCaption() != null) {
            CssLayout wrap = new CssLayout();
            wrap.setWidth("100%");
            wrap.addComponent(content);
            return new WrappedComponent(wrap, dropHandler);
        } else {
            return new WrappedComponent(content, dropHandler);
        }
    }

    private static class WrappedComponent extends DragAndDropWrapper {

        private final DropHandler dropHandler;

        public WrappedComponent(Component content, DropHandler dropHandler) {
            super(content);
            this.dropHandler = dropHandler;
            setDragStartMode(DragStartMode.WRAPPER);
        }

        @Override
        public DropHandler getDropHandler() {
            return dropHandler;
        }

    }

    Color[] colors = new Color[] { new Color(52, 154, 255),
            new Color(242, 81, 57), new Color(255, 201, 35),
            new Color(83, 220, 164) };
    int colorIndex = -1;

    private AbstractComponent createComponentFromPaletteItem(String caption,
            String data) {

        if (caption != null) {
            if (caption.equals("text")) {
                final CssLayout l = new CssLayout();
                l.addStyleName("text-editor");
                l.addStyleName("edit");
                l.setWidth("100%");
                final RichTextArea rta = new RichTextArea();
                rta.setWidth("100%");
                if (data == null)
                    rta.setValue(Generator.randomText(30));
                else
                    rta.setValue(data);
                l.addComponent(rta);
                final Label text = new Label();
                final Button save = new Button("Save");
                save.addStyleName("default");
                save.addStyleName("small");
                save.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (save.getCaption().equals("Save")) {
                            l.removeStyleName("edit");
                            l.removeComponent(rta);
                            l.addComponent(text, 0);
                            text.setValue(rta.getValue());
                            save.setCaption("");
                            save.removeStyleName("default");
                            save.addStyleName("icon-edit");
                            save.setDescription("Edit");
                        } else {
                            l.addStyleName("edit");
                            l.removeComponent(text);
                            l.addComponent(rta, 0);
                            rta.focus();
                            rta.selectAll();
                            save.setCaption("Save");
                            save.addStyleName("default");
                            save.removeStyleName("icon-edit");
                            save.setDescription(null);
                        }
                    }
                });
                rta.focus();
                rta.selectAll();
                l.addComponent(save);
                return l;
            } else if (caption.equals("grid")) {
                Table t = new Table() {
                    @Override
                    protected String formatPropertyValue(Object rowId,
                            Object colId, Property<?> property) {
                        if (colId.equals("Revenue")) {
                            if (property != null && property.getValue() != null) {
                                Double r = (Double) property.getValue();
                                String ret = new DecimalFormat("#.##")
                                        .format(r);
                                return "$" + ret;
                            } else {
                                return "";
                            }
                        }
                        return super
                                .formatPropertyValue(rowId, colId, property);
                    }
                };
                t.setCaption("Top 10 Titles by Revenue");
                t.setContainerDataSource(((DashboardUI) getUI()).dataProvider
                        .getRevenueByTitle());
                t.setWidth("100%");
                t.setPageLength(0);
                t.addStyleName("plain");
                t.addStyleName("borderless");
                t.setSortEnabled(false);
                t.setColumnAlignment("Revenue", Align.RIGHT);
                t.setRowHeaderMode(RowHeaderMode.INDEX);

                return t;
            } else if (caption.equals("chart")) {
                return new TopSixTheatersChart();
            }
        }

        return null;
    }

    private class ReorderLayoutDropHandler implements DropHandler {

        private AbstractOrderedLayout layout;

        public ReorderLayoutDropHandler(AbstractOrderedLayout layout) {
            this.layout = layout;
        }

        public AcceptCriterion getAcceptCriterion() {
            // return new SourceIs(component)
            return AcceptAll.get();
        }

        public void drop(DragAndDropEvent dropEvent) {
            Transferable transferable = dropEvent.getTransferable();
            Component sourceComponent = transferable.getSourceComponent();

            TargetDetails dropTargetData = dropEvent.getTargetDetails();
            DropTarget target = dropTargetData.getTarget();

            if (sourceComponent.getParent() != layout) {
                AbstractComponent c = getWrappedComponent(
                        createComponentFromPaletteItem(
                                sourceComponent.getCaption(), null), this);

                int index = 0;
                Iterator<Component> componentIterator = layout
                        .getComponentIterator();
                Component next = null;
                while (next != target && componentIterator.hasNext()) {
                    next = componentIterator.next();
                    if (next != sourceComponent) {
                        index++;
                    }
                }

                if (dropTargetData.getData("verticalLocation").equals(
                        VerticalDropLocation.TOP.toString())) {
                    index--;
                    if (index <= 0) {
                        index = 1;
                    }
                }

                layout.addComponent(c, index);
            }

            if (sourceComponent instanceof WrappedComponent) {
                // find the location where to move the dragged component
                boolean sourceWasAfterTarget = true;
                int index = 0;
                Iterator<Component> componentIterator = layout
                        .getComponentIterator();
                Component next = null;
                while (next != target && componentIterator.hasNext()) {
                    next = componentIterator.next();
                    if (next != sourceComponent) {
                        index++;
                    } else {
                        sourceWasAfterTarget = false;
                    }
                }
                if (next == null || next != target) {
                    // component not found - if dragging from another layout
                    return;
                }

                // drop on top of target?
                if (dropTargetData.getData("verticalLocation").equals(
                        VerticalDropLocation.MIDDLE.toString())) {
                    if (sourceWasAfterTarget) {
                        index--;
                    }
                }

                // drop before the target?
                else if (dropTargetData.getData("verticalLocation").equals(
                        VerticalDropLocation.TOP.toString())) {
                    index--;
                    if (index <= 0) {
                        index = 1;
                    }
                }

                // move component within the layout
                layout.removeComponent(sourceComponent);
                layout.addComponent(sourceComponent, index);
            }
        }
    }

    public void autoCreate(int which, Transferable items, Table table) {
        editors.addTab(createEditorInstance(which, items, table)).setClosable(
                true);
        editors.setSelectedTab(editors.getComponentCount() - 1);
    };

}

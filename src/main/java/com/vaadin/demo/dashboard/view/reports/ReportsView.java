package com.vaadin.demo.dashboard.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.demo.dashboard.component.TopSixTheatersChart;
import com.vaadin.demo.dashboard.component.TopTenMoviesTable;
import com.vaadin.demo.dashboard.data.dummy.DummyDataGenerator;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.demo.dashboard.event.QuickTicketsEvent.ReportsCountUpdatedEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ReportsView extends TabSheet implements View, CloseHandler {

    public ReportsView() {
        setSizeFull();
        addStyleName("reports");
        setCloseHandler(this);

        addTab(buildDrafts());
    }

    private Component buildDrafts() {
        final VerticalLayout allDrafts = new VerticalLayout();
        allDrafts.setSizeFull();
        allDrafts.setCaption("All Drafts");

        VerticalLayout titleAndDrafts = new VerticalLayout();
        titleAndDrafts.setSizeUndefined();
        titleAndDrafts.setSpacing(true);
        titleAndDrafts.addStyleName("drafts");
        allDrafts.addComponent(titleAndDrafts);
        allDrafts
                .setComponentAlignment(titleAndDrafts, Alignment.MIDDLE_CENTER);

        Label draftsTitle = new Label("Drafts");
        draftsTitle.addStyleName(ValoTheme.LABEL_H1);
        draftsTitle.setSizeUndefined();
        titleAndDrafts.addComponent(draftsTitle);
        titleAndDrafts.setComponentAlignment(draftsTitle, Alignment.TOP_CENTER);

        titleAndDrafts.addComponent(buildDraftsList());

        return allDrafts;
    }

    private Component buildDraftsList() {
        HorizontalLayout drafts = new HorizontalLayout();
        drafts.setSpacing(true);

        drafts.addComponent(buildDraftThumb());
        drafts.addComponent(buildCreateBox());

        return drafts;
    }

    private Component buildDraftThumb() {
        VerticalLayout draftThumb = new VerticalLayout();
        draftThumb.setSpacing(true);

        draftThumb.addStyleName("draft-thumb");
        Image draft = new Image(null, new ThemeResource(
                "img/draft-report-thumb.png"));
        draft.setWidth(160.0f, Unit.PIXELS);
        draft.setHeight(200.0f, Unit.PIXELS);
        draft.setDescription("Click to edit");
        draftThumb.addComponent(draft);
        Label draftTitle = new Label(
                "Monthly revenue<br><span>Last modified 1 day ago</span>",
                ContentMode.HTML);
        draftTitle.setSizeUndefined();
        draftThumb.addComponent(draftTitle);

        final Button delete = new Button("×");
        delete.setDescription("Delete draft");
        delete.setPrimaryStyleName("delete-button");
        delete.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Notification.show("Not implemented in this demo");
            }
        });
        draftThumb.addComponent(delete);

        draftThumb.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getButton() == MouseButton.LEFT
                        && event.getChildComponent() != delete) {
                    addReport(1, null, null);
                }
            }
        });

        return draftThumb;
    }

    private Component buildCreateBox() {
        VerticalLayout createBox = new VerticalLayout();
        createBox.setWidth(160.0f, Unit.PIXELS);
        createBox.setHeight(200.0f, Unit.PIXELS);
        createBox.addStyleName("create");

        Button create = new Button("Create New");
        create.addStyleName(ValoTheme.BUTTON_PRIMARY);
        create.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addReport(0, null, null);
            }
        });

        createBox.addComponent(create);
        createBox.setComponentAlignment(create, Alignment.MIDDLE_CENTER);
        return createBox;
    }

    private Component createEditorInstance(int which, Transferable items,
            Table table) {
        VerticalLayout editor = new VerticalLayout();
        editor.setSizeFull();
        editor.addStyleName("editor");
        editor.addStyleName(ValoTheme.DRAG_AND_DROP_WRAPPER_NO_HORIZONTAL_DRAG_HINTS);

        if (which == 1) {
            editor.setCaption("Monthly revenue");
        } else if (which == 2) {
            editor.setCaption("Generated report from selected transactions");
        } else {
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("M/dd/yyyy");
            editor.setCaption("Unnamed Report – " + (df.format(new Date()))
                    + " (" + getComponentCount() + ")");
        }

        Component palette = buildPalette();
        editor.addComponent(palette);
        editor.setComponentAlignment(palette, Alignment.TOP_CENTER);

        final SortableLayout canvas = new SortableLayout(editor.getCaption(),
                which, items, table);
        canvas.setWidth(100.0f, Unit.PERCENTAGE);
        canvas.addStyleName("canvas");
        editor.addComponent(canvas);
        editor.setExpandRatio(canvas, 1);

        return editor;
    }

    private Component buildPalette() {
        HorizontalLayout paletteLayout = new HorizontalLayout();
        paletteLayout.setMargin(true);
        paletteLayout.setSpacing(true);
        paletteLayout.setWidthUndefined();
        paletteLayout.setHeight(100.0f, Unit.PIXELS);
        paletteLayout.addStyleName("palette");
        paletteLayout.setCaption("Drag items to the canvas");

        paletteLayout.addComponent(buildPaletteItem("Text Block",
                "img/palette-text.png", "text"));
        paletteLayout.addComponent(buildPaletteItem("Top 10 Movies",
                "img/palette-grid.png", "grid"));
        paletteLayout.addComponent(buildPaletteItem("Top 4 Revenue",
                "img/palette-chart.png", "chart"));
        return paletteLayout;
    }

    private Component buildPaletteItem(String title, String imageThemeUrl,
            String type) {
        CssLayout wrap = new CssLayout();
        wrap.setWidth(120.0f, Unit.PIXELS);

        Image itemImage = new Image(null, new ThemeResource(imageThemeUrl));
        wrap.addComponent(itemImage);

        Label caption = new Label(title);
        caption.setSizeUndefined();
        wrap.addComponent(caption);

        DragAndDropWrapper ddWrap = new DragAndDropWrapper(wrap);
        ddWrap.setSizeUndefined();
        ddWrap.setDragStartMode(DragStartMode.WRAPPER);
        ddWrap.setData(type);
        return ddWrap;
    }

    public void addReport(int which, Transferable items, Table table) {
        addTab(createEditorInstance(which, items, table)).setClosable(true);
        DashboardEventBus.post(new ReportsCountUpdatedEvent(
                getComponentCount() - 1));
        setSelectedTab(getComponentCount() - 1);
    }

    @Override
    public void onTabClose(TabSheet tabsheet, final Component tabContent) {
        String windowCaption = "Unsaved Changes";
        String message = "You have not saved this report. Do you want to save or discard any changes you've made to this report?";
        String okCaption = "Save";
        String cancelCaption = "Cancel";
        String notOKCaption = "Don't Save";

        ConfirmDialog.show(UI.getCurrent(), windowCaption, message, okCaption,
                cancelCaption, notOKCaption, new ConfirmDialog.Listener() {

                    @Override
                    public void onClose(ConfirmDialog cd) {
                        if (!cd.isCanceled()) {
                            removeComponent(tabContent);
                            DashboardEventBus
                                    .post(new ReportsCountUpdatedEvent(
                                            getComponentCount() - 1));
                        }

                        if (cd.isConfirmed()) {
                            Notification
                                    .show("The report was saved as a draft",
                                            "Actually, the report was just closed and deleted forever. As this is only a demo, it doesn't persist any data.",
                                            Type.TRAY_NOTIFICATION);
                        }
                    }
                });
    }

    @Override
    public void enter(ViewChangeEvent event) {
    }

    private Component createComponentFromPaletteItem(Object data) {

        if (data != null) {
            if (data.equals("text")) {
                final CssLayout l = new CssLayout();
                l.addStyleName("text-editor");
                l.addStyleName("edit");
                l.setWidth("100%");
                final RichTextArea rta = new RichTextArea();
                rta.setWidth("100%");
                if (data == null) {
                    rta.setValue(DummyDataGenerator.randomText(30));
                } else {
                    // rta.setValue(data);
                }
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
                            save.removeStyleName(ValoTheme.BUTTON_PRIMARY);
                            save.addStyleName("icon-edit");
                            save.setDescription("Edit");
                        } else {
                            l.addStyleName("edit");
                            l.removeComponent(text);
                            l.addComponent(rta, 0);
                            rta.focus();
                            rta.selectAll();
                            save.setCaption("Save");
                            save.addStyleName(ValoTheme.BUTTON_PRIMARY);
                            save.removeStyleName("icon-edit");
                            save.setDescription(null);
                        }
                    }
                });
                rta.focus();
                rta.selectAll();
                l.addComponent(save);
                return l;
            } else if (data.equals("grid")) {
                return new TopTenMoviesTable();
            } else if (data.equals("chart")) {
                return new TopSixTheatersChart();
            }
        }

        return null;
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
                    Tab tab = getTab(SortableLayout.this.getParent());
                    String t = event.getText();
                    if (t == null || t.equals("")) {
                        t = " ";
                    }
                    tab.setCaption(t);
                }
            });
            layout.addComponent(title);

            dropHandler = new ReorderLayoutDropHandler();

            Label l = new Label("Drag items here");
            l.setSizeUndefined();

            if (which == 1) {
                addComponent(createComponentFromPaletteItem("chart"));
                addComponent(createComponentFromPaletteItem("grid"));
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

                addComponent(createComponentFromPaletteItem("text"));
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
                            Component c = createComponentFromPaletteItem(((AbstractComponent) sourceComponent)
                                    .getData());
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
            layout.addComponent(getWrappedComponent(component));
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

        private WrappedComponent getWrappedComponent(Component content) {
            if (((AbstractComponent) content).getData() != null) {
                CssLayout wrap = new CssLayout();
                wrap.setWidth(100.0f, Unit.PERCENTAGE);
                wrap.addComponent(content);
                return new WrappedComponent(wrap, dropHandler);
            } else {
                return new WrappedComponent(content, dropHandler);
            }
        }

        private class WrappedComponent extends DragAndDropWrapper {
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

        private class ReorderLayoutDropHandler implements DropHandler {

            @Override
            public AcceptCriterion getAcceptCriterion() {
                // return new SourceIs(component)
                return AcceptAll.get();
            }

            @Override
            public void drop(DragAndDropEvent dropEvent) {
                Transferable transferable = dropEvent.getTransferable();
                Component sourceComponent = transferable.getSourceComponent();

                TargetDetails dropTargetData = dropEvent.getTargetDetails();
                DropTarget target = dropTargetData.getTarget();

                if (sourceComponent.getParent() != layout) {
                    AbstractComponent c = getWrappedComponent(createComponentFromPaletteItem(((AbstractComponent) sourceComponent)
                            .getData()));

                    int index = 0;
                    Iterator<Component> componentIterator = layout.iterator();
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
                    Iterator<Component> componentIterator = layout.iterator();
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

    };

}

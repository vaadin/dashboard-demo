package com.vaadin.demo.dashboard.view.reports;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.demo.dashboard.component.TopSixTheatersChart;
import com.vaadin.demo.dashboard.component.TopTenMoviesTable;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.ThemeResource;
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
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ReportEditor extends VerticalLayout {

    private final ReportEditorListener listener;
    private final SortableLayout canvas;

    public ReportEditor(ReportEditorListener listener) {
        this.listener = listener;
        setSizeFull();
        addStyleName("editor");
        addStyleName(ValoTheme.DRAG_AND_DROP_WRAPPER_NO_HORIZONTAL_DRAG_HINTS);

        Component palette = buildPalette();
        addComponent(palette);
        setComponentAlignment(palette, Alignment.TOP_CENTER);

        canvas = new SortableLayout();
        canvas.setWidth(100.0f, Unit.PERCENTAGE);
        canvas.addStyleName("canvas");
        addComponent(canvas);
        setExpandRatio(canvas, 1);
    }

    public void setTitle(String title) {
        canvas.setTitle(title);
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
                "img/palette-text.png", PaletteItemType.TEXT));
        paletteLayout.addComponent(buildPaletteItem("Top 10 Movies",
                "img/palette-grid.png", PaletteItemType.TABLE));
        paletteLayout.addComponent(buildPaletteItem("Top 4 Revenue",
                "img/palette-chart.png", PaletteItemType.CHART));
        return paletteLayout;
    }

    private Component buildPaletteItem(String title, String imageThemeUrl,
            PaletteItemType type) {
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

    public void addWidget(PaletteItemType paletteItemType, Object prefillData) {
        canvas.addComponent(paletteItemType, prefillData);
    }

    public class SortableLayout extends CustomComponent {

        private VerticalLayout layout;
        private final DropHandler dropHandler;
        private TextField titleLabel;
        private DragAndDropWrapper placeholder;

        public SortableLayout() {
            setCompositionRoot(layout = new VerticalLayout());
            layout.addStyleName("canvas-layout");

            titleLabel = new TextField();
            titleLabel.addStyleName("title");
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("M/dd/yyyy");

            titleLabel.addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    String t = titleLabel.getValue();
                    if (t == null || t.equals("")) {
                        t = " ";
                    }
                    listener.titleChanged(t, ReportEditor.this);
                }
            });
            layout.addComponent(titleLabel);

            dropHandler = new ReorderLayoutDropHandler();

            Label l = new Label("Drag items here");
            l.setSizeUndefined();

            placeholder = new DragAndDropWrapper(l);
            placeholder.addStyleName("placeholder");
            placeholder.setDropHandler(new DropHandler() {

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return AcceptAll.get();
                }

                @Override
                public void drop(DragAndDropEvent event) {
                    Transferable transferable = event.getTransferable();
                    Component sourceComponent = transferable
                            .getSourceComponent();

                    if (sourceComponent != layout.getParent()) {
                        Object type = ((AbstractComponent) sourceComponent)
                                .getData();
                        addComponent((PaletteItemType) type, null);
                    }
                }
            });
            layout.addComponent(placeholder);
        }

        public void setTitle(String title) {
            this.titleLabel.setValue(title);
        }

        // private WrappedComponent getWrappedComponent(Component content) {
        // if (((AbstractComponent) content).getData() != null) {
        // CssLayout wrap = new CssLayout();
        // wrap.setWidth(100.0f, Unit.PERCENTAGE);
        // wrap.addComponent(content);
        // return new WrappedComponent(wrap, dropHandler);
        // } else {
        // return new WrappedComponent(content, dropHandler);
        // }
        // }

        public void addComponent(PaletteItemType paletteItemType,
                Object prefillData) {
            if (placeholder.getParent() != null) {
                layout.removeComponent(placeholder);
            }
            layout.addComponent(new WrappedComponent(
                    createComponentFromPaletteItem(paletteItemType, prefillData)));
        }

        private Component createComponentFromPaletteItem(PaletteItemType type,
                Object prefillData) {
            Component result = null;
            if (type == PaletteItemType.TEXT) {
                // TODO: To its own class and cleanup
                final CssLayout textLayout = new CssLayout();
                textLayout.addStyleName("text-editor");
                textLayout.addStyleName("edit");
                textLayout.setWidth(100.0f, Unit.PERCENTAGE);
                final RichTextArea rta = new RichTextArea();
                rta.setWidth(100.0f, Unit.PERCENTAGE);
                if (prefillData != null) {
                    rta.setValue(String.valueOf(prefillData));
                }
                textLayout.addComponent(rta);
                final Label text = new Label();
                text.setContentMode(ContentMode.HTML);
                final Button save = new Button("Save");
                save.addStyleName(ValoTheme.BUTTON_PRIMARY);
                save.addStyleName(ValoTheme.BUTTON_SMALL);
                save.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (save.getCaption().equals("Save")) {
                            textLayout.removeStyleName("edit");
                            textLayout.removeComponent(rta);
                            textLayout.addComponent(text, 0);
                            text.setValue(rta.getValue());
                            save.setCaption("");
                            save.removeStyleName(ValoTheme.BUTTON_PRIMARY);
                            save.addStyleName("icon-edit");
                            save.setDescription("Edit");
                        } else {
                            textLayout.addStyleName("edit");
                            textLayout.removeComponent(text);
                            textLayout.addComponent(rta, 0);
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
                textLayout.addComponent(save);
                result = textLayout;
            } else if (type == PaletteItemType.TABLE) {
                result = new TopTenMoviesTable();
            } else if (type == PaletteItemType.CHART) {
                result = new TopSixTheatersChart();
            } else if (type == PaletteItemType.TRANSACTIONS) {
                // TODO: To its own class and cleanup
                Collection<Transaction> transactions = (Collection<Transaction>) prefillData;
                CssLayout labels = new CssLayout();
                labels.addComponent(new Label(
                        "<strong>Selected transactions</strong>",
                        ContentMode.HTML));
                if (transactions != null) {
                    for (Transaction transaction : transactions) {
                        CssLayout root = new CssLayout();
                        root.addStyleName("transaction");

                        SimpleDateFormat df = new SimpleDateFormat();
                        df.applyPattern("MM/dd/yyyy hh:mm:ss a");

                        Label l = new Label(df.format((transaction.getTime()))
                                + "<br>" + transaction.getCity() + ", "
                                + transaction.getCountry());
                        l.setSizeUndefined();
                        l.setContentMode(ContentMode.HTML);
                        l.addStyleName("time");
                        root.addComponent(l);

                        l = new Label(transaction.getTitle());
                        l.setSizeUndefined();
                        l.addStyleName("movie-title");
                        root.addComponent(l);

                        l = new Label("Seats: "
                                + transaction.getSeats()
                                + "<br>"
                                + "Revenue: $"
                                + new DecimalFormat("#.##").format(transaction
                                        .getPrice()), ContentMode.HTML);
                        l.setSizeUndefined();
                        l.addStyleName("seats");
                        root.addComponent(l);

                    }
                }
                result = labels;
            }

            return result;
        }

        private class WrappedComponent extends DragAndDropWrapper {

            public WrappedComponent(Component content) {
                super(content);
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
                    Object paletteItemType = ((AbstractComponent) sourceComponent)
                            .getData();

                    AbstractComponent c = new WrappedComponent(
                            createComponentFromPaletteItem(
                                    (PaletteItemType) paletteItemType, null));

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

    }

    public interface ReportEditorListener {
        public void titleChanged(String newTitle, ReportEditor editor);
    }

    public enum PaletteItemType {
        TEXT, TABLE, CHART, TRANSACTIONS
    }
}
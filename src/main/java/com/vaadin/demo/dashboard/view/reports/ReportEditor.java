package com.vaadin.demo.dashboard.view.reports;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

import com.vaadin.demo.dashboard.component.InlineTextEditor;
import com.vaadin.demo.dashboard.component.TopSixTheatersChart;
import com.vaadin.demo.dashboard.component.TopTenMoviesTable;
import com.vaadin.demo.dashboard.component.TransactionsListing;
import com.vaadin.demo.dashboard.domain.Transaction;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "serial", "unchecked" })
public final class ReportEditor extends VerticalLayout {

    private final ReportEditorListener listener;
    private final SortableLayout canvas;

    public ReportEditor(final ReportEditorListener listener) {
        this.listener = listener;
        setSizeFull();
        addStyleName("editor");
        addStyleName(ValoTheme.DRAG_AND_DROP_WRAPPER_NO_HORIZONTAL_DRAG_HINTS);
        setMargin(false);
        setSpacing(false);

        Component palette = buildPalette();
        addComponent(palette);
        setComponentAlignment(palette, Alignment.TOP_CENTER);

        canvas = new SortableLayout();
        canvas.setWidth(100.0f, Unit.PERCENTAGE);
        canvas.addStyleName("canvas");
        addComponent(canvas);
        setExpandRatio(canvas, 1);
    }

    public void setTitle(final String title) {
        canvas.setTitle(title);
    }

    private Component buildPalette() {
        HorizontalLayout paletteLayout = new HorizontalLayout();
        paletteLayout.setWidthUndefined();
        paletteLayout.addStyleName("palette");

        paletteLayout.addComponent(buildPaletteItem(PaletteItemType.TEXT));
        paletteLayout.addComponent(buildPaletteItem(PaletteItemType.TABLE));
        paletteLayout.addComponent(buildPaletteItem(PaletteItemType.CHART));

        paletteLayout.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(final LayoutClickEvent event) {
                if (event.getChildComponent() != null) {
                    PaletteItemType data = (PaletteItemType) ((DragAndDropWrapper) event
                            .getChildComponent()).getData();
                    addWidget(data, null);
                }
            }
        });

        return paletteLayout;
    }

    private Component buildPaletteItem(final PaletteItemType type) {
        Label caption = new Label(type.getIcon().getHtml() + type.getTitle(),
                ContentMode.HTML);
        caption.setSizeUndefined();

        DragAndDropWrapper ddWrap = new DragAndDropWrapper(caption);
        ddWrap.setSizeUndefined();
        ddWrap.setDragStartMode(DragStartMode.WRAPPER);
        ddWrap.setData(type);
        return ddWrap;
    }

    public void addWidget(final PaletteItemType paletteItemType,
            final Object prefillData) {
        canvas.addComponent(paletteItemType, prefillData);
    }

    public final class SortableLayout extends CustomComponent {

        private VerticalLayout layout;
        private final DropHandler dropHandler;
        private TextField titleLabel;
        private DragAndDropWrapper placeholder;

        public SortableLayout() {
            layout = new VerticalLayout();
            setCompositionRoot(layout);
            layout.addStyleName("canvas-layout");
            layout.setMargin(false);
            layout.setSpacing(false);

            titleLabel = new TextField();
            titleLabel.addStyleName("title");
            SimpleDateFormat df = new SimpleDateFormat();
            df.applyPattern("M/dd/yyyy");

            titleLabel.addValueChangeListener(event -> {
                String t = titleLabel.getValue();
                if (t == null || t.equals("")) {
                    t = " ";
                }
                listener.titleChanged(t, ReportEditor.this);
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
                public void drop(final DragAndDropEvent event) {
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

        public void setTitle(final String title) {
            titleLabel.setValue(title);
        }

        public void addComponent(final PaletteItemType paletteItemType,
                final Object prefillData) {
            if (placeholder.getParent() != null) {
                layout.removeComponent(placeholder);
            }
            layout.addComponent(
                    new WrappedComponent(createComponentFromPaletteItem(
                            paletteItemType, prefillData)),
                    1);
        }

        private Component createComponentFromPaletteItem(
                final PaletteItemType type, final Object prefillData) {
            Component result = null;
            if (type == PaletteItemType.TEXT) {
                result = new InlineTextEditor(prefillData != null
                        ? String.valueOf(prefillData) : null);
            } else if (type == PaletteItemType.TABLE) {
                result = new TopTenMoviesTable();
            } else if (type == PaletteItemType.CHART) {
                result = new TopSixTheatersChart();
            } else if (type == PaletteItemType.TRANSACTIONS) {
                result = new TransactionsListing(
                        (Collection<Transaction>) prefillData);
            }

            return result;
        }

        private class WrappedComponent extends DragAndDropWrapper {

            public WrappedComponent(final Component content) {
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
            public void drop(final DragAndDropEvent dropEvent) {
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

                    if (dropTargetData.getData("verticalLocation")
                            .equals(VerticalDropLocation.TOP.toString())) {
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
                    if (dropTargetData.getData("verticalLocation")
                            .equals(VerticalDropLocation.MIDDLE.toString())) {
                        if (sourceWasAfterTarget) {
                            index--;
                        }
                    }

                    // drop before the target?
                    else if (dropTargetData.getData("verticalLocation")
                            .equals(VerticalDropLocation.TOP.toString())) {
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
        void titleChanged(String newTitle, ReportEditor editor);
    }

    public enum PaletteItemType {
        TEXT("Text Block", FontAwesome.FONT), TABLE("Top 10 Movies",
                FontAwesome.TABLE), CHART("Top 6 Revenue",
                        FontAwesome.BAR_CHART_O), TRANSACTIONS(
                                "Latest transactions", null);

        private final String title;
        private final FontAwesome icon;

        PaletteItemType(final String title, final FontAwesome icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public FontAwesome getIcon() {
            return icon;
        }

    }
}
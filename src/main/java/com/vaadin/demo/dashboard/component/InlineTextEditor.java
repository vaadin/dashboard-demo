package com.vaadin.demo.dashboard.component;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.themes.ValoTheme;

public class InlineTextEditor extends CustomComponent {

    private final Property<String> property = new ObjectProperty<String>(
            "Enter text here...");
    private final Component editor;
    private final Component readOnly;

    public InlineTextEditor(Object prefillData) {
        setWidth(100.0f, Unit.PERCENTAGE);

        editor = buildEditor();
        readOnly = buildReadOnly();

        if (prefillData != null) {
            property.setValue(String.valueOf(prefillData));
        }

        setCompositionRoot(editor);
    }

    private Component buildReadOnly() {
        final Label text = new Label(property);
        text.setContentMode(ContentMode.HTML);

        Button editButton = new Button(FontAwesome.EDIT);
        editButton.addStyleName(ValoTheme.BUTTON_SMALL);
        editButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        editButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setCompositionRoot(editor);
            }
        });

        CssLayout result = new CssLayout(text, editButton);
        result.addStyleName("text-editor");
        result.setSizeFull();
        result.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                if (event.getChildComponent() == text && event.isDoubleClick()) {
                    setCompositionRoot(editor);
                }
            }
        });
        return result;
    }

    private Component buildEditor() {
        final RichTextArea rta = new RichTextArea(property);
        rta.setWidth(100.0f, Unit.PERCENTAGE);
        rta.addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent event) {
                rta.focus();
                rta.selectAll();
            }
        });

        Button save = new Button("Save");
        save.setDescription("Edit");
        save.addStyleName(ValoTheme.BUTTON_PRIMARY);
        save.addStyleName(ValoTheme.BUTTON_SMALL);
        save.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setCompositionRoot(readOnly);
            }
        });

        CssLayout result = new CssLayout(rta, save);
        result.addStyleName("edit");
        result.setSizeFull();
        return result;
    }

}

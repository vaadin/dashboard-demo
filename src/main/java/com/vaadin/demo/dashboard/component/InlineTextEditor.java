package com.vaadin.demo.dashboard.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class InlineTextEditor extends CustomComponent {

    private final Component editor;
    private final Component readOnly;
    private final Label text = new Label();

    public InlineTextEditor(final String initialValue) {
        setWidth(100.0f, Unit.PERCENTAGE);
        addStyleName("inline-text-editor");

        if (initialValue != null) {
            editor = buildEditor(initialValue);
            readOnly = buildReadOnly(initialValue);
        } else {
            editor = buildEditor("Enter text here...");
            readOnly = buildReadOnly("Enter text here...");
        }

        setCompositionRoot(editor);
    }

    private Component buildReadOnly(final String initialValue) {
        text.setValue(initialValue);
        text.setContentMode(ContentMode.HTML);

        Button editButton = new Button(FontAwesome.EDIT);
        editButton.addStyleName(ValoTheme.BUTTON_SMALL);
        editButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        editButton.addClickListener(event -> setCompositionRoot(editor));

        CssLayout result = new CssLayout(text, editButton);
        result.addStyleName("text-editor");
        result.setSizeFull();
        result.addLayoutClickListener(event -> {
            if (event.getChildComponent() == text && event.isDoubleClick()) {
                setCompositionRoot(editor);
            }
        });
        return result;
    }

    private Component buildEditor(final String initialValue) {
        final RichTextArea rta = new RichTextArea(initialValue);
        rta.addValueChangeListener(event -> text.setValue(event.getValue()));
        rta.setWidth(100.0f, Unit.PERCENTAGE);
        rta.addAttachListener(event -> {
            rta.focus();
            rta.selectAll();
        });

        Button save = new Button("Save");
        save.setDescription("Edit");
        save.addStyleName(ValoTheme.BUTTON_PRIMARY);
        save.addStyleName(ValoTheme.BUTTON_SMALL);
        save.addClickListener(event -> setCompositionRoot(readOnly));

        CssLayout result = new CssLayout(rta, save);
        result.addStyleName("edit");
        result.setSizeFull();
        return result;
    }

}

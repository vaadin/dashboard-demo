package com.vaadin.demo.dashboard;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Window;

public class HelpOverlay extends Window {

    public HelpOverlay() {
        setContent(new CssLayout());
        setPrimaryStyleName("help-overlay");
        setDraggable(false);
        setResizable(false);
    }

    public void addComponent(Component c) {
        ((CssLayout) getContent()).addComponent(c);
    }

}

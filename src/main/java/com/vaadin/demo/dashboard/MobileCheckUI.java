package com.vaadin.demo.dashboard;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@Theme(Reindeer.THEME_NAME)
@Title("QuickTickets Dashboard")
public class MobileCheckUI extends UI {

    @Override
    protected void init(final VaadinRequest request) {
        setWidth("400px");
        setContent(new VerticalLayout() {
            {
                setMargin(true);
                addComponent(new Label(
                        "<h1>QuickTickets Dashboard</h1><h3>This Vaadin demo application is not designed for mobile devices.</h3><p>If you wish, you can continue to <a href=\""
                                + request.getContextPath()
                                + request.getPathInfo()
                                + "?mobile=false\">load it anyway</a>.</p><p>You can also <a href=\"https://vaadin.com/blog/-/blogs/it-s-demo-time\">read more about the demo application</a> and it's design from the Vaadin blog.</p>",
                        ContentMode.HTML));
            }
        });

    }
}

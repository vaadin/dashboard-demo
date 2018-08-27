package com.vaadin.demo.dashboard.view;

import java.lang.management.MemoryType;

import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Tag;
import com.netflix.spectator.gc.GcEvent;
import com.netflix.spectator.gc.GcEventListener;
import com.netflix.spectator.gc.GcLogger;
import com.netflix.spectator.jvm.Jmx;

import com.vaadin.demo.dashboard.DashboardNavigator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;

/*
 * Dashboard MainView is a simple HorizontalLayout that wraps the menu on the
 * left and creates a simple container for the navigator on the right.
 */
@SuppressWarnings("serial")
public class MainView extends HorizontalLayout {
    private final Registry registry;
    private final GcLogger gc;
    private final TextArea memoryData;

    public MainView(boolean isDebug) {
        setSizeFull();
        addStyleName("mainview");

        if (isDebug) {
            registry = new DefaultRegistry();
            gc = new GcLogger();
            memoryData = new TextArea();
            debugGarbage();
        } else {
            registry = null;
            gc = null;
            memoryData = null;
        }

        addComponent(new DashboardMenu());

        ComponentContainer content = new CssLayout();
        content.addStyleName("view-content");
        content.setSizeFull();
        addComponent(content);
        setExpandRatio(content, 1.0f);

        new DashboardNavigator(content);
    }

    private void debugGarbage() {
        System.setProperty("spectator.api.gaugePollingFrequency", "PT1S");
        Jmx.registerStandardMXBeans(registry);
        gc.start(new GcEventListener() {
            @Override
            public void onComplete(GcEvent gcEvent) {
                System.out.println("GC happened: " + gcEvent.toString());
                measureCurrentMemory();
            }
        });

        addComponent(new Button("Test", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                measureCurrentMemory();
            }
        }));
        addComponent(new Button("GC", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                System.gc();
            }
        }));
        memoryData.setEnabled(false);
        addComponent(memoryData);
    }

    private void measureCurrentMemory() {
        double totalMemoryB = 0;
        double totalMemoryMB = 0;
        for (Meter meter : registry) {
            Id id = meter.id();
            if ("jvm.memory.used".equals(id.name()) && containsTag(id.tags(), "memtype", MemoryType.HEAP.name())) {
                for (Measurement measurement : meter.measure()) {
                    totalMemoryB += measurement.value();
                    totalMemoryMB += measurement.value() / 1024 / 1024;
                }
            }
        }
        memoryData.setValue(String.format("Total memory (B): %.0f\nTotal memory (MB): %.0f", totalMemoryB, totalMemoryMB));
    }

    private boolean containsTag(Iterable<Tag> tags, String key, String value) {
        for (Tag tag : tags) {
            if (equals(tag.key(), key) && equals(tag.value(), value)) {
                return true;
            }
        }
        return false;
    }

    // Should be replaced with Object.equals when Java 1.8 is used
    private boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}

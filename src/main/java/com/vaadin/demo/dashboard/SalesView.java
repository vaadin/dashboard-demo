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
import java.util.ArrayList;
import java.util.Date;

import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.DataProvider.Movie;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class SalesView extends VerticalLayout implements View {

    private Timeline timeline;

    Color[] colors = new Color[] { new Color(52, 154, 255),
            new Color(242, 81, 57), new Color(255, 201, 35),
            new Color(83, 220, 164) };
    int colorIndex = -1;

    @Override
    public void enter(ViewChangeEvent event) {
        setSizeFull();
        addStyleName("timeline");

        Label header = new Label("Revenue by Movie Title");
        header.addStyleName("h1");
        addComponent(header);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth("100%");
        toolbar.setSpacing(true);
        toolbar.setMargin(true);
        toolbar.addStyleName("toolbar");
        addComponent(toolbar);

        final ComboBox movieSelect = new ComboBox();
        ArrayList<Movie> movies = DataProvider.getMovies();
        for (Movie m : movies) {
            movieSelect.addItem(m.title);
        }
        movieSelect.setWidth("300px");
        toolbar.addComponent(movieSelect);
        movieSelect.addShortcutListener(new ShortcutListener("Add",
                KeyCode.ENTER, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                addSelectedMovie(movieSelect);
            }
        });

        Button add = new Button("Add");
        add.addStyleName("default");
        add.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addSelectedMovie(movieSelect);
            }
        });
        toolbar.addComponent(add);
        toolbar.setComponentAlignment(add, Alignment.BOTTOM_LEFT);

        Button clear = new Button("Clear");
        clear.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                timeline.removeAllGraphDataSources();
            }
        });
        toolbar.addComponent(clear);
        toolbar.setComponentAlignment(clear, Alignment.BOTTOM_RIGHT);
        toolbar.setExpandRatio(clear, 1);

        timeline = new Timeline();
        timeline.setDateSelectVisible(false);
        timeline.setChartModesVisible(false);
        timeline.setGraphShadowsEnabled(false);
        timeline.setZoomLevelsVisible(false);
        timeline.setSizeFull();
        timeline.setNoDataSourceCaption("<span class=\"v-label h2 light\">Add a data set from the dropdown above</span>");

        addComponent(timeline);
        setExpandRatio(timeline, 1);

        // Add first 4 by default
        int i = 0;
        for (Movie m : DataProvider.getMovies()) {
            addDataSet(m.title);
            if (++i > 3)
                break;
        }

        Date start = new Date();
        start.setMonth(start.getMonth() - 2);
        Date end = new Date();
        if (timeline.getGraphDatasources().size() > 0)
            timeline.setVisibleDateRange(start, end);

    }

    private void addSelectedMovie(final ComboBox movieSelect) {
        if (movieSelect.getValue() != null
                && !movieSelect.getValue().equals("")) {
            String title = movieSelect.getValue().toString();
            addDataSet(title);
            movieSelect.removeItem(title);
            movieSelect.setValue(null);
        }
    }

    private void addDataSet(String title) {
        IndexedContainer revenue = ((DashboardUI) getUI()).dataProvider
                .getRevenueForTitle(title);
        timeline.addGraphDataSource(revenue, "timestamp", "revenue");
        colorIndex = (colorIndex >= colors.length - 1 ? 0 : ++colorIndex);
        timeline.setGraphOutlineColor(revenue, colors[colorIndex]);
        timeline.setBrowserOutlineColor(revenue, colors[colorIndex]);
        timeline.setBrowserFillColor(revenue, colors[colorIndex].brighter());
        timeline.setGraphLegend(revenue, title);
        timeline.setEventCaptionPropertyId("date");
        timeline.setVerticalAxisLegendUnit(revenue, "$");
    }
}

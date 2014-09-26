package com.vaadin.demo.dashboard.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.vaadin.maddon.ListContainer;

import com.vaadin.addon.timeline.Timeline;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class SalesView extends VerticalLayout implements View {

    private final Timeline timeline;
    private ComboBox movieSelect;

    private static final Color[] COLORS = new Color[] {
            new Color(52, 154, 255), new Color(242, 81, 57),
            new Color(255, 201, 35), new Color(83, 220, 164) };
    private int colorIndex = -1;

    public SalesView() {
        setSizeFull();
        addStyleName("sales");

        addComponent(buildHeader());
        addComponent(buildToolbar());

        timeline = buildTimeline();
        addComponent(timeline);
        setExpandRatio(timeline, 1);

        initMovieSelect();
        // Add first 4 by default
        List<Movie> subList = new ArrayList<Movie>(DashboardUI
                .getDataProvider().getMovies()).subList(0, 4);
        for (Movie m : subList) {
            addDataSet(m);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        if (timeline.getGraphDatasources().size() > 0) {
            timeline.setVisibleDateRange(calendar.getTime(), new Date());
        }
    }

    private void initMovieSelect() {
        Collection<Movie> movies = DashboardUI.getDataProvider().getMovies();
        Container movieContainer = new ListContainer<Movie>(Movie.class, movies);
        movieSelect.setContainerDataSource(movieContainer);
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        header.setWidth(100.0f, Unit.PERCENTAGE);
        header.setSpacing(true);
        header.setMargin(true);

        Label titleLabel = new Label("Revenue by Movie Title");
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H1);
        header.addComponent(titleLabel);

        return header;
    }

    private Component buildToolbar() {
        CssLayout toolbar = new CssLayout();
        toolbar.setWidth(100.0f, Unit.PERCENTAGE);
        toolbar.addStyleName("toolbar");

        movieSelect = new ComboBox();
        movieSelect.setWidth(300.0f, Unit.PIXELS);
        movieSelect.setItemCaptionPropertyId("title");
        movieSelect.addShortcutListener(new ShortcutListener("Add",
                KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                addDataSet((Movie) movieSelect.getValue());
            }
        });
        toolbar.addComponent(movieSelect);

        final Button add = new Button("Add");
        add.setEnabled(false);
        add.addStyleName(ValoTheme.BUTTON_PRIMARY);
        toolbar.addComponent(add);

        movieSelect.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                add.setEnabled(event.getProperty().getValue() != null);
            }
        });

        final Button clear = new Button("Clear");
        clear.addStyleName("clearbutton");
        clear.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                timeline.removeAllGraphDataSources();
                initMovieSelect();
                clear.setEnabled(false);
            }
        });
        toolbar.addComponent(clear);

        add.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                addDataSet((Movie) movieSelect.getValue());
                clear.setEnabled(true);
            }
        });

        return toolbar;
    }

    private Timeline buildTimeline() {
        Timeline timeline = new Timeline();
        timeline.setDateSelectVisible(false);
        timeline.setChartModesVisible(false);
        timeline.setGraphShadowsEnabled(false);
        timeline.setZoomLevelsVisible(false);
        timeline.setSizeFull();
        timeline.setNoDataSourceCaption("<span class=\"v-label h2 light\">Add a data set from the dropdown above</span>");
        return timeline;
    }

    private void addDataSet(Movie movie) {
        movieSelect.removeItem(movie);
        movieSelect.setValue(null);

        Collection<MovieRevenue> dailyRevenue = DashboardUI.getDataProvider()
                .getDailyRevenuesByMovie(movie.getId());

        ListContainer<MovieRevenue> dailyRevenueContainer = new ListContainer<MovieRevenue>(
                MovieRevenue.class, dailyRevenue);

        dailyRevenueContainer.sort(new Object[] { "timestamp" },
                new boolean[] { true });

        timeline.addGraphDataSource(dailyRevenueContainer, "timestamp",
                "revenue");
        colorIndex = (colorIndex >= COLORS.length - 1 ? 0 : ++colorIndex);
        timeline.setGraphOutlineColor(dailyRevenueContainer, COLORS[colorIndex]);
        timeline.setBrowserOutlineColor(dailyRevenueContainer,
                COLORS[colorIndex]);
        timeline.setBrowserFillColor(dailyRevenueContainer,
                COLORS[colorIndex].brighter());
        timeline.setGraphCaption(dailyRevenueContainer, movie.getTitle());
        timeline.setEventCaptionPropertyId("date");
        timeline.setVerticalAxisLegendUnit(dailyRevenueContainer, "$");
    }

    @Override
    public void enter(ViewChangeEvent event) {
    }
}

package com.vaadin.demo.dashboard.view.sales;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.Movie;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
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

    private final Chart timeline;
    private ComboBox<Movie> movieSelect;
    private Collection<Movie> movies;

    public SalesView() {
        setSizeFull();
        addStyleName("sales");
        setMargin(false);
        setSpacing(false);

        addComponent(buildHeader());

        timeline = buildTimeline();
        addComponent(timeline);
        setExpandRatio(timeline, 1);

        initMovieSelect();
        // Add first 4 by default
        List<Movie> subList = new ArrayList<Movie>(
                DashboardUI.getDataProvider().getMovies()).subList(0, 4);
        for (Movie m : subList) {
            addDataSet(m);
        }
    }

    private void initMovieSelect() {
        movies = new HashSet<>(DashboardUI.getDataProvider().getMovies());
        movieSelect.setItems(movies);
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("viewheader");
        Responsive.makeResponsive(header);

        Label titleLabel = new Label("Revenue by Movie");
        titleLabel.setSizeUndefined();
        titleLabel.addStyleName(ValoTheme.LABEL_H1);
        titleLabel.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        header.addComponents(titleLabel, buildToolbar());

        return header;
    }

    private Component buildToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addStyleName("toolbar");

        movieSelect = new ComboBox<>();
        movieSelect.setItemCaptionGenerator(Movie::getTitle);
        movieSelect.addShortcutListener(
                new ShortcutListener("Add", KeyCode.ENTER, null) {
                    @Override
                    public void handleAction(final Object sender,
                            final Object target) {
                        addDataSet(movieSelect.getValue());
                    }
                });

        final Button add = new Button("Add");
        add.setEnabled(false);
        add.addStyleName(ValoTheme.BUTTON_PRIMARY);

        CssLayout group = new CssLayout(movieSelect, add);
        group.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
        toolbar.addComponent(group);

        movieSelect.addSelectionListener(
                event -> add.setEnabled(event.getValue() != null));

        final Button clear = new Button("Clear");
        clear.addStyleName("clearbutton");
        clear.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                timeline.getConfiguration().setSeries(new ArrayList<>());
                timeline.drawChart();
                initMovieSelect();
                clear.setEnabled(false);
            }
        });
        toolbar.addComponent(clear);

        add.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                addDataSet(movieSelect.getValue());
                clear.setEnabled(true);
            }
        });

        return toolbar;
    }

    private Chart buildTimeline() {
        Chart result = new Chart();
        result.setSizeFull();

        result.setTimeline(true);

        result.getConfiguration().getRangeSelector().setEnabled(false);

        Legend legend = result.getConfiguration().getLegend();
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setEnabled(true);
        return result;
    }

    private void addDataSet(final Movie movie) {
        movies.remove(movie);
        movieSelect.setValue(null);
        movieSelect.getDataProvider().refreshAll();

        Collection<MovieRevenue> revenues = DashboardUI.getDataProvider()
                .getDailyRevenuesByMovie(movie.getId());

        DataSeries movieSeries = new DataSeries();
        for (MovieRevenue revenue : revenues) {
            DataSeriesItem item = new DataSeriesItem();
            item.setX(revenue.getTimestamp());
            item.setY(revenue.getRevenue());
            movieSeries.add(item);
        }
        movieSeries.setName(movie.getTitle());
        timeline.getConfiguration().addSeries(movieSeries);
        timeline.drawChart();
    }

    @Override
    public void enter(final ViewChangeEvent event) {
    }
}

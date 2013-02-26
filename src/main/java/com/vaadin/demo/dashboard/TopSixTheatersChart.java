package com.vaadin.demo.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.DataProvider.Movie;

public class TopSixTheatersChart extends Chart {

    public TopSixTheatersChart() {
        // TODO this don't actually visualize top six theaters, but just makes a
        // pie chart
        super(ChartType.PIE);

        setCaption("Popular Movies");
        getConfiguration().setTitle("");
        getConfiguration().getChart().setType(ChartType.PIE);
        setWidth("100%");
        setHeight("90%");

        List<Series> series = new ArrayList<Series>();

        ArrayList<Movie> movies = DataProvider.getMovies();
        for (int i = 0; i < 6; i++) {
            Movie movie = movies.get(i);
            series.add(new ListSeries(movie.title, movie.score));
        }
        getConfiguration().setSeries(series);
    }

}

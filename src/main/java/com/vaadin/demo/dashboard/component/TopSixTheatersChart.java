package com.vaadin.demo.dashboard.component;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.demo.dashboard.data.dummy.DummyDataProvider;
import com.vaadin.demo.dashboard.domain.Movie;

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

        DataSeries series = new DataSeries();

        List<Movie> movies = new ArrayList<Movie>(DummyDataProvider.getMovies());
        for (int i = 0; i < 6; i++) {
            Movie movie = movies.get(i);
            series.add(new DataSeriesItem(movie.getTitle(), movie.getScore()));
        }
        getConfiguration().setSeries(series);
    }

}

package com.vaadin.demo.dashboard.component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.domain.MovieRevenue;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

public class TopTenMoviesTable extends Table {

    @Override
    protected String formatPropertyValue(Object rowId, Object colId,
            Property<?> property) {
        String result = super.formatPropertyValue(rowId, colId, property);
        if (colId.equals("revenue")) {
            if (property != null && property.getValue() != null) {
                Double r = (Double) property.getValue();
                String ret = new DecimalFormat("#.##").format(r);
                result = "$" + ret;
            } else {
                result = "";
            }
        }
        return result;
    }

    public TopTenMoviesTable() {
        setCaption("Top 10 Titles by Revenue");

        setPageLength(0);
        addStyleName(ValoTheme.TABLE_BORDERLESS);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        setSortEnabled(false);
        setColumnAlignment("revenue", Align.RIGHT);
        setRowHeaderMode(RowHeaderMode.INDEX);
        setSizeFull();
        setHeightUndefined();

        List<MovieRevenue> movieRevenues = new ArrayList<MovieRevenue>(
                DashboardUI.getDataProvider().getTotalMovieRevenues());
        Collections.sort(movieRevenues, new Comparator<MovieRevenue>() {
            @Override
            public int compare(MovieRevenue o1, MovieRevenue o2) {
                return o2.getRevenue().compareTo(o1.getRevenue());
            }
        });

        setContainerDataSource(new BeanItemContainer<MovieRevenue>(
                MovieRevenue.class, movieRevenues.subList(0, 10)));

        setVisibleColumns("title", "revenue");
        setColumnHeaders("Title", "Revenue");

        setSortContainerPropertyId("revenue");
        setSortAscending(false);
    }

}

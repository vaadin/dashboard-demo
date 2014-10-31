package com.vaadin.demo.dashboard.component;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Credits;
import com.vaadin.addon.charts.model.DashStyle;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.Marker;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.demo.dashboard.data.dummy.DummyDataGenerator;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class SparklineChart extends VerticalLayout {

    public SparklineChart(final String name, final String unit,
            final String prefix, final Color color, final int howManyPoints,
            final int min, final int max) {
        setSizeUndefined();
        addStyleName("spark");
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        int[] values = DummyDataGenerator.randomSparklineValues(howManyPoints,
                min, max);

        Label current = new Label(prefix + values[values.length - 1] + unit);
        current.setSizeUndefined();
        current.addStyleName(ValoTheme.LABEL_HUGE);
        addComponent(current);

        Label title = new Label(name);
        title.setSizeUndefined();
        title.addStyleName(ValoTheme.LABEL_SMALL);
        title.addStyleName(ValoTheme.LABEL_LIGHT);
        addComponent(title);

        addComponent(buildSparkline(values, color));

        List<Integer> vals = Arrays.asList(ArrayUtils.toObject(values));
        Label highLow = new Label("High <b>" + java.util.Collections.max(vals)
                + "</b> &nbsp;&nbsp;&nbsp; Low <b>"
                + java.util.Collections.min(vals) + "</b>", ContentMode.HTML);
        highLow.addStyleName(ValoTheme.LABEL_TINY);
        highLow.addStyleName(ValoTheme.LABEL_LIGHT);
        highLow.setSizeUndefined();
        addComponent(highLow);

    }

    private Component buildSparkline(final int[] values, final Color color) {
        Chart spark = new Chart();
        spark.getConfiguration().setTitle("");
        spark.getConfiguration().getChart().setType(ChartType.LINE);
        spark.getConfiguration().getChart().setAnimation(false);
        spark.setWidth("120px");
        spark.setHeight("40px");

        DataSeries series = new DataSeries();
        for (int i = 0; i < values.length; i++) {
            DataSeriesItem item = new DataSeriesItem("", values[i]);
            series.add(item);
        }
        spark.getConfiguration().setSeries(series);
        spark.getConfiguration().getTooltip().setEnabled(false);

        Configuration conf = series.getConfiguration();
        Legend legend = new Legend();
        legend.setEnabled(false);
        conf.setLegend(legend);

        Credits c = new Credits("");
        spark.getConfiguration().setCredits(c);

        PlotOptionsLine opts = new PlotOptionsLine();
        opts.setAllowPointSelect(false);
        opts.setColor(color);
        opts.setDataLabels(new Labels(false));
        opts.setLineWidth(1);
        opts.setShadow(false);
        opts.setDashStyle(DashStyle.SOLID);
        opts.setMarker(new Marker(false));
        opts.setEnableMouseTracking(false);
        opts.setAnimation(false);
        spark.getConfiguration().setPlotOptions(opts);

        XAxis xAxis = spark.getConfiguration().getxAxis();
        YAxis yAxis = spark.getConfiguration().getyAxis();

        SolidColor transparent = new SolidColor(0, 0, 0, 0);

        xAxis.setLabels(new Labels(false));
        xAxis.setTickWidth(0);
        xAxis.setLineWidth(0);

        yAxis.setTitle(new Title(""));
        yAxis.setAlternateGridColor(transparent);
        yAxis.setLabels(new Labels(false));
        yAxis.setLineWidth(0);
        yAxis.setGridLineWidth(0);

        return spark;
    }
}

package NSGA2;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;


public class PlotVerifier extends ApplicationFrame {
    private int numSeries = 3;
    //    private XYSeries[] series;
    private XYSeriesCollection collection;
    private XYLineAndShapeRenderer renderer;
    private final Color DARK_GREEN = new Color (30, 115, 48);
    private final Color PURPLE = new Color(105,49, 211);
    private final Color DARK_ORANGE = new Color(202, 82, 9);
    private final Color MAROON = new Color(113, 12, 26);
    private final Color CYAN = new Color(14, 164, 182);
    private final Color[] colors = {
//            Color.YELLOW,
            Color.BLACK, Color.BLUE, Color.MAGENTA, Color.RED, DARK_GREEN, PURPLE, DARK_ORANGE, MAROON, CYAN,
            Color.DARK_GRAY, Color.GRAY
    };
    private ChartPanel chartPanel;
    private  JFreeChart chart;
    private int colorIter = 0;
    private int seriesCounter = 0;
    private final String RESULT_EXTENSION = ".txt";
    private final String RESULT_DIR = "D:/COMSOL Research/35) test/circle removal/";
    private final String SAVE_DIR = "D:\\COMSOL Research\\63) pattern testing\\";
    /**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param title  the frame title.
     */
    public PlotVerifier(final String title) {
        super(title);
        collection = new XYSeriesCollection();
//        series = new XYSeries[numSeries];
        renderer = new XYLineAndShapeRenderer();
    }

    public static void main(final String[] args) throws IOException {
        PlotVerifier p = new PlotVerifier("Concentrations");
        File pressure = new File("D:\\COMSOL Research\\63) pattern testing\\pressure_wholechip.txt");
        File conc = new File("D:\\COMSOL Research\\63) pattern testing\\conc_wholechip.txt");

        double[][] pressures = getFileDataPts(new BufferedReader(new FileReader(pressure)));
        double[][] concs = getFileDataPts(new BufferedReader(new FileReader(pressure)));

        p.addDataPts(new XYSeries("concentrations"), pressures);
//        p.addDataPts(new XYSeries("pressures"), pressures);
        p.createChart();
    }

    private double[][] addDatapts(BufferedReader br) throws IOException {
        ArrayList<double[]> data = new ArrayList<>();
        String s = br.readLine();

        while (s != null) {
            String[] split = s.split("\\s+");
            double[] datapt = {Double.parseDouble(split[0]), Double.parseDouble(split[1])};
            data.add(datapt);
            s = br.readLine();
        }
        double[][] result = new double[data.size()][2];
        for (int i = 0; i < result.length;i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    private static double[][] getFileDataPts(BufferedReader br) throws IOException {
        ArrayList<double[]> data = new ArrayList<>();
        String s = br.readLine();
//        System.out.println(s);

        while (s != null) {
            String[] split = s.split("\\s+");
//            for (String item : split)
//                System.out.println(item);
            double[] datapt = {Double.parseDouble(split[0]), Double.parseDouble(split[1])};
            data.add(datapt);
            s = br.readLine();
        }
        double[][] result = new double[data.size()][2];
        for (int i = 0; i < result.length; i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    public void addDataPts(XYSeries series, double[][] data) {
//        int numCharts = 0;
        for (int i = 0; i < data.length; i++) {
            series.add(data[i][0], data[i][1]);
            if(colorIter == colors.length)
                colorIter = 0;
        }
        renderer.setSeriesPaint(seriesCounter,colors[colorIter++]);
//        renderer.setSeriesShapesVisible(seriesCounter++, false);
//        renderer.setBaseShapesVisible(true);
        renderer.setBaseLinesVisible(false);
        collection.addSeries(series);
    }

    public void createChart() throws IOException {
        chart = ChartFactory.createXYLineChart(
                "",
                "x-pos",
                "y-pos",
                collection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(renderer);
//        chart.removeLegend();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
//        domain.setRange(0, 1);
//        range.setRange(0, 0.6);

//        chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
//        setContentPane(chartPanel);
        String newChartName = SAVE_DIR + "chip_design.png";
        System.out.println("Creating File: " + newChartName);
        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 2500, 800 );

    }

}

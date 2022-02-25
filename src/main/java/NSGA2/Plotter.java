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


public class Plotter extends ApplicationFrame {
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
            Color.YELLOW, Color.BLACK, Color.BLUE, Color.MAGENTA, Color.RED, DARK_GREEN, PURPLE, DARK_ORANGE, MAROON, CYAN,
            Color.DARK_GRAY, Color.GRAY
    };
    private ChartPanel chartPanel;
    private  JFreeChart chart;
    private int colorIter = 0;
    private int seriesCounter = 0;
    private final String RESULT_EXTENSION = ".txt";
    private final String RESULT_DIR = "D:/COMSOL Research/35) test/circle removal/";
    private final String SAVE_DIR = RESULT_DIR + "plots/";
    private String currentPop = "";
    /**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param title  the frame title.
     */
    public Plotter(final String title) {
        super(title);
        collection = new XYSeriesCollection();
//        series = new XYSeries[numSeries];
        renderer = new XYLineAndShapeRenderer();
    }

    public void addDataPts(XYSeries series, double[][] data) {
//        int numCharts = 0;
        for (int i = 0; i < data.length; i++) {
            series.add(data[i][0], data[i][1]);
            if(colorIter == colors.length)
                colorIter = 0;
        }
        renderer.setSeriesPaint(seriesCounter,colors[colorIter++]);
        renderer.setSeriesShapesVisible(seriesCounter++, false);
        renderer.setBaseShapesVisible(false);
        collection.addSeries(series);
    }

    public void addHorizontal() {
        XYSeries line = new XYSeries("0.33 line");
        line.add(0, 1.0/3.0);
        line.add(1, 1.0/3.0);
        renderer.setSeriesPaint(seriesCounter,colors[colorIter++]);
        renderer.setSeriesShapesVisible(seriesCounter++, false);
        renderer.setBaseShapesVisible(false);
        collection.addSeries(line);
    }

    public void createChart() throws IOException {
        chart = ChartFactory.createXYLineChart(
                "Concentrations",
                "Exit Position (top to bottom)",
                "Concentration (mol/m^3)",
                collection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(renderer);
//        chart.removeLegend();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        domain.setRange(0, 1);
        range.setRange(0, 0.6);

//        chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
//        setContentPane(chartPanel);
        String newChartName = SAVE_DIR + currentPop + ".png";
        System.out.println("Creating File: " + newChartName);
        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 1000, 800 );

    }

    public static void main(final String[] args) throws IOException {
        Plotter p = new Plotter("Concentrations");
        File dir = new File(p.RESULT_DIR);
        File[] directoryListing = dir.listFiles();
        char nameSeparator = '_';

        File saveDir = new File(p.SAVE_DIR);
        saveDir.mkdir();
        if (directoryListing != null) {
            for (File item : directoryListing) {
                String filename = item.toString();
                String extension = filename.substring(filename.length() - p.RESULT_EXTENSION.length());
                if(extension.equals(p.RESULT_EXTENSION)) {
//                    String filePop = filename.substring(p.RESULT_DIR.length() + 1, filename.indexOf(nameSeparator));
//                    if (filePop.equals(p.currentPop)) {
                        p.addDataPts(new XYSeries(filename.substring(p.RESULT_DIR.length())), p.getConc(filename));
//                    }
//                    else if(filePop.equals("best")) {
//                        //do nothing - ignore file
//                    }
//                    else {
//                        if (!p.currentPop.equals("")) {
//                            p.createChart();
//                        }
//                        p = new Plotter("Concentrations");
//                        p.currentPop = filePop;
//                        p.addDataPts(new XYSeries(filename.substring(p.RESULT_DIR.length())), p.getConc(filename));
//                    }
                }
            }
            p.createChart();
        }
        else {
            throw new IOException("Directory " + p.RESULT_DIR + " has no files.");
        }

//        String file8 = "D:/COMSOL Research/21) 20 run but 0.1-0.2 radii instead 0.05-0.2, 12k evals/396_exitconc.txt";
//        String file9 = "D:/COMSOL Research/22) 21 run but 25k evals, 1k pop (most beginning runs lost)/861_exitconc.txt";
//
//        p.addDataPts(new XYSeries("section obj, 12k obj, 500 pop"), p.getConc(file8));
//        p.addDataPts(new XYSeries("section obj, 15k obj, 1000 pop"), p.getConc(file9));
//        p.addHorizontal();

//        p.createChart();
//        p.pack();
//        RefineryUtilities.centerFrameOnScreen(p);
//        p.setVisible(true);


    }
    public double[][] getConc(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        return addDatapts(br);
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

}
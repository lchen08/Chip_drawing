package NSGA2;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.util.ArrayList;

public class SolutionPlotter extends ApplicationFrame {
    //        private XYSeries[] series;
    private XYSeriesCollection collection;
    //            private XYLineAndShapeRenderer renderer;
    private JFreeChart chart;
    private String saveDir;
    private static final String BEST_DIR = "D:/COMSOL Research/35) test/more human/101 posts original spacing/";
//        private static final String NONPARETO_DIR = "D:/COMSOL Research/51) repeat 50, 4000 evals/";
//    private static final String NONPARETO_DIR = "D:/COMSOL Research/51) repeat 50, 4000 evals/";
    private static final String NONPARETO_DIR = "G:/My Drive/COMSOL runs/from Desktop/51) repeat 50, 4000 evals/";
    private static final String PARETO_DIR = NONPARETO_DIR + "paretosaves/";
//private static final String PARETO_DIR = "G:/My Drive/COMSOL runs/from Desktop/46) repeat 42, adjusted mutations, P100, 5k evals/paretosaves/";
//        private static final String PARETO_DIR = "D:/COMSOL Research/46) repeat 42, adjusted mutations, P100, 5k evals/paretosaves/";
//    private final String HUMAN_RESULT_CONC_FILE = BEST_DIR + "0_exitconc.txt";
//    private final String HUMAN_RESULT_PRESSURE_FILE = BEST_DIR + "0_entrypressure.txt";
    private final String HUMAN_RESULT_CONC_FILE = BEST_DIR + "Posts101_exitconc.txt";
    private final String HUMAN_RESULT_PRESSURE_FILE = BEST_DIR + "Posts101_entrypressure.txt";
//    private static final String SAVE_DIR = NONPARETO_DIR + "paretosaves4/";
    private static final String SAVE_DIR = "D:/COMSOL Research/";

    private final Color CB_ORANGE = new Color(213, 90, 0);
    private final Color CB_GREEN = new Color(53, 155, 115);
    private final Color[] colors = {CB_ORANGE, CB_GREEN, Color.BLACK};
    private int colorIter = 0;
    private int seriesCounter = 0;
    private int maxRange = 50;
    private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    public static void main(String[] args) throws IOException {
        ArrayList<ParetoSolution> solutions = new ArrayList();
        File dir = new File(BEST_DIR);
        File[] directoryListing = dir.listFiles();
        boolean foundStart = false;
        for (int i = 900; i <= 5050; i++) {
            String filename = "Pareto_" + i;
            File pressureFile = new File(PARETO_DIR + "Pareto_" + i + "_entrypressure.txt");
            if (pressureFile.exists()) {
                foundStart = true;
                File concFile = new File(PARETO_DIR + "Pareto_" + i + "_exitconc.txt");
                System.out.println("got to here");

                double[][] pressures = getFileDataPts(new BufferedReader(new FileReader(pressureFile)));
                double[][] concs = getFileDataPts(new BufferedReader(new FileReader(concFile)));
                double capture = getCaptureEfficiency(concs);
                double pressure = pressures[pressures.length / 2][1];
                solutions.add(new ParetoSolution(capture, pressure, filename));
                System.out.println(concFile.getName() + "  " + capture +  " " + pressure);
            } else if (foundStart) {
                i = 5051;
            }
        }

        System.out.println("Size: " + solutions.size());
        SolutionPlotter sp = new SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);
//        SolutionPlotter sp = new SolutionPlotter("Capture vs Pressure", SAVE_DIR);
        sp.createChart();
    }

    public SolutionPlotter(final String title, ArrayList<ParetoSolution> solutions, String saveDir)
            throws IOException {
        super(title);
        collection = new XYSeriesCollection();
//            series = new XYSeries[numSolutions];
//            renderer = new XYLineAndShapeRenderer();
//        addParetoSecond();
        addDataPts(solutions);
        addNonParetoPoints();
//        addHumanPts(HUMAN_RESULT_CONC_FILE, HUMAN_RESULT_PRESSURE_FILE);
        this.saveDir = saveDir;
        File theDir = new File(saveDir);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    public SolutionPlotter(final String title, String saveDir)
            throws IOException {
        super(title);
        collection = new XYSeriesCollection();
        addNonParetoPoints();
        addHumanPts(HUMAN_RESULT_CONC_FILE, HUMAN_RESULT_PRESSURE_FILE);
        this.saveDir = saveDir;
        File theDir = new File(saveDir);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    public void createChart() throws IOException {
        chart = ChartFactory.createScatterPlot(
                null,
//                "31-Post Chips: Pareto Front Comparison",
//                "Capture Efficiency vs Pressure",
                "% Capture Efficiency",
                "Inlet Pressure (Pa)",
                collection,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();

//        Font titleFont = new Font("Calibri", Font.BOLD, 30);
//        Font axisFont = new Font("Calibri", Font.PLAIN, 28);
//        Font tickFont = new Font("Calibri", Font.PLAIN, 20);
//        Font legendFont = new Font("Calibri", Font.PLAIN, 24);
//        plot.setRenderer(renderer);
//        //        chart.removeLegend();
//        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
//        NumberAxis range = (NumberAxis) plot.getRangeAxis();
//        LegendTitle legend = chart.getLegend();
////        domain.setRange(0, 100);
//        range.setRange(0, maxRange);
////        chart.getTitle().setFont(titleFont);
//        domain.setLabelFont(axisFont);
//        domain.setTickLabelFont(tickFont);
//        range.setLabelFont(axisFont);
//        range.setTickLabelFont(tickFont);
//        legend.setItemFont(legendFont);
//
//        //        chartPanel = new ChartPanel(chart);
//        //        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
//        //        setContentPane(chartPanel);

        Font titleFont = new Font("Calibri", Font.BOLD, 40);
        Font axisFont = new Font("Calibri", Font.BOLD, 40);
        Font tickFont = new Font("Calibri", Font.PLAIN, 40);
        Font legendFont = new Font("Calibri", Font.PLAIN, 40);
        plot.setRenderer(renderer);
        //        chart.removeLegend();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        LegendTitle legend = chart.getLegend();
        domain.setRange(50, 100);
        range.setRange(0, 50);
        domain.setTickUnit(new NumberTickUnit(25));
        range.setTickUnit(new NumberTickUnit(50));
//        chart.getTitle().setFont(titleFont);
        NumberAxis[] axes = {domain, range};
        for (NumberAxis axis : axes) {
            axis.setLabelFont(axisFont);
            axis.setTickLabelFont(tickFont);
            axis.setTickMarksVisible(true);
            axis.setTickMarksVisible(true);
            axis.setAxisLinePaint(Color.BLACK);

            axis.setAxisLineVisible(true);
            axis.setTickMarkOutsideLength(5);
            axis.setTickMarkPaint(Color.BLACK);
            axis.setTickMarkStroke(new BasicStroke(8.0f));
            axis.setAxisLineStroke(new BasicStroke(6.0f));
        }
        range.setLabelFont(axisFont);
        range.setTickLabelFont(tickFont);
        legend.setItemFont(legendFont);



        int padding = 30;
        chart.setPadding(new RectangleInsets(padding,0,0,padding));
//        chart.getPlot().setBackgroundPaint( Color.WHITE );


        LegendTitle legendTitle = ((JFreeChart)chart).getLegend();
//        LegendTitle newLegend = new LegendTitle(plot, new ColumnArrangement(), new ColumnArrangement());
        LegendTitle newLegend = new LegendTitle(plot);
        newLegend.setPosition(legendTitle.getPosition());
//        newLegend.setPosition(RectangleEdge.LEFT);
        newLegend.setBackgroundPaint(legendTitle.getBackgroundPaint());
        newLegend.setItemLabelPadding(new RectangleInsets(2, 2, 2, 50));
        newLegend.setItemFont(legendFont);
//        newLegend.setFrame(new BlockBorder(Color.BLACK));
        chart.removeLegend();
        chart.addLegend(newLegend);
        chart.getPlot().setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint(null);


        String newChartName = saveDir + "paretograph.png";
        System.out.println("Creating File: " + newChartName);
        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 1000, 800);
//        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 900, 800);
    }

    public void addDataPts(ArrayList<ParetoSolution> solutions) {
        XYSeries series = new XYSeries("Pareto-Optimal");
//        XYSeries series = new XYSeries("31 Posts, P100, G40");
        for (int i = 0; i < solutions.size(); i++) {
            ParetoSolution solution = solutions.get(i);
            System.out.println("File: " + solutions.get(i).modelName + ", Capture Efficiency: " +
                    solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
            series.add(solution.captureEfficiency, solution.entryPressure);
        }
            renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//            renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//            renderer.setSeriesShapesVisible(seriesCounter++, false);
//            renderer.setBaseShapesVisible(false);

        double size = 10.0;
        double delta = size / 2.0;
        Shape shape2 = new Ellipse2D.Double(-delta, -delta, size, size);
//        renderer.setUseOutlinePaint(true);
//        renderer.setDrawOutlines(true);
//        renderer.setBaseOutlinePaint(Color.GRAY);
        renderer.setSeriesShape(0, shape2);
        renderer.setSeriesShape(1, shape2);
        renderer.setSeriesShape(2, shape2);

        renderer.setBaseLinesVisible(false);
        collection.addSeries(series);
    }

    public void addHumanPts(String bestConcTxt, String bestPressureTxt) throws IOException {
        System.out.println("Adding human created");
        XYSeries series = new XYSeries("Human Created");
        addTxtPtsToSeries(series, bestConcTxt, bestPressureTxt);
        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
        renderer.setBaseLinesVisible(false);
        collection.addSeries(series);
    }

    private void addNonParetoPoints() throws IOException {
        File dir = new File(NONPARETO_DIR);
        File[] directoryListing = dir.listFiles();
        XYSeries series = new XYSeries("Non-Pareto");
        String exitConcName = "exitconc.txt";
        String entryPressureName = "entrypressure.txt";

        if (directoryListing != null) {
            for (File item : directoryListing) {
                String filename = item.toString();
                if (filename.contains(exitConcName)) {
                    String pressureFilename = filename.substring(0, filename.length() - exitConcName.length());
                    pressureFilename = pressureFilename + entryPressureName;
                    addTxtPtsToSeries(series, filename, pressureFilename);
                }
            }
        }

        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
        renderer.setBaseLinesVisible(false);
        collection.addSeries(series);
    }

//    private void addParetoSecond() {
//        XYSeries series = new XYSeries("31 Posts, P25, G40");
//
//        for (double[] row : oneKpareto) {
//            series.add(row[0], row[1]);
//        }
//        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//        renderer.setBaseLinesVisible(false);
//        collection.addSeries(series);
//
//
//        XYSeries series2 = new XYSeries("31 Posts, P25, G80");
//
//        for (double[] row : twoKpareto) {
//            series2.add(row[0], row[1]);
//        }
//        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//        renderer.setBaseLinesVisible(false);
//        collection.addSeries(series2);
//    }


    public void addTxtPtsToSeries(XYSeries series, String concTxt, String pressureTxt) throws IOException {
        double[][] concs = getFileDataPts(new BufferedReader(new FileReader(concTxt)));
        double[][] pressures = getFileDataPts(new BufferedReader(new FileReader(pressureTxt)));
//        XYSeries series = new XYSeries("Human Created");
        double pressure = pressures[pressures.length / 2][1];
        if (pressure < 250) {
            series.add(getCaptureEfficiency(concs), pressure);
        }
        System.out.println("File: " + concTxt + " Capture Efficiency: " + getCaptureEfficiency(concs) + ", Pressure: " +
                pressures[pressures.length / 2][1]);
//        collection.addSeries(series);
    }

    private static double getCaptureEfficiency(double[][] concs) {
//        for (double[] row : concs) {
//            System.out.println(row[1]);
//        }
        double result = 0;
        for (int i = 0; i < concs.length; i++) {
            result += concs[i][1];
        }
        result /= (concs.length * CircularPostMixerProblem.INFLOW_CONC_1);
//        System.out.println(result);
        return (1 - result) * 100.0;
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
}

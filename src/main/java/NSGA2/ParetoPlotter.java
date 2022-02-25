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

public class ParetoPlotter extends ApplicationFrame {
    //        private XYSeries[] series;
    private XYSeriesCollection collection;
    //            private XYLineAndShapeRenderer renderer;
    private JFreeChart chart;
    private String saveDir;
    private static final String ROOT_DIR = "D:/COMSOL Research/";
    private static final String[] PARETO_DIRS = {
//            ROOT_DIR + "47) paretosaves/",
//            ROOT_DIR + "48) repeat 47, 2k evals/paretosaves/",
//            ROOT_DIR + "46) paretosaves/"

//            "G:/My Drive/COMSOL runs/from Desktop/49) repeat 42, 100 circles, P50, 2000 evals/paretosaves/",
            "G:/My Drive/COMSOL runs/from Desktop/50) repeat 49, varied circles, 100 max/paretosaves/",
//            "G:/My Drive/COMSOL runs/from Desktop/51) repeat 50, 4000 evals/paretosaves/",
//            "G:/My Drive/COMSOL runs/from Desktop/58) repeat 57/paretosaves/",
            "G:/My Drive/COMSOL runs/from Desktop/67) max 100, 0.2mm squares, P50, E2000, 3_div_vars/paretosaves/",
//            ROOT_DIR + "70) repeat 67, E4k/paretosaves/",
//            ROOT_DIR + "59) repeat 58, 5_div_vars instead of 3/paretosaves/",
//            ROOT_DIR + "60) repeat 58, 10_div_vars instead of 3/paretosaves/",
            "G:/My Drive/COMSOL runs/from Desktop/64) max 100, 0.2mm equilaterals,P50, E2000, 3_div_vars/paretosaves/",
//            ROOT_DIR + "65) repeat 64 4k evals/paretosaves/",
//            ROOT_DIR + "69) repeat 65, 3_div_vars(circles)/paretosaves/",
            "G:/My Drive/COMSOL runs/from Desktop/66) max 100, 0.2mm pinwheels, P50, E2000, 3_div_vars/paretosaves/",
//            "G:/My Drive/COMSOL runs/from Desktop/68) repeat 66, E4k/paretosaves/",
    };
    private static final String[] GRAPH_NAMES = {
//            "S25, G40",
//            "S25, G80",
//            "S100, G50",
//            "100 fixed circles, 2K",
            "<=100 circles, 2K",
//            "<=100 circles, 4k",
//            "<=100 squares, P50, G40, E2K (old)",
            "<=100 squares, 2K",
//            "<=100 squares, 4k",
//            "<=100 squares, P50, G40, E2K, 5/total vars",
//            "<=100 squares, P50, G40, E2K, 10/total vars",
            "<=100 triangles, 2K",
//            "<=100 triangles, 4k",
//            "<=100 triangles, 4K (circle's mut)",
            "<=100 pinwheel, 2K",
//            "<=100 pinwheel, 4k",
    };


    private final Color CB_YELLOW_ORANGE = new Color(230, 159, 0);
    private final Color CB_BLUE = new Color(86, 180, 233);
    private final Color CB_GREEN = new Color(0, 158, 115);
    private final Color CB_ORANGE = new Color(213, 94, 0);
    private final Color CB_PINK = new Color(204, 121, 167);
    private final Color[] colors = {CB_YELLOW_ORANGE, CB_GREEN, CB_BLUE, Color.BLACK, CB_PINK, CB_ORANGE, Color.BLUE,
//            Color.CYAN, Color.GREEN,
            Color.MAGENTA};
    private int colorIter = 0;
    private int seriesCounter = 0;
    private int maxRange = 300;
    private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    private final int CHART_WIDTH = 1000;
    private final int CHART_HEIGHT = 800;
    //there was a bug with run 53 where the wrong values were saved to the array used to create the pareto graphs.
    // However, the values were saved and can be regraphed through this array (also in a saved text file)
    private static double[] run53 = {
            50.54808095403946, 0.4199182461390278,
            73.31407755315480, 2.2515300959751400,
            72.65052311398300, 1.9116886381641400,
            65.19023256383740, 0.9635183656995990,
            69.79447705045550, 1.4194296155388000,
            65.95334400050010, 1.1787478521974900,
            71.92297407389410, 1.6635931762896300,
            68.84388326302040, 1.2512465004294300,
            52.04578723320190, 0.4701817120438510,
            71.00619719292760, 1.5106262018527400,
            67.53496056972360, 1.2011378995139400,
            56.53666613578130, 0.6562255840557370,
            54.27894436546830, 0.4945650796055690,
            57.89203753953990, 0.6774212923424830,
            60.84250923763470, 0.7816012666226200,
            63.35318322852750, 0.9138615765964840,
            59.99164327162840, 0.7306072994707460,
            71.29254705893620, 1.6162575445011000,
            61.71648668678300, 0.8257322890909870,
            58.68303925986840, 0.7265754472465290,
            62.97194944677830, 0.8326133291297550,
            55.98630783460770, 0.5771736743477160,
            51.02388894943680, 0.4385527072474640,
            55.42303213481790, 0.5703542238862550,
            54.56211581741380, 0.5212317342237490
    };

    public static void main(String[] args) throws IOException {
        ParetoPlotter plotter = new ParetoPlotter("Capture Efficiency vs Pressure",ROOT_DIR);

        //run 53 only due to Pareto front file errors
        ArrayList<ParetoSolution> solutions = new ArrayList();
        int count = 1;
//        for (int i = 0; i < run53.length;i+=2) {
//            solutions.add(new ParetoSolution(run53[i], run53[i+1], Integer.toString(count++)));
//        }
//        plotter.addDataPts(solutions, "S25, G40");

        for (int dirPtr = 0; dirPtr < PARETO_DIRS.length; dirPtr++) {
            solutions = new ArrayList();
            boolean foundStart = false;
            for (int i = 900; i <= 5050; i++) {
                String filename = "Pareto_" + i;
                File pressureFile = new File(PARETO_DIRS[dirPtr] + "Pareto_" + i + "_entrypressure.txt");
                if (pressureFile.exists()) {
                    foundStart = true;
                    File concFile = new File(PARETO_DIRS[dirPtr] + "Pareto_" + i + "_exitconc.txt");
//                    System.out.println("got to here");

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
            plotter.addDataPts(solutions, GRAPH_NAMES[dirPtr]);
        }
        plotter.createChart();
    }

    public ParetoPlotter(final String title, ArrayList<ParetoSolution> solutions, String saveDir)
            throws IOException {
        super(title);
        collection = new XYSeriesCollection();
//            series = new XYSeries[numSolutions];
//            renderer = new XYLineAndShapeRenderer();
//        addParetoSecond();
//        addDataPts(solutions);
//        addNonParetoPoints();
//        addHumanPts(HUMAN_RESULT_CONC_FILE, HUMAN_RESULT_PRESSURE_FILE);
        this.saveDir = saveDir;
        File theDir = new File(saveDir);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }

    public ParetoPlotter(final String title, String saveDir) throws IOException {
        super(title);
        collection = new XYSeriesCollection();
//        addNonParetoPoints();
//        addHumanPts(HUMAN_RESULT_CONC_FILE, HUMAN_RESULT_PRESSURE_FILE);
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
                false,
                false,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();

        Font titleFont = new Font("Calibri", Font.BOLD, 40);
        Font axisFont = new Font("Calibri", Font.BOLD, 40);
        Font tickFont = new Font("Calibri", Font.PLAIN, 40);
        Font legendFont = new Font("Calibri", Font.PLAIN, 36);
        plot.setRenderer(renderer);
        //        chart.removeLegend();
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        LegendTitle legend = chart.getLegend();
        domain.setRange(40, 100.5);
//        range.setRange(0, maxRange);
//        range.setRange(0, 750);
        range.setRange(0, 25);
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
//        legend.setItemFont(legendFont);

        int padding = 30;
        chart.setPadding(new RectangleInsets(padding,0,0,padding));
//        chart.getPlot().setBackgroundPaint( Color.WHITE );

        domain.setTickUnit(new NumberTickUnit(20));
//        range.setTickUnit(new NumberTickUnit(250));
        range.setTickUnit(new NumberTickUnit(5));


//        LegendTitle legendTitle = ((JFreeChart)chart).getLegend();
////        LegendTitle newLegend = new LegendTitle(plot, new ColumnArrangement(), new ColumnArrangement());
//        LegendTitle newLegend = new LegendTitle(plot);
//        newLegend.setPosition(legendTitle.getPosition());
////        newLegend.setPosition(RectangleEdge.LEFT);
//        newLegend.setBackgroundPaint(legendTitle.getBackgroundPaint());
//        newLegend.setItemLabelPadding(new RectangleInsets(2, 2, 2, 50));
//        newLegend.setItemFont(legendFont);
////        newLegend.setFrame(new BlockBorder(Color.BLACK));
//        chart.removeLegend();
//        chart.addLegend(newLegend);

        chart.getPlot().setBackgroundPaint( Color.WHITE );
        plot.setOutlinePaint(null);



        //        chartPanel = new ChartPanel(chart);
        //        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
        //        setContentPane(chartPanel);
        String newChartName = saveDir + "paretograph compare.png";
        System.out.println("Creating File: " + newChartName);
//        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, CHART_WIDTH, CHART_HEIGHT);
        ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 700, 400);

    }

    public void addDataPts(ArrayList<ParetoSolution> solutions, String title) {
        XYSeries series = new XYSeries(title);
//        XYSeries series = new XYSeries("31 Posts, P100, G40");
        for (int i = 0; i < solutions.size(); i++) {
            ParetoSolution solution = solutions.get(i);
            System.out.println("File: " + solutions.get(i).modelName + ", Capture Efficiency: " +
                    solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
            series.add(solution.captureEfficiency, solution.entryPressure);
        }
//        renderer.setSeriesLinesVisible(seriesCounter, true);
        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//            renderer.setSeriesShapesVisible(seriesCounter++, false);
//            renderer.setBaseShapesVisible(false);

        double size = 10.0;
        double delta = size / 2.0;
        Shape shape2 = new Ellipse2D.Double(-delta, -delta, size, size);
//        renderer.setUseOutlinePaint(true);
//        renderer.setDrawOutlines(true);
//        renderer.setBaseOutlinePaint(Color.BLACK);
        renderer.setSeriesShape(0, shape2);
        renderer.setSeriesShape(1, shape2);
        renderer.setSeriesShape(2, shape2);
        renderer.setSeriesShape(3, shape2);
        renderer.setSeriesShape(4, shape2);
        renderer.setSeriesShape(5, shape2);

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

//    private void addNonParetoPoints() throws IOException {
//        File dir = new File(NONPARETO_DIR);
//        File[] directoryListing = dir.listFiles();
//        XYSeries series = new XYSeries("Automated Chips");
//        String exitConcName = "exitconc.txt";
//        String entryPressureName = "entrypressure.txt";
//
//        if (directoryListing != null) {
//            for (File item : directoryListing) {
//                String filename = item.toString();
//                if (filename.contains(exitConcName)) {
//                    String pressureFilename = filename.substring(0, filename.length() - exitConcName.length());
//                    pressureFilename = pressureFilename + entryPressureName;
//                    addTxtPtsToSeries(series, filename, pressureFilename);
//                }
//            }
//        }
//
//        renderer.setSeriesPaint(seriesCounter++,colors[colorIter++]);
//        renderer.setBaseLinesVisible(false);
//        collection.addSeries(series);
//    }

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
        series.add(getCaptureEfficiency(concs), pressures[pressures.length / 2][1]);
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

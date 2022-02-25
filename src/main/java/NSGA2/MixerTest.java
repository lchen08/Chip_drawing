package NSGA2;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import com.comsol.model.util.ModelUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.moeaframework.Executor;
import org.moeaframework.core.*;
import org.moeaframework.core.variable.RealVariable;


/**
 * http://moeaframework.org/javadoc/org/moeaframework/core/operator/StandardOperators.html
 */
public class MixerTest {
    public static TSimpleChip mixer = new TSimpleChip(CircularPostMixerProblem.INFLOW_CONC_1,
            CircularPostMixerProblem.INFLOW_CONC_2,
            CircularPostMixerProblem.MAIN_CHANNEL_DIM,
            CircularPostMixerProblem.CROSS_CHANNEL_DIM);

    private static final int NUM_CIRCLES = CircularPostMixerProblem.getNumCircles();
    private static int circleTotalVars = CircularPostMixerProblem.TOTAL_VARS;
    private static int rectTotalVars = RectanglePostMixerProblem.TOTAL_VARS;
    private static int triangleTotalVars = TrianglePostProblem.TOTAL_VARS;
    private static int pinwheelTotalVars = PinwheelPostProblem.TOTAL_VARS;
    //change this to the appropriate problem class
    private static final int UM_RATE = RectanglePostMixerProblem.UM_RATE;
    private static final int UX_RATE = RectanglePostMixerProblem.UX_RATE;

    private static final int COARSE_MESH = 9;
    private static final int NORMAL_MESH = 5;
    private static final int FINE_MESH = 1;
    private static int meshSize = COARSE_MESH;

    static double problemRuntime = 0;
//    static private final double FLOW_RATE = 0.03;


    public static void main(String[] args) throws IOException {
//        runCircleProblem();
        runRectProblem();
//        runTriangleProblem();
//        runPinwheelProblem();

//        ArrayList<ChipDesign> coarseDesigns = CircularPostMixerProblem.designs;
//        ArrayList<ChipDesign> normalDesigns = runDesignsWithNewMesh(NORMAL_MESH, coarseDesigns);
//        ArrayList<ChipDesign> fineDesigns = runDesignsWithNewMesh(FINE_MESH, coarseDesigns);
//        ArrayList<ChipDesign> coarsePareto = getPareto(coarseDesigns);
//        ArrayList<ChipDesign> normalPareto = getPareto(normalDesigns);
//        ArrayList<ChipDesign> finePareto = getPareto(fineDesigns);
//
////        System.out.println("Check design sizes: " + coarseDesigns.size() + " " + normalDesigns.size() + " " +
////                fineDesigns.size() + "\n");
//        for (int i = 0; i < coarseDesigns.size(); i++) {
//            System.out.println("Design " + i);
//            System.out.println(coarseDesigns.get(i).filename);
//            System.out.println("Coarse - Capture: " + coarseDesigns.get(i).concObj + "%, Pressure: " +
//                    coarseDesigns.get(i).pressure + " Runtime (ms): " + coarseDesigns.get(i).runtime);
//            System.out.println(normalDesigns.get(i).filename);
//            System.out.println("Normal - Capture: " + normalDesigns.get(i).concObj + "%, Pressure: " +
//                    normalDesigns.get(i).pressure + " Runtime (ms): " + normalDesigns.get(i).runtime);
//            System.out.println(fineDesigns.get(i).filename);
//            System.out.println("Fine - Capture: " + fineDesigns.get(i).concObj + "%, Pressure: " +
//                    fineDesigns.get(i).pressure + " Runtime (ms): " + fineDesigns.get(i).runtime);
//        }
//
//        System.out.println("\nCoarse Pareto Size: " + coarsePareto.size());
//        for (int paretoIter = 0; paretoIter < coarsePareto.size(); paretoIter++) {
//            System.out.println(coarsePareto.get(paretoIter).filename + "\nCapture: " +
//                    coarsePareto.get(paretoIter).concObj + "%, Pressure: " + coarsePareto.get(paretoIter).pressure);
//        }
//        System.out.println("\nNormal Pareto Size: " + normalPareto.size());
//        for (int paretoIter = 0; paretoIter < normalPareto.size(); paretoIter++) {
//            System.out.println(normalPareto.get(paretoIter).filename + "\nCapture: " +
//                    normalPareto.get(paretoIter).concObj + "%, Pressure: " + normalPareto.get(paretoIter).pressure);
//        }
//        System.out.println("\nFine Pareto Size: " + finePareto.size());
//        for (int paretoIter = 0; paretoIter < finePareto.size(); paretoIter++) {
//            System.out.println(finePareto.get(paretoIter).filename + "\nCapture: " +
//                    finePareto.get(paretoIter).concObj + "%, Pressure: " + finePareto.get(paretoIter).pressure);
//        }
//
//        System.out.println("\n");
//        System.out.println("NSGAII completed.");
        ModelUtil.closeWindows();
        ModelUtil.clear();
        ModelUtil.disconnect();
        System.exit(0);
    }

    public static void runPinwheelProblem() throws IOException {
        Instant begin = Instant.now();
        mixer.init();
        mixer.doFISRBaseSetup(PinwheelPostProblem.STOICHIOMETRIC_COEFF);

        //runs NSGAII algorithm with given parameters
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(PinwheelPostProblem.class)
                .withMaxEvaluations(PinwheelPostProblem.TOTAL_EVALS)
                .withProperty("um.rate", UM_RATE) //uniform mutation
                .withProperty("ux.rate", UX_RATE) //uniform crossover
                .withProperty("populationSize", PinwheelPostProblem.POPULATION_SIZE)
                .run();

        Instant end = Instant.now();
//        final long endTime = System.currentTimeMillis();
        long duration = (long) (Duration.between(begin, end).toMillis() / 1000.0);

        BufferedWriter runtimeWriter = new BufferedWriter(new FileWriter(mixer.getExportDir() + "/runtime.txt"));
        runtimeWriter.write("\nTotal Stats:\n");
        runtimeWriter.write("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration
                + "\n");
        runtimeWriter.write("COMSOL time (ms): " + mixer.getTotalTime() + "\n");
        runtimeWriter.write("NSGAII time (ms):" + problemRuntime + "\n");
        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(result));
        ArrayList<ParetoSolution> solutions = new ArrayList();
        final String SAVE_DIR = mixer.getExportDir() + "paretosaves/";
        int solutionsIter = 0;

        File paretoDir = new File(SAVE_DIR);
        if (!paretoDir.exists()) {
            paretoDir.mkdirs();
        }

        if (getNumValidSolutions(result) != 0) {
            for (Solution solution : result) {
                ArrayList<double[]> postArrayList = new ArrayList<>();
                if (!solution.violatesConstraints()) {
                    System.out.println("Solution " + solutionsIter);

                    int postIndex = 0;

                    //population pinwheel array
                    for (int j = 0; j < pinwheelTotalVars; ) {
                        if (Double.parseDouble(solution.getVariable(j+(PinwheelPostProblem.VARS_FOR_NSGAII - 1)).toString()) >
                                PinwheelPostProblem.POST_ACCEPTANCE_CRITERIA) {
                            double posX = Double.parseDouble(solution.getVariable(j++).toString());
                            double posY = Double.parseDouble(solution.getVariable(j++).toString());
                            double rotation = Double.parseDouble(solution.getVariable(j++).toString());
                            System.out.println("Post " + postIndex++ + "(" + posX + ", " + posY + " rotation: " + rotation);
                            double[] postRow = {posX, posY, PinwheelPostProblem.RECT_WIDTH,
                                    PinwheelPostProblem.RECT_LENGTH, PinwheelPostProblem.RADIUS, rotation};
                            postArrayList.add(postRow);
                            j++;
                        }
                        else {
                            j = j + PinwheelPostProblem.VARS_FOR_NSGAII;
                        }
                    }
                    double[][] postInfo = new double[postArrayList.size()][PinwheelPostProblem.NUM_POST_VARS];
                    for (int post = 0; post < postArrayList.size(); post++) {
                        postInfo[post] = postArrayList.get(post);
                    }

//                    double flowRate = Double.parseDouble(solution.getVariable(CircularPostMixerProblem.getFlowRateVarIndex()).
//                            toString());
//                    System.out.println("Best chip's flow rate (m/s): " + flowRate);

//                    System.out.println("
////                    All Pass?: " + Test2.check(circlesInfo, flowRate));
//                    createParetoFiles(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR, SAVE_DIR, solutionsIter++);

                    createPinwheelParetoFiles(postInfo, RectanglePostMixerProblem.MIN_FLOW_RATE,
                            RectanglePostMixerProblem.HAS_FISR);
//                    mixer.start(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR);

                    solutions.add(new ParetoSolution(
                            -solution.getObjective(PinwheelPostProblem.CONC_OBJECTIVE_INDEX),
                            solution.getObjective(PinwheelPostProblem.PRESSURE_OBJECTIVE_INDEX),
                            mixer.getLastModelName())
                    );
                }
            }
            SolutionPlotter plotter = new SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);

            runtimeWriter.write("\n\n");
            for (ParetoSolution solution : solutions) {
                System.out.println("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
                runtimeWriter.write("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure + "\n");
            }

            runtimeWriter.close();
            plotter.createChart();
        }
    }

    public static void runTriangleProblem() throws IOException {
        Instant begin = Instant.now();
        mixer.init();
        mixer.doFISRBaseSetup(TrianglePostProblem.STOICHIOMETRIC_COEFF);

        //runs NSGAII algorithm with given parameters
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(TrianglePostProblem.class)
                .withMaxEvaluations(TrianglePostProblem.TOTAL_EVALS)
                .withProperty("um.rate", UM_RATE) //uniform mutation
                .withProperty("ux.rate", UX_RATE) //uniform crossover
                .withProperty("populationSize", TrianglePostProblem.POPULATION_SIZE)
                .run();

        Instant end = Instant.now();
        long duration = (long) (Duration.between(begin, end).toMillis() / 1000.0);

        BufferedWriter runtimeWriter = new BufferedWriter(new FileWriter(mixer.getExportDir() + "/runtime.txt"));
        runtimeWriter.write("\nTotal Stats:\n");
        runtimeWriter.write("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration
                + "\n");
        runtimeWriter.write("COMSOL time (ms): " + mixer.getTotalTime() + "\n");
        runtimeWriter.write("NSGAII time (ms):" + problemRuntime + "\n");
        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(result));
        ArrayList<ParetoSolution> solutions = new ArrayList();
        final String SAVE_DIR = mixer.getExportDir() + "paretosaves/";
        int solutionsIter = 0;

        File paretoDir = new File(SAVE_DIR);
        if (!paretoDir.exists()) {
            paretoDir.mkdirs();
        }

        if (getNumValidSolutions(result) != 0) {
            for (Solution solution : result) {
                ArrayList<double[]> triangleArrayList = new ArrayList<>();
                if (!solution.violatesConstraints()) {
                    System.out.println("Solution " + solutionsIter);

                    int triangleIndex = 0;

                    //population triangle array
                    for (int j = 0; j < triangleTotalVars; ) {
                        if (Double.parseDouble(solution.getVariable(
                                j+TrianglePostProblem.TRIANGLE_VARS_FOR_NSGAII - 1).toString()) >
                                TrianglePostProblem.TRIANGLE_ACCEPTANCE_CRITERIA) {
                            double x1 = Double.parseDouble(solution.getVariable(j++).toString());
                            double y1 = Double.parseDouble(solution.getVariable(j++).toString());
                            double rotation = Double.parseDouble(solution.getVariable(j++).toString());
                            System.out.println("Triangle origin " + triangleIndex++ + "(" + x1 + ", " + y1 + " rotation: "
                                    + rotation);
                            final double equilateralAngle = Math.PI/3;
                            double x2 = x1 + TrianglePostProblem.MIN_WIDTH * Math.cos(rotation);
                            double y2 = y1 + TrianglePostProblem.MIN_WIDTH * Math.sin(rotation);
                            double x3 = x1 + TrianglePostProblem.MIN_WIDTH * Math.cos(rotation + equilateralAngle);
                            double y3 = y1 + TrianglePostProblem.MIN_WIDTH * Math.sin(rotation + equilateralAngle);
                            double[] triangleRow = {x1, y1, x2, y2, x3, y3};
                            triangleArrayList.add(triangleRow);
                            j++;
                        }
                        else {
                            j = j + TrianglePostProblem.TRIANGLE_VARS_FOR_NSGAII;
                        }
                    }
                    double[][] triangleInfo = new double[triangleArrayList.size()][TrianglePostProblem.NUM_TRIANGLE_VARS];
                    for (int triangle = 0; triangle < triangleArrayList.size(); triangle++) {
                        triangleInfo[triangle] = triangleArrayList.get(triangle);
                    }

//                    double flowRate = Double.parseDouble(solution.getVariable(CircularPostMixerProblem.getFlowRateVarIndex()).
//                            toString());
//                    System.out.println("Best chip's flow rate (m/s): " + flowRate);

//                    System.out.println("
////                    All Pass?: " + Test2.check(circlesInfo, flowRate));
//                    createParetoFiles(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR, SAVE_DIR, solutionsIter++);

                    createTriangleParetoFiles(triangleInfo, TrianglePostProblem.MIN_FLOW_RATE,
                            TrianglePostProblem.HAS_FISR);
//                    mixer.start(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR);

                    solutions.add(new ParetoSolution(
                            -solution.getObjective(TrianglePostProblem.CONC_OBJECTIVE_INDEX),
                            solution.getObjective(TrianglePostProblem.PRESSURE_OBJECTIVE_INDEX),
                            mixer.getLastModelName())
                    );
                }
            }
            SolutionPlotter plotter = new SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);

            runtimeWriter.write("\n\n");
            for (ParetoSolution solution : solutions) {
                System.out.println("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
                runtimeWriter.write("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure + "\n");
            }
            runtimeWriter.close();
            plotter.createChart();
        }
    }

    public static void runRectProblem() throws IOException {
        Instant begin = Instant.now();
        mixer.init();
        mixer.doFISRBaseSetup(RectanglePostMixerProblem.STOICHIOMETRIC_COEFF);

        //runs NSGAII algorithm with given parameters
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(RectanglePostMixerProblem.class)
                .withMaxEvaluations(RectanglePostMixerProblem.TOTAL_EVALS)
                .withProperty("um.rate", UM_RATE) //uniform mutation
                .withProperty("ux.rate", UX_RATE) //uniform crossover
                .withProperty("populationSize", RectanglePostMixerProblem.POPULATION_SIZE)
                .run();

        Instant end = Instant.now();
//        final long endTime = System.currentTimeMillis();
        long duration = (long) (Duration.between(begin, end).toMillis() / 1000.0);

        BufferedWriter runtimeWriter = new BufferedWriter(new FileWriter(mixer.getExportDir() + "/runtime.txt"));
        runtimeWriter.write("\nTotal Stats:\n");
        runtimeWriter.write("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration
                + "\n");
        runtimeWriter.write("COMSOL time (ms): " + mixer.getTotalTime() + "\n");
        runtimeWriter.write("NSGAII time (ms):" + problemRuntime + "\n");
        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(result));
        ArrayList<ParetoSolution> solutions = new ArrayList();
        final String SAVE_DIR = mixer.getExportDir() + "paretosaves/";
        int solutionsIter = 0;

        File paretoDir = new File(SAVE_DIR);
        if (!paretoDir.exists()) {
            paretoDir.mkdirs();
        }

        if (getNumValidSolutions(result) != 0) {
            for (Solution solution : result) {
                ArrayList<double[]> rectArrayList = new ArrayList<>();
                if (!solution.violatesConstraints()) {
                    System.out.println("Solution " + solutionsIter);

                    int rectIndex = 0;

                    //population rect array
                    for (int j = 0; j < rectTotalVars; ) {
                        if (Double.parseDouble(solution.getVariable(j+3).toString()) >
                                RectanglePostMixerProblem.RECT_ACCEPTANCE_CRITERIA) {
                            double posX = Double.parseDouble(solution.getVariable(j++).toString());
                            double posY = Double.parseDouble(solution.getVariable(j++).toString());
                            double rotation = Double.parseDouble(solution.getVariable(j++).toString());
                            System.out.println("Rectangle " + rectIndex++ + "(" + posX + ", " + posY + " rotation: " + rotation);
                            double[] rectRow = {posX, posY, RectanglePostMixerProblem.MIN_WIDTH,
                                    RectanglePostMixerProblem.MIN_WIDTH,rotation};
                            rectArrayList.add(rectRow);
                            j++;
                        }
                        else {
                            j = j + 4;
                        }
                    }
                    double[][] rectInfo = new double[rectArrayList.size()][RectanglePostMixerProblem.NUM_RECT_VARS];
                    for (int rect = 0; rect < rectArrayList.size(); rect++) {
                        rectInfo[rect] = rectArrayList.get(rect);
                    }

//                    double flowRate = Double.parseDouble(solution.getVariable(CircularPostMixerProblem.getFlowRateVarIndex()).
//                            toString());
//                    System.out.println("Best chip's flow rate (m/s): " + flowRate);

//                    System.out.println("
////                    All Pass?: " + Test2.check(circlesInfo, flowRate));
//                    createParetoFiles(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR, SAVE_DIR, solutionsIter++);

                    createRectParetoFiles(rectInfo, RectanglePostMixerProblem.MIN_FLOW_RATE,
                            RectanglePostMixerProblem.HAS_FISR);
//                    mixer.start(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR);

                    solutions.add(new ParetoSolution(
                            -solution.getObjective(RectanglePostMixerProblem.CONC_OBJECTIVE_INDEX),
                            solution.getObjective(RectanglePostMixerProblem.PRESSURE_OBJECTIVE_INDEX),
                            mixer.getLastModelName())
                    );
                }
            }
            SolutionPlotter plotter = new SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);

            runtimeWriter.write("\n\n");
            for (ParetoSolution solution : solutions) {
                System.out.println("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
                runtimeWriter.write("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure + "\n");
            }

            runtimeWriter.close();
            plotter.createChart();
        }
    }

    public static void runCircleProblem() throws IOException {
        Instant begin = Instant.now();
//        final long startTime = System.currentTimeMillis();
        mixer.init();
//        mixer.doMixerBaseSetup();
        mixer.doFISRBaseSetup(CircularPostMixerProblem.STOICHIOMETRIC_COEFF);

        //runs NSGAII algorithm with given parameters
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(CircularPostMixerProblem.class)
                .withMaxEvaluations(CircularPostMixerProblem.TOTAL_EVALS)
                .withProperty("um.rate", CircularPostMixerProblem.UM_RATE) //uniform mutation
                .withProperty("ux.rate", CircularPostMixerProblem.UX_RATE) //uniform crossover
                .withProperty("populationSize", CircularPostMixerProblem.POPULATION_SIZE)
                .run();
//        NondominatedPopulation result = evaluateInjectPopulation();

        Instant end = Instant.now();
//        final long endTime = System.currentTimeMillis();
        long duration = (long) (Duration.between(begin, end).toMillis() / 1000.0);

        BufferedWriter runtimeWriter = new BufferedWriter(new FileWriter(mixer.getExportDir() + "/runtime.txt"));
        runtimeWriter.write("\nTotal Stats:\n");
        runtimeWriter.write("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration
                + "\n");
        runtimeWriter.write("COMSOL time (ms): " + mixer.getTotalTime() + "\n");
        runtimeWriter.write("NSGAII time (ms):" + problemRuntime + "\n");
        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(result));
        ArrayList<ParetoSolution> solutions = new ArrayList();
        final String SAVE_DIR = mixer.getExportDir() + "paretosaves/";
        int solutionsIter = 0;
//        double[][] circlesInfo = new double[NUM_CIRCLES][NUM_CIRCLE_VARS];

        File paretoDir = new File(SAVE_DIR);
        if (!paretoDir.exists()) {
            paretoDir.mkdirs();
        }

        if (getNumValidSolutions(result) != 0) {
            for (Solution solution : result) {
                ArrayList<double[]> circlesArrayList = new ArrayList<>();
                if (!solution.violatesConstraints()) {
                    System.out.println("Solution " + solutionsIter);

                    int circIndex = 0;

                    //population circles array
                    for (int j = 0; j < circleTotalVars; ) {
                        if (Double.parseDouble(solution.getVariable(j+2).toString()) >
                                CircularPostMixerProblem.CIRCLE_ACCEPTANCE_CRITERIA) {
                            double posX = Double.parseDouble(solution.getVariable(j++).toString());
//                        double posY = Double.parseDouble(solution.getVariable(j++).toString());
                            double posY = Double.parseDouble(solution.getVariable(j++).toString());
                            //TODO fix when radius changes
//                        double radius = Double.parseDouble(solution.getVariable(j++).toString());
                            double radius = CircularPostMixerProblem.MIN_RADIUS;
                            System.out.println("Circle " + circIndex++ + "(" + posX + ", " + posY + " radius: " + radius);
                            double[] circleRow = {posX, posY, radius};
                            circlesArrayList.add(circleRow);
//                            circlesInfo[circIndex++] = circleRow;
                            j++;
                        }
                        else {
                            j = j + 3;
                        }
                    }
                    double[][] circlesInfo = new double[circlesArrayList.size()][CircularPostMixerProblem.NUM_CIRCLE_VARS];
                    for (int circle = 0; circle < circlesArrayList.size(); circle++) {
                        circlesInfo[circle] = circlesArrayList.get(circle);
                    }

//                    double flowRate = Double.parseDouble(solution.getVariable(CircularPostMixerProblem.getFlowRateVarIndex()).
//                            toString());
//                    System.out.println("Best chip's flow rate (m/s): " + flowRate);

//                    System.out.println("
////                    All Pass?: " + Test2.check(circlesInfo, flowRate));
//                    createParetoFiles(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR, SAVE_DIR, solutionsIter++);
                    createParetoFiles(circlesInfo, CircularPostMixerProblem.MIN_FLOW_RATE,
                            CircularPostMixerProblem.HAS_FISR);
//                    mixer.start(circlesInfo, flowRate, CircularPostMixerProblem.HAS_FISR);

//                    renameBestFiles();
                    System.out.println("Got to ParetoSolution section");
                    solutions.add(new ParetoSolution(
                            -solution.getObjective(CircularPostMixerProblem.CONC_OBJECTIVE_INDEX),
                            solution.getObjective(CircularPostMixerProblem.PRESSURE_OBJECTIVE_INDEX),
                            mixer.getLastModelName())
                    );
                    System.out.println("Finished creating ParetoSolution section");
                }
            }
            runtimeWriter.write("\n\n");
            SolutionPlotter plotter = new SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);
            for (ParetoSolution solution : solutions) {
                System.out.println("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
                runtimeWriter.write("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure + "\n");
            }
            System.out.println("Finished creating plotter ParetoSolution section");
            runtimeWriter.close();
            plotter.createChart();
        }
    }

    public static ArrayList<ChipDesign> getPareto(ArrayList<ChipDesign> designs) {
        ArrayList<ChipDesign> results = new ArrayList<>();
        for (int design = 0; design < designs.size(); design++) {
            ChipDesign toAdd = designs.get(design);
            if (results.size() == 0) {
                results.add(toAdd);
            }
            else {
                for (int newPos = 0; newPos < results.size(); newPos++) {
                    ChipDesign currentDesign = results.get(newPos);
                    if(toAdd.concObj <= currentDesign.concObj) {
                        if(toAdd.pressure < currentDesign.pressure) {
                            if(newPos == 0) {
                                results.add(newPos, toAdd);
                                newPos = results.size();
                            }
                            else {
                                boolean didRemoval = false;
                                results.add(newPos, toAdd);
                                for (int checkIter = newPos - 1; checkIter >= 0; checkIter--) {
                                    ChipDesign prevDesign = results.get(checkIter);
                                    if(toAdd.pressure <= prevDesign.pressure) {
                                        results.remove(checkIter);
                                        didRemoval = true;
                                    }
                                    else if (didRemoval == true) {
                                        checkIter = -1;
                                    }

                                }
                                newPos = results.size();
                            }
                        }
                        else {
                            newPos = results.size();
                        }
                    }
                    else if(newPos == results.size() - 1) {
                        boolean didRemoval = false;
                        for (int checkIter = newPos; checkIter >= 0; checkIter--) {
                            ChipDesign prevDesign = results.get(checkIter);
                            if(toAdd.pressure <= prevDesign.pressure) {
                                results.remove(checkIter);
                                didRemoval = true;
                            }
                            else if (didRemoval == true) {
                                checkIter = -1;
                            }

                        }
                        results.add(toAdd);
                    }
                }
            }
        }
        return results;
    }

    public static ArrayList<ChipDesign> runDesignsWithNewMesh(int meshSize, ArrayList<ChipDesign> designs) throws IOException {
        mixer.resetFileIter();
        ArrayList<ChipDesign> newDesigns = new ArrayList<>();

        for (int designIter = 0; designIter < designs.size(); designIter++) {
            ChipDesign design = new ChipDesign(designs.get(designIter).postInfo);
            int popNumber = designIter / CircularPostMixerProblem.POPULATION_SIZE + 1;
            mixer.setFilenamePrefix("Mesh" + meshSize + "_P" + popNumber + "_");

            Instant startMixerTime = Instant.now();
            try {
                mixer.start(designs.get(designIter).postInfo, CircularPostMixerProblem.flowRate,
                        CircularPostMixerProblem.HAS_FISR, meshSize);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Instant endMixerTime = Instant.now();

            if (!mixer.getErrorStatus()) {
                design.concObj = CircularPostMixerProblem.getCaptureEfficiency(null);
                design.pressure = CircularPostMixerProblem.getMidPressureValue(null);

            }
            else {
                design.concObj = CircularPostMixerProblem.getCaptureEfficiency(mixer.getConc());
                design.pressure = CircularPostMixerProblem.getMidPressureValue(mixer.getPressure());
            }
            design.runtime = Duration.between(startMixerTime, endMixerTime).toMillis();
            design.mesh = meshSize;
            design.filename = mixer.getLastModelName();
            newDesigns.add(design);
        }
        return newDesigns;
    }

    /**
     * Simply recreates the pareto files only with the Pareto name with the given circular post data.
     * @param circlesInfo
     * @param flowRate
     * @param hasFISR
     * @throws IOException
     */
    public static void createParetoFiles(double[][] circlesInfo, double flowRate, boolean hasFISR)
            throws IOException {
        MixerTest.mixer.setFilenamePrefix("Pareto" + "_");
        mixer.start(circlesInfo, flowRate, hasFISR, meshSize);
        System.out.println("Pareto File created: " + mixer.getLastModelName());
    }

    /**
     * Simple recreates the Pareto files only with the Pareto name with the given rectangular post data.
     * @param rectInfo
     * @param flowRate
     * @param hasFISR
     * @throws IOException
     */
    public static void createRectParetoFiles(double[][] rectInfo, double flowRate, boolean hasFISR)
            throws IOException {
        MixerTest.mixer.setFilenamePrefix("Pareto" + "_");
        mixer.startRect(rectInfo, flowRate, hasFISR);

        System.out.println("Pareto File created: " + mixer.getLastModelName());
    }

    /**
     * Simple recreates the Pareto files only with the Pareto name with the given triangular post data.
     * @param triangleInfo
     * @param flowRate
     * @param hasFISR
     * @throws IOException
     */
    public static void createTriangleParetoFiles(double[][] triangleInfo, double flowRate, boolean hasFISR)
            throws IOException {
        MixerTest.mixer.setFilenamePrefix("Pareto" + "_");
        mixer.startTriangle(triangleInfo, flowRate, hasFISR);

        System.out.println("Pareto File created: " + mixer.getLastModelName());
    }

    /**
     * Simple recreates the Pareto files only with the Pareto name with the given pinwheel post data.
     * @param postInfo
     * @param flowRate
     * @param hasFISR
     * @throws IOException
     */
    public static void createPinwheelParetoFiles(double[][] postInfo, double flowRate, boolean hasFISR)
            throws IOException {
        MixerTest.mixer.setFilenamePrefix("Pareto" + "_");
        mixer.startPinwheel(postInfo, flowRate, hasFISR);

        System.out.println("Pareto File created: " + mixer.getLastModelName());
    }

    private static int getNumValidSolutions(NondominatedPopulation result) {
        Solution solution = result.get(0);
        return solution.violatesConstraints() ? 0 : result.size();
    }

    static class SolutionPlotter extends ApplicationFrame {
        private XYSeriesCollection collection;
        private JFreeChart chart;
        private String saveDir;
        private final String BEST_DIR = "D:\\COMSOL Research\\35) test\\more human\\101 posts original spacing\\";
        private final String HUMAN_RESULT_CONC_FILE = BEST_DIR + "Posts101_exitconc.txt";
        private final String HUMAN_RESULT_PRESSURE_FILE = BEST_DIR + "Posts101_entrypressure.txt";


        public SolutionPlotter(final String title, ArrayList<ParetoSolution> solutions, String saveDir)
                throws IOException {
            super(title);
            collection = new XYSeriesCollection();
            addDataPts(solutions);
            addHumanPts(HUMAN_RESULT_CONC_FILE, HUMAN_RESULT_PRESSURE_FILE);
            this.saveDir = saveDir;
        }

        public void createChart() throws IOException {
            chart = ChartFactory.createScatterPlot(
                    "Capture Efficiency vs Pressure",
                    "Capture Efficiency",
                    "Pressure",
                    collection,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false
            );
            XYPlot plot = (XYPlot) chart.getPlot();
            String newChartName = saveDir + "paretograph.png";
            System.out.println("Creating File: " + newChartName);
            ChartUtilities.saveChartAsPNG(new File(newChartName), chart, 1000, 800);
        }

        public void addDataPts(ArrayList<ParetoSolution> solutions) {
            XYSeries series = new XYSeries("Pareto Front's plots");
            for (int i = 0; i < solutions.size(); i++) {
                ParetoSolution solution = solutions.get(i);
                series.add(solution.captureEfficiency, solution.entryPressure);
            }
            collection.addSeries(series);
        }

        /**
         * Adds the human data point using the concentration and pressure data files associated with the chip.
         * @param bestConcTxt
         * @param bestPressureTxt
         * @throws IOException
         */
        public void addHumanPts(String bestConcTxt, String bestPressureTxt) throws IOException {
            double[][] concs = getFileDataPts(new BufferedReader(new FileReader(bestConcTxt)));
            double[][] pressures = getFileDataPts(new BufferedReader(new FileReader(bestPressureTxt)));
            XYSeries series = new XYSeries("Human Generated");
            double capture = getCaptureEfficiency(concs);
            double pressure = pressures[pressures.length / 2][1];
            series.add(capture, pressure);
            System.out.println("Human created with no spaces: capture efficiency: " + capture + ", pressure: " +
                    pressure);
            collection.addSeries(series);
        }

        /**
         * Calculates the capture efficiency with a given list of concentrations. The concentration values are in the
         * 2nd index of the 2D array.
         * @param concs
         * @return
         */
        private double getCaptureEfficiency(double[][] concs) {
            double result = 0;
            for (int i = 0; i < concs.length; i++) {
                result += concs[i][1];
            }
            result /= (concs.length * CircularPostMixerProblem.INFLOW_CONC_1);
            return (1 - result) * 100.0;
        }

        /**
         * Gets all the file data points from a file and puts it in an array.
         * @param br
         * @return
         * @throws IOException
         */
        private double[][] getFileDataPts(BufferedReader br) throws IOException {
            ArrayList<double[]> data = new ArrayList<>();
            String s = br.readLine();

            while (s != null) {
                String[] split = s.split("\\s+");
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
}
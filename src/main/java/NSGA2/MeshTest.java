package NSGA2;

import NSGA2.CircularPostMeshProblem;
import com.comsol.model.util.ModelUtil;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.*;
import org.moeaframework.core.operator.real.UM;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MeshTest {
    private static CircularPostMeshProblem problem = new CircularPostMeshProblem();
    private static Initialization initialization;
    private static TournamentSelection selection = new TournamentSelection(2, new ChainedComparator(
            new ParetoDominanceComparator(), new CrowdingComparator()));
    private static final int UM_RATE = problem.UM_RATE;
    private static final int UX_RATE = problem.UX_RATE;
    private static int circleTotalVars = problem.TOTAL_VARS;
    private static Variation variation = new GAVariation(
            new UM(UM_RATE),
            new UniformCrossover(UX_RATE));
    private static NSGAII algorithm = new NSGAII(
            problem,
            new NondominatedSortingPopulation(),
            null, // no archive
            selection,
            variation,
            initialization);
    static TSimpleMixer mixer = new TSimpleMixer(problem.INFLOW_CONC_1,
            problem.INFLOW_CONC_2,
            problem.MAIN_CHANNEL_DIM,
            problem.CROSS_CHANNEL_DIM);
    static double problemRuntime = 0;
    private static int genNumber = 0;


    public static void main(String[] args) throws IOException {
        Instant begin = Instant.now();
        mixer.init();
        mixer.doFISRBaseSetup(problem.STOICHIOMETRIC_COEFF);

        System.out.println("Got to here 47");
        mixer.setFilenamePrefix("G" + genNumber++ + "_");
        List<Solution> initialPop = getInitialPop();
        mixer.setFilenamePrefix("G" + genNumber + "_");
        System.out.println("Got to here 52");
        NondominatedPopulation run = getRunWithPredefined(initialPop);
        System.out.println("Got to here 54");
        for (int i = 0; i < problem.NUM_GENERATIONS; i++) {
            System.out.println("Got to here 56");
            List<Solution> result = new ArrayList<Solution>();
            run.forEach(result::add);
            run = getRunWithPredefined(result);
        }
        Instant end = Instant.now();

        long duration = (long) (Duration.between(begin, end).toMillis() / 1000.0);

        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(run));
        ArrayList<ParetoSolution> solutions = new ArrayList();
        final String SAVE_DIR = mixer.getExportDir() + "paretosaves/";
        int solutionsIter = 0;
//        double[][] circlesInfo = new double[NUM_CIRCLES][NUM_CIRCLE_VARS];

        File paretoDir = new File(SAVE_DIR);
        if (!paretoDir.exists()) {
            paretoDir.mkdirs();
        }

        if (getNumValidSolutions(run) != 0) {
            for (Solution solution : run) {
                ArrayList<double[]> circlesArrayList = new ArrayList<>();
                if (!solution.violatesConstraints()) {
                    System.out.println("Solution " + solutionsIter);

                    int circIndex = 0;

                    //population circles array
                    for (int j = 0; j < circleTotalVars; ) {
                        if (Double.parseDouble(solution.getVariable(j + 2).toString()) >
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
                        } else {
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
                            CircularPostMixerProblem.HAS_FISR, SAVE_DIR, solutionsIter++);
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
            MixerTest.SolutionPlotter plotter = new MixerTest.SolutionPlotter("Capture vs Pressure", solutions, SAVE_DIR);
            for (ParetoSolution solution : solutions) {
                System.out.println("Filename: " + solution.modelName + ", Capture Efficiency: " +
                        solution.captureEfficiency + ", Pressure: " + solution.entryPressure);
            }
            System.out.println("Finished creating plotter ParetoSolution section");

            plotter.createChart();
        }
        System.out.println("\n");
        System.out.println("NSGAII completed.");
        ModelUtil.closeWindows();
        ModelUtil.clear();
        ModelUtil.disconnect();
        System.exit(0);
    }

    private static int getNumValidSolutions(NondominatedPopulation result) {
        Solution solution = result.get(0);
        return solution.violatesConstraints() ? 0 : result.size();
    }

    public static void createParetoFiles(double[][] circlesInfo, double flowRate, boolean hasFISR, String saveDir,
                                         int paretoNum)
            throws IOException {
        MixerTest.mixer.setFilenamePrefix("Pareto" + "_");
        mixer.start(circlesInfo, flowRate, hasFISR);

        System.out.println("Pareto File created: " + mixer.getLastModelName());
    }


    public static NondominatedPopulation getRunWithPredefined(List<Solution> initialPop) {
//        List<Solution> result = new ArrayList<Solution>();
        initialPop = problem.adjustMesh(initialPop);

        initialization = new InjectedInitialization(
                problem,
                CircularPostMixerProblem.POPULATION_SIZE,
                initialPop);

        System.out.println("Got to here 169");

//        algorithm = new NSGAII(
//                problem,
//                new NondominatedSortingPopulation(),
//                null, // no archive
//                selection,
//                variation,
//                initialization);

        while (algorithm.getNumberOfEvaluations() < problem.TOTAL_EVALS) {
            algorithm.step();
        }

//        algorithm.getResult().forEach(result::add);
        return algorithm.getResult();
    }

    public static List<Solution> getInitialPop() {
        List<Solution> result = new ArrayList<Solution>();
        final int POPULATION_SIZE = CircularPostMixerProblem.POPULATION_SIZE;

        System.out.println("Got to here 191");
        initialization = new RandomInitialization(
                problem,
                POPULATION_SIZE);

        algorithm = new NSGAII(
                problem,
                new NondominatedSortingPopulation(),
                null, // no archive
                selection,
                variation,
                initialization);


        System.out.println(algorithm.getNumberOfEvaluations());
        System.out.println("Pop Size: " + POPULATION_SIZE);
        while (algorithm.getNumberOfEvaluations() < problem.TOTAL_EVALS) {
            algorithm.step();
            System.out.println(algorithm.getNumberOfEvaluations());
        }

        algorithm.getPopulation().forEach(result::add);
        System.out.println("Got to end of getInitialPop");
        return result;
    }
}
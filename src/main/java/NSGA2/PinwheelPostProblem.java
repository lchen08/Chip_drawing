package NSGA2;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * <b><u>CS220 Assignment #3 - NGSAII</b></u>
 * <br>
 * This is the main class which creates the problems to send to MOEA to
 * evaluate for creating solutions using the NGSAII algorithm.
 * <p>
 * This code was created with the help of the following website:
 * http://keyboardscientist.weebly.com/blog/moea-framework-defining-new-problems
 *
 * @author Lisa Chen
 * @version 1.0
 * @since Jan 26, 2021
 */
public class PinwheelPostProblem extends AbstractProblem {
    private final String[] OBJECTIVES = {"flat", "0.45x", "high 0.1 edge", "high 0.7 edge", "section objective", "0 conc"};
    static final int NUM_OBJECTIVES = 2; //set 1 just for concentration
    static final int NUM_POST_VARS = 6; //x position, y pos, rectangle width, rectangle length, circle radius, rotation
    static final int VARS_FOR_NSGAII = 4;
    private static final int NUM_FLOW_RATE_VARS = 1;
    static final int CONC_OBJECTIVE_INDEX = 0;
    static final int PRESSURE_OBJECTIVE_INDEX = 1;

    //user specifications
    private final static int NUM_POSTS = 100;
    private final int OBJECTIVE_TYPE_INDEX = 5;
    static final boolean HAS_FISR = true;
    static final double STOICHIOMETRIC_COEFF = 1;
    static final double POST_ACCEPTANCE_CRITERIA = 0.5;

    static final double INFLOW_CONC_1 = 1;
    static final double INFLOW_CONC_2 = 1; //set to 0 for mixer

    static final double RECT_WIDTH = 0.2;
    static final double RECT_LENGTH = 0.05;
    static final double RADIUS = 0.05;
    static final double MIN_ROTATION = 0;
    static final double MAX_ROTATION = 90;
    static final double[] MAIN_CHANNEL_DIM = {-5, 0, 10, 1}; //to add to addRect, x, y, width, height in mm
    static final double[] CROSS_CHANNEL_DIM = {-4, -1, 1, 3}; //to add to addRect, x, y, width, height in mm

    static final int POPULATION_SIZE = 50;
    static final int TOTAL_EVALS = 4000;
    static final int NUM_UM_PER_TOTAL_VARS = 3;
    static final int NUM_UX_PER_TOTAL_VARS = 3;

    private final double MAX_REYNOLDS = 1500;
    private final double MAX_PRESSURE = 5; //5 Pascals
    private final double ENTRY_EXIT_BUFFER = MAIN_CHANNEL_DIM[3] * 2;
    private final double MIN_X = MAIN_CHANNEL_DIM[0] + ENTRY_EXIT_BUFFER;
    //    private final double MIN_X = MAIN_CHANNEL_DIM[0] + ENTRY_EXIT_BUFFER;
    private final double MAX_X = MAIN_CHANNEL_DIM[0] + MAIN_CHANNEL_DIM[2] - ENTRY_EXIT_BUFFER;
    private final double MIN_Y = MAIN_CHANNEL_DIM[1];
    private final double MAX_Y = MAIN_CHANNEL_DIM[1] + MAIN_CHANNEL_DIM[3];
    static final double MIN_FLOW_RATE = 0.001;
    static final double MAX_FLOW_RATE = 0.001;
    //end user specifications

    final static int TOTAL_VARS = NUM_POSTS * VARS_FOR_NSGAII;
    static final int UM_RATE = NUM_UM_PER_TOTAL_VARS / TOTAL_VARS;
    static final int UX_RATE = NUM_UX_PER_TOTAL_VARS / TOTAL_VARS;
    private int constraintNotSatisfied = -1;
    private int constraintSatisfied = 0;
    static int numConstraints;
    private double[][] postInfo;
    private double flowRate;
    private static double calcMaxFlowRate;
    private int errorConstraintIndex;
    private int flowRateConstraintIndex;
    static int flowRateVarIndex;
    private int numEvals = 0;
    private long elapsedTime = 0;


    public PinwheelPostProblem() throws InterruptedException {
        super(TOTAL_VARS, NUM_OBJECTIVES, getConstraints());
        setCalcMaxFlowRate();
    }

    public String getObjectiveName(int index) {
        return OBJECTIVES[index];
    }

    public void setCalcMaxFlowRate() {
        double maxFlow = MAX_REYNOLDS / (1000000.0 * MixerTest.mixer.getChannelWidthM());
        double maxFlow2 = MAX_PRESSURE * MixerTest.mixer.getCrossSectionAreaM() / (8.0 * Math.PI *
                MixerTest.mixer.getWaterViscosity() * MixerTest.mixer.getChannelLengthM());
//        System.out.println(maxFlow);
//        System.out.println(maxFlow2);
        calcMaxFlowRate = maxFlow < maxFlow2 ? maxFlow : maxFlow2;
    }

    public static double getMaxFlowRateAllowed() {
        return calcMaxFlowRate;
    }

    public static int getFlowRateVarIndex() {
        return flowRateVarIndex;
    }

    public static int getNumPosts() {
        return NUM_POSTS;
    }


    private static int getConstraints() {
        int rangeConstraints = 0;
//        int rangeConstraints = NUM_CIRCLES * 4; //TODO bring back when circles are not allowed to collide
        int errorConstraints = 1;
//        int numCombos = NUM_CIRCLES * (NUM_CIRCLES - 1) / 2;
        int numCombos = 0; //Remove for collision
//        int numFlowRates = 1;
        int numFlowRates = 0;
//        System.out.println("Combos: " + numCombos);
        numConstraints = rangeConstraints + errorConstraints + (int) numCombos + numFlowRates;
//        numConstraints = rangeConstraints + (int)numCombos;
//        System.out.println("# Constraints: " + numConstraints);
        return numConstraints;
    }


    /**
     * Creates a new solution object with set variables, the number of
     * objectives, and number of constraints.
     *
     * @return A new empty solution object with variables set
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(TOTAL_VARS, NUM_OBJECTIVES,
                numConstraints);
//        for (int i = 0; i < TOTAL_VARS - 1; i++) { //if using flow var
        //X, Y, Pos, Height, rotation
        for (int i = 0; i < TOTAL_VARS; i++) {
            solution.setVariable(i++, new RealVariable(MIN_X + RECT_LENGTH, MAX_X - RECT_LENGTH));
            solution.setVariable(i++, new RealVariable(MIN_Y - RECT_LENGTH, MAX_Y));
            solution.setVariable(i++, new RealVariable(MIN_ROTATION, MAX_ROTATION));
            solution.setVariable(i, new RealVariable(0, 1)); //for when you want varied posts
        }
//        solution.setVariable(flowRateVarIndex, new RealVariable(MIN_FLOW_RATE, MAX_FLOW_RATE)); //flow rate

        return solution;
    }

    /**
     * Evaluates the problem and stores the solutions created by multi-objective
     * algorithm based on set objectives and constraints for the problem.
     *
     * @param solution The solution object to store the solutions
     */
    @Override
    public void evaluate(Solution solution) {
        Instant evalTimeStart = Instant.now();
//        boolean hadError;
//        numEvals++;
        Instant start = Instant.now();
        System.out.println("Got to here");
        assignVariablesToArray(solution);
        assignFlowRateVar(solution);


        flowRate = MIN_FLOW_RATE; //TODO remove when flow rate is varying
//        while (solution.violatesConstraints()) {
//            solution = newSolution();
//            assignVariablesToCircleArray(solution);
//            setConstraints(solution);
//            System.out.println("Got to here");
//        }
//        do {
//            try {
//        if (!solution.violatesConstraints()) {
        Instant foundSol = Instant.now();
        MixerTest.problemRuntime += Duration.between(start, foundSol).toMillis();
        int popNumber = numEvals++ / POPULATION_SIZE + 1;
        MixerTest.mixer.setFilenamePrefix("P" + popNumber + "_");
        try {
            MixerTest.mixer.startPinwheel(postInfo, flowRate, HAS_FISR);
        } catch (Exception e) {
            System.out.println("startPinwheel gave an error");
            e.printStackTrace();
        }

        Instant objTime = Instant.now();
        try {
            setConcObjective(solution, MixerTest.mixer.getConc(), OBJECTIVE_TYPE_INDEX);
            setPressureObjective(solution, MixerTest.mixer.getPressure());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to get concentrations from file");
        }
        setErrorConstraints(solution);
        if (solution.violatesConstraints()) {
            setConcObjective(solution, null, OBJECTIVE_TYPE_INDEX);
            setPressureObjective(solution, null);
        }
        Instant objEndTime = Instant.now();
//            System.out.println("Time to test objective: " + Duration.between(objTime, objEndTime).toMillis());
        MixerTest.problemRuntime += Duration.between(objTime, objEndTime).toMillis();
//        }
//        else {
//            Instant objTime = Instant.now();
//            setConcObjective(solution, null, OBJECTIVE_TYPE_INDEX);
//            setPressureObjective(solution, null);
//            setErrorConstraints(solution);
//
//            Instant objEndTime = Instant.now();
////            System.out.println("Time to test objective: " + Duration.between(objTime, objEndTime).toMillis());
//            MixerTest.problemRuntime += Duration.between(objTime, objEndTime).toMillis();
//
//        }

//        Instant lastConsStart = Instant.now();
//        setErrorConstraints(solution);
//        if (!solution.violatesConstraints()) {
//            setObjective(solution, null, OBJECTIVE_INDEX);
//        }
        Instant lastConstEnd = Instant.now();
//        double decimalPlaces = 1000.0;
//        System.out.println(Math.round(solution.getObjective(0) * decimalPlaces) / decimalPlaces);
//        System.out.println("Last Constraint time: " + Duration.between(lastConsStart, lastConstEnd).toMillis());
//        MixerTest.problemRuntime += Duration.between(lastConsStart, lastConstEnd).toMillis();
        elapsedTime += Duration.between(evalTimeStart, lastConstEnd).toMillis();
        System.out.println("Evaluation " + numEvals + " Current total runtime (ms): " + elapsedTime);

//        System.out.println();
//        System.out.println(numEvals++);
    }

    private void assignFlowRateVar(Solution solution) {
        flowRate = ((RealVariable) solution.getVariable(flowRateVarIndex)).getValue();
    }

    private void assignVariablesToArray(Solution solution) {
//        int iter = 0;
//        circlesInfo = new double[NUM_CIRCLES][NUM_CIRCLE_VARS];
        ArrayList<double[]> postArrayList = new ArrayList<>();
//        System.out.println("Num variables: " + solution.getNumberOfVariables());
        for (int iter = 0; iter < solution.getNumberOfVariables(); iter += VARS_FOR_NSGAII) {
            if (((RealVariable) solution.getVariable(iter + (VARS_FOR_NSGAII - 1))).getValue() >=
                    (1 - POST_ACCEPTANCE_CRITERIA)) {
                postArrayList.add(
                        new double[]{
                                ((RealVariable) solution.getVariable(iter)).getValue()
                                , ((RealVariable) solution.getVariable(iter + 1)).getValue()
                                , RECT_WIDTH, RECT_LENGTH, RADIUS
                                , ((RealVariable) solution.getVariable(iter + 2)).getValue()
                        });
            }
        }
        System.out.println("Num posts: " + postArrayList.size());
        postInfo = new double[postArrayList.size()][NUM_POST_VARS];
        for (int post = 0; post < postArrayList.size(); post++) {
            postInfo[post] = postArrayList.get(post);
        }
    }

    /**
     * Sets the objectives for the problem that the solution must try to
     * optimize to meet.
     *
     * @param solution The solution object to add the objective for the results
     */
    private void setConcObjective(Solution solution, double[][] concs, int objective) {
        double result = 0;
        if (concs == null) {
            solution.setObjective(CONC_OBJECTIVE_INDEX, Double.MAX_VALUE);
        } else if (objective == 5) {
            for (int i = 0; i < concs.length; i++) {
                result += concs[i][1];
            }
            result /= (concs.length * CircularPostMixerProblem.INFLOW_CONC_1);
            solution.setObjective(CONC_OBJECTIVE_INDEX, -100.0 * (1 - result));
        } else {

            for (int i = 0; i < concs.length; i++) {
                result += Math.pow(getConcGoal(objective, concs[i][0], concs[i][1]) - concs[i][1], 2);
            }
            solution.setObjective(CONC_OBJECTIVE_INDEX, Math.sqrt(result));
        }
    }

    private void setPressureObjective(Solution solution, double[][] pressures) {
        if (pressures == null) {
            solution.setObjective(PRESSURE_OBJECTIVE_INDEX, Double.MAX_VALUE);
        } else {
            solution.setObjective(PRESSURE_OBJECTIVE_INDEX, pressures[pressures.length / 2][1]);
        }
    }


    private double getConcGoal(int objective, double x, double y) throws RuntimeException {
        switch (objective) {
            case 0:
                return 1.0 / 3.0;
            case 1:
                return 0.45 * x;
            case 2:
                return x <= 0.9 ? -2.0 / 3.0 : 2.0 / 3.0;
            case 3:
                return x <= 0.7 ? -2.0 / 3.0 : 2.0 / 3.0;
            case 4:
                return x > 0.7 ? 2.0 / 3.0 : y; //only tests for x >0.7
            case 5:
                return 0;
            default:
                throw new RuntimeException("Objective type incorrect: " + objective);
        }
    }

    private void setErrorConstraints(Solution solution) {
        solution.setConstraint(errorConstraintIndex, MixerTest.mixer.getErrorStatus() == false ?
                constraintSatisfied : constraintNotSatisfied);
    }

    private void setFlowRateConstraints(Solution solution) {
        solution.setConstraint(flowRateConstraintIndex, flowRate <= calcMaxFlowRate ? constraintSatisfied :
                constraintNotSatisfied);
    }
}
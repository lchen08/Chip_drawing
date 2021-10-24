package NSGA2;

import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CircularPostMeshProblem extends AbstractProblem {{
}
    static final int NUM_OBJECTIVES = 2; //set 1 just for concentration
    static final int NUM_CIRCLE_VARS = 3;
    static final int CONC_OBJECTIVE_INDEX = 0;
    static final int PRESSURE_OBJECTIVE_INDEX = 1;
    private static final int CIRCLE_VARS_FOR_NSGAII = 3;

    //user specifications
    private final static int NUM_CIRCLES = 20;
    private final int OBJECTIVE_TYPE_INDEX = 5;
    static final boolean HAS_FISR = true;
    static final double STOICHIOMETRIC_COEFF = 1;
    static final double CIRCLE_ACCEPTANCE_CRITERIA = 0.5;

    static final double INFLOW_CONC_1 = 1;
    static final double INFLOW_CONC_2 = 1; //set to 0 for mixer
    static final double[] MAIN_CHANNEL_DIM = {-5, 0, 10, 1}; //to add to addRect, x, y, width, height in mm
    static final double[] CROSS_CHANNEL_DIM = {-4, -1, 1, 3}; //to add to addRect, x, y, width, height in mm

    static final int POPULATION_SIZE = 20;
    static final int TOTAL_EVALS = POPULATION_SIZE*2;
    static final int NUM_UM_PER_TOTAL_VARS = 3;
    static final int NUM_UX_PER_TOTAL_VARS = 3;
    static final int NUM_GENERATIONS = 3;

    static final int MIN_MESH_SIZE = 1;
    static final int MAX_MESH_SIZE = 9;

    private final double MAX_REYNOLDS = 1500;
    private final double MAX_PRESSURE = 5; //5 Pascals
    static final double MIN_RADIUS = 0.1;
    private final double MAX_RADIUS = 0.1;
    private final double ENTRY_EXIT_BUFFER = MAIN_CHANNEL_DIM[3] * 2;
    private final double MIN_X = MAIN_CHANNEL_DIM[0] + ENTRY_EXIT_BUFFER * 2;
    //    private final double MIN_X = MAIN_CHANNEL_DIM[0] + ENTRY_EXIT_BUFFER;
    private final double MAX_X = MAIN_CHANNEL_DIM[0] + MAIN_CHANNEL_DIM[2] - ENTRY_EXIT_BUFFER;
    private final double MIN_Y = MAIN_CHANNEL_DIM[1];
    private final double MAX_Y = MAIN_CHANNEL_DIM[1] + MAIN_CHANNEL_DIM[3];
    static final double MIN_FLOW_RATE = 0.001;
    static final double MAX_FLOW_RATE = 0.001;
    //end user specifications

    final static int NUM_MESH_VARS = 1;
    final static int TOTAL_VARS = NUM_CIRCLES * CIRCLE_VARS_FOR_NSGAII + NUM_MESH_VARS;
    final static int NUM_CONSTRAINTS = 1; //just error constraint
    static final int UM_RATE = NUM_UM_PER_TOTAL_VARS / TOTAL_VARS;
    static final int UX_RATE = NUM_UX_PER_TOTAL_VARS / TOTAL_VARS;
    private int constraintNotSatisfied = -1;
    private int constraintSatisfied = 0;
    private double[][] circlesInfo;
    private double flowRate;
    private static double calcMaxFlowRate;
    static int meshVarIndex = TOTAL_VARS - 1;
    private int numEvals = 0;
    private long elapsedTime = 0;
    private int errorConstraintIndex;
    private final int MESH_STEP = 4;

    public CircularPostMeshProblem() {
        super(TOTAL_VARS, NUM_OBJECTIVES, NUM_CONSTRAINTS);
        errorConstraintIndex = 0;
    }


    public List<Solution> adjustMesh(List<Solution> population) {
        for (Solution solution : population) {
            RealVariable meshValue = (RealVariable) solution.getVariable(meshVarIndex);
            int meshSetting = (int) meshValue.getValue();
            if(meshSetting > MIN_MESH_SIZE)
                meshValue.setValue(meshSetting - MESH_STEP);
            solution.setVariable(meshVarIndex,meshValue);
        }
        return population;
    }

    public static int getNumCircles() {
        return NUM_CIRCLES;
    }

    @Override
    public void evaluate(Solution solution) {
        Instant evalTimeStart = Instant.now();
        Instant start = Instant.now();
        System.out.println("Got to here");
        assignVariablesToCircleArray(solution);
        flowRate = MIN_FLOW_RATE;

        Instant foundSol = Instant.now();
        MeshTest.problemRuntime += Duration.between(start, foundSol).toMillis();

//        int popNumber = numEvals++ / POPULATION_SIZE + 1;
//        MeshTest.mixer.setFilenamePrefix("P" + popNumber + "_");

//        System.out.println("Got to CircularPost 107");
        try {
            RealVariable meshValue = (RealVariable) solution.getVariable(meshVarIndex);
            int meshSetting = (int) meshValue.getValue();
            MeshTest.mixer.start(circlesInfo, flowRate, HAS_FISR, meshSetting);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("got to here");

        Instant objTime = Instant.now();
        try {
            setConcObjective(solution, MeshTest.mixer.getConc(), OBJECTIVE_TYPE_INDEX);
            setPressureObjective(solution, MeshTest.mixer.getPressure());
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
        MeshTest.problemRuntime += Duration.between(objTime, objEndTime).toMillis();

        Instant lastConstEnd = Instant.now();
        elapsedTime += Duration.between(evalTimeStart, lastConstEnd).toMillis();
        System.out.println("Evaluation " + numEvals + " Current total runtime (ms): " + elapsedTime);
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(TOTAL_VARS, NUM_OBJECTIVES, NUM_CONSTRAINTS);
        for (int i = 0; i < TOTAL_VARS - 1;) {
            solution.setVariable(i++, new RealVariable(MIN_X + MIN_RADIUS, MAX_X - MIN_RADIUS));
            solution.setVariable(i++, new RealVariable(MIN_Y - MIN_RADIUS, MAX_Y + MIN_RADIUS));
            solution.setVariable(i++, new RealVariable(0, 1)); //for when you want varied posts
        }
        solution.setVariable(meshVarIndex, new RealVariable(MIN_MESH_SIZE, MIN_MESH_SIZE)); //mesh setting
        return solution;
    }

    private void assignVariablesToCircleArray(Solution solution) {
        ArrayList<double[]> circlesArrayList = new ArrayList<>();
//        System.out.println("Num variables: " + solution.getNumberOfVariables());
        for (int iter = 0; iter < TOTAL_VARS - 1; iter += CIRCLE_VARS_FOR_NSGAII) {
            if (((RealVariable) solution.getVariable(iter + 2)).getValue() >=
                    (1 - CIRCLE_ACCEPTANCE_CRITERIA)) {
                circlesArrayList.add(
                        new double[]{
                                ((RealVariable) solution.getVariable(iter)).getValue()
                                , ((RealVariable) solution.getVariable(iter + 1)).getValue()
                                , MIN_RADIUS
                        });
            }
        }
        System.out.println("Adding " + circlesArrayList.size() + " circles.");
        circlesInfo = new double[circlesArrayList.size()][NUM_CIRCLE_VARS];
        System.out.println("Got to Problem 165");
        for (int circle = 0; circle < circlesArrayList.size(); circle++) {
            circlesInfo[circle] = circlesArrayList.get(circle);
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
        }
        else if (objective == 5) {
            for (int i = 0; i < concs.length; i++) {
                result += concs[i][1];
            }
            result /= (concs.length * CircularPostMixerProblem.INFLOW_CONC_1);
            solution.setObjective(CONC_OBJECTIVE_INDEX, -100.0 * (1 - result));
        }
        else {
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
        solution.setConstraint(errorConstraintIndex, MeshTest.mixer.getErrorStatus() == false ?
                constraintSatisfied : constraintNotSatisfied);
    }

    class LinkedSolution extends Solution {
        double originalPressure;
        double originalConc;

        public LinkedSolution(int numberOfVariables, int numberOfObjectives) {
            super(numberOfVariables, numberOfObjectives);
        }
    }
}

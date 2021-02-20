import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * <b><u>CS220 Assignment #3 - NGSAII</b></u>
 * <br>
 * This is the main class which creates the problems to send to MOEA to
 * evaluate for creating solutions using the NGSAII algorithm.
 *
 * This code was created with the help of the following website:
 * http://keyboardscientist.weebly.com/blog/moea-framework-defining-new-problems
 *
 * @author Lisa Chen
 * @since Jan 26, 2021
 * @version 1.0
 */
public class MixerProblem extends AbstractProblem {
    private static final int NUM_OBJECTIVES = 1;
    private int constraintNotSatisfied = -1;
    private int constraintSatisfied = 0;
    private static final int NUM_CIRCLE_VARS = 3;
    private static int numCircles = 10;
    private double minRadius = 0.05;
    private double maxRadius = 0.2;
    private double minX = -4;
    private double maxX = -0.8;
    private double minY = 0;
    private double maxY = 1;
    private final static int TOTAL_VARS = numCircles * NUM_CIRCLE_VARS;
    private static int numConstraints;
    private double[][] circlesInfo;
    private final String[] OBJECTIVES = {"flat", "0.45x", "high edge", "section objective"};
    private int objType = 3;
    private ArrayList<Boolean> errors;
//    private int numEvals = 1;

    public MixerProblem() {
        super(TOTAL_VARS, NUM_OBJECTIVES, getConstraints(numCircles));

        circlesInfo = new double[numCircles][NUM_CIRCLE_VARS];
        errors = new ArrayList<>();
    }

    public static int getNumCircles() {
        return numCircles;
    }

//    public static int getNumCircleVars() {
//        return NUM_CIRCLE_VARS;
//    }

    private static int getConstraints(int numCircles) {
        int rangeConstraints = numCircles * 4;
        int errorConstraints = 1;
        int numCombos = numCircles * (numCircles - 1) / 2;
        int numFlowRates = 1;
//        System.out.println("Combos: " + numCombos);
        numConstraints = rangeConstraints + errorConstraints + (int)numCombos;
//        numConstraints = rangeConstraints + (int)numCombos;
//        System.out.println("# Constraints: " + numConstraints);
        return numConstraints;
    }


    /**
     * Creates a new solution object with set variables, the number of
     * objectives, and number of constraints.
     * @return A new empty solution object with variables set
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(TOTAL_VARS, NUM_OBJECTIVES,
                numConstraints);
        for (int i = 0; i < TOTAL_VARS - 1; i++) {
            solution.setVariable(i++, new RealVariable(minX+minRadius,maxX-minRadius));
            solution.setVariable(i++, new RealVariable(minY+minRadius,maxY-minRadius));
            solution.setVariable(i, new RealVariable(minRadius,maxRadius));
        }
        return solution;
    }

    /**
     * Evaluates the problem and stores the solutions created by multi-objective
     * algorithm based on set objectives and constraints for the problem.
     * @param solution The solution object to store the solutions
     */
    @Override
    public void evaluate(Solution solution) {
//        boolean hadError;
        Instant start = Instant.now();
        assignVariablesToCircleArray(solution);
        setBoundaryConstraints(solution);

//        System.out.println("Info: " + ((RealVariable)solution.getVariable(16)).getValue());
//        while (solution.violatesConstraints()) {
//            solution = newSolution();
//            assignVariablesToCircleArray(solution);
//            setConstraints(solution);
//            System.out.println("Got to here");
//        }
//        do {
//            try {
        if (!solution.violatesConstraints()) {
            Instant foundSol = Instant.now();
//            System.out.println("Time to find within position constraints: " + Duration.between(start,
//                    foundSol).toMillis());
            MixerTest.problemRuntime += Duration.between(start, foundSol).toMillis();
            MixerTest.mixer.start(circlesInfo);

//                hadError = false;
//            } catch (Exception e) {
//                hadError = true;
//                File lastFile = new File(MixerTest.mixer.getLastFilename());
//                String path = lastFile.getPath();
//                String newPath = path.substring(0,path.length()-4) + "(error)" + path.substring(path.length()-4);
//                lastFile.renameTo(new File(newPath));
//                solution = newSolution();
//                assignVariablesToCircleArray(solution);
//                setConstraints(solution);
//            }
//        }while (hadError == true);
            Instant objTime = Instant.now();
            try {
                setObjective(solution, MixerTest.mixer.getConc(), objType);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("failed to get concentrations from file");
            }
            Instant objEndTime = Instant.now();
//            System.out.println("Time to test objective: " + Duration.between(objTime, objEndTime).toMillis());
            MixerTest.problemRuntime += Duration.between(objTime, objEndTime).toMillis();
        }
        Instant lastConsStart = Instant.now();
        setErrorConstraints(solution);
        Instant lastConstEnd = Instant.now();
//        System.out.println("Last Constraint time: " + Duration.between(lastConsStart, lastConstEnd).toMillis());
        MixerTest.problemRuntime += Duration.between(lastConsStart, lastConstEnd).toMillis();

//        System.out.println();
//        System.out.println(numEvals++);
    }

    private void assignVariablesToCircleArray(Solution solution) {
        int iter = 0;
        for (int row = 0; row < circlesInfo.length; row++) {
            for (int col = 0; col < circlesInfo[row].length; col++) {
                circlesInfo[row][col] = ((RealVariable)solution.getVariable(iter++)).getValue();
//                System.out.println(circlesInfo[row][col]);
            }
        }
    }

    /**
     * Sets the objectives for the problem that the solution must try to
     * optimize to meet.
     * @param solution The solution object to add the objective for the results
     */
    private void setObjective(Solution solution, double concs[][], int objective) {
        double result = 0;

        for (int i = 0; i < concs.length; i++) {
            result += Math.pow(getConcGoal(objective, concs[i][0], concs[i][1]) - concs[i][1], 2);
        }
        solution.setObjective(0, Math.sqrt(result));
    }

    private double getConcGoal(int obj, double x, double y) throws RuntimeException {
        switch(obj) {
            case 0:
                return 1.0/3.0;
            case 1:
                return 0.45 * x;
            case 2:
                return x <= 0.7 ? -2.0/3.0 : 2.0/3.0;
            case 3:
                return x > 0.7 ? 2.0/3.0 : y; //only tests for x >0.7
            default:
                throw new RuntimeException("Objective type incorrect: " + obj);
        }
    }

    /**
     * Sets the interference edge and colors assigned consecutively constraints
     * for the problem that the solution must obey.
     * @param solution The solution object to constrain the results
     */
    private void setBoundaryConstraints(Solution solution) {
        int constraintInd = 0;

        for (int i = 0; i < circlesInfo.length; i++) {
            double x1 = circlesInfo[i][0];
            double y1 = circlesInfo[i][1];
            double radius1 = circlesInfo[i][2];
            solution.setConstraint(constraintInd++, x1 - radius1 > minX ? constraintSatisfied : constraintNotSatisfied);
            solution.setConstraint(constraintInd++, x1 + radius1 < maxX ? constraintSatisfied : constraintNotSatisfied);
            solution.setConstraint(constraintInd++, y1 - radius1 > minY ? constraintSatisfied : constraintNotSatisfied);
            solution.setConstraint(constraintInd++, y1 + radius1 < maxY ? constraintSatisfied : constraintNotSatisfied);
//            solution.setConstraint(constraintInd++, MixerTest.mixer.getErrorStatus() == false ? constraintSatisfied :
//                    constraintNotSatisfied);

            for (int j = i+1; j < circlesInfo.length; j++) {
                double distanceReq = radius1 + circlesInfo[j][2];
                double fxn = Math.sqrt(Math.pow(circlesInfo[j][0] - x1, 2) + Math.pow(circlesInfo[j][1] - y1, 2));
                solution.setConstraint(constraintInd++, fxn > distanceReq ? constraintSatisfied :
                        constraintNotSatisfied);
            }
        }
    }

    private void setErrorConstraints(Solution solution) {
        solution.setConstraint(numberOfConstraints - 1, MixerTest.mixer.getErrorStatus() == false ?
                constraintSatisfied : constraintNotSatisfied);
    }
}
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import com.comsol.model.util.ModelUtil;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

public class MixerTest {
    private static final int NUM_CIRCLE_VARS = 3;
    private static final int NUM_CIRCLES = MixerProblem.getNumCircles();
    private static int totalVars = NUM_CIRCLES * NUM_CIRCLE_VARS;;
    static TSimpleMixer mixer = new TSimpleMixer();
    static double problemRuntime = 0;
//    static private final double FLOW_RATE = 0.03;

    public static void main(String[] args) {
        Instant begin = Instant.now();
//        final long startTime = System.currentTimeMillis();
        mixer.init();
//        mixer.doBaseSetup(FLOW_RATE);
        mixer.doBaseSetup();
        double rate = 1 / totalVars;

        //runs NGSAII algorithm with given parameters
        NondominatedPopulation result = new Executor()
                .withAlgorithm("NSGAII")
                .withProblemClass(MixerProblem.class)
                .withMaxEvaluations(12000)
//                .withProperty("sbx.rate", rate) //simulated binary crossover
//                .withProperty("sbx.distributionIndex", 15.0)
//                .withProperty("pm.rate", rate) //polynomial mutation
//                .withProperty("pm.distributionIndex", 15.0)
//                .withProperty("ux.rate", 0.2) //uniform crossover
                .withProperty("populationSize", 500)
                .run();

        Instant end = Instant.now();
//        final long endTime = System.currentTimeMillis();
        long duration = (long) (Duration.between(begin, end).toMillis()/1000.0);

        System.out.println("\nTotal Stats:");
        System.out.println("Total Runtime (ms): " + (duration * 1000.0) + "\nTotal Runtime(seconds): " + duration);
        System.out.println("COMSOL time (ms): " + mixer.getTotalTime());
        System.out.println("NSGAII time (ms):" + problemRuntime);

        System.out.println("\nPareto Front size: " + getNumValidSolutions(result));
        int i = 0;
        double[][] circlesInfo = new double[NUM_CIRCLES][NUM_CIRCLE_VARS];

        if (getNumValidSolutions(result) != 0) {
            for (Solution solution : result) {
                if (!solution.violatesConstraints()) {
//                    System.out.println("Solution " + i);

                    int circIndex = 0;
                    for (int j = 0; j < totalVars; j+=3) {
                        double posX = Double.parseDouble(solution.getVariable(j).toString());
                        double posY = Double.parseDouble(solution.getVariable(j+1).toString());
                        double radius = Double.parseDouble(solution.getVariable(j+2).toString());
//                        System.out.println("Circle " + circIndex + "(" + posX + ", " + posY + " radius: " + radius);
                        double[] circleRow = {posX, posY, radius};
                        circlesInfo[circIndex++] = circleRow;
                    }

                    double flowRate = Double.parseDouble(solution.getVariable(MixerProblem.getFlowRateVarIndex()).
                            toString());

                    System.out.println("All Pass?: " + Test2.check(circlesInfo));

                    mixer.start(circlesInfo, flowRate);
                    System.out.println("FIle created: " + mixer.getLastFilename());

                }
            }
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
}
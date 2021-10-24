package NSGA2;

import com.comsol.model.util.ModelUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PostArrayChip {
    private static final double INFLOW_CONC_1 = 1;
    private static final double INFLOW_CONC_2 = 1;
    private static final double[] MAIN_CHANNEL_DIM = {-5, 0, 10, 1}; //to add to addRect, x, y, width, height in mm
    private static final double[] CROSS_CHANNEL_DIM = {-4, -1, 1, 3};
    private static final double ENTRY_EXIT_BUFFER = MAIN_CHANNEL_DIM[3] * 2;
//    private static final double ENTRY_EXIT_BUFFER = MAIN_CHANNEL_DIM[3] * 6;
    private static final double STOICHIOMETRIC_COEFF = 1;
    private static final boolean HAS_FISR = true;
    private static double flowRate = 0.001;
    private static final int NUM_CIRCLE_VARS = 3;
    private final static int NUM_CIRCLES = 24;
    private final static double RADIUS = 0.1;
//    private final static double RADIUS = 0.15;

//    private static int[] numCirclesPerRow = {3, 4};
    private static int[] numCirclesPerRow = {4, 4};
    private static double[] spaceTakenPerRow = {numCirclesPerRow[0] * 0.1 * 2, numCirclesPerRow[1] * 0.1 * 2};
//    private static double circleSpacing = (MAIN_CHANNEL_DIM[3] - spaceTakenPerRow[1])/(numCirclesPerRow[1] + 1 * 1.0);
    private static double circleSpacing = (MAIN_CHANNEL_DIM[3] - spaceTakenPerRow[1])/(numCirclesPerRow[1] - 1 * 1.0);
//    private static double rowSpacing = (RADIUS + (circleSpacing / 2.0)) * Math.pow(3.0, 0.5) * 1.8;
    private static double rowSpacing = (RADIUS + (circleSpacing / 2.0)) * Math.pow(3.0, 0.5) * 4;
    private static double[][] circlesInfo = new double[NUM_CIRCLES][NUM_CIRCLE_VARS];;
    private static final String ROOT = "D:\\COMSOL Research\\63) pattern testing\\";
    private static String concFile = ROOT + "0_exitconc.txt";
    private static String pressureFile = ROOT + "0_entrypressure.txt";

    public static void main(String[] args) throws IOException {
//        System.out.println(circleSpacing);
//        System.out.println(rowSpacing);
//        TSimpleMixer mixer = new TSimpleMixer(INFLOW_CONC_1, INFLOW_CONC_2, MAIN_CHANNEL_DIM, CROSS_CHANNEL_DIM);
//        mixer.init();
//        mixer.doFISRBaseSetup(STOICHIOMETRIC_COEFF);
//        populateCircleArray();
//        mixer.start(circlesInfo, flowRate, HAS_FISR);
//        ModelUtil.closeWindows();
//        ModelUtil.clear();
//        ModelUtil.disconnect();

        String concFile = "D:\\COMSOL Research\\63) pattern testing\\analyzing 100 chip\\" + "removestep16_exitconc.txt";
        String pressureFile = "D:\\COMSOL Research\\63) pattern testing\\analyzing 100 chip\\" + "removestep16_entrypressure.txt";

        double[][] concs = getFileDataPts(new BufferedReader(new FileReader(concFile)));
        double[][] pressures = getFileDataPts(new BufferedReader(new FileReader(pressureFile)));
        double capture = getCaptureEfficiency(concs);
        double pressure = pressures[pressures.length / 2][1];

//        System.out.println("Post added: ");
        System.out.println("Capture (%):" + capture);
        System.out.println("Center Pressure (Pa): " + pressure);

        double avgPressure = 0;
        for (double[] datapt : pressures) {
            avgPressure += datapt[1];
        }
        avgPressure = avgPressure / (pressures.length * 1.0);
        System.out.println("Average Pressure (Pa): " + avgPressure);

//        concFile = "D:\\COMSOL Research\\63) pattern testing\\7 shifted\\remove center\\" + "nopostadded_exitconc.txt";
//        pressureFile = "D:\\COMSOL Research\\63) pattern testing\\7 shifted\\remove center\\" + "nopostadded_entrypressure.txt";
//
//        concs = getFileDataPts(new BufferedReader(new FileReader(concFile)));
//        pressures = getFileDataPts(new BufferedReader(new FileReader(pressureFile)));
//        capture = getCaptureEfficiency(concs);
//        pressure = pressures[pressures.length / 2][1];
//
//        System.out.println("No Post added: ");
//        System.out.println("Capture:" + capture);
//        System.out.println("Center Pressure: " + pressure);
//
//        avgPressure = 0;
//        for (double[] datapt : pressures) {
//            avgPressure += datapt[1];
//        }
//        avgPressure = avgPressure / (pressures.length * 1.0);
//        System.out.println(avgPressure);

        System.exit(0);
    }

    public PostArrayChip() {
        populateCircleArray();
    }

    public double[][] getPostArrayPos() {
        return circlesInfo;
    }
    /**
     * Circle array created where each row is a circle's description with x-position, y-position, and radius
     * @return
     */
    private static void populateCircleArray() {
//        double startingXPos = MAIN_CHANNEL_DIM[0] + MAIN_CHANNEL_DIM[2] - ENTRY_EXIT_BUFFER;
        double startingXPos = MAIN_CHANNEL_DIM[0] + MAIN_CHANNEL_DIM[2] - ENTRY_EXIT_BUFFER * 1.5;
//        double startingYPosType0 = MAIN_CHANNEL_DIM[1] + (circleSpacing * 1.5) + (RADIUS * 2);
        double startingYPosType0 = MAIN_CHANNEL_DIM[1] + (circleSpacing * 0.5) + (RADIUS * 2);
//        double startingYPosType1 = MAIN_CHANNEL_DIM[1] + circleSpacing + RADIUS;
        double startingYPosType1 = MAIN_CHANNEL_DIM[1] + RADIUS;
        int circleInRowCount = 0;
        int circleRowType = 0;
//        double startingYPos = startingYPosType0;
        double startingYPos = startingYPosType1;
        for (int circleRow = 0; circleRow < NUM_CIRCLES; circleRow++) {
            circleInRowCount++;
            circlesInfo[circleRow][0] = startingXPos;
            circlesInfo[circleRow][1] = startingYPos;
            circlesInfo[circleRow][2] = RADIUS;
            if(circleInRowCount == numCirclesPerRow[circleRowType]) {
                if (circleRowType == 0) {
                    circleRowType = 1;
                    startingYPos = startingYPosType1;
                } else {
                    circleRowType = 0;
                    startingYPos = startingYPosType0;
                    circleRowType = 1;
                    startingYPos = startingYPosType1;

                }
                circleInRowCount = 0;
//                startingXPos -= rowSpacing;

            }
            else {
                startingYPos += circleSpacing + (RADIUS * 2);
                startingXPos -= (circleSpacing / Math.sqrt(3)) + (RADIUS * 2);
            }
        }
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
}

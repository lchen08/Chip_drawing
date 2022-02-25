package NSGA2;

import org.moeaframework.util.io.FileUtils;

import java.io.*;
import java.util.ArrayList;

public class BestRunCalculator {
    private static final int OBJECTIVE = 5;
    private static final String TXT_RESULT_EXTENSION = ".txt";
    private static final String RESULT_DIR = "D:/COMSOL Research/34) repeat 33/";
    private static double bestDist = -1;
    private static String bestFilename = "";
    private static char groupNameSeparator = '_';
    private static final String MIXER_FILENAME_END = "_mixer.mph";
    private static final String CONCGRAPH_FILENAME_END = "_concgraph.png";
    private static final String CONCIMG_FILENAME_END = "_concimg.png";
    private static final String MIXER_EXT = ".mph";
    private static final String IMAGE_EXT = ".png";
    private static final String BEST_FILENAME = "best";

    public static void main(String[] args) throws IOException {
        File dir = new File(RESULT_DIR);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File item : directoryListing) {
                String filename = item.toString();
                String extension = filename.substring(filename.length() - TXT_RESULT_EXTENSION.length());
                if(extension.equals(TXT_RESULT_EXTENSION)) {
                    BufferedReader br = new BufferedReader(new FileReader(item));
                    double currentDist = calcDistance(getConcs(br), OBJECTIVE);
                    if (currentDist < bestDist || bestDist == -1) {
                        bestDist = currentDist;
                        bestFilename = filename;
                    }
                }
            }
            System.out.println("Best file found: " + bestFilename);
            createBestResultFiles(bestFilename);

        }
        else {
            throw new IOException("Directory " + RESULT_DIR + " has no files.");
        }
    }

    private static void createBestResultFiles(String bestFilename) throws IOException {
        String fileGroupName = bestFilename.substring(0,lastCharOccurrence(bestFilename, groupNameSeparator));
        String origName = bestFilename.substring(RESULT_DIR.length(), bestFilename.length() -
                TXT_RESULT_EXTENSION.length());
        FileUtils.copy(new File(bestFilename), new File(RESULT_DIR + BEST_FILENAME + "(" + origName + ")" +
                TXT_RESULT_EXTENSION));
        FileUtils.copy(new File(fileGroupName + MIXER_FILENAME_END),
                new File(RESULT_DIR + BEST_FILENAME + MIXER_EXT));
        FileUtils.copy(new File(fileGroupName + CONCGRAPH_FILENAME_END),
                new File(RESULT_DIR + BEST_FILENAME + "graph" + IMAGE_EXT));
        FileUtils.copy(new File(fileGroupName + CONCIMG_FILENAME_END),
                new File(RESULT_DIR + BEST_FILENAME + "img" + IMAGE_EXT));
    }

    public static int lastCharOccurrence(String source, char character) {
        String segment = source;
        int currentCharIndex = 0;
        int finalIndex = 0;
        while (currentCharIndex != -1) {
            currentCharIndex = segment.indexOf(character);
            if (currentCharIndex != -1) {
                segment = segment.substring(++currentCharIndex);
                finalIndex += currentCharIndex;
            }
        }
        return finalIndex - 1;
    }

    private static double[][] getConcs(BufferedReader br) throws IOException {
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

    private static double calcDistance(double concs[][], int objective) {
        double result = 0;

        for (int i = 0; i < concs.length; i++) {
            result += Math.pow(getConcGoal(objective, concs[i][0], concs[i][1]) - concs[i][1], 2);
        }
        return Math.sqrt(result);
    }
    private static double getConcGoal(int objective, double x, double y) throws RuntimeException {
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
}

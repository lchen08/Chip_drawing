package NSGA2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AverageConcentration {
//    private static String exitConcFilename = "D:/COMSOL Research/35) test/spacing between walls/0_exitconc.txt";
    private static String exitConcFilename = "D:/COMSOL Research/34) repeat 33/best(P90_4982_exitconc).txt";
    public static void main(String[] args) throws IOException {
        System.out.println(exitConcFilename + "\n");
        BufferedReader br = new BufferedReader(new FileReader(exitConcFilename));
        double[][] concs = addDatapts(br);
        double average = 0;
        for (int row = 0; row < concs.length; row++) {
            average += concs[row][1];
        }
        average /= concs.length;
        System.out.println(average);
    }
    private static double[][] addDatapts(BufferedReader br) throws IOException {
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

package NSGA2;

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CountPosts {
    private static final String ROOT = "D:\\COMSOL Research\\70) repeat 67, E4k\\";
    private static int allMinPosts = -1;
    private static int allMaxPosts = -1;
    private static double allAvgPosts = 0;
    private static String allMinFilename = "";
    private static String allMaxFilename = "";
    private static int nonErrorMinPosts = -1;
    private static int nonErrorMaxPosts = -1;
    private static String nonErrorMinFilename = "";
    private static String nonErrorMaxFilename = "";
    private static double nonErrorAvgPosts = 0;
    private static int nonErrorCounts = 0;
    private static int allCounts = 0;
    private static final String SAVE_EXT = ".java";
    private static final String MODEL_NAME = "Model";
    private static final String MODEL_EXT = ".mph";
    private static ArrayList<int[]> postQty = new ArrayList<>();
    private static String uncountedFiles = "";

    public static void main(String[] args) throws IOException {
//        ModelUtil.initStandalone(false);
        String newFilename = "";

        File dir = new File(ROOT);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File item : directoryListing) {
//                String filename = item.toString();
                String filename = item.getName();
//                System.out.println(filename);
                if (filename.contains(MODEL_EXT)) {
//                    System.out.println(item.toString());
//                    Model model = null;
                    try {
//                        model = ModelUtil.load(MODEL_NAME, item.toString());
                        newFilename = filename.substring(0, filename.indexOf("mixer") - 1);
//                        System.out.println(newFilename);
                        try {
//                            model.save(ROOT + newFilename, SAVE_EXT.substring(1));
                        } catch (Exception f) {
                            f.printStackTrace();
                        }

//                        if(filename.contains("P1_")) {
                            int numPosts = getPostCount(getPostLines(ROOT + newFilename + SAVE_EXT));
                            addToPostQtyList(numPosts);

                            System.out.println(filename);
                            System.out.println("Qty: " + numPosts);

                            allCounts++;
                            if (!filename.contains("error")) {
                                nonErrorCounts++;
                                if (nonErrorMinPosts == -1 || numPosts < nonErrorMinPosts) {
                                    System.out.println("Is a min compared to: " + nonErrorMinPosts);
                                    nonErrorMinPosts = numPosts;
                                    nonErrorMinFilename = filename;
                                }
                                if (nonErrorMaxPosts == -1 || numPosts > nonErrorMaxPosts) {
                                    System.out.println("Is a max compared to: " + nonErrorMaxPosts);
                                    nonErrorMaxPosts = numPosts;
                                    nonErrorMaxFilename = filename;
                                }
                                nonErrorAvgPosts += numPosts;
                            }
                            if (allMinPosts == -1 || numPosts < allMinPosts) {
                                System.out.println("Is a min compared to: " + allMinPosts);
                                allMinPosts = numPosts;
                                allMinFilename = filename;
                            }
                            if (allMaxPosts == -1 || numPosts > allMaxPosts) {
                                System.out.println("Is a max compared to: " + allMaxPosts);
                                allMaxPosts = numPosts;
                                allMaxFilename = filename;
                            }
                            allAvgPosts += numPosts;
//                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        uncountedFiles += newFilename + "\n";
                    }
                }
            }
        }

        System.out.println("Verifying info:");
        System.out.println("No error counts: " + nonErrorCounts);
        System.out.println("With errors counts: " + allCounts);

        System.out.println("Excluding error files:");
        System.out.println("Min: " + nonErrorMinPosts + " (" + nonErrorMinFilename + ")");
        System.out.println("Max: " + nonErrorMaxPosts + " (" + nonErrorMaxFilename + ")");
        System.out.println("Average: " + nonErrorAvgPosts / (nonErrorCounts * 1.0));

        System.out.println("With error files:");
        System.out.println("Min: " + allMinPosts + " (" + allMinFilename + ")");
        System.out.println("Max: " + allMaxPosts + " (" + allMaxFilename + ")");
        System.out.println("Average: " + allAvgPosts / (allCounts * 1.0));

        for (int[] postInfo : postQty) {
            System.out.println("Num posts: " + postInfo[0] + ": " + postInfo[1]);
        }

        System.out.println("\nFiles that couldn't be counted (must manually count .mph file): \n" + uncountedFiles);
    }

    private static void addToPostQtyList(int numPosts) {
        boolean found = false;
        for (int[] postInfo : postQty) {
            if (postInfo[0] == numPosts) {
                postInfo[1] += 1;
                found = true;
            }
        }
        if (!found) {
            int[] newPostCount = {numPosts, 1};
            postQty.add(newPostCount);
        }
    }

    private static ArrayList<String> getPostLines(String filename) throws FileNotFoundException {
        Scanner s = new Scanner(new File(filename));
        ArrayList<String> postCodeLines = new ArrayList<>();
        while (s.hasNext()) {
            String line = s.nextLine();
//            System.out.println(line);
            if (line.contains("model.component(\"comp1\").geom(\"geom1\").create(\"c") ||
                    line.contains("model.component(\"comp1\").geom(\"geom1\").create(\"tri")) {
                postCodeLines.add(line);
            }
        }

//        System.out.println(postCodeLines.size());
//        for (String line : postCodeLines) {
//            System.out.println(line);
//        }
        return postCodeLines;
    }

    private static int getPostCount(ArrayList<String> postLines) {
        return postLines.size();
    }


}

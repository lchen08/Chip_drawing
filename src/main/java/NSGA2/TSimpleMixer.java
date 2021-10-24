package NSGA2;

import com.comsol.model.*;
import com.comsol.model.util.*;
import org.apache.commons.math3.util.Precision;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Model exported on Dec 22 2020, 11:03 by COMSOL 5.5.0.359.
 */
public class TSimpleMixer {
    private static Model model;
    //  private String exportDir = System.getProperty("user.dir") + "/exports/";
    private String exportDir = "D:/COMSOL Research/63) pattern testing/";
    private double boundaryBuffer = 0.001;
    private ArrayList<String> circleNames;
    private ArrayList<Rectangle> rectangles;
    private ArrayList<String> rectNames;
    private ArrayList<String> rectPostNames;
    private final double inflowConc1;
    private final double inflowConc2;
    private String exitConcFilename = "";
    private String entryPressureFilename = "";
    private String concImgFilename = "";
    private String concGraphFilename = "";
    private String pressureGraphFilename = "";
    private String fullChipPressureFilename = "";
    private String fullChipConcFilename = "";
    private String modelName = "";
    private String diffName = "dif1";
    private String fisrName = "fisr1";
    private double[][] currentCircleSet;
    private double[][] currentRectSet;
    private double goal = 0.5;
    private String compName = "comp1";
    private String geomName = "geom1";
    private String solName = "sol1";
    private int fileIter = 0;
    private double inletMPerSec = 0;
    private boolean hadError = false;
    final int DEFAULT_MESH_SIZE = 4;
    private String filenamePrefix = "";
    private final String[] COMSOL_MESH_NAMES = {"Extremely fine", "Extra fine", "Finer", "Fine", "Normal", "Coarse", "Coarser",
            "Extra coarse", "Extremely coarse"};
    private double totalTime = 0;
    private final double[] MAIN_CHANNEL_DIM; //to add to addRect, x, y, width, height in mm
    private final double[] CROSS_CHANNEL_DIM; //to add to addRect, x, y, width, height in mm
    private final double WATER_VISCOSITY = 0.00089;
    private final double CROSS_SECTION_AREA_MM;
    private double[][] pressureTable;


//    public static void main(String[] args) throws IOException, InterruptedException {
//        TSimpleMixer demo = new TSimpleMixer(1,0);
//
//        demo.init();
////    demo.start();
////    demo.rewriteFiles();
//        System.out.println("COMSOL program completed.");
//
//        ModelUtil.disconnect();
////    return;
//    }


    public TSimpleMixer(double inflowConc1, double inflowConc2, double[] mainChannelDim, double[] crossChannelDim) {
        MAIN_CHANNEL_DIM = mainChannelDim;
        CROSS_CHANNEL_DIM = crossChannelDim;
        CROSS_SECTION_AREA_MM = Math.pow(MAIN_CHANNEL_DIM[3] / 1000.0, 2);
        this.inflowConc1 = inflowConc1;
        this.inflowConc2 = inflowConc2;
    }

    public void resetFileIter() {
        fileIter = 0;
    }

    public double getChannelWidthM() {
        return MAIN_CHANNEL_DIM[3] / 1000.0;
    }

    public double getChannelLengthM() {
        return MAIN_CHANNEL_DIM[2] / 1000.0;
    }

    public double getCrossSectionAreaM() {
        return CROSS_SECTION_AREA_MM;
    }

    public double getWaterViscosity() {
        return WATER_VISCOSITY;
    }

    public String getExportDir() {
        return exportDir;
    }


    public void init() {
//    ModelUtil.initStandalone(true);
        ModelUtil.initStandalone(false);
        circleNames = new ArrayList<String>();
        rectangles = new ArrayList<Rectangle>();
        rectNames = new ArrayList<String>();
        rectPostNames = new ArrayList<String>();
        currentCircleSet = null;
    }

//  public void rewriteFiles() throws IOException {
//    String dir = "D:/COMSOL Research/15) Feb 14 run (0.02, 12000 evals, 0.45x)/";
//    for (int i = 0; i <= 4000; i++) {
//      File file = new File(dir + i + "_mixer.mph");
//      if (!file.exists())
//        return;
//      model = ModelUtil.load("Model",dir + i + "_mixer.mph");
//      model.resetHist();
//      model.save(dir + i + "_mixer");
////      model.save(dir + "(compacted)" + i + "_mixer");
//    }
//  }

    public void doFISRBaseSetup(double stoichiometricCoeff) {
//    this.inletMPerSec = inletMPerSec;

        try {
            Instant start = Instant.now();
            model = createInitialGeoms(stoichiometricCoeff); //originally run()

            Instant stop = Instant.now();
            System.out.println("Initial setup: " + Duration.between(start, stop).toMillis());
            totalTime += Duration.between(start, stop).toMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doMixerBaseSetup() {
//    this.inletMPerSec = inletMPerSec;

        try {
            Instant start = Instant.now();
            model = createInitialGeoms(); //originally run()

            Instant stop = Instant.now();
            System.out.println("Initial setup: " + Duration.between(start, stop).toMillis());
            totalTime += Duration.between(start, stop).toMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(double[][] circleInfo, double inletMPerSec, boolean hasFISR) throws FileNotFoundException {
        start(circleInfo, inletMPerSec, hasFISR, DEFAULT_MESH_SIZE);
    }
    //  public void start(double[][] circleInfo) {
    public void start(double[][] circleInfo, double inletMPerSec, boolean hasFISR, int meshSize) throws FileNotFoundException {
        hadError = false;
        currentCircleSet = new double[circleInfo.length][3];

        removeCircles();

        System.out.println("Got to removing circles.");
        Instant start = Instant.now();
//        try {
        for (int i = 0; i < circleInfo.length; i++) {
            System.out.println(circleInfo[i][0] + " " + circleInfo[i][1] + " " + circleInfo[i][2]);
            double posX = circleInfo[i][0];
            double posY = circleInfo[i][1];
            double radius = circleInfo[i][2];
            addCircle("c" + i, posX, posY, radius);
            double[] circleData = {posX, posY, radius};
            currentCircleSet[i] = circleData;
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println("Got to adding circles.");

        String[] circlesArr = new String[circleNames.size()];
        circlesArr = circleNames.toArray(circlesArr);
        String[] rectArr = new String[rectNames.size()];
        rectArr = rectNames.toArray(rectArr);

        try {
            model.component(compName).geom(geomName).create("dif1", "Difference");
        } catch (Exception e) {
            model.component(compName).geom(geomName).feature().remove(diffName);
            model.component(compName).geom(geomName).create(diffName, "Difference");
        }

        model.component(compName).geom(geomName).feature(diffName).selection("input").set(rectArr);
        model.component(compName).geom(geomName).feature(diffName).selection("input2")
                .set(circlesArr);

        System.out.println("Got to difference.");


        try {
            model.component(compName).geom(geomName).run();
            model.component(compName).geom(geomName).runPre("fin");

            System.out.println("Got to after run and before FISR");
            if (hasFISR) {
//            try {
//                model.component(compName).physics("tds").create(fisrName, "FastIrreversibleSurfaceReaction", 1);
//                System.out.println("applied FISR");
//            } catch(Exception e) {
//                model.component(compName).physics("tds").feature().remove(fisrName);
//                model.component(compName).physics("tds").create(fisrName, "FastIrreversibleSurfaceReaction", 1);
//                System.out.println("applied FISR2");
//            }
//            getEdges();
//            getVertices();
                applyFISR(getFISREdges(getEdges(), getVertices()), fisrName);

                setMeshSize(meshSize);
                model.component(compName).mesh("mesh1").automatic(true);

                recreateSol("st1", "v1", "s1");
                Instant stop = Instant.now();

                System.out.println("Updating chip's geometries: " + Duration.between(start, stop).toMillis());
                totalTime += Duration.between(start, stop).toMillis();

                model.component(compName).physics("spf").feature("inl1").set("U0in", inletMPerSec);

                runResultSetup();
            }
        } catch (Exception e) {
            File file = new File("error" + fileIter + ".log");
            PrintStream ps = new PrintStream(file);
            ps.println("Error for not-created file occurring after " + fileIter);
            System.out.println(circleInfo.length);
            for (int row = 0; row < circleInfo.length; row++) {
                ps.print("Circle " + row + ": ");
                for (int param = 0; param < circleInfo[row].length; param++) {
                    ps.print(circleInfo[row][param] + " ");
                }
                ps.println();
            }
            try {
                modelName = exportDir + filenamePrefix + fileIter + "_mixer(error)";
                model.resetHist();
                model.save(modelName);
            } catch (IOException f) {
                System.out.println("Failed to create mph file.");
            }
            fileIter++;

            e.printStackTrace(ps);
            ps.close();
            hadError = true;
            System.out.println("set the result to error");
        }
    }

    public void startRect(double[][] rectInfo, double inletMPerSec, boolean hasFISR) throws FileNotFoundException {
        hadError = false;
        currentRectSet = new double[rectInfo.length][5];

        removeRectPosts();

        System.out.println("Got to removing rectangles.");
        Instant start = Instant.now();
//        try {
        for (int i = 0; i < rectInfo.length; i++) {
            System.out.println("X: " + rectInfo[i][0] + " Y: " + rectInfo[i][1] + " W: " + rectInfo[i][2] +
                    " H: " + rectInfo[i][3] + " Rot: " + rectInfo[i][4]);
            double posX = rectInfo[i][0];
            double posY = rectInfo[i][1];
            double width = rectInfo[i][2];
            double height = rectInfo[i][3];
            double rotation = rectInfo[i][4];
            addRectPosts("r" + i, posX, posY, width, height, rotation);
            double[] rectData = {posX, posY, width, height, rotation};
            currentRectSet[i] = rectData;
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        System.out.println("Got to adding rectangle posts.");

        String[] rectPostArr = new String[rectPostNames.size()];
        rectPostArr = rectPostNames.toArray(rectPostArr);
        String[] rectArr = new String[rectNames.size()];
        rectArr = rectNames.toArray(rectArr);

        try {
            model.component(compName).geom(geomName).create("dif1", "Difference");
        } catch (Exception e) {
            model.component(compName).geom(geomName).feature().remove(diffName);
            model.component(compName).geom(geomName).create(diffName, "Difference");
        }

        model.component(compName).geom(geomName).feature(diffName).selection("input").set(rectArr);
        model.component(compName).geom(geomName).feature(diffName).selection("input2")
                .set(rectPostArr);

        System.out.println("Got to difference.");


        try {
            model.component(compName).geom(geomName).run();
            model.component(compName).geom(geomName).runPre("fin");

            System.out.println("Got to after run and before FISR");
            if (hasFISR) {
//            try {
//                model.component(compName).physics("tds").create(fisrName, "FastIrreversibleSurfaceReaction", 1);
//                System.out.println("applied FISR");
//            } catch(Exception e) {
//                model.component(compName).physics("tds").feature().remove(fisrName);
//                model.component(compName).physics("tds").create(fisrName, "FastIrreversibleSurfaceReaction", 1);
//                System.out.println("applied FISR2");
//            }
//            getEdges();
//            getVertices();
                applyFISR(getFISREdges(getEdges(), getVertices()), fisrName);

                setMeshSize(DEFAULT_MESH_SIZE);
                model.component(compName).mesh("mesh1").automatic(true);

                recreateSol("st1", "v1", "s1");
                Instant stop = Instant.now();

                System.out.println("Updating chip's geometries: " + Duration.between(start, stop).toMillis());
                totalTime += Duration.between(start, stop).toMillis();

                model.component(compName).physics("spf").feature("inl1").set("U0in", inletMPerSec);

                runResultSetup();
            }
        } catch (Exception e) {
            hadError = true;
            File file = new File("error" + fileIter + ".log");
            PrintStream ps = new PrintStream(file);
            ps.println("Error for not-created file occurring after " + fileIter);
            System.out.println("Number rectangles: " + rectInfo.length);
            for (int row = 0; row < rectInfo.length; row++) {
                ps.print("Rect " + row + ": ");
//                System.out.println("Rect " + row + ": ");
                for (int param = 0; param < rectInfo[row].length; param++) {
                    ps.print(rectInfo[row][param] + " ");
                }
                ps.println();
            }

            e.printStackTrace(ps);
            ps.close();

            try {
                modelName = exportDir + filenamePrefix + fileIter + "_mixer(error)";
                fileIter++;
                model.save(modelName);
            } catch (Exception f) {
                System.out.println("Failed to create errored mph file in the Exception.");
                f.printStackTrace();
            }

            System.out.println("got to end of exception");
        }
    }

    public boolean getErrorStatus() {
        return hadError;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double[][] getConc() throws IOException {
        System.out.println(exitConcFilename + "\n");
        BufferedReader br = new BufferedReader(new FileReader(exitConcFilename));
        return addDatapts(br);
    }

    public double[][] getPressure() throws IOException {
        System.out.println(entryPressureFilename + "\n");
        BufferedReader br = new BufferedReader(new FileReader(entryPressureFilename));
        return addDatapts(br);
    }

    public String getLastExitConcFilename() {
        return exitConcFilename;
    }

    public String getLastConcImgFilename() {
        return concImgFilename;
    }

    public String getLastModelName() {
        return modelName + ".mph";
    }

    public String getLastConcGraphFilename() {
        return concGraphFilename;
    }

    public String getLastPressureGraphFilename() {
        return pressureGraphFilename;
    }

    public String getLastEntryPressureFilename() {
        return entryPressureFilename;
    }

    public void setFilenamePrefix(String prefix) {
        filenamePrefix = prefix;
    }

    public void start() throws IOException {
        inletMPerSec = 0.001;

        try {
            Instant start = Instant.now();
            model = createInitialGeoms(); //originally run()
            Instant stop = Instant.now();
            System.out.println("Initial setup: " + Duration.between(start, stop).toMillis());
            totalTime += Duration.between(start, stop).toMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }

//    Instant start = Instant.now();
        runResultSetup();
//    Instant stop = Instant.now();
//    System.out.println("Initial run results setup and computation: " + Duration.between(start, stop).toMillis());

        updateChipRandCircles();
        for (int i = 1; i <= 10; i++) { //change to 9 if testing mesh
            System.out.println("\nUpdate Chip " + i);
            Instant start2 = Instant.now();
            updateChipRandCircles();
            Instant stop2 = Instant.now();
            System.out.println("Updating chip's geometries: " + Duration.between(start2, stop2).toMillis());
            totalTime += Duration.between(start2, stop2).toMillis();

//      setMeshSize(i);

//      Instant start3 = Instant.now();
            runResultSetup();
//      Instant stop3 = Instant.now();
//      System.out.println("Loop " + i + " run results setup and computation: " +
//              Duration.between(start2, stop2).toMillis());
        }
    }

    /**
     * Does the setup and execution of the running the solution and output.
     */
    private void runResultSetup() {
        model.sol(solName).attach("std1");
        int j = 0;

//        double[][] edges = getEdges();
//        double[][] vertices = getVertices();
//        System.out.println("edges");
//        for (int i = 0; i < edges.length; i++) {
//            for (int k = 0; k < edges[i].length;k++) {
//                System.out.print(edges[i][k] + " ");
//            }
//            System.out.println();
//        }
//        System.out.println("vertices");
//        for (int i = 0; i < vertices.length; i++) {
//            for (int k= 0; k < vertices[i].length; k++) {
//                System.out.print(vertices[i][k] + " ");
//            }
//            System.out.println();
//        }

        Instant start = Instant.now();
        try {
            model.sol(solName).runAll();
        } catch (Exception e) {
            e.printStackTrace();
            hadError = true;
        }
        Instant stop = Instant.now();
        System.out.println("model.sol(\"sol1\").runAll() method: " + Duration.between(start, stop).toMillis());
        totalTime += Duration.between(start, stop).toMillis();

        Instant start2 = Instant.now();
        //does the setup of the .txt file of the line graph's conc values at the exit
//    exitConcFilename = exportDir  + filenamePrefix + fileIter + "_exitconc"+ ".txt";

        if (hadError == true) {
            exitConcFilename = exportDir + filenamePrefix + fileIter + "_exitconc(error).txt";
            entryPressureFilename = exportDir + filenamePrefix + fileIter + "_entrypressure(error).txt";
            concImgFilename = exportDir + filenamePrefix + fileIter + "_concimg(error).png";
            concGraphFilename = exportDir + filenamePrefix + fileIter + "_concgraph(error).png";
            pressureGraphFilename = exportDir + filenamePrefix + fileIter + "_pressuregraph(error).png";
            modelName = exportDir + filenamePrefix + fileIter + "_mixer(error)";
            fullChipPressureFilename = exportDir + filenamePrefix + fileIter + "_fullchippressure(error).txt";
            fullChipConcFilename = exportDir + filenamePrefix + fileIter + "_fullchipconc(error).txt";

        } else {
            exitConcFilename = exportDir + filenamePrefix + fileIter + "_exitconc.txt";
            entryPressureFilename = exportDir + filenamePrefix + fileIter + "_entrypressure.txt";
            concImgFilename = exportDir + filenamePrefix + fileIter + "_concimg.png";
            pressureGraphFilename = exportDir + filenamePrefix + fileIter + "_pressuregraph.png";
            concGraphFilename = exportDir + filenamePrefix + fileIter + "_concgraph.png";
            modelName = exportDir + filenamePrefix + fileIter + "_mixer";
            fullChipPressureFilename = exportDir + filenamePrefix + fileIter + "_fullchippressure.txt";
            fullChipConcFilename = exportDir + filenamePrefix + fileIter + "_fullchipconc.txt";

        }

        model.result().export("plot1").set("plotgroup", "pg4");
        model.result().export("plot1").set("plot", "lngr1");
        model.result().export("plot1").set("filename", exitConcFilename);
        model.result().export("plot1").set("header", false);
        model.result().export("plot1").run();

        model.result().export("plot2").set("plotgroup", "pg5");
        model.result().export("plot2").set("plot", "lngr1");
        model.result().export("plot2").set("filename", entryPressureFilename);
        model.result().export("plot2").set("header", false);
        model.result().export("plot2").run();

        model.result().export("plot3").set("plotgroup", "pg1");
        model.result().export("plot3").set("filename", fullChipConcFilename);
        model.result().export("plot3").set("header", false);
        model.result().export("plot3").run();
        model.result().export("plot4").set("plotgroup", "pg3");
        model.result().export("plot4").set("filename", fullChipPressureFilename);
        model.result().export("plot4").set("header", false);
        model.result().export("plot4").set("plot", "surf1");
        model.result().export("plot4").run();

        //does the setup of an image file of the concentration gradient
        model.result().export("img1").set("imagetype", "png");
        model.result().export("img1").set("axes2d", "on");
        model.result().export("img1").set("legend1d", "on");
        model.result().export("img1").set("legend2d", "on");
        model.result().export("img1").set("axes1d", "on");
        model.result().export("img1").set("axes2d", "on");
        model.result().export("img1").set("logo2d", "on");
        model.result().export("img1").set("title2d", "off");
        model.result().export("img1").set("options2d", true);
        model.result().export("img1").set("pngfilename", concImgFilename);

        model.result().export("img1").run(); //not auto-populated by COMSOL - needed for export to run

        //does the setup of an image file of the velocity
//    model.result().export("img3").set("imagetype", "png");
//    model.result().export("img3").set("axes2d", "on");
//    model.result().export("img3").set("legend2d", "on");
//    model.result().export("img3").set("options2d", true);
//    model.result().export("img3").set("pngfilename", exportDir + filenamePrefix +  fileIter + "_velocityimg" + ".png");
//    model.result().export("img3").run(); //not auto-populated by COMSOL - needed for export to run

        //does the setup of an image file of the concentration gradient at the exit point
        model.result().export("img2").set("imagetype", "png");
        model.result().export("img2").set("pngfilename", concGraphFilename);
        model.result().export("img2").run(); //not auto-populated by COMSOL - needed for export to run

        //does the setup of an image file of the concentration gradient at the exit point
        model.result().export("img4").set("imagetype", "png");
        model.result().export("img4").set("pngfilename", pressureGraphFilename);
        model.result().export("img4").run(); //not auto-populated by COMSOL - needed for export to run

        try {
            model.resetHist();
        } catch (Exception e) { }

//    if(!hadError)
//      model.save(exportDir  + "Mixer" + filenamePrefix + fileIter, "java");

        try {
            model.save(modelName);
        } catch (IOException e) {
            System.out.println("Failed to create mph file.");
        }
        fileIter++;
        Instant stop2 = Instant.now();
        System.out.println("Creating exports: " + Duration.between(start2, stop2).toMillis());
        totalTime += Duration.between(start2, stop2).toMillis();
    }

//  private double getAverageConc(double[][] data) {
//    int count = data.length;
//    double sum = 0;
//    for (double[] row : data)
//      sum += row[1];
//    return sum/(count * 1.0);
//  }

    private double[][] readFirstHalfConcs(String filename) throws IOException {
        ArrayList<double[]> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(exitConcFilename));
        double midpt = 0.5;
        return addDatapts(br, data, midpt);
    }

    private double[][] addDatapts(BufferedReader br) throws IOException {
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

    private double[][] addDatapts(BufferedReader br, ArrayList<double[]> data, double maxYPos) throws IOException {
        String s = br.readLine();
        String[] split = s.split("\\s+");
        double yPos = Double.parseDouble(split[0]);
        if (yPos < maxYPos) {
            double[] datapt = {yPos, Double.parseDouble(split[1])};
            data.add(datapt);
            return addDatapts(br, data, maxYPos);
        } else {
            double[][] result = new double[data.size()][2];
            for (int i = 0; i < result.length; i++) {
                result[i] = data.get(i);
            }
            return result;
        }
    }

    /**
     * Sets the mesh size with a given string size, with same text as what is found in COMSOL.
     *
     * @param size String corresponding to COMSOL mesh setting
     */
    void setMeshSize(String size) {
        int sizeIndex = -1;
        for (int i = 0; i < COMSOL_MESH_NAMES.length; i++) {
            if (size.equals(COMSOL_MESH_NAMES[i])) {
                sizeIndex = i;
                i = COMSOL_MESH_NAMES.length;
            }
        }
        if (sizeIndex == -1) {
            System.out.println("Incorrect text input (" + size + ") for mesh size (must match COMSOL). Set mesh to Normal.");
            setMeshSize(5);
        } else
            setMeshSize(sizeIndex + 1);
    }

    /**
     * Sets the mesh size with a given int size based on COMSOL's values. 1 is set to finest and 9 is set to coarsest
     * setting.
     *
     * @param size Value corresponding to how coarse or how fine the mesh should be
     */
    private void setMeshSize(int size) {
        model.component(compName).mesh("mesh1").autoMeshSize(size);
    }

    /**
     * Updates the chip to add random circles for testing funectionality.
     */
    private void updateChipRandCircles() {
        removeCircles();
        addRandSizedCircles(20, 0.05, 0.1, -5, 1.2, 0, 1);
        String[] circlesArr = new String[circleNames.size()];
        circlesArr = circleNames.toArray(circlesArr);
        String[] rectArr = new String[rectNames.size()];
        rectArr = rectNames.toArray(rectArr);

        try {
            model.component(compName).geom(geomName).create(diffName, "Difference");
        } catch (Exception e) {
            model.component(compName).geom(geomName).feature().remove("dif1");
            model.component(compName).geom(geomName).create(diffName, "Difference");
        }

        model.component(compName).geom(geomName).feature(diffName).selection("input").set(rectArr);
        model.component(compName).geom(geomName).feature(diffName).selection("input2")
                .set(circlesArr);

        model.component(compName).geom(geomName).run();
        model.component(compName).geom(geomName).runPre("fin");

        recreateSol("st1", "v1", "s1");
    }

    /**
     * Removes all circles from the chip.
     */
    private void removeCircles() {
        if (circleNames.size() > 0) {
            for (String name : circleNames) {
                model.component(compName).geom(geomName).feature().remove(name);
            }
            circleNames = new ArrayList<>();
        }
    }

    private void removeRectPosts() {
        if (rectPostNames.size() > 0) {
            for (String name : rectPostNames) {
                model.component(compName).geom(geomName).feature().remove(name);
            }
            rectPostNames = new ArrayList<>();
        }
    }

    /**
     * Removes a given circle from a chip.
     *
     * @param name The name of the circle to remove.
     */
    private void removeCircle(String name) {
        removeGeom(name);
        circleNames.remove(name);
    }

    /**
     * Removes a given geometry from the current chip.
     *
     * @param name The name of the geometry
     */
    private void removeGeom(String name) {
        model.component(compName).geom(geomName).feature().remove(name);
    }

    /**
     * Adds random circles to the current chip with given quantity, radius range, and position boundaries. This is for
     * testing purposes. A boundary buffer is added to ensure there is no collision from rounding.
     *
     * @param numCircles The number of circles to add
     * @param minRadius  The minimum radius for the circles
     * @param maxRadius  The maximum radius for the circles
     * @param minX       The minimum X position for the center of a circle
     * @param maxX       The maximum X position for the center of a circle
     * @param minY       The minimum Y position for the center of a circle
     * @param maxY       The maximum Y position for the center of a circle
     */
    public void addRandSizedCircles(int numCircles, double minRadius, double maxRadius, double minX, double maxX,
                                    double minY, double maxY) {
        currentCircleSet = new double[numCircles][3];
        int precision = 2;
        for (int i = 1; i <= numCircles; i++) {
            double radius = Precision.round((minRadius + Math.random() * (maxRadius - minRadius)), precision);
            double minBound = minX + radius + boundaryBuffer;
            double maxBound = maxX - radius - boundaryBuffer;
            double posX = ((int) ((minBound + Math.random() * (maxBound - minBound)) * 100)) / 100.0;
            minBound = minY + radius + boundaryBuffer;
            maxBound = maxY - radius - boundaryBuffer;
            double posY = ((int) ((minBound + Math.random() * (maxBound - minBound)) * 100)) / 100.0;
            addCircle("c" + i, posX, posY, radius);
            double[] circleData = {posX, posY, radius};
            currentCircleSet[i - 1] = circleData;
        }
    }

    /**
     * Adds a circle geometry to the current chip with given name, position, and radius of the circle.
     *
     * @param name   The name of the circle to add
     * @param posX   The x position of the center of the circle
     * @param posY   The y position of the center of the circle
     * @param radius The radius of the circle
     */
    public void addCircle(String name, double posX, double posY, double radius) {
        circleNames.add(name);
        model.component(compName).geom(geomName).create(name, "Circle");
        model.component(compName).geom(geomName).feature(name).set("pos", new double[]{posX, posY});
        model.component(compName).geom(geomName).feature(name).set("r", radius);
    }

    public void addRectPosts(String name, double posX, double posY, double width, double height, double rotation) {
        rectPostNames.add(name);
        model.component(compName).geom(geomName).create(name, "Rectangle");
        model.component(compName).geom(geomName).feature(name).set("size", new double[]{width, height});
        model.component(compName).geom(geomName).feature(name).set("pos", new double[]{posX, posY});
        model.component(compName).geom(geomName).feature(name).set("rot", rotation);
    }

    /**
     * Adds a rectangle geometry to the current chip with a given name, position of the left bottom corner, width, and
     * height
     *
     * @param name
     * @param posX
     * @param posY
     * @param width
     * @param height
     */
    public void addRectangle(String name, double posX, double posY, double width, double height) {
        rectangles.add(new Rectangle(name, posX, posY, width, height));
        rectNames.add(name);
        model.component(compName).geom(geomName).create(name, "Rectangle");
        model.component(compName).geom(geomName).feature(name).set("pos", new double[]{posX, posY});
        model.component(compName).geom(geomName).feature(name).set("size", new double[]{width, height});
    }

    /**
     * This is the initial creation of the chip. It contains the first chip geometries, the setup of the material,
     * laminar flow, diluted species
     *
     * @return
     */
    public Model createInitialGeoms() {
        model = ModelUtil.create("Model");

        model.modelPath("C:\\Users\\Athena\\Desktop\\College stuff\\UCR\\Research\\COMSOL");

        model.label("Tsimplemixer.mph");

        model.component().create(compName, true);

        model.component(compName).geom().create(geomName, 2);

        model.component(compName).mesh().create("mesh1");
        model.component(compName).geom(geomName).lengthUnit("mm");

        //TODO eventually take out chip's rectangles hard-coding
        //adds the initial chip geometries and runs it
        addRectangle("channel1", MAIN_CHANNEL_DIM[0], MAIN_CHANNEL_DIM[1], MAIN_CHANNEL_DIM[2], MAIN_CHANNEL_DIM[3]);
//        addRectangle("channel2", CROSS_CHANNEL_DIM[0], CROSS_CHANNEL_DIM[1], CROSS_CHANNEL_DIM[2], CROSS_CHANNEL_DIM[3]);
        model.component(compName).geom(geomName).run();

        //TODO eventually allow for different materials
        initiateWaterMat();

        //TODO eventually put these in parts based on whether it is needed
        //Diluted Species initial setup
        model.component(compName).physics().create("tds", "DilutedSpecies", geomName);
        model.component(compName).physics("tds").create("in1", "Inflow", 1);
        model.component(compName).physics("tds").feature("in1").selection().set(
                getEdgeNum(getRectEdge("channel1", "left")));
//        model.component(compName).physics("tds").create("in2", "Inflow", 1);
//        model.component(compName).physics("tds").feature("in2").selection().set(
//                getEdgeNum(getRectEdge("channel2", "top")),
//                getEdgeNum(getRectEdge("channel2", "bottom")));
        model.component(compName).physics("tds").create("out1", "Outflow", 1);
        model.component(compName).physics("tds").feature("out1").selection().set(
                getEdgeNum(getRectEdge("channel1", "right")));

        //TODO eventually put these in parts based on whether it is needed
        //laminar flow initial setup
        model.component(compName).physics().create("spf", "LaminarFlow", geomName);
        model.component(compName).physics("spf").create("inl1", "InletBoundary", 1);
        model.component(compName).physics("spf").feature("inl1").selection().set(
                getEdgeNum(getRectEdge("channel1", "left"))
//                , getEdgeNum(getRectEdge("channel2", "top"))
//                , getEdgeNum(getRectEdge("channel2", "bottom"))
        );
        model.component(compName).physics("spf").create("out1", "OutletBoundary", 1);
        model.component(compName).physics("spf").feature("out1").selection().set(
                getEdgeNum(getRectEdge("channel1", "right")));

//    model.component(compName).physics("spf").feature("inl1").set("U0in", inletMPerSec);

        setMeshSize(DEFAULT_MESH_SIZE);
        model.component(compName).mesh("mesh1").automatic(true);

        //TODO eventually edit to adjust based on chip size
        //view setup
        model.component(compName).view("view1").axis().set("xmin", -5.154999732971191);
        model.component(compName).view("view1").axis().set("xmax", 1.3549991846084595);
        model.component(compName).view("view1").axis().set("ymin", -4.32006311416626);
        model.component(compName).view("view1").axis().set("ymax", 5.32006311416626);


        model.study().create("std1");
        model.study("std1").create("stat", "Stationary");

        model.sol().create(solName);

        createSol("st1", "v1", "s1");

        createResultObjects();

        return model;
    }

    public Model createInitialGeoms(double stoichiometricCoeff) {
        model = ModelUtil.create("Model");

        model.modelPath("C:\\Users\\Athena\\Desktop\\College stuff\\UCR\\Research\\COMSOL");

        model.label("Tsimplemixer.mph");

        model.component().create(compName, true);

        model.component(compName).geom().create(geomName, 2);

        model.component(compName).mesh().create("mesh1");
        model.component(compName).geom(geomName).lengthUnit("mm");

        //TODO eventually take out chip's rectangles hard-coding
        //adds the initial chip geometries and runs it
        addRectangle("channel1", MAIN_CHANNEL_DIM[0], MAIN_CHANNEL_DIM[1], MAIN_CHANNEL_DIM[2], MAIN_CHANNEL_DIM[3]);
//        addRectangle("channel2", CROSS_CHANNEL_DIM[0], CROSS_CHANNEL_DIM[1], CROSS_CHANNEL_DIM[2], CROSS_CHANNEL_DIM[3]);
        model.component(compName).geom(geomName).run();

        //TODO eventually allow for different materials
        initiateWaterMat();

        //TODO eventually put these in parts based on whether it is needed
        //Diluted Species initial setup
        model.component(compName).physics().create("tds", "DilutedSpecies", geomName);
        model.component(compName).physics("tds").create("in1", "Inflow", 1);
        model.component(compName).physics("tds").feature("in1").selection().set(
                getEdgeNum(getRectEdge("channel1", "left")));
//        model.component(compName).physics("tds").create("in2", "Inflow", 1);
//        model.component(compName).physics("tds").feature("in2").selection().set(
//                getEdgeNum(getRectEdge("channel2", "top")),
//                getEdgeNum(getRectEdge("channel2", "bottom")));
        model.component(compName).physics("tds").create("out1", "Outflow", 1);
        model.component(compName).physics("tds").feature("out1").selection().set(
                getEdgeNum(getRectEdge("channel1", "right")));

        //TODO eventually put these in parts based on whether it is needed
        //laminar flow initial setup
        model.component(compName).physics().create("spf", "LaminarFlow", geomName);
        model.component(compName).physics("spf").create("inl1", "InletBoundary", 1);
        model.component(compName).physics("spf").feature("inl1").selection().set(
                getEdgeNum(getRectEdge("channel1", "left"))
//                , getEdgeNum(getRectEdge("channel2", "top"))
//                , getEdgeNum(getRectEdge("channel2", "bottom"))
        );
        model.component(compName).physics("spf").create("out1", "OutletBoundary", 1);
        model.component(compName).physics("spf").feature("out1").selection().set(
                getEdgeNum(getRectEdge("channel1", "right")));


        model.component(compName).physics("tds").feature("cdm1").set("u_src", "root.comp1.u");
//        model.component(compName).physics("tds").feature("in1").set("c0", 1);
        model.component("comp1").physics("tds").feature("in1").setIndex("c0", inflowConc1, 0);
//        model.component("comp1").physics("tds").feature("in2").setIndex("c0", inflowConc2, 0);
        model.component(compName).physics("tds").create(fisrName, "FastIrreversibleSurfaceReaction", 1);
        model.component("comp1").physics("tds").feature(fisrName).setIndex("Vi0", stoichiometricCoeff, 0);
//    model.component(compName).physics("spf").feature("inl1").set("U0in", inletMPerSec);

        setMeshSize(DEFAULT_MESH_SIZE);
        model.component(compName).mesh("mesh1").automatic(true);

        //TODO eventually edit to adjust based on chip size
        //view setup
        model.component(compName).view("view1").axis().set("xmin", -5.154999732971191);
        model.component(compName).view("view1").axis().set("xmax", 1.3549991846084595);
        model.component(compName).view("view1").axis().set("ymin", -4.32006311416626);
        model.component(compName).view("view1").axis().set("ymax", 5.32006311416626);


        model.study().create("std1");
        model.study("std1").create("stat", "Stationary");

        model.sol().create(solName);

        createSol("st1", "v1", "s1");

        createResultObjects();
        model.hist().disable();

        return model;
    }

    /**
     * This method creates all result objects. This method must be called in the initial run and can never be called
     * more than once.
     */
    private void createResultObjects() {
        //TODO eventually allow to alter what results are needed to create
        model.result().create("pg1", "PlotGroup2D");
        model.result().create("pg2", "PlotGroup2D");
        model.result().create("pg3", "PlotGroup2D");
        model.result().create("pg4", "PlotGroup1D");
        model.result().create("pg5", "PlotGroup1D");
        model.result("pg1").create("surf1", "Surface");
        model.result("pg1").create("con1", "Contour");
        model.result("pg2").create("surf1", "Surface");
        model.result("pg2").feature("surf1").set("expr", "spf.U");

        //new for analysis
        model.result("pg2").create("surf2", "Surface");
        model.result("pg2").create("surf3", "Surface");
        model.result("pg2").create("arws1", "ArrowSurface");
        model.result("pg2").create("str1", "Streamline");
        model.result("pg2").feature("str1").selection().set(1);
        model.result("pg2").feature("surf2").set("expr", "u");
        model.result("pg2").feature("surf3").set("expr", "v");

//        model.result("pg3").create("con1", "Contour");
//        model.result("pg3").feature("con1").set("expr", "p");
        model.result("pg3").create("surf1", "Surface");
        model.result("pg3").feature("surf1").set("expr", "p");
        model.result("pg4").create("lngr1", "LineGraph");
        model.result("pg4").feature("lngr1").selection().set(getEdgeNum(getRectEdge("channel1", "right")));
        model.result("pg5").create("lngr1", "LineGraph");
        model.result("pg5").feature("lngr1").selection().set(getEdgeNum(getRectEdge("channel1", "left")));
        model.result("pg5").feature("lngr1").set("expr", "p");


        model.result("pg1").label("Concentration (tds)");
        model.result("pg1").set("titletype", "custom");
        model.result("pg1").feature("surf1").set("descr", "Concentration");
        model.result("pg1").feature("surf1").set("rangecoloractive", true);
        model.result("pg1").feature("surf1").set("rangecolormax", 1);
        model.result("pg1").feature("surf1").set("resolution", "normal");
        model.result("pg1").feature("con1").set("levelmethod", "levels");
        model.result("pg1").feature("con1").set("levels", 0.5);
        model.result("pg1").feature("con1").set("coloring", "uniform");
        model.result("pg1").feature("con1").set("color", "black");
        model.result("pg1").feature("con1").set("resolution", "normal");

        model.result("pg2").label("Velocity (spf)");
        model.result("pg2").set("frametype", "spatial");
        model.result("pg2").feature("surf1").label("Surface");
        model.result("pg2").feature("surf1").set("smooth", "internal");
        model.result("pg2").feature("surf1").set("resolution", "normal");

        //new for analysis
        model.result("pg2").feature("surf2").active(false);
        model.result("pg2").feature("surf2").set("resolution", "normal");
        model.result("pg2").feature("surf3").active(false);
        model.result("pg2").feature("surf3").set("resolution", "normal");
        model.result("pg2").feature("arws1").set("expr", new String[]{"u", "v"});
        model.result("pg2").feature("arws1").set("descr", "Velocity field");
        model.result("pg2").feature("arws1").set("scale", 96.87740702794427);
        model.result("pg2").feature("arws1").set("scaleactive", false);
        model.result("pg2").feature("str1").set("posmethod", "start");
        model.result("pg2").feature("str1").set("pointtype", "arrow");
        model.result("pg2").feature("str1").set("arrowcount", 70);
        model.result("pg2").feature("str1").set("arrowscale", 51.057192285725385);
        model.result("pg2").feature("str1").set("arrowcountactive", false);
        model.result("pg2").feature("str1").set("arrowscaleactive", false);
        model.result("pg2").feature("str1").set("resolution", "normal");

        model.result("pg3").label("Pressure (spf)");
        model.result("pg3").set("frametype", "spatial");
//        model.result("pg3").feature("con1").label("Contour");
//        model.result("pg3").feature("con1").set("number", 40);
//        model.result("pg3").feature("con1").set("levelrounding", false);
//        model.result("pg3").feature("con1").set("smooth", "internal");
//        model.result("pg3").feature("con1").set("resolution", "normal");
        model.result("pg3").feature("surf1").set("resolution", "normal");

        model.result("pg4").set("xlabel", "Exit Position (top to bottom)");
        model.result("pg4").set("xlabelactive", true);
        model.result("pg4").feature("lngr1").set("resolution", "normal");
        model.result("pg4").set("axislimits", true);
        model.result("pg4").set("xmin", 0);
        model.result("pg4").set("xmax", 1);
        model.result("pg4").set("ymin", 0);
        model.result("pg4").set("ymax", 1);

        model.result("pg5").set("xlabel", "Entry Position (top to bottom)");
        model.result("pg5").set("xlabelactive", true);
        model.result("pg5").feature("lngr1").set("resolution", "normal");

        model.result().export().create("plot1", "Plot");
        model.result().export().create("plot2", "pg5", "lngr1", "Plot");
        model.result().export().create("plot3", "Plot");
        model.result().export().create("plot4", "Plot");

        model.result().export().create("img1", "pg1", "Image");
        model.result().export().create("img2", "pg4", "Image");
        model.result().export().create("img3", "pg2", "Image");
        model.result().export().create("img4", "pg5", "Image");

//        model.result().numerical().create("av1", "AvLine");
//        model.result().numerical("av1").set("intsurface", true);
//        model.result().numerical("av1").selection().set(getEdgeNum(getRectEdge(rectNames.get(0), "left")));
//        model.result().numerical("av1").set("expr", new String[]{"p"});
//        model.result().numerical("av1").set("descr", new String[]{"Pressure"});
//        model.result().table().create("tbl1", "Table");
//        model.result().table("tbl1").comments("Line Average 1");
//        model.result().numerical("av1").set("table", "tbl1");
//        model.result().numerical("av1").setResult();
//        double[][] avgPressure = model.result().table("tbl1").getReal();
//        System.out.println("Table row: " + avgPressure.length);
//        System.out.println("Table col: " + avgPressure[0].length);
//        for (double[] row : avgPressure) {
//            for (double value : row) {
//                System.out.println(value);
//            }
//        }


    }

    private void recreateSol(String studyStepName, String varName, String stationaryName) {
//    model.study("std1").feature("stat").set("notlistsolnum", 1);
//    model.study("std1").feature("stat").set("notsolnum", "1");
//    model.study("std1").feature("stat").set("listsolnum", 1);
//    model.study("std1").feature("stat").set("solnum", "1");
        model.sol(solName).feature().remove(studyStepName);
        model.sol(solName).feature().remove(varName);
        model.sol(solName).feature().remove(stationaryName);
        createSol(studyStepName, varName, stationaryName);
    }

    private void createSol(String studyStepName, String varName, String stationaryName) {
        model.sol(solName).study("std1");
        model.sol(solName).create(studyStepName, "StudyStep");
        model.sol(solName).create(varName, "Variables");
        model.sol(solName).create(stationaryName, "Stationary");

        model.sol(solName).feature(studyStepName).set("study", "std1");
        model.sol(solName).feature(studyStepName).set("studystep", "stat");
        model.sol(solName).feature(varName).set("control", "stat");

//    model.sol(solName).feature("s1").set("stol", 0.001);
//    model.sol(solName).feature("s1").create("seDef", "Segregated");
        model.sol(solName).feature(stationaryName).create("fc1", "FullyCoupled");
        model.sol(solName).feature(stationaryName).create("d1", "Direct");
        model.sol(solName).feature(stationaryName).create("i1", "Iterative");
        model.sol(solName).feature(stationaryName).feature("i1").create("mg1", "Multigrid");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("pr").create("sc1", "SCGS");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("po").create("sc1", "SCGS");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("cs").create("d1", "Direct");
        model.sol(solName).feature(stationaryName).feature().remove("fcDef");
//    model.sol(solName).feature("s1").feature().remove("seDef");

        model.sol(solName).feature(stationaryName).feature("aDef").set("cachepattern", true);
        model.sol(solName).feature(stationaryName).feature("fc1").set("linsolver", "d1");
        model.sol(solName).feature(stationaryName).feature("fc1").set("initstep", 0.01);
        model.sol(solName).feature(stationaryName).feature("fc1").set("minstep", 1.0E-6);
        model.sol(solName).feature("s1").feature("fc1").set("dtech", "auto");
        model.sol(solName).feature(stationaryName).feature("fc1").set("maxiter", 100);
        model.sol(solName).feature(stationaryName).feature("d1").label("Direct, fluid flow variables (spf) (merged)");
        model.sol(solName).feature(stationaryName).feature("d1").set("linsolver", "pardiso");
        model.sol(solName).feature(stationaryName).feature("d1").set("pivotperturb", 1.0E-13);
        model.sol(solName).feature(stationaryName).feature("i1").label("AMG, fluid flow variables (spf)");
        model.sol(solName).feature(stationaryName).feature("i1").set("nlinnormuse", true);
        model.sol(solName).feature(stationaryName).feature("i1").set("maxlinit", 200);
        model.sol(solName).feature(stationaryName).feature("i1").set("rhob", 20);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").set("prefun", "saamg");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").set("maxcoarsedof", 80000);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").set("strconn", 0.02);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").set("saamgcompwise", true);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").set("usesmooth", false);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("pr").feature("sc1")
                .set("linesweeptype", "ssor");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("pr").feature("sc1").set("iter", 0);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("pr").feature("sc1")
                .set("scgsvertexrelax", 0.7);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("po").feature("sc1")
                .set("linesweeptype", "ssor");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("po").feature("sc1").set("iter", 1);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("po").feature("sc1")
                .set("scgsvertexrelax", 0.7);
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("cs").feature("d1")
                .set("linsolver", "pardiso");
        model.sol(solName).feature(stationaryName).feature("i1").feature("mg1").feature("cs").feature("d1")
                .set("pivotperturb", 1.0E-13);
    }

    private void applyFISR(int[] edges, String fisrName) {
//        System.out.print("File "+ fileIter + ": ");
        for (int edge : edges) {
//            System.out.print(edge + " ");
            model.component(compName).physics("tds").feature(fisrName).selection().set(edge);
        }
        System.out.println();
        model.component(compName).physics("tds").feature(fisrName).selection().set(edges);
    }

    private int[] getFISREdges(double[][] edges, double[][] vertices) {
        ArrayList<Integer> results = new ArrayList<>();
        ArrayList<Integer> allowedVertices = new ArrayList<>();
        int numVertices = vertices[0].length;
        int numEdges = edges[0].length;
        double leftBound = MAIN_CHANNEL_DIM[0];
        double bottomBound = MAIN_CHANNEL_DIM[1];
        double rightBound = MAIN_CHANNEL_DIM[0] + MAIN_CHANNEL_DIM[2];
        double topBound = MAIN_CHANNEL_DIM[1] + MAIN_CHANNEL_DIM[3];
        final double EPSILON = 1E-4;

//        for (int i = 0; i < numVertices; i++) {
//            double vert1 = vertices[0][i];
//            double vert2 = vertices[1][i];
////            System.out.println("Vert  " + i + ": " + vert1 + ", " + vert2);
////            if (vert1 > leftBound + EPSILON && vert1 < rightBound - EPSILON && vert2 > bottomBound + EPSILON && vert2 <
////                    topBound - EPSILON)
//            if (vert1 >= leftBound && vert1 <= rightBound)
////            if (vert1 > leftBound && vert1 < rightBound && vert2 > bottomBound && vert2 < topBound)
//            {
//                    allowedVertices.add(i + 1);
////                    System.out.println("Allowed vert (" + (i+1) + "): " + vert1 + ", " + vert2);
//            }
//        }
//
//        for (int i = 0; i < numEdges; i++) {
//            int edge1 = (int) edges[0][i];
//            int edge2 = (int) edges[1][i];
////            System.out.println("edge row: " + i + " , " + edges[0][i] + " " + edges[1][i]);
//            if (allowedVertices.contains(edge1) && allowedVertices.contains(edge2)) {
//                results.add(i + 1);
////                System.out.println("Allowed edge (" + (i+1) + "): " + edge1 + ", " + edge2);
//            }
//        }

        int leftEdgeNum = getEdgeNum(getRectEdge(rectNames.get(0), "left"));
        int rightEdgeNum = getEdgeNum(getRectEdge(rectNames.get(0), "right"));
        for (int edgeNum = 1; edgeNum <= numEdges; edgeNum++) {
            if (!(edgeNum == leftEdgeNum || edgeNum == rightEdgeNum)) {
                results.add(edgeNum);
            }
        }
        int[] intResult = new int[results.size()];
        for (int i = 0; i < results.size(); i++) {
            intResult[i] = results.get(i);
        }
        return intResult;
    }

    public int getEdgeNum(double[] verticesCoords) {
        int vert1Index = getVertexNum(verticesCoords[0], verticesCoords[1]);
        int vert2Index = getVertexNum(verticesCoords[2], verticesCoords[3]);
        if (vert1Index == -1 || vert2Index == -1)
            return -1;

        double[][] edges = getEdges();
        for (int i = 0; i < edges[0].length; i++) {
            double vert1 = edges[0][i];
            double vert2 = edges[1][i];
            if ((vert1Index == vert1 && vert2Index == vert2) || (vert2Index == vert1 && vert1Index == vert2))
                return i + 1;
        }
        return -2;
    }

    public int getVertexNum(double x, double y) {
        double[][] vertices = getVertices();
        for (int i = 0; i < vertices[0].length; i++) {
            double xCoord = vertices[0][i];
            double yCoord = vertices[1][i];
            if ((x == xCoord && y == yCoord) || (y == xCoord && x == yCoord)) {
                return i + 1;
            }
        }
        return -1;
    }

    public double[] getRectEdge(String rectName, String side) {
        Rectangle rect = getRect(rectName);
        double vert1X;
        double vert1Y;
        double vert2X;
        double vert2Y;
        boolean isLeft = side.equals("left");
        boolean isRight = side.equals("right");

        if (isLeft || side.equals("bottom")) {
            vert1X = rect.verticesCoords[0][0];
            vert1Y = rect.verticesCoords[0][1];
            if (isLeft) {
                vert2X = rect.verticesCoords[1][0];
                vert2Y = rect.verticesCoords[1][1];
            } else {
                vert2X = rect.verticesCoords[3][0];
                vert2Y = rect.verticesCoords[3][1];
            }
        } else if (isRight || side.equals("top")) {
            vert1X = rect.verticesCoords[2][0];
            vert1Y = rect.verticesCoords[2][1];
            if (side.equals("right")) {
                vert2X = rect.verticesCoords[3][0];
                vert2Y = rect.verticesCoords[3][1];
            } else {
                vert2X = rect.verticesCoords[1][0];
                vert2Y = rect.verticesCoords[1][1];
            }
        } else {
            return null;
        }
        double[] verticesCoords = {vert1X, vert1Y, vert2X, vert2Y};
        return verticesCoords;
    }

    public Rectangle getRect(String name) {
        for (int i = 0; i < rectangles.size(); i++) {
            Rectangle rect = rectangles.get(i);
            if (rect.name.equals(name))
                return rect;
        }
        return null;
    }

    public double[][] getEdges() {
        double[][] edgeData = model.component(compName).geom(geomName).getEdge();
        return edgeData;
    }

    /**
     * Gets the list of all vertices in the chip from COMSOL.
     *
     * @return The list of vertices in the chip from COMSOL.
     */
    public double[][] getVertices() {
        double[][] vertexData = model.component(compName).geom(geomName).getVertex();
        return vertexData;
    }

    /**
     * Does the setup of the material for water.
     **/
    private void initiateWaterMat() {
        model.component(compName).material().create("mat1", "Common");
        model.component(compName).material("mat1").propertyGroup("def").func().create("eta", "Piecewise");
        model.component(compName).material("mat1").propertyGroup("def").func().create("Cp", "Piecewise");
        model.component(compName).material("mat1").propertyGroup("def").func().create("rho", "Piecewise");
        model.component(compName).material("mat1").propertyGroup("def").func().create("k", "Piecewise");
        model.component(compName).material("mat1").propertyGroup("def").func().create("cs", "Interpolation");
        model.component(compName).material("mat1").propertyGroup("def").func().create("an1", "Analytic");
        model.component(compName).material("mat1").propertyGroup("def").func().create("an2", "Analytic");
        model.component(compName).material("mat1").propertyGroup("def").func().create("an3", "Analytic");
        model.component(compName).material("mat1").label("Water");
        model.component(compName).material("mat1").set("family", "water");
        model.component(compName).material("mat1").propertyGroup("def").func("eta").set("arg", "T");
        model.component(compName).material("mat1").propertyGroup("def").func("eta")
                .set("pieces", new String[][]{{"273.15", "413.15", "1.3799566804-0.021224019151*T^1+1.3604562827E-4*T^2-4.6454090319E-7*T^3+8.9042735735E-10*T^4-9.0790692686E-13*T^5+3.8457331488E-16*T^6"}, {"413.15", "553.75", "0.00401235783-2.10746715E-5*T^1+3.85772275E-8*T^2-2.39730284E-11*T^3"}});
        model.component(compName).material("mat1").propertyGroup("def").func("eta").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("eta").set("fununit", "Pa*s");
        model.component(compName).material("mat1").propertyGroup("def").func("Cp").set("arg", "T");
        model.component(compName).material("mat1").propertyGroup("def").func("Cp")
                .set("pieces", new String[][]{{"273.15", "553.75", "12010.1471-80.4072879*T^1+0.309866854*T^2-5.38186884E-4*T^3+3.62536437E-7*T^4"}});
        model.component(compName).material("mat1").propertyGroup("def").func("Cp").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("Cp").set("fununit", "J/(kg*K)");
        model.component(compName).material("mat1").propertyGroup("def").func("rho").set("arg", "T");
        model.component(compName).material("mat1").propertyGroup("def").func("rho").set("smooth", "contd1");
        model.component(compName).material("mat1").propertyGroup("def").func("rho")
                .set("pieces", new String[][]{{"273.15", "293.15", "0.000063092789034*T^3-0.060367639882855*T^2+18.9229382407066*T-950.704055329848"}, {"293.15", "373.15", "0.000010335053319*T^3-0.013395065634452*T^2+4.969288832655160*T+432.257114008512"}});
        model.component(compName).material("mat1").propertyGroup("def").func("rho").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("rho").set("fununit", "kg/m^3");
        model.component(compName).material("mat1").propertyGroup("def").func("k").set("arg", "T");
        model.component(compName).material("mat1").propertyGroup("def").func("k")
                .set("pieces", new String[][]{{"273.15", "553.75", "-0.869083936+0.00894880345*T^1-1.58366345E-5*T^2+7.97543259E-9*T^3"}});
        model.component(compName).material("mat1").propertyGroup("def").func("k").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("k").set("fununit", "W/(m*K)");
        model.component(compName).material("mat1").propertyGroup("def").func("cs")
                .set("table", new String[][]{{"273", "1403"},
                        {"278", "1427"},
                        {"283", "1447"},
                        {"293", "1481"},
                        {"303", "1507"},
                        {"313", "1526"},
                        {"323", "1541"},
                        {"333", "1552"},
                        {"343", "1555"},
                        {"353", "1555"},
                        {"363", "1550"},
                        {"373", "1543"}});
        model.component(compName).material("mat1").propertyGroup("def").func("cs").set("interp", "piecewisecubic");
        model.component(compName).material("mat1").propertyGroup("def").func("cs").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("cs").set("fununit", "m/s");
        model.component(compName).material("mat1").propertyGroup("def").func("an1").label("Analytic ");
        model.component(compName).material("mat1").propertyGroup("def").func("an1").set("funcname", "alpha_p");
        model.component(compName).material("mat1").propertyGroup("def").func("an1").set("expr", "-1/rho(T)*d(rho(T),T)");
        model.component(compName).material("mat1").propertyGroup("def").func("an1").set("args", new String[]{"T"});
        model.component(compName).material("mat1").propertyGroup("def").func("an1").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("an1").set("fununit", "1/K");
        model.component(compName).material("mat1").propertyGroup("def").func("an1")
                .set("plotargs", new String[][]{{"T", "273.15", "373.15"}});
        model.component(compName).material("mat1").propertyGroup("def").func("an2").set("funcname", "gamma_w");
        model.component(compName).material("mat1").propertyGroup("def").func("an2")
                .set("expr", "1+(T/Cp(T))*(alpha_p(T)*cs(T))^2");
        model.component(compName).material("mat1").propertyGroup("def").func("an2").set("args", new String[]{"T"});
        model.component(compName).material("mat1").propertyGroup("def").func("an2").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("an2").set("fununit", "1");
        model.component(compName).material("mat1").propertyGroup("def").func("an2")
                .set("plotargs", new String[][]{{"T", "273.15", "373.15"}});
        model.component(compName).material("mat1").propertyGroup("def").func("an3").set("funcname", "muB");
        model.component(compName).material("mat1").propertyGroup("def").func("an3").set("expr", "2.79*eta(T)");
        model.component(compName).material("mat1").propertyGroup("def").func("an3").set("args", new String[]{"T"});
        model.component(compName).material("mat1").propertyGroup("def").func("an3").set("argunit", "K");
        model.component(compName).material("mat1").propertyGroup("def").func("an3").set("fununit", "Pa*s");
        model.component(compName).material("mat1").propertyGroup("def").func("an3")
                .set("plotargs", new String[][]{{"T", "273.15", "553.75"}});
        model.component(compName).material("mat1").propertyGroup("def").set("thermalexpansioncoefficient", "");
        model.component(compName).material("mat1").propertyGroup("def").set("bulkviscosity", "");
        model.component(compName).material("mat1").propertyGroup("def")
                .set("thermalexpansioncoefficient", new String[]{"alpha_p(T)", "0", "0", "0", "alpha_p(T)", "0", "0", "0", "alpha_p(T)"});
        model.component(compName).material("mat1").propertyGroup("def").set("bulkviscosity", "muB(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("thermalexpansioncoefficient_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").descr("bulkviscosity_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").set("dynamicviscosity", "eta(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("dynamicviscosity_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").set("ratioofspecificheat", "gamma_w(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("ratioofspecificheat_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def")
                .set("electricconductivity", new String[]{"5.5e-6[S/m]", "0", "0", "0", "5.5e-6[S/m]", "0", "0", "0", "5.5e-6[S/m]"});
        model.component(compName).material("mat1").propertyGroup("def").descr("electricconductivity_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").set("heatcapacity", "Cp(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("heatcapacity_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").set("density", "rho(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("density_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def")
                .set("thermalconductivity", new String[]{"k(T)", "0", "0", "0", "k(T)", "0", "0", "0", "k(T)"});
        model.component(compName).material("mat1").propertyGroup("def").descr("thermalconductivity_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").set("soundspeed", "cs(T)");
        model.component(compName).material("mat1").propertyGroup("def").descr("soundspeed_symmetry", "");
        model.component(compName).material("mat1").propertyGroup("def").addInput("temperature");
    }

    class Rectangle {
        String name;
        double posX;
        double posY;
        double width;
        double height;
        double[][] verticesCoords;
        double rotation;

        public Rectangle(String name, double posX, double posY, double width, double height) {
            verticesCoords = new double[4][2];
            this.name = name;
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            //origin
            verticesCoords[0][0] = posX;
            verticesCoords[0][1] = posY;
            //top left corner
            verticesCoords[1][0] = posX;
            verticesCoords[1][1] = posY + height;
            //top right corner
            verticesCoords[2][0] = posX + width;
            verticesCoords[2][1] = posY + height;
            //bottom right corner
            verticesCoords[3][0] = posX + width;
            verticesCoords[3][1] = posY;

            rotation = 0;
        }

        public Rectangle(String name, double posX, double posY, double width, double height, double rotation) {
            this(name, posX, posY, width, height);

            this.rotation = rotation * Math.PI / 180.0;
            double cosine = Math.cos(rotation);
            double sine = Math.sin(rotation);

            //top left corner
            double newX = (cosine * verticesCoords[1][0]) - (sine * verticesCoords[1][1]);
            double newY = (sine * verticesCoords[1][0]) + (cosine * verticesCoords[1][1]);
            verticesCoords[1][0] = newX;
            verticesCoords[1][0] = newY;

            //top right corner
            newX = (cosine * verticesCoords[2][0]) - (sine * verticesCoords[2][1]);
            newY = (sine * verticesCoords[2][0]) + (cosine * verticesCoords[2][1]);
            verticesCoords[2][0] = newX;
            verticesCoords[2][0] = newY;

            //bottom right corner
            newX = (cosine * verticesCoords[3][0]) - (sine * verticesCoords[3][1]);
            newY = (sine * verticesCoords[3][0]) + (cosine * verticesCoords[3][1]);
            verticesCoords[3][0] = newX;
            verticesCoords[3][0] = newY;

        }
    }
}

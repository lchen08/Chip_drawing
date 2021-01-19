/*
 * TSimpleMixer.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;
import org.apache.commons.math3.util.Precision;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** Model exported on Dec 22 2020, 11:03 by COMSOL 5.5.0.359. */
public class TSimpleMixer {
  private Model model;
  private String exportDir = System.getProperty("user.dir") + "/exports/";
  private double boundaryBuffer = 0.001;
  private ArrayList<String> circleNames;
  private String exitConcFilename = "";
  private double[][] currentCircleSet;
  private double[][] bestCircleSet;
  private double bestAvgDiff;
  private double goal = 0.5;

  public static void main(String[] args) throws IOException {
    TSimpleMixer demo = new TSimpleMixer();
    demo.init();
    demo.start();
    System.out.println("COMSOL program completed.");
    demo.updateForNewDesign();

    ModelUtil.disconnect();
  }

  public void init() {
    ModelUtil.initStandalone(true);
    circleNames = new ArrayList<String>();
    currentCircleSet = null;
  }

  private void updateForNewDesign() throws IOException {


    //saves a COMSOL-friendly .java
//    model.save(exportDir + "COMSOL_TSimpleMixer", "java");
  }

  public void start() throws IOException {
    try {
      model = run();
    } catch (Exception e) {
      e.printStackTrace();
    }

//    updateChip();
    runResultSetup();


    double[][] firstHalf = readFirstHalfConcs(exitConcFilename);
    System.out.println("Original");
    for (double[] row : firstHalf)
      System.out.println(row[0] + " " + row[1]);

    double avg = getAverageConc(firstHalf);
    bestAvgDiff = Math.abs(avg - goal);
    bestCircleSet = null;
    System.out.println("\nAverage Conc: " + avg + "\n");

////    double[][] firstHalf = readFirstHalfConcs(exitConcFilename);
//    int i = 0;
//    int bestIter = i;
//    int maxIters = 5;
//    while (i++ < maxIters) {
//      updateChip();
//      runResultSetup();
//
//      System.out.println("Chip " + i);
//      for (double[] row : firstHalf)
//        System.out.println(row[0] + " " + row[1]);
//
//      avg = getAverageConc(firstHalf);
//      System.out.println("\nAverage Conc: " + avg + "\n");
//
//      double diff = avg - goal;
//      if (diff > bestAvgDiff) {
//        bestAvgDiff = diff;
//        bestCircleSet = currentCircleSet;
//        bestIter = i;
//      }
//    }
  }

  private void runResultSetup() {
    model.sol("sol1").runAll();

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

    model.result("pg3").label("Pressure (spf)");
    model.result("pg3").set("frametype", "spatial");
    model.result("pg3").feature("con1").label("Contour");
    model.result("pg3").feature("con1").set("number", 40);
    model.result("pg3").feature("con1").set("levelrounding", false);
    model.result("pg3").feature("con1").set("smooth", "internal");
    model.result("pg3").feature("con1").set("resolution", "normal");

    model.result("pg4").set("xlabel", "Arc length (mm)");
    model.result("pg4").set("xlabelactive", false);
    model.result("pg4").feature("lngr1").set("resolution", "normal");

    //not auto-populated by COMSOL - needed for export to run
//    model.result("pg1").run();
//    model.result("pg4").run();

    //creates the .txt file of the line graph's conc values at the exit
    int i = 0;
    model.result().export("plot1").set("plotgroup", "pg4");
    model.result().export("plot1").set("plot", "lngr1");
    exitConcFilename = exportDir + "exitconc_" + i + ".txt";
    model.result().export("plot1").set("filename", exitConcFilename);
    model.result().export("plot1").set("header", false);
    model.result().export("plot1").run();

    //creates an image file of the concentration gradient
    model.result().export().create("img1","pg1","Image");
    model.result().export("img1").set("imagetype", "png");
    model.result().export("img1").set("pngfilename", exportDir + "concimg_" + i++ + ".png");
    model.result().export("img1").run(); //not auto-populated by COMSOL - needed for export to run


    //creates an image file of the concentration gradient at the exit point
    model.result().export().create("img2","pg4","Image");
    model.result().export("img2").set("imagetype", "png");
    model.result().export("img2").set("pngfilename", exportDir + "concgraph" + i++ + ".png");
    model.result().export("img2").run(); //not auto-populated by COMSOL - needed for export to run
  }

  private double getAverageConc(double[][] data) {
    int count = data.length;
    double sum = 0;
    for (double[] row : data)
      sum += row[1];
    return sum/(count * 1.0);
  }

  private double[][] readFirstHalfConcs(String filename) throws IOException {
    ArrayList<double[]> data = new ArrayList<double[]>();
    BufferedReader br = new BufferedReader(new FileReader(exitConcFilename));
    double midpt = 0.5;
    return addDatapts(br, data, midpt);
  }

  private double[][] addDatapts(BufferedReader br, ArrayList<double[]> data, double maxYPos) throws IOException {
    String s = br.readLine();
    String[] split = s.split("\\s+");
    double yPos = Double.parseDouble(split[0]);
    if (yPos < maxYPos) {
      double[] datapt = {yPos, Double.parseDouble(split[1])};
      data.add(datapt);
      return addDatapts(br, data, maxYPos);
    }
    else {
      double[][] result = new double[data.size()][2];
      for (int i = 0; i < result.length;i++) {
        result[i] = data.get(i);
      }
      return result;
    }
  }

  private void updateChip() {
//    removeCircles();
    addRandSizedCircles(10, 0.01,0.05, -5, 1.2, 0, 1);
    String[] circlesArr = new String[circleNames.size()];
    circlesArr = circleNames.toArray(circlesArr);
//    String[] rectArr = new String[rectNames.size()];
//    rectArr = rectNames.toArray(rectArr);
    model.component("comp1").geom("geom1").create("dif1", "Difference");
//    model.component("comp1").geom("geom1").feature("dif1").selection("input").set(rectArr);
    model.component("comp1").geom("geom1").feature("dif1").selection("input2").set(circlesArr);
    model.component("comp1").geom("geom1").run();
  }

  private void removeCircles() {
    for (String name : circleNames) {
      model.component("comp1").geom("geom1").feature().remove(name);
    }
    circleNames = new ArrayList<String>();
  }

  public void addRandSizedCircles(int numCircles, double minRadius, double maxRadius, double minX, double maxX,
                                  double minY, double maxY) {
    currentCircleSet = new double[numCircles][3];
    int precision = 2;
    for (int i = 1; i <= numCircles; i++) {
      double radius = Precision.round((minRadius + Math.random() * (maxRadius-minRadius)), precision);
      double minBound = minX + radius + boundaryBuffer;
      double maxBound = maxX - radius - boundaryBuffer;
      double posX = ((int) ((minBound + Math.random() * (maxBound - minBound)) * 100))/100.0;
      minBound = minY + radius + boundaryBuffer;
      maxBound = maxY - radius - boundaryBuffer;
      double posY = ((int) ((minBound + Math.random() * (maxBound - minBound)) * 100))/100.0;
      addCircle("c" + i, posX, posY, radius);
      double[] circleData = {posX, posY, radius};
      currentCircleSet[i-1] = circleData;
    }
  }

  public void addCircle(String name, double posX, double posY, double radius) {
    circleNames.add(name);
    model.component("comp1").geom("geom1").create(name, "Circle");
    model.component("comp1").geom("geom1").feature(name).set("pos", new double[]{posX, posY});
    model.component("comp1").geom("geom1").feature(name).set("r", radius);
//        System.out.println("Circle radius: " + radius + "   Circle pos: " + posX + ", " + posY);
  }

  public Model run() {
    model = ModelUtil.create("Model");

    model.modelPath("C:\\Users\\Athena\\Desktop\\College stuff\\UCR\\Research\\COMSOL");

    model.label("Tsimplemixer.mph");

    model.component().create("comp1", true);

    model.component("comp1").geom().create("geom1", 2);

    model.component("comp1").mesh().create("mesh1");

    model.component("comp1").geom("geom1").lengthUnit("mm");
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("pos", new int[]{-5, 0});
    model.component("comp1").geom("geom1").feature("r1").set("size", new double[]{6.2, 1});
    model.component("comp1").geom("geom1").create("r2", "Rectangle");
    model.component("comp1").geom("geom1").feature("r2").set("pos", new int[]{-4, -1});
    model.component("comp1").geom("geom1").feature("r2").set("size", new int[]{1, 3});

    model.component("comp1").geom("geom1").run();

    initiateWaterMat();

    model.component("comp1").physics().create("tds", "DilutedSpecies", "geom1");
    model.component("comp1").physics("tds").create("in1", "Inflow", 1);
    model.component("comp1").physics("tds").feature("in1").selection().set(1);
    model.component("comp1").physics("tds").create("in2", "Inflow", 1);
    model.component("comp1").physics("tds").feature("in2").selection().set(5, 10);
    model.component("comp1").physics("tds").create("out1", "Outflow", 1);
    model.component("comp1").physics("tds").feature("out1").selection().set(16);
    model.component("comp1").physics().create("spf", "LaminarFlow", "geom1");
    model.component("comp1").physics("spf").create("inl1", "InletBoundary", 1);
    model.component("comp1").physics("spf").feature("inl1").selection().set(1, 5, 10);
    model.component("comp1").physics("spf").create("out1", "OutletBoundary", 1);
    model.component("comp1").physics("spf").feature("out1").selection().set(16);

    model.component("comp1").view("view1").axis().set("xmin", -5.154999732971191);
    model.component("comp1").view("view1").axis().set("xmax", 1.3549991846084595);
    model.component("comp1").view("view1").axis().set("ymin", -4.32006311416626);
    model.component("comp1").view("view1").axis().set("ymax", 5.32006311416626);


    model.component("comp1").physics("tds").feature("cdm1").set("u_src", "root.comp1.u");
    model.component("comp1").physics("tds").feature("in1").set("c0", 1);
    model.component("comp1").physics("spf").feature("inl1").set("U0in", 0.001);

    model.study().create("std1");
    model.study("std1").create("stat", "Stationary");

    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("s1", "Stationary");
    model.sol("sol1").feature("s1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("s1").create("d1", "Direct");
    model.sol("sol1").feature("s1").create("i1", "Iterative");
    model.sol("sol1").feature("s1").feature("i1").create("mg1", "Multigrid");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("pr").create("sc1", "SCGS");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("po").create("sc1", "SCGS");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("cs").create("d1", "Direct");
    model.sol("sol1").feature("s1").feature().remove("fcDef");

    model.result().create("pg1", "PlotGroup2D");
    model.result().create("pg2", "PlotGroup2D");
    model.result().create("pg3", "PlotGroup2D");
    model.result().create("pg4", "PlotGroup1D");
    model.result("pg1").create("surf1", "Surface");
    model.result("pg1").create("con1", "Contour");
    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").feature("surf1").set("expr", "spf.U");
    model.result("pg3").create("con1", "Contour");
    model.result("pg3").feature("con1").set("expr", "p");
    model.result("pg4").create("lngr1", "LineGraph");
    model.result("pg4").feature("lngr1").selection().set(16);
    model.result().export().create("plot1", "Plot");

    model.sol("sol1").attach("std1");
    model.sol("sol1").feature("s1").feature("aDef").set("cachepattern", true);
    model.sol("sol1").feature("s1").feature("fc1").set("linsolver", "d1");
    model.sol("sol1").feature("s1").feature("fc1").set("initstep", 0.01);
    model.sol("sol1").feature("s1").feature("fc1").set("minstep", 1.0E-6);
    model.sol("sol1").feature("s1").feature("fc1").set("maxiter", 100);
    model.sol("sol1").feature("s1").feature("d1").label("Direct, fluid flow variables (spf) (merged)");
    model.sol("sol1").feature("s1").feature("d1").set("linsolver", "pardiso");
    model.sol("sol1").feature("s1").feature("d1").set("pivotperturb", 1.0E-13);
    model.sol("sol1").feature("s1").feature("i1").label("AMG, fluid flow variables (spf)");
    model.sol("sol1").feature("s1").feature("i1").set("nlinnormuse", true);
    model.sol("sol1").feature("s1").feature("i1").set("maxlinit", 200);
    model.sol("sol1").feature("s1").feature("i1").set("rhob", 20);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").set("prefun", "saamg");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").set("maxcoarsedof", 80000);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").set("strconn", 0.02);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").set("saamgcompwise", true);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").set("usesmooth", false);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("pr").feature("sc1")
         .set("linesweeptype", "ssor");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("pr").feature("sc1").set("iter", 0);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("pr").feature("sc1")
         .set("scgsvertexrelax", 0.7);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("po").feature("sc1")
         .set("linesweeptype", "ssor");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("po").feature("sc1").set("iter", 1);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("po").feature("sc1")
         .set("scgsvertexrelax", 0.7);
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("cs").feature("d1")
         .set("linsolver", "pardiso");
    model.sol("sol1").feature("s1").feature("i1").feature("mg1").feature("cs").feature("d1")
         .set("pivotperturb", 1.0E-13);

    return model;
  }

  private void initiateWaterMat() {
    model.component("comp1").material().create("mat1", "Common");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("eta", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("Cp", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("rho", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("k", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("cs", "Interpolation");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an1", "Analytic");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an2", "Analytic");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an3", "Analytic");
    model.component("comp1").material("mat1").label("Water");
    model.component("comp1").material("mat1").set("family", "water");
    model.component("comp1").material("mat1").propertyGroup("def").func("eta").set("arg", "T");
    model.component("comp1").material("mat1").propertyGroup("def").func("eta")
            .set("pieces", new String[][]{{"273.15", "413.15", "1.3799566804-0.021224019151*T^1+1.3604562827E-4*T^2-4.6454090319E-7*T^3+8.9042735735E-10*T^4-9.0790692686E-13*T^5+3.8457331488E-16*T^6"}, {"413.15", "553.75", "0.00401235783-2.10746715E-5*T^1+3.85772275E-8*T^2-2.39730284E-11*T^3"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("eta").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("eta").set("fununit", "Pa*s");
    model.component("comp1").material("mat1").propertyGroup("def").func("Cp").set("arg", "T");
    model.component("comp1").material("mat1").propertyGroup("def").func("Cp")
            .set("pieces", new String[][]{{"273.15", "553.75", "12010.1471-80.4072879*T^1+0.309866854*T^2-5.38186884E-4*T^3+3.62536437E-7*T^4"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("Cp").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("Cp").set("fununit", "J/(kg*K)");
    model.component("comp1").material("mat1").propertyGroup("def").func("rho").set("arg", "T");
    model.component("comp1").material("mat1").propertyGroup("def").func("rho").set("smooth", "contd1");
    model.component("comp1").material("mat1").propertyGroup("def").func("rho")
            .set("pieces", new String[][]{{"273.15", "293.15", "0.000063092789034*T^3-0.060367639882855*T^2+18.9229382407066*T-950.704055329848"}, {"293.15", "373.15", "0.000010335053319*T^3-0.013395065634452*T^2+4.969288832655160*T+432.257114008512"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("rho").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("rho").set("fununit", "kg/m^3");
    model.component("comp1").material("mat1").propertyGroup("def").func("k").set("arg", "T");
    model.component("comp1").material("mat1").propertyGroup("def").func("k")
            .set("pieces", new String[][]{{"273.15", "553.75", "-0.869083936+0.00894880345*T^1-1.58366345E-5*T^2+7.97543259E-9*T^3"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("k").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("k").set("fununit", "W/(m*K)");
    model.component("comp1").material("mat1").propertyGroup("def").func("cs")
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
    model.component("comp1").material("mat1").propertyGroup("def").func("cs").set("interp", "piecewisecubic");
    model.component("comp1").material("mat1").propertyGroup("def").func("cs").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("cs").set("fununit", "m/s");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").label("Analytic ");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").set("funcname", "alpha_p");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").set("expr", "-1/rho(T)*d(rho(T),T)");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").set("args", new String[]{"T"});
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1").set("fununit", "1/K");
    model.component("comp1").material("mat1").propertyGroup("def").func("an1")
            .set("plotargs", new String[][]{{"T", "273.15", "373.15"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("an2").set("funcname", "gamma_w");
    model.component("comp1").material("mat1").propertyGroup("def").func("an2")
            .set("expr", "1+(T/Cp(T))*(alpha_p(T)*cs(T))^2");
    model.component("comp1").material("mat1").propertyGroup("def").func("an2").set("args", new String[]{"T"});
    model.component("comp1").material("mat1").propertyGroup("def").func("an2").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("an2").set("fununit", "1");
    model.component("comp1").material("mat1").propertyGroup("def").func("an2")
            .set("plotargs", new String[][]{{"T", "273.15", "373.15"}});
    model.component("comp1").material("mat1").propertyGroup("def").func("an3").set("funcname", "muB");
    model.component("comp1").material("mat1").propertyGroup("def").func("an3").set("expr", "2.79*eta(T)");
    model.component("comp1").material("mat1").propertyGroup("def").func("an3").set("args", new String[]{"T"});
    model.component("comp1").material("mat1").propertyGroup("def").func("an3").set("argunit", "K");
    model.component("comp1").material("mat1").propertyGroup("def").func("an3").set("fununit", "Pa*s");
    model.component("comp1").material("mat1").propertyGroup("def").func("an3")
            .set("plotargs", new String[][]{{"T", "273.15", "553.75"}});
    model.component("comp1").material("mat1").propertyGroup("def").set("thermalexpansioncoefficient", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("bulkviscosity", "");
    model.component("comp1").material("mat1").propertyGroup("def")
            .set("thermalexpansioncoefficient", new String[]{"alpha_p(T)", "0", "0", "0", "alpha_p(T)", "0", "0", "0", "alpha_p(T)"});
    model.component("comp1").material("mat1").propertyGroup("def").set("bulkviscosity", "muB(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("thermalexpansioncoefficient_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").descr("bulkviscosity_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("dynamicviscosity", "eta(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("dynamicviscosity_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("ratioofspecificheat", "gamma_w(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("ratioofspecificheat_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def")
            .set("electricconductivity", new String[]{"5.5e-6[S/m]", "0", "0", "0", "5.5e-6[S/m]", "0", "0", "0", "5.5e-6[S/m]"});
    model.component("comp1").material("mat1").propertyGroup("def").descr("electricconductivity_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("heatcapacity", "Cp(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("heatcapacity_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("density", "rho(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("density_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def")
            .set("thermalconductivity", new String[]{"k(T)", "0", "0", "0", "k(T)", "0", "0", "0", "k(T)"});
    model.component("comp1").material("mat1").propertyGroup("def").descr("thermalconductivity_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").set("soundspeed", "cs(T)");
    model.component("comp1").material("mat1").propertyGroup("def").descr("soundspeed_symmetry", "");
    model.component("comp1").material("mat1").propertyGroup("def").addInput("temperature");
  }

}
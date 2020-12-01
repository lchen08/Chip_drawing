/*
 * NewSimpleMixer.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Dec 1 2020, 13:38 by COMSOL 5.5.0.359. */
public class NewSimpleMixer {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.modelPath("C:\\Users\\Athena\\Desktop\\College stuff\\UCR\\Research\\COMSOL");

    model.label("SimpleMixer3.mph");

    model.component().create("comp1", true);

    model.component("comp1").geom().create("geom1", 2);

    model.component("comp1").mesh().create("mesh1");

    model.component("comp1").geom("geom1").lengthUnit("mm");
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("pos", new double[]{-3.4, 0});
    model.component("comp1").geom("geom1").feature("r1").set("size", new int[]{1, 1});
    model.component("comp1").geom("geom1").create("r2", "Rectangle");
    model.component("comp1").geom("geom1").feature("r2").set("pos", new double[]{-2.4, 0});
    model.component("comp1").geom("geom1").feature("r2").set("size", new double[]{3.4, 1});
    model.component("comp1").geom("geom1").run();
    model.component("comp1").geom("geom1").run("fin");

    model.component("comp1").material().create("mat1", "Common");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("eta", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("Cp", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("rho", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("k", "Piecewise");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("cs", "Interpolation");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an1", "Analytic");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an2", "Analytic");
    model.component("comp1").material("mat1").propertyGroup("def").func().create("an3", "Analytic");

    model.component("comp1").physics().create("spf", "LaminarFlow", "geom1");
    model.component("comp1").physics("spf").create("inl1", "InletBoundary", 1);
    model.component("comp1").physics("spf").feature("inl1").selection().set(2, 3);
    model.component("comp1").physics("spf").create("out1", "OutletBoundary", 1);
    model.component("comp1").physics("spf").feature("out1").selection().set(7);
    model.component("comp1").physics().create("tds", "DilutedSpecies", "geom1");
    model.component("comp1").physics("tds").create("in1", "Inflow", 1);
    model.component("comp1").physics("tds").feature("in1").selection().set(3);
    model.component("comp1").physics("tds").create("in2", "Inflow", 1);
    model.component("comp1").physics("tds").feature("in2").selection().set(2);
    model.component("comp1").physics("tds").create("out1", "Outflow", 1);
    model.component("comp1").physics("tds").feature("out1").selection().set(7);

    model.component("comp1").view("view1").axis().set("xmin", -3.9100823402404785);
    model.component("comp1").view("view1").axis().set("xmax", 1.5100822448730469);
    model.component("comp1").view("view1").axis().set("ymin", -1.9161614179611206);
    model.component("comp1").view("view1").axis().set("ymax", 2.915891170501709);

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

    model.component("comp1").physics("spf").feature("inl1").set("U0in", 0.01);
    model.component("comp1").physics("tds").feature("cdm1").set("u_src", "root.comp1.u");
    model.component("comp1").physics("tds").feature("in1").set("c0", 1);
    model.component("comp1").physics("tds").feature("in1").label("Inflow 1M");
    model.component("comp1").physics("tds").feature("in2").label("Inflow 0M");

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
    model.result("pg2").create("surf1", "Surface");
    model.result("pg3").create("surf1", "Surface");
    model.result("pg3").create("con1", "Contour");
    model.result("pg3").feature("surf1").set("expr", "c");
    model.result("pg3").feature("con1").set("expr", "c");
    model.result("pg4").create("lngr1", "LineGraph");
    model.result("pg4").feature("lngr1").selection().set(7);
    model.result("pg4").feature("lngr1").set("expr", "c");
    model.result().export().create("data1", "Data");
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
    model.sol("sol1").runAll();

    model.result("pg1").label("Velocity (spf)");
    model.result("pg1").set("frametype", "spatial");
    model.result("pg1").feature("surf1").label("Surface");
    model.result("pg1").feature("surf1").set("smooth", "internal");
    model.result("pg1").feature("surf1").set("resolution", "normal");
    model.result("pg2").label("Pressure (spf)");
    model.result("pg2").set("frametype", "spatial");
    model.result("pg2").feature("surf1").set("resolution", "normal");
    model.result("pg3").label("Concentration (tds)");
    model.result("pg3").set("titletype", "custom");
    model.result("pg3").feature("surf1").set("rangecoloractive", true);
    model.result("pg3").feature("surf1").set("rangecolormax", 1);
    model.result("pg3").feature("surf1").set("resolution", "normal");
    model.result("pg3").feature("con1").set("levelmethod", "levels");
    model.result("pg3").feature("con1").set("levels", 0.5);
    model.result("pg3").feature("con1").set("coloring", "uniform");
    model.result("pg3").feature("con1").set("color", "black");
    model.result("pg3").feature("con1").set("resolution", "normal");
    model.result("pg4").set("xlabel", "Arc length (mm)");
    model.result("pg4").set("ylabel", "Concentration (mol/m<sup>3</sup>)");
    model.result("pg4").set("xlabelactive", false);
    model.result("pg4").set("ylabelactive", false);
    model.result("pg4").feature("lngr1").set("resolution", "normal");
    model.result().export("data1").set("expr", new String[]{"c"});
    model.result().export("data1").set("unit", new String[]{"mol/m^3"});
    model.result().export("data1").set("descr", new String[]{"Concentration"});
    model.result().export("data1")
         .set("filename", "C:\\Users\\Athena\\Desktop\\Chip_Drawing\\src\\main\\java\\out.txt");
    model.result().export("plot1").set("plotgroup", "pg4");
    model.result().export("plot1").set("plot", "lngr1");
    model.result().export("plot1")
         .set("filename", "C:\\Users\\Athena\\Desktop\\Chip_Drawing\\src\\main\\java\\out2.txt");

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}

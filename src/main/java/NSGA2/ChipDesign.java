package NSGA2;

import java.util.ArrayList;

public class ChipDesign {
    double[][] postInfo;
    double pressure;
    double concObj;
    double runtime;
    int mesh;
    String filename;

    public ChipDesign(double[][] postInfo) {
        this.postInfo = postInfo;
    }
    public ChipDesign(double concObj, double pressure) {
        this.pressure = pressure;
        this.concObj = concObj;
    }
}

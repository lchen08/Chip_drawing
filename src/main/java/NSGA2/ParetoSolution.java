package NSGA2;

public class ParetoSolution {
    double captureEfficiency;
    double entryPressure;
    String modelName;
    public ParetoSolution(double captureEfficiency, double entryPressure, String modelName) {
        this.captureEfficiency = captureEfficiency;
        this.entryPressure = entryPressure;
        this.modelName = modelName;
    }
}

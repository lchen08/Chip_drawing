package NSGA2;

import NSGA2.ChipDesign;

import java.util.ArrayList;

public class ParetoTester {

    public static void main(String[] args) {
        ArrayList<ChipDesign> designs = new ArrayList<>();
        designs.add(new ChipDesign(Double.MAX_VALUE, Double.MAX_VALUE));
        designs.add(new ChipDesign(23, 23));
        designs.add(new ChipDesign(22, 5));
        designs.add(new ChipDesign(22, 30));
        designs.add(new ChipDesign(5, 8));
        designs.add(new ChipDesign(11, 7));
        designs.add(new ChipDesign(23, 8));
        designs.add(new ChipDesign(26, 6));
        designs.add(new ChipDesign(5, 4));
        designs.add(new ChipDesign(8, 8));
        designs.add(new ChipDesign(9, 4));
        designs.add(new ChipDesign(11, 4));
        designs.add(new ChipDesign(27, 4.5));

        ArrayList<ChipDesign> pareto = getPareto(designs);
        for (ChipDesign design : pareto) {
            System.out.println(design.concObj + ", " + design.pressure);
        }

    }

    public static ArrayList<ChipDesign> getPareto(ArrayList<ChipDesign> designs) {
        ArrayList<ChipDesign> results = new ArrayList<>();
        for (int design = 0; design < designs.size(); design++) {
            ChipDesign toAdd = designs.get(design);
            if (results.size() == 0) {
                results.add(toAdd);
            }
            else {
                for (int newPos = 0; newPos < results.size(); newPos++) {
                    ChipDesign currentDesign = results.get(newPos);
                    if(toAdd.concObj <= currentDesign.concObj) {
                        if(toAdd.pressure < currentDesign.pressure) {
                            if(newPos == 0) {
                                results.add(newPos, toAdd);
                                newPos = results.size();
                            }
                            else {
                                boolean didRemoval = false;
                                results.add(newPos, toAdd);
                                for (int checkIter = newPos - 1; checkIter >= 0; checkIter--) {
                                    ChipDesign prevDesign = results.get(checkIter);
                                    if(toAdd.pressure <= prevDesign.pressure) {
                                        results.remove(checkIter);
                                        didRemoval = true;
                                    }
                                    else if (didRemoval == true) {
                                        checkIter = -1;
                                    }

                                }
                                newPos = results.size();
                            }
                        }
                        else {
                            newPos = results.size();
                        }
                    }
                    else if(newPos == results.size() - 1) {
                        boolean didRemoval = false;
                        for (int checkIter = newPos; checkIter >= 0; checkIter--) {
                            ChipDesign prevDesign = results.get(checkIter);
                            if(toAdd.pressure <= prevDesign.pressure) {
                                results.remove(checkIter);
                                didRemoval = true;
                            }
                            else if (didRemoval == true) {
                                checkIter = -1;
                            }

                        }
                        results.add(toAdd);
                    }
                }
            }
        }
        return results;
    }
}

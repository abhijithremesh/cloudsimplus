package org.cloudsimplus.examples.Model;

import java.util.Arrays;
import java.util.List;

public class Experiment {

    String NASA = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";
    String KTH = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz";

    List<String> workloadTypes;
    List<Integer> numOfCloudets;
    List<Integer> numOfGenerationsList;
    List<Integer> popSizeList;
    List<Integer> chromosomeLenList;
    List<Integer> numOfHeuristicsList;
    List<Integer> eliteCountList;
    List<Integer> tournamentCountList;
    List<Double> crossoverRateList;
    List<Double> mutationRateList;
    List<Double> w1List;
    List<Double> w2List;

    public static void main(String[] args) {
        new Experiment();
    }

    private Experiment(){

        workloadTypes = Arrays.asList(NASA,NASA);
        numOfCloudets = Arrays.asList(1000,2000);
        numOfGenerationsList = Arrays.asList(10,20);
        popSizeList = Arrays.asList(10,10);

        w1List = Arrays.asList(0.3,0.3);
        w2List = Arrays.asList(0.7,0.7);

        eliteCountList = Arrays.asList(2,2);
        tournamentCountList = Arrays.asList(3,3);
        crossoverRateList = Arrays.asList(0.5,0.5);
        mutationRateList = Arrays.asList(0.4,0.4);


        HybridModel hm = new HybridModel();

        hm.numOfGenerations = numOfGenerationsList.get(0);




    }



}

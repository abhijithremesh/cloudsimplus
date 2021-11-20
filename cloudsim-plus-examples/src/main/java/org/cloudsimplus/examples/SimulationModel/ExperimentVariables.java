package org.cloudsimplus.examples.SimulationModel;

public class ExperimentVariables {

    public int numOfGenerations, popSize, chromosomeLength, numOfHeuristics;
    public int eliteCount, tournamentCount;
    public double crossoverRate, mutationRate;
    public double w1, w2, w3;

    public ExperimentVariables( int numOfGenerations, int popSize, int chromosomeLength, int numOfHeuristics,
                                int eliteCount, int tournamentCount, double crossoverRate, double mutationRate,
                                double w1, double w2, double w3){

        this.numOfGenerations = numOfGenerations;
        this.popSize = popSize;
        this.chromosomeLength = chromosomeLength;
        this.numOfHeuristics = numOfHeuristics;
        this.eliteCount = eliteCount;
        this.tournamentCount = tournamentCount;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
    }


}

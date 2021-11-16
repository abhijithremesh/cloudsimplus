package org.cloudsimplus.examples.Model;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudsimplus.examples.HybridModel.MyBroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimpleGA {

    List<SingleChromosome> chromosomeList = new ArrayList<>();
    List<Double> fitnessList = new ArrayList<>();

    List<SingleChromosome> childChromosomesList = new ArrayList<>();
    List<SingleChromosome> nextPopulation = new ArrayList<>();

    List<Double> generationBestFitnessValueList = new ArrayList<>();
    List<SingleChromosome> generationBestChromosomeList = new ArrayList<>();

    public List<SingleChromosome> createInitialPopulation(int n, int len, int bound){

        for (int i=0; i<n; i++){
            ArrayList<SingleGene> newChromosome = new ArrayList<SingleGene>();
            for(int j = 0; j< len; j++){
                Random r = new Random();
                SingleGene g = new SingleGene();
                g.setSchedulingHeuristic(r.nextInt(bound));
                newChromosome.add(g);
            }
            SingleChromosome chromosome = new SingleChromosome(newChromosome);
            this.chromosomeList.add(chromosome);
        }
        return chromosomeList;
    }

    public void computeMakespan(MyBroker broker, List<SingleChromosome> chromosomeList){

        double makespan = broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size() - 1).getFinishTime();
        System.out.println("Makespan: "+roundDecimals(makespan));
        this.fitnessList.add(roundDecimals(makespan));
        this. chromosomeList = chromosomeList;


        System.out.println("chromosomeList: ");
        printChromosomes(chromosomeList);

        System.out.println("\nfitnessList: "+fitnessList);

    }

    public void elitismSelection(int eliteCount){

        System.out.println("Performing Elitism");

        List<SingleChromosome> eliteChromosomes = new ArrayList<>();

        for(int i=0; i < eliteCount; i++){

            eliteChromosomes.add(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));
            this.chromosomeList.remove(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));
            this.fitnessList.remove(Collections.min(fitnessList));
        }

        this.nextPopulation.addAll(eliteChromosomes);

        System.out.print("eliteChromosomes: ");
        printChromosomes(eliteChromosomes);


    }

    public SingleChromosome tournamentSelection(int tournamentCount){

        System.out.println("Performing Tournament Selection");

        List<Integer> chromosomeIndices = new ArrayList<>();
        List<Double> fitnessValues = new ArrayList<>();
        for(int j =0; j < tournamentCount; j++){
            chromosomeIndices.add(chromosomeList.indexOf(chromosomeList.get(new Random().nextInt(chromosomeList.size()))));
        }
        System.out.println(chromosomeIndices);
        chromosomeIndices.forEach(integer -> fitnessValues.add(fitnessList.get(integer)));
        System.out.println(fitnessValues);


        SingleChromosome tourChromosome = chromosomeList.get(chromosomeIndices.get(fitnessValues.indexOf(Collections.min(fitnessValues))));

        System.out.print("tournamentChromosome: ");
        printChromosome(tourChromosome);

        return tourChromosome;

    }

    public void parentSelectionCrossoverMutation(int tournamentCount, double crossoverRate, double mutationRate){

        for (int i=0; i<chromosomeList.size(); i++){

            SingleChromosome parentChromosomeOne = tournamentSelection(tournamentCount);
            SingleChromosome parentChromosomeTwo = tournamentSelection(tournamentCount);

            System.out.print("parentChromosomeOne: ");
            printChromosome(parentChromosomeOne);

            System.out.print("parentChromosomeTwo: ");
            printChromosome(parentChromosomeTwo);

            SingleChromosome childChromosome = null;

            switch (new Random().nextInt(4)){
                case 0:
                    System.out.println("Performing Single Point Crossover.....");
                    childChromosome = singlePointCrossover(parentChromosomeOne,parentChromosomeTwo,crossoverRate);
                    break;
                case 1:
                    System.out.println("Performing Two Point Crossover.....");
                    childChromosome = twoPointCrossover(parentChromosomeOne,parentChromosomeTwo,crossoverRate);
                    break;
                case 2:
                    System.out.println("Performing Random Crossover.....");
                    childChromosome = randomCrossover(parentChromosomeOne,parentChromosomeTwo,crossoverRate);
                    break;
                case 3:
                    System.out.println("Performing Uniform Crossover.....");
                    childChromosome = uniformCrossover(parentChromosomeOne,parentChromosomeTwo,crossoverRate);
                    break;
            }

            this.childChromosomesList.add(childChromosome);

        }


        mutateChildChromosomes(childChromosomesList, mutationRate);

        this.nextPopulation.addAll(childChromosomesList);
        System.out.println("nextPopulationSize: "+nextPopulation.size());

        System.out.print("nextPopulationFirstCandidate: ");
        printChromosome(nextPopulation.get(0));

        this.fitnessList.clear();
        this.childChromosomesList.clear();
        this.chromosomeList.clear();

    }

    public SingleChromosome singlePointCrossover(SingleChromosome parentChromosomeOne, SingleChromosome parentChromosomeTwo, double crossoverRate){


        if(new Random().nextDouble() < crossoverRate ){
            int crossoverPoint = new Random().nextInt(parentChromosomeOne.getGeneList().size());
            System.out.println("crossoverPoint: "+crossoverPoint);
            ArrayList<SingleGene> newChromosome = new ArrayList<SingleGene>();
            for(int i=0; i < parentChromosomeOne.getGeneList().size(); i++){
                if (i < crossoverPoint){
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
                else{
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            SingleChromosome childChromosome = new SingleChromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            SingleChromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public SingleChromosome twoPointCrossover(SingleChromosome parentChromosomeOne, SingleChromosome parentChromosomeTwo, double crossoverRate){


        if(new Random().nextDouble() < crossoverRate ) {
            int crossoverPointOne = new Random().nextInt(parentChromosomeOne.getGeneList().size());
            int crossoverPointTwo = new Random().nextInt(parentChromosomeOne.getGeneList().size());
            if (crossoverPointOne == crossoverPointTwo) {
                if (crossoverPointOne == 0) {
                    crossoverPointTwo++;
                } else {
                    crossoverPointOne--;
                }
            }

            if (crossoverPointTwo < crossoverPointOne) {
                int temp = crossoverPointOne;
                crossoverPointOne = crossoverPointTwo;
                crossoverPointTwo = temp;
            }

            System.out.println("crossoverPoints: "+crossoverPointOne+", "+crossoverPointTwo);

            ArrayList<SingleGene> newChromosome = new ArrayList<SingleGene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (i < crossoverPointOne || i > crossoverPointTwo) {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }

            SingleChromosome childChromosome = new SingleChromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            SingleChromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public SingleChromosome randomCrossover(SingleChromosome parentChromosomeOne, SingleChromosome parentChromosomeTwo, double crossoverRate){

        if(new Random().nextDouble() < crossoverRate ) {
            ArrayList<SingleGene> newChromosome = new ArrayList<SingleGene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (new Random().nextInt(2) == 0) {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            SingleChromosome childChromosome = new SingleChromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            SingleChromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public SingleChromosome uniformCrossover(SingleChromosome parentChromosomeOne, SingleChromosome parentChromosomeTwo, double crossoverRate){

        if(new Random().nextDouble() < crossoverRate ) {
            ArrayList<SingleGene> newChromosome = new ArrayList<SingleGene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (i % 2 == 0) {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    SingleGene g = new SingleGene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            SingleChromosome childChromosome = new SingleChromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            SingleChromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public SingleChromosome mutate(SingleChromosome childChromosome, double mutationRate){

        System.out.println("Performing mutation");

        if(new Random().nextDouble() < mutationRate ) {
            int lowerLim = new Random().nextInt(childChromosome.getGeneList().size());
            int upperLim = new Random().nextInt(childChromosome.getGeneList().size());

            SingleChromosome mutatedChildChromosome = new SingleChromosome(childChromosome.getGeneList());

            if (lowerLim == upperLim) {
                if (lowerLim == 0) {
                    upperLim++;
                } else {
                    lowerLim--;
                }
            }

            System.out.print("childChromosome: ");
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();

            SingleGene g1 = childChromosome.getGeneList().get(lowerLim);
            SingleGene g2 = childChromosome.getGeneList().get(upperLim);
            mutatedChildChromosome.getGeneList().set(lowerLim, g2);
            mutatedChildChromosome.getGeneList().set(upperLim, g1);

            System.out.print("mutatedChildChromosome: ");
            printChromosome(mutatedChildChromosome);

            return mutatedChildChromosome;
        } else {
            SingleChromosome mutatedChildChromosome = new SingleChromosome(childChromosome.getGeneList());
            System.out.print("mutatedChildChromosome: ");
            printChromosome(mutatedChildChromosome);
            return mutatedChildChromosome;

        }

    }

    public void mutateChildChromosomes(List<SingleChromosome> childChromosomesList, double mutationRate){

        for (SingleChromosome childChromosome : childChromosomesList
        ) {
            mutate(childChromosome,mutationRate);
        }

    }

    public List<SingleChromosome> getNextPopulation(){
        List<SingleChromosome> nextPop = new ArrayList<>(nextPopulation);
        this.nextPopulation.clear();
        return nextPop;
    }

    public void generationBest(){

        List<SingleChromosome> chromosomeList = this.chromosomeList;
        List<Double> fitnessList = this.fitnessList;

        generationBestFitnessValueList.add(Collections.min(fitnessList));
        generationBestChromosomeList.add(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));

        System.out.println("generationBestFitnessValueList: "+generationBestFitnessValueList);
        System.out.print("generationBestChromosomeList: ");
        for (SingleChromosome c: generationBestChromosomeList
        ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }
        System.out.println();


    }

    public void computeFitness(Datacenter datacenter, DatacenterBroker broker, List<SingleChromosome> chromosomeList, double w1, double w2){


        double makespan = broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime();
        double totalHostPowerConsumption =0;
        for (Host h: datacenter.getHostList()
        ) {
            double utilizationPercentMean = h.getCpuUtilizationStats().getMean();
            double watts = h.getPowerModel().getPower(utilizationPercentMean);
            totalHostPowerConsumption += watts;
        }
        double fitness = w1 * makespan + w2 * totalHostPowerConsumption;

        this.fitnessList.add(roundDecimals(fitness));
        this. chromosomeList = chromosomeList;

        System.out.println("chromosomeList: ");
        printChromosomes(chromosomeList);

        System.out.println("\nfitnessList: "+fitnessList);

    }

    public void printPerformanceMetrics(Datacenter datacenter, DatacenterBroker broker){

        double makespan = roundDecimals(broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime());
        double throughput = roundDecimals(broker.getCloudletFinishedList().size()/makespan);

        System.out.println("finishedCloudlets: "+broker.getCloudletFinishedList().size());
        System.out.println("makespan: "+makespan);
        System.out.println("throughput: "+throughput);

        List<Double> HostCpuUtilizationList = new ArrayList<>();
        double totalHostCpuUtilization = 0;
        double totalHostPowerConsumption = 0;
        for (Host h: datacenter.getHostList()
        ) {
            double utilizationPercentMean = h.getCpuUtilizationStats().getMean();
            double utilizationPercentCount = h.getCpuUtilizationStats().count();
            double watts = h.getPowerModel().getPower(utilizationPercentMean);
            HostCpuUtilizationList.add(roundDecimals(utilizationPercentMean));
            totalHostCpuUtilization += utilizationPercentMean;
            totalHostPowerConsumption += watts;
        }

        //System.out.println("totalHostCpuUtilization: "+roundDecimals(totalHostCpuUtilization*100));
        //System.out.println("HostCpuUtilizationList: "+HostCpuUtilizationList);
        System.out.println("totalHostPowerConsumption: "+roundDecimals(totalHostPowerConsumption));

    }

    private double roundDecimals(double value){
        return  Math.round(value * 100.0) / 100.0;
    }

    private void printChromosomes(List<SingleChromosome> chromosomeList){
        for (SingleChromosome c: chromosomeList
        ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }
        System.out.println();
    }

    private void printChromosome(SingleChromosome chromosome){
        chromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
        System.out.println();
    }

}

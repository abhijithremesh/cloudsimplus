package org.cloudsimplus.examples.SimulationModel;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudsimplus.examples.HybridModel.MyBroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GA {

    List<Chromosome> chromosomeList = new ArrayList<>();

    List<Double> makespanList = new ArrayList<>();
    //List<Double> degreeOfImbalanceList = new ArrayList<>();
    List<Double> totalWaitingTimeList = new ArrayList<>();
    List<Double> flowTimeList = new ArrayList<>();
    List<Double> fitnessList = new ArrayList<>();

    List<Chromosome> childChromosomesList = new ArrayList<>();
    List<Chromosome> nextPopulation = new ArrayList<>();

    List<Double> generationBestFitnessValueList = new ArrayList<>();
    List<Chromosome> generationBestChromosomeList = new ArrayList<>();

    public List<Chromosome> createInitialPopulation(int n, int len, int bound){

        for (int i=0; i<n; i++){
            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for(int j = 0; j< len; j++){
                Random r = new Random();
                Gene g = new Gene();
                g.setSchedulingHeuristic(r.nextInt(bound));
                newChromosome.add(g);
            }
            Chromosome chromosome = new Chromosome(newChromosome);
            this.chromosomeList.add(chromosome);
        }
        return chromosomeList;
    }

    public void computeMakespan(MyBroker broker){

        double makespan = broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size() - 1).getFinishTime();
        System.out.println("Makespan: "+roundDecimals(makespan));
        this.makespanList.add(roundDecimals(makespan));
        System.out.println("\nmakespanList: "+makespanList);

    }

    public void computeFlowTime(MyBroker broker){
        double flowTime = 0.0;
        for (Cloudlet c : broker.getCloudletFinishedList()
        ) {
            flowTime += c.getWaitingTime() + c.getActualCpuTime() + c.getSubmissionDelay();
        }
        this.flowTimeList.add(roundDecimals(flowTime));
        System.out.println("\nflowTimeList: "+flowTimeList);
    }

    public void computeTotalWaitingTime(MyBroker broker){
        double totalWaitingTime = 0.0;
        for (Cloudlet c: broker.getCloudletFinishedList()
        ) {
            totalWaitingTime += c.getWaitingTime();
        }
        this.totalWaitingTimeList.add(roundDecimals(totalWaitingTime));
        System.out.println("\ntotalWaitingTimeList: "+totalWaitingTimeList);
    }

    /*
    public void computeDegreeofImbalance(MyBroker broker){
        double degreeOfImbalance = 0.0;
        List <Double> vmExecTimeList = new ArrayList<Double>();
        for (Vm v: broker.getVmCreatedList()
        ) {
            vmExecTimeList.add(v.getTotalExecutionTime());
        }
        degreeOfImbalance = (Collections.max(vmExecTimeList) - Collections.min(vmExecTimeList))/vmExecTimeList.stream().mapToDouble(d -> d).average().orElse(0.0);
        this.degreeOfImbalanceList.add(degreeOfImbalance);
        System.out.println("\ndegreeOfImbalanceList: "+degreeOfImbalanceList);
    }

     */



    public void computeFitness(List<Chromosome> chromosomeList, double w1, double w2, double w3){

        this. chromosomeList = chromosomeList;

        //this.makespanList = this.makespanList.stream().map(f->((1127.02-f)/(1127.02-18.26)*100)).collect(Collectors.toList());
        this.flowTimeList = this.flowTimeList.stream().map(f->((1130793.77-f)/(1130793.77-3495.38)*100)).collect(Collectors.toList());
        this.totalWaitingTimeList = this.totalWaitingTimeList.stream().map(f->((1107153.9-f)/(1107153.9-2471.02)*100)).collect(Collectors.toList());
        //this.degreeOfImbalanceList = this.degreeOfImbalanceList.stream().map(f->(2.49-f)/(2.49-0.35)).collect(Collectors.toList());
        //this.makespanList = this.makespanList.stream().map(f->(41.27/f)).collect(Collectors.toList());
        //this.degreeOfImbalanceList = this.degreeOfImbalanceList.stream().map(f->(0.35/f)).collect(Collectors.toList());
        //this.makespanList = scalingMethodFour(this.makespanList);
        //this.degreeOfImbalanceList = scalingMethodFour(this.degreeOfImbalanceList);

        System.out.println("makespanList: "+this.makespanList);
        System.out.println("flowTimeList: "+this.flowTimeList);
        System.out.println("totalWaitingTimeList: "+this.totalWaitingTimeList);
        //System.out.println("degreeOfImbalanceList: "+this.degreeOfImbalanceList);

        for (int i = 0; i < chromosomeList.size(); i++){
            double fitness = w1 * totalWaitingTimeList.get(i) + w2 * flowTimeList.get(i);
            //double fitness = w1 * makespanList.get(i) + w2 * degreeOfImbalanceList.get(i);
            //double fitness = w1 * makespanList.get(i) + w2 * flowTimeList.get(i);
            //double fitness = w1 * makespanList.get(i) + w2 * totalWaitingTimeList.get(i);
            //double fitness = w1 * makespanList.get(i) + w2 * totalWaitingTimeList.get(i) + w3 *  flowTimeList.get(i);
            this.fitnessList.add(fitness);
        }

        System.out.println("chromosomeList: ");
        printChromosomes(chromosomeList);
        System.out.println("\nfitnessList: "+fitnessList);

    }


//    public void computeFitness(List<Chromosome> chromosomeList){
//        this. chromosomeList = chromosomeList;
//        for (int i = 0; i < chromosomeList.size(); i++){
//            //this.fitnessList.add(makespanList.get(i));
//            //this.fitnessList.add(totalWaitingTimeList.get(i));
//            //this.fitnessList.add(flowTimeList.get(i));
//        }
//        System.out.println("chromosomeList: ");
//        printChromosomes(chromosomeList);
//        System.out.println("\nfitnessList: "+fitnessList);
//    }


    public void elitismSelection(int eliteCount){

        System.out.println("Performing Elitism");

        List<Chromosome> eliteChromosomes = new ArrayList<>();

        for(int i=0; i < eliteCount; i++){

            eliteChromosomes.add(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));
            this.chromosomeList.remove(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));
            this.fitnessList.remove(Collections.min(fitnessList));
        }

        this.nextPopulation.addAll(eliteChromosomes);

        System.out.print("eliteChromosomes: ");
        printChromosomes(eliteChromosomes);


    }

    public Chromosome tournamentSelection(int tournamentCount){

        System.out.println("Performing Tournament Selection");

        List<Integer> chromosomeIndices = new ArrayList<>();
        List<Double> fitnessValues = new ArrayList<>();
        for(int j =0; j < tournamentCount; j++){
            chromosomeIndices.add(chromosomeList.indexOf(chromosomeList.get(new Random().nextInt(chromosomeList.size()))));
        }
        System.out.println(chromosomeIndices);
        chromosomeIndices.forEach(integer -> fitnessValues.add(fitnessList.get(integer)));
        System.out.println(fitnessValues);


        Chromosome tourChromosome = chromosomeList.get(chromosomeIndices.get(fitnessValues.indexOf(Collections.min(fitnessValues))));

        System.out.print("tournamentChromosome: ");
        printChromosome(tourChromosome);

        return tourChromosome;

    }

    public void parentSelectionCrossoverMutation(int tournamentCount, double crossoverRate, double mutationRate){

        for (int i=0; i<chromosomeList.size(); i++){

            Chromosome parentChromosomeOne = tournamentSelection(tournamentCount);
            Chromosome parentChromosomeTwo = tournamentSelection(tournamentCount);

            System.out.print("parentChromosomeOne: ");
            printChromosome(parentChromosomeOne);

            System.out.print("parentChromosomeTwo: ");
            printChromosome(parentChromosomeTwo);

            Chromosome childChromosome = null;

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

        this.makespanList.clear();
        //this.degreeOfImbalanceList.clear();
        this.totalWaitingTimeList.clear();
        this.flowTimeList.clear();
        this.fitnessList.clear();

        this.childChromosomesList.clear();
        this.chromosomeList.clear();

    }

    public Chromosome singlePointCrossover(Chromosome parentChromosomeOne, Chromosome parentChromosomeTwo, double crossoverRate){


        if(new Random().nextDouble() < crossoverRate ){
            int crossoverPoint = new Random().nextInt(parentChromosomeOne.getGeneList().size());
            System.out.println("crossoverPoint: "+crossoverPoint);
            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for(int i=0; i < parentChromosomeOne.getGeneList().size(); i++){
                if (i < crossoverPoint){
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
                else{
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            Chromosome childChromosome = new Chromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public Chromosome twoPointCrossover(Chromosome parentChromosomeOne, Chromosome parentChromosomeTwo, double crossoverRate){


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

            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (i < crossoverPointOne || i > crossoverPointTwo) {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }

            Chromosome childChromosome = new Chromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public Chromosome randomCrossover(Chromosome parentChromosomeOne, Chromosome parentChromosomeTwo, double crossoverRate){

        if(new Random().nextDouble() < crossoverRate ) {
            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (new Random().nextInt(2) == 0) {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            Chromosome childChromosome = new Chromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public Chromosome uniformCrossover(Chromosome parentChromosomeOne, Chromosome parentChromosomeTwo, double crossoverRate){

        if(new Random().nextDouble() < crossoverRate ) {
            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for (int i = 0; i < parentChromosomeOne.getGeneList().size(); i++) {
                if (i % 2 == 0) {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeOne.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                } else {
                    Gene g = new Gene();
                    g.setSchedulingHeuristic(parentChromosomeTwo.getGeneList().get(i).getSchedulingHeuristic());
                    newChromosome.add(g);
                }
            }
            Chromosome childChromosome = new Chromosome(newChromosome);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            printChromosome(childChromosome);
            return childChromosome;
        }

    }

    public Chromosome mutate(Chromosome childChromosome, double mutationRate){

        System.out.println("Performing mutation");

        if(new Random().nextDouble() < mutationRate ) {
            int lowerLim = new Random().nextInt(childChromosome.getGeneList().size());
            int upperLim = new Random().nextInt(childChromosome.getGeneList().size());

            Chromosome mutatedChildChromosome = new Chromosome(childChromosome.getGeneList());

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

            Gene g1 = childChromosome.getGeneList().get(lowerLim);
            Gene g2 = childChromosome.getGeneList().get(upperLim);
            mutatedChildChromosome.getGeneList().set(lowerLim, g2);
            mutatedChildChromosome.getGeneList().set(upperLim, g1);

            System.out.print("mutatedChildChromosome: ");
            printChromosome(mutatedChildChromosome);

            return mutatedChildChromosome;
        } else {
            Chromosome mutatedChildChromosome = new Chromosome(childChromosome.getGeneList());
            System.out.print("mutatedChildChromosome: ");
            printChromosome(mutatedChildChromosome);
            return mutatedChildChromosome;

        }

    }

    public void mutateChildChromosomes(List<Chromosome> childChromosomesList, double mutationRate){

        for (Chromosome childChromosome : childChromosomesList
             ) {
            mutate(childChromosome,mutationRate);
        }

    }

    public List<Chromosome> getNextPopulation(){
        List<Chromosome> nextPop = new ArrayList<>(nextPopulation);
        this.nextPopulation.clear();
        return nextPop;
    }

    public void generationBest(){

        List<Chromosome> chromosomeList = this.chromosomeList;
        List<Double> fitnessList = this.fitnessList;

        generationBestFitnessValueList.add(Collections.min(fitnessList));
        generationBestChromosomeList.add(chromosomeList.get(fitnessList.indexOf(Collections.min(fitnessList))));

        System.out.println("generationBestFitnessValueList: "+generationBestFitnessValueList);
        System.out.print("generationBestChromosomeList: ");
        for (Chromosome c: generationBestChromosomeList
        ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }
        System.out.println();


    }

    public void printPerformanceMetrics(Datacenter datacenter, DatacenterBroker broker){

        double makespan = roundDecimals(broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime());
        double throughput = roundDecimals(broker.getCloudletFinishedList().size()/makespan);

        System.out.println("finishedCloudlets: "+broker.getCloudletFinishedList().size());
        System.out.println("makespan: "+makespan);
        System.out.println("throughput: "+throughput);


    }

    private double roundDecimals(double value){
        return  Math.round(value * 100.0) / 100.0;
    }

    private void printChromosomes(List<Chromosome> chromosomeList){
        for (Chromosome c: chromosomeList
        ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }
        System.out.println();
    }

    private void printChromosome(Chromosome chromosome){
        chromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
        System.out.println();
    }

    public List<Double> scalingMethodOne(List<Double> list){

        double maxValue = Collections.max(list);
        list = list.stream().map(f->f/maxValue).collect(Collectors.toList());
        return list;

    }

    public List<Double> scalingMethodTwo(List<Double> list){

        double minValue = Collections.min(list);
        list = list.stream().map(f->minValue/f).collect(Collectors.toList());
        return list;

    }

    public List<Double> scalingMethodThree(List<Double> list){

        double maxValue = Collections.max(list), minValue = Collections.min(list);
        list = list.stream().map(f->(maxValue-f)/(maxValue-minValue)).collect(Collectors.toList());
        return list;

    }

    public List<Double> scalingMethodFour(List<Double> list){

        list = list.stream().map(f->(1/f)).collect(Collectors.toList());
        Double sum = list.stream()
            .reduce(0.0, (a, b) -> a + b);
        list = list.stream().map(f->f/sum).collect(Collectors.toList());
        return list;

    }

}

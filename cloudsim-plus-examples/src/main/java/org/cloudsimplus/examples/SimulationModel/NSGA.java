package org.cloudsimplus.examples.SimulationModel;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudsimplus.examples.HybridModel.MyBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NSGA {


    List<Double> makespanList = new ArrayList<>();
    List<Double> powerConsumptionList = new ArrayList<>();

    public List<Chromosome> createInitialPopulation(int n, int len, int bound){

        List<Chromosome> chromosomeList = new ArrayList<>(n);

        for (int i=0; i<n; i++){
            ArrayList<Gene> newChromosome = new ArrayList<Gene>();
            for(int j = 0; j< len; j++){
                Random r = new Random();
                Gene g = new Gene();
                g.setSchedulingHeuristic(r.nextInt(bound));
                newChromosome.add(g);
            }
            Chromosome chromosome = new Chromosome(newChromosome);
            chromosomeList.add(chromosome);
        }
        return chromosomeList;
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

    public void computeMakespan(MyBroker broker){
        double makespan = broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime();
        System.out.println("Makespan: "+roundDecimals(makespan));
        this.makespanList.add(roundDecimals(makespan));
        System.out.println("\nmakespanList: "+makespanList);
    }

    public void computePowerConsumption(Datacenter datacenter){

        double totalHostPowerConsumption = 0;
        for (Host h :datacenter.getHostList()
             ) {
            double utilizationPercentMean = h.getCpuUtilizationStats().getMean();
            double watts = h.getPowerModel().getPower(utilizationPercentMean);
            totalHostPowerConsumption += watts;
        }
        this.powerConsumptionList.add(roundDecimals(totalHostPowerConsumption));
        System.out.println("\npowerConsumptionList: "+powerConsumptionList);

    }

    public List<Chromosome> createChildChromosomes(List<Chromosome> chromosomeList){

        printChromosomes(chromosomeList);

        List<Chromosome> childChromosomesList = new ArrayList<>(chromosomeList.size());

        for (int i=0; i<chromosomeList.size(); i++){

            Chromosome parentChromosomeOne = chromosomeList.get(new Random().nextInt(chromosomeList.size()));
            Chromosome parentChromosomeTwo = chromosomeList.get(new Random().nextInt(chromosomeList.size()));

            System.out.print("parentChromosomeOne: ");
            printChromosome(parentChromosomeOne);

            System.out.print("parentChromosomeTwo: ");
            printChromosome(parentChromosomeTwo);

            Chromosome childChromosome = null;

            switch (new Random().nextInt(4)){
                case 0:
                    System.out.println("Performing Single Point Crossover.....");
                    childChromosome = singlePointCrossover(parentChromosomeOne,parentChromosomeTwo,0.5);
                    break;
                case 1:
                    System.out.println("Performing Two Point Crossover.....");
                    childChromosome = twoPointCrossover(parentChromosomeOne,parentChromosomeTwo,0.5);
                    break;
                case 2:
                    System.out.println("Performing Random Crossover.....");
                    childChromosome = randomCrossover(parentChromosomeOne,parentChromosomeTwo,0.5);
                    break;
                case 3:
                    System.out.println("Performing Uniform Crossover.....");
                    childChromosome = uniformCrossover(parentChromosomeOne,parentChromosomeTwo,0.5);
                    break;
            }

            childChromosomesList.add(childChromosome);

        }

        System.out.println(childChromosomesList.size());

        childChromosomesList = mutateChildChromosomes(childChromosomesList, 0.4);

        printChromosomes(childChromosomesList);

        return childChromosomesList;

    }

    public List<Chromosome> combineChromosomes(List<Chromosome> parents, List<Chromosome> children){

        List<Chromosome> allChromosomes = new ArrayList<>(parents.size()+children.size());

        allChromosomes.addAll(parents);
        allChromosomes.addAll(children);

        printChromosomes(allChromosomes);

        return allChromosomes;

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

    public List<Chromosome> mutateChildChromosomes(List<Chromosome> childChromosomesList, double mutationRate){

        for (Chromosome childChromosome : childChromosomesList
        ) {
            mutate(childChromosome,mutationRate);
        }

        return childChromosomesList;

    }

    public void nonDominatedSorting(List<Chromosome> chromosomes, List<Double> fitnessOne, List<Double> fitnessTwo){

        List<Integer> front = new ArrayList<>();
        for(int i = 0; i< chromosomes.size();i++){
           for (int j = i +1; j < chromosomes.size(); j++){
               if (((fitnessOne.get(i)<= fitnessOne.get(j)) && fitnessTwo.get(i)<= fitnessTwo.get(j)) && ((fitnessOne.get(i)< fitnessOne.get(j)) || fitnessTwo.get(i) < fitnessTwo.get(j)) ){
                   front.add(i);
               }
           }
        }


        System.out.println(front);

    }








}

package org.cloudsimplus.examples.MyModel;

import org.cloudsimplus.examples.HybridModel.MyBroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithmNew {

    List<Chromosome> chromosomeList = new ArrayList<>();
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

    public void computeMakespan(MyBroker broker, List<Chromosome> chromosomeList){

        double makespan = broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size() - 1).getFinishTime();
        this.fitnessList.add(makespan);
        this. chromosomeList = chromosomeList;
        System.out.println("Makespan: "+makespan);

        System.out.println("chromosomeList: ");
        for (Chromosome c: chromosomeList
             ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }

        System.out.println("\nfitnessList: "+fitnessList);

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
        for (Chromosome c: eliteChromosomes
        ) {
            c.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.print(" ");
        }
        System.out.println();
        System.out.println();


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

        chromosomeList.get(chromosomeIndices.get(fitnessValues.indexOf(Collections.min(fitnessValues)))).getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
        System.out.println();

        return chromosomeList.get(chromosomeIndices.get(fitnessValues.indexOf(Collections.min(fitnessValues))));


    }

    public void parentSelectionCrossoverMutation(){

        for (int i=0; i<chromosomeList.size(); i++){

            Chromosome parentChromosomeOne = tournamentSelection(3);
            Chromosome parentChromosomeTwo = tournamentSelection(3);

            System.out.print("parentChromosomeOne: ");
            parentChromosomeOne.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
            System.out.print("parentChromosomeTwo: ");
            parentChromosomeTwo.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();

            Chromosome childChromosome = null;

            switch (new Random().nextInt(4)){
                case 0:
                    System.out.println("Performing Single Point Crossover.....");
                    childChromosome = singlePointCrossover(parentChromosomeOne,parentChromosomeTwo,0.9);
                    break;
                case 1:
                    System.out.println("Performing Two Point Crossover.....");
                    childChromosome = twoPointCrossover(parentChromosomeOne,parentChromosomeTwo,0.9);
                    break;
                case 2:
                    System.out.println("Performing Random Crossover.....");
                    childChromosome = randomCrossover(parentChromosomeOne,parentChromosomeTwo,0.9);
                    break;
                case 3:
                    System.out.println("Performing Uniform Crossover.....");
                    childChromosome = uniformCrossover(parentChromosomeOne,parentChromosomeTwo,0.9);
                    break;
            }

            System.out.println();

            this.childChromosomesList.add(childChromosome);

        }


        mutateChildChromosomes(childChromosomesList, 0.9);

        this.nextPopulation.addAll(childChromosomesList);
        System.out.println("nextPopulationSize: "+nextPopulation.size());

        System.out.print("nextPopulationFirstCandidate: ");
        nextPopulation.get(0).getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()+" "));


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
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
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
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
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
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
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
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
            return childChromosome;
        } else {
            Chromosome childChromosome;
            childChromosome = ((new Random().nextInt(2) == 0) ? parentChromosomeOne : parentChromosomeTwo);
            System.out.print("childChromosome: ");
            childChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
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
            mutatedChildChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();

            return mutatedChildChromosome;
        } else {
            Chromosome mutatedChildChromosome = new Chromosome(childChromosome.getGeneList());
            System.out.print("mutatedChildChromosome: ");
            mutatedChildChromosome.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic()));
            System.out.println();
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

}

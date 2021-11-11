package org.cloudsimplus.examples.MyModel;

import java.util.ArrayList;

public class Chromosome {

    ArrayList<Gene> geneList;

    public Chromosome(ArrayList<Gene> geneList) {
        this.geneList = geneList;
    }

    public ArrayList<Gene> getGeneList() {
        return this.geneList;
    }

    public void updateGene(int index, int Gene){
        Gene gene = this.geneList.get(index);
        gene.setSchedulingHeuristic(Gene);
        this.geneList.set(index,gene);
    }





}

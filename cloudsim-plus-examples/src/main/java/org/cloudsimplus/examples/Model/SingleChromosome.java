package org.cloudsimplus.examples.Model;

import java.util.ArrayList;

public class SingleChromosome {

    ArrayList<SingleGene> geneList;

    public SingleChromosome(ArrayList<SingleGene> geneList) {
        this.geneList = geneList;
    }

    public ArrayList<SingleGene> getGeneList() {
        return this.geneList;
    }

    public void updateGene(int index, int Gene) {
        SingleGene gene = this.geneList.get(index);
        gene.setSchedulingHeuristic(Gene);
        this.geneList.set(index, gene);
    }

}



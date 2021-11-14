/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2021 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.examples.Neo;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.examples.HybridModel.GeneticAlgorithmA;
import org.cloudsimplus.examples.HybridModel.GeneticAlgorithmB;
import org.cloudsimplus.examples.HybridModel.MyBroker;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.util.Log;

import java.util.*;

/**
 * A minimal but organized, structured and re-usable CloudSim Plus example
 * which shows good coding practices for creating simulation scenarios.
 *
 * <p>It defines a set of constants that enables a developer
 * to change the number of Hosts, VMs and Cloudlets to create
 * and the number of {@link Pe}s for Hosts, VMs and Cloudlets.</p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */


public class InfraGreyModelSpace1 {

    private static final double INTERVAL = 25;
    private static final int  HOSTS = 20;
    private static final int  HOST_PES = 128;
    private static final int  HOST_RAM = 1024; //in Megabytes
    private static final long HOST_BW = 1000; //in Megabits/s
    private static final long HOST_STORAGE = 100_000; //in Megabytes
    private static final int  HOST_MIPS = 4000;

    private static final int VMS = 20;
    private static final int VM_PES = 128;
    private static final int VM_RAM = 1024; //in Megabytes
    private static final long VM_BW = 1000; //in Megabits/s
    private static final long VM_STORAGE = 100_000; //in Megabytes
    private static int VM_MIPS;

    List<Integer> VM_MIPSList = new ArrayList<Integer>() {{
        add(1000);
        add(2000);
        add(3000);
        add(4000);
    } };

    private static final int CLOUDLETS = 2000;
    private static final int CLOUDLET_PES = 2;
    private static final int CLOUDLET_LENGTH = 10_000;

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile =  16000; // Integer.MAX_VALUE
    //private static final String WORKLOAD_FILENAME = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz";
    //private static final String WORKLOAD_FILENAME = "workload/swf/HPC2N-2002-2.2-cln.swf.gz";     // 202871
    private static final String WORKLOAD_FILENAME = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";  // 18239

    private CloudSim simulation;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    MyBroker broker0;
    int heuristicIndex;
    int schedulingHeuristic;

    ArrayList<Integer> solutionCandidate = new ArrayList<>();
    ArrayList<List<Cloudlet>> heuristicSpecificFinishedCloudletsList = new ArrayList<List<Cloudlet>>();

    public static void main(String[] args) {
        new InfraGreyModelSpace1();
    }

    private InfraGreyModelSpace1() {

        Log.setLevel(Level.OFF);

        // Generating Initial Population
        GeneticAlgorithmB ga = new GeneticAlgorithmB();
        ArrayList<ArrayList> solutionCandidatesList = ga.createInitialPopulation(50, 8);
        System.out.println("initialPopulation: " + solutionCandidatesList);

        // Identifying and Storing the best solution candidates of each generation
        double generationAvgFittestValue;
        double generationBestFittestValue;
        double bestFittestValue;
        ArrayList<Integer> bestFittestCandidate = new ArrayList<>();
        ArrayList<Double> generationAvgFitnessValuesList = new ArrayList<Double>();
        ArrayList<Double> generationBestFitnessValuesList = new ArrayList<Double>();
        ArrayList<Integer> generationBestSolutionCandidate = new ArrayList<>();
        ArrayList<ArrayList> generationBestSolutionCandidateList = new ArrayList<>();

        for (int generations = 0; generations < 100; generations++) {

            ArrayList<Double> solutionCandidatesFitnessList = new ArrayList<>();

            System.out.printf("%n=================================== GENERATION " + generations + " STARTS ==========================================%n");

            System.out.printf("%nsolutionCandidatesList: " + solutionCandidatesList + "%n%n");

            for (int i = 0; i < solutionCandidatesList.size(); i++) {

                heuristicIndex = 0;

                System.out.printf("%n***************** SOLUTION CANDIDATE " + i + " OF GENERATION "+ generations +"ENDS ****************%n");

                simulation = new CloudSim();
                datacenter0 = createDatacenter();
                //datacenter0.setSchedulingInterval(10);

                broker0 = new MyBroker(simulation);

                vmList = createVmsSpaceShared();
                //cloudletList = createCloudlets();
                cloudletList = createCloudletsFromWorkloadFile();
                considerSubmissionTimes(0);

                broker0.submitVmList(vmList);
                broker0.submitCloudletList(cloudletList);
                //broker0.setVmDestructionDelayFunction(v -> 1.0);

                simulation.addOnClockTickListener(this::pauseSimulation);
                simulation.addOnSimulationPauseListener(this::switchSchedulingHeuristics);

                solutionCandidate = solutionCandidatesList.get(i);
                System.out.printf("%nSolution Candidate: " + solutionCandidate + "%n%n");
                schedulingHeuristic = solutionCandidate.get(heuristicIndex);
                System.out.println("Heuristic Switched to " + schedulingHeuristic);
                //broker0.selectSchedulingPolicy(schedulingHeuristic, vmList);

                simulation.start();


                final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
                //new CloudletsTableBuilder(finishedCloudlets).build();
                //List <Cloudlet> FinishedCloudlets = getFinishedCloudlets(finishedCloudlets);
                System.out.println("Finished Cloudlets: "+finishedCloudlets.size());

                System.out.println("vms_created: "+broker0.getVmCreatedList().size());
                System.out.println("Simulation Time: " + simulation.clock());
                System.out.println("finished cloudlets: " + finishedCloudlets.size());


                double fitness = evaluatePerformanceMetrics("makespan");
                //double fitness = evaluatePerformanceMetrics("degreeOfImbalance");
                //double fitness = evaluatePerformanceMetrics("throughput");

                solutionCandidatesFitnessList.add(fitness);
                System.out.println("Solution Candidate Fitness List: "+solutionCandidatesFitnessList);

                System.out.printf("%n***************** SOLUTION CANDIDATE " + i + " OF GENERATION "+ generations +" ENDS ****************%n");

            }


            System.out.println("solutionCandidatesList:" + solutionCandidatesList);
            System.out.println("solutionCandidatesFitnessList: " + solutionCandidatesFitnessList);
            System.out.println("solutionCandidatesListSize: " + solutionCandidatesList.size());
            System.out.println("solutionCandidatesFitnessListSize: " + solutionCandidatesFitnessList.size());


            generationAvgFittestValue = ga.getGenerationAvgFittestValue(solutionCandidatesFitnessList);
            generationAvgFitnessValuesList.add(generationAvgFittestValue);
            generationBestFittestValue = ga.getGenerationBestFittestValue(solutionCandidatesFitnessList,"min");
            generationBestFitnessValuesList.add(generationBestFittestValue);
            generationBestSolutionCandidate = ga.getGenerationBestFittestSolutionCandidate(solutionCandidatesList, solutionCandidatesFitnessList,"min");
            generationBestSolutionCandidateList.add(generationBestSolutionCandidate);
            bestFittestValue = ga.getBestFittestValue(generationBestFitnessValuesList,"min");
            bestFittestCandidate = ga.getBestFittestSolutionCandidate(generationBestSolutionCandidateList,generationBestFitnessValuesList,"min");
            System.out.println("generationAvgFitnessValue: "+generationAvgFittestValue);
            System.out.println("generationAvgFitnessValuesList: "+generationAvgFitnessValuesList);
            System.out.println("generationBestFittestValue: "+generationBestFittestValue);
            System.out.println("generationBestFittestValuesList: "+generationBestFitnessValuesList);
            System.out.println("generationBestSolutionCandidate: "+generationBestSolutionCandidate);
            System.out.println("generationBestSolutionCandidateList: "+generationBestSolutionCandidateList);
            System.out.println("bestFittestValue: "+bestFittestValue);
            System.out.println("bestFittestCandidate: "+bestFittestCandidate);

            System.out.println("=================================== GENERATION "+generations+" ENDS ==========================================");

            String flag = "min";
            int eliteCount = 3;
            int tournamentCount = 4;
            double crossoverRate = 0.5;
            double mutationRate = 0.4;
            solutionCandidatesList = ga.generationEvolve(solutionCandidatesList,solutionCandidatesFitnessList,flag,eliteCount,tournamentCount, crossoverRate, mutationRate);

            System.out.println("=================================== GENERATION "+generations+" EVOLVED ==========================================");


        }

    }

    private List<Cloudlet> getFinishedCloudlets(List<Cloudlet> list) {

        Set<Cloudlet> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);
        return list;

    }

    public void switchSchedulingHeuristics(EventInfo pauseInfo) {

        heuristicIndex ++;

        schedulingHeuristic = solutionCandidate.get((heuristicIndex % 24));
        System.out.println("Heuristic Switched to "+schedulingHeuristic);
        //broker0.selectSchedulingPolicy(schedulingHeuristic, vmList);

        broker0.getCloudletSubmittedList().clear();
        System.out.println("broker submitted list cleared...");
        broker0.getCloudletCreatedList().clear();
        System.out.println("broker created list cleared...");

        List <CloudletExecution> all_exec = new ArrayList<>();
        for (Vm v : vmList
        ) {
            List<CloudletExecution> execList = v.getCloudletScheduler().getCloudletExecList();
            //all_exec.addAll(execList);
            v.getCloudletScheduler().getCloudletWaitingList().clear();
            all_exec.addAll(execList);
        }

        for (CloudletExecution c: all_exec
        ) {
            cloudletList.removeIf(cloudlet -> cloudlet.getId() == c.getCloudletId());
        }

        //System.out.println(all_exec);
        //System.out.println(cloudletList);
        //System.out.println(broker0.getCloudletWaitingList().size());

        broker0.submitCloudletList(cloudletList);

        //broker0.submitCloudletList(broker0.getCloudletWaitingList());

        simulation.resume();
        System.out.println("simulation resumed...");

    }

    private void pauseSimulation( EventInfo evt) {
        if((int)evt.getTime() == INTERVAL * (heuristicIndex + 1)){

            simulation.pause();

            System.out.printf("%n# Simulation paused at %.2f second%n%n", Math.floor(simulation.clock()));

            postSimulationHeuristicSpecificFinishedCloudlets(broker0);

            System.out.printf("Total Cloudlets processed: "+broker0.getCloudletFinishedList().size()+"%n");

            cloudletList.removeAll(broker0.getCloudletFinishedList());
            System.out.printf("Remaining Cloudlets: "+cloudletList.size()+"%n%n");

        }
    }

    private Datacenter createDatacenter() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            Host host = createHost();
            hostList.add(host);
        }
        return new DatacenterSimple(simulation, hostList);
    }

    private Host createHost() {
        final List<Pe> peList = new ArrayList<>(HOST_PES);
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(HOST_MIPS));
        }
        Host h = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
        h.setVmScheduler(new VmSchedulerSpaceShared());
        return h;
    }

    private List<Vm> createVmsSpaceShared() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            VM_MIPS = VM_MIPSList.get(i % 4);
            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE);
            vm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
            list.add(vm);
        }
        return list;
    }

    private List<Cloudlet> createCloudletsFromWorkloadFile() {
        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 3);
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        this.cloudletList = reader.generateWorkload();
        System.out.printf("# Created %12d Cloudlets for %n", this.cloudletList.size());
        return cloudletList;
    }

    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        for (int i = 0; i < CLOUDLETS; i++) {
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
            cloudlet.setSizes(1);
            list.add(cloudlet);
        }
        return list;
    }

    private void totalHostMIPSCapacity(){
        double totalHostMIPSCapacity = 0.0;
        for (Host h: datacenter0.getHostList()
        ) {
            totalHostMIPSCapacity += h.getTotalMipsCapacity();
        }
        System.out.println("Total HOST MIPS capacity: "+totalHostMIPSCapacity);
    }

    private void totalVmMIPSCapacity(){
        double totalVmMIPSCapacity = 0.0;
        for (Vm v: broker0.getVmCreatedList()
        ) {
            totalVmMIPSCapacity += v.getTotalMipsCapacity();
        }
        System.out.println("Total VMs MIPS capacity: "+totalVmMIPSCapacity);
    }

    private void considerSubmissionTimes(int n) {

        if (n == 1) {
            double minSubdelay = cloudletList.get(0).getSubmissionDelay();
            for (Cloudlet c : cloudletList
            ) {
                c.setSubmissionDelay(c.getSubmissionDelay() - minSubdelay);
            }
        } else if (n == 0){
            cloudletList.forEach(c->c.setSubmissionDelay(0));
        }

    }

    private double evaluatePerformanceMetrics(String metric) {

        double metricValue = 0;
        double makespan = broker0.getCloudletFinishedList().get(broker0.getCloudletFinishedList().size() - 1).getFinishTime();

        double throughput = broker0.getCloudletFinishedList().size() / makespan;

        if (metric == "makespan") {
            metricValue = makespan;
            System.out.println("makespan: " + ((double)Math.round(metricValue *  100.0)/100));
        }  else if (metric == "throughput") {
            metricValue = throughput;
            System.out.println("throughput: " + ((double) Math.round(metricValue * 100.0) / 100));
        }

        return metricValue;

    }

    public void postSimulationHeuristicSpecificFinishedCloudlets(MyBroker myBroker){

        List<Cloudlet> allFinishedCloudlets = myBroker.getCloudletFinishedList();
        heuristicSpecificFinishedCloudletsList.add(allFinishedCloudlets);
        int items = heuristicSpecificFinishedCloudletsList.size();
        List<Cloudlet> heuristicSpecificFinishedCloudlets = new ArrayList<Cloudlet>();
        //if (brokerh.getCloudletSubmittedList().size() > brokerh.getCloudletFinishedList().size()) {
        if (items == 1) {
            heuristicSpecificFinishedCloudlets = heuristicSpecificFinishedCloudletsList.get(0);
        } else if (items > 1) {
            List<Cloudlet> lastItem = heuristicSpecificFinishedCloudletsList.get(items - 1);
            List<Cloudlet> secondLastItem = heuristicSpecificFinishedCloudletsList.get(items - 2);
            List<Cloudlet> differences = new ArrayList<>(lastItem);
            differences.removeAll(secondLastItem);
            //heuristicSpecificFinishedCloudletsList.get(items - 1).removeAll(heuristicSpecificFinishedCloudletsList.get(items - 2));
            //heuristicSpecificFinishedCloudlets = heuristicSpecificFinishedCloudletsList.get(items - 1);
            heuristicSpecificFinishedCloudlets = differences;
        }
        //}

        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();
        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();
        System.out.printf("Heuristic Cloudlets processed: "+heuristicSpecificFinishedCloudlets.size()+"%n");
        //System.out.println("Cloudlets Heuristics processed: "+heuristicSpecificFinishedCloudlets);
        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();

    }

}

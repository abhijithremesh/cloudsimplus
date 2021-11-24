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
package org.cloudsimplus.examples.SimulationModel;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecution;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModelHostSpec;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
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


public class HybridModelGA {

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


    private static final int CLOUDLETS = 13000;
    private static final int CLOUDLET_PES = 10;
    private static final int CLOUDLET_LENGTH = 1000;
//    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile =  13000; // Integer.MAX_VALUE
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


    Chromosome solutionCandidate;
    GA gnew = new GA();
    ArrayList<List<Cloudlet>> heuristicSpecificFinishedCloudletsList = new ArrayList<List<Cloudlet>>();
    ArrayList<ExperimentVariables> exVariables = new ArrayList<>();

    public static void main(String[] args) {
        new HybridModelGA();
    }

    private HybridModelGA() {

        exVariables.add(new ExperimentVariables(50,10,24,7,2,3,0.5,0.4, 0.50,0.50, 0.5));
        //exVariables.add(new ExperimentVariables(50,10,24,5,2,3,0.5,0.4, 0.60,0.20, 0.20));
        //exVariables.add(new ExperimentVariables(50,20,24,7,2,3,0.5,0.4, 0.80,0.10, 0.10));
        //exVariables.add(new ExperimentVariables(50,20,24,5,2,3,0.5,0.4, 0.90,0.05, 0.05));

        for (ExperimentVariables ev: exVariables
             ) {

            ev.printExperimentVariables();

            Log.setLevel(Level.OFF);

            for (int ws = 0; ws < 1; ws++) {

                System.out.println("******************************************** WorkloadNum: "+ws+" ******************************************************");

                List<Integer> workloadSizes = Arrays.asList(13000,16000,18239);
                int workloadSize = workloadSizes.get(ws);


                for (int opt = 0; opt < 1; opt++) {

                    System.out.println("******************************************** Optimization: "+opt+" ******************************************************");

                    List<Chromosome> chromosomeList = gnew.createInitialPopulation(ev.popSize, ev.chromosomeLength, ev.numOfHeuristics);

                    for (int generation = 0; generation < ev.numOfGenerations; generation++) {

                        System.out.println("*********************************Generation " + generation + " starts**************************************\n");

                        for (int i = 0; i < chromosomeList.size(); i++) {

                            System.out.println("*********************Chromosome " + i + " of generation " + generation + " starts***************************************\n");

                            heuristicIndex = 0;

//                            simulation = new CloudSim();
//                            datacenter0 = createDatacenter();
//                            broker0 = new MyBroker(simulation);
//                            vmList = createVmsSpaceShared();
//                            cloudletList = createCloudletsFromWorkloadFile(workloadSize);
//                            cloudletList = createCloudlets();
//                            cloudletList = new ArrayList<>();
//                            cloudletList.addAll(workload);
//                            considerSubmissionTimes(0);
//                            broker0.submitVmList(vmList);
//                            broker0.submitCloudletList(cloudletList);

                            simulation = new CloudSim();
                            datacenter0 = createDatacenter();
                            broker0 = new MyBroker(simulation);
                            vmList = createVmsAndSubmit();
                            cloudletList = createWorkloadCloudletsAndSubmit(workloadSize);


                            simulation.addOnClockTickListener(this::pauseSimulation);
                            simulation.addOnSimulationPauseListener(this::switchSchedulingHeuristics);

                            solutionCandidate = chromosomeList.get(i);
                            solutionCandidate.getGeneList().forEach(gene -> System.out.print(gene.getSchedulingHeuristic() + " "));
                            schedulingHeuristic = solutionCandidate.getGeneList().get(heuristicIndex).getSchedulingHeuristic();
                            System.out.println("\n\nHeuristic Switched to " + schedulingHeuristic);
                            broker0.selectSchedulingPolicy(schedulingHeuristic, vmList, cloudletList);

                            simulation.start();

                            gnew.printPerformanceMetrics(datacenter0, broker0);

                            gnew.computeMakespan(broker0);
                            gnew.computeTotalWaitingTime(broker0);
                            gnew.computeFlowTime(broker0);


                            cloudletList = null;
                            vmList = null;
                            broker0 = null;
                            datacenter0.getHostList().forEach(h -> h = null);
                            datacenter0 = null;
                            simulation = null;


                            System.out.println("*********************************Chromosome " + i + " of generation " + generation + " ends********************************\n");

                        }


                        gnew.computeFitness(chromosomeList, ev.w1, ev.w2, ev.w3);
                        //gnew.computeFitness(chromosomeList);

                        gnew.generationBest();

                        System.out.println("\n*********************************Generation " + generation + " ends**************************************\n");

                        gnew.elitismSelection(ev.eliteCount);
                        gnew.parentSelectionCrossoverMutation(ev.tournamentCount, ev.crossoverRate, ev.mutationRate);

                        chromosomeList = gnew.getNextPopulation();


                    }

                }

            }

        }

    }


    public void switchSchedulingHeuristics(EventInfo pauseInfo) {

        heuristicIndex ++;

        schedulingHeuristic = solutionCandidate.getGeneList().get((heuristicIndex % 24)).getSchedulingHeuristic();

        System.out.println("Heuristic Switched to "+schedulingHeuristic);
        broker0.selectSchedulingPolicy(schedulingHeuristic, vmList, cloudletList);

        broker0.submitCloudletList(cloudletList);

        simulation.resume();
        System.out.println("simulation resumed...");



    }

    private void pauseSimulation( EventInfo evt) {

        if((int)evt.getTime() == INTERVAL * (heuristicIndex + 1)){

            simulation.pause();
            System.out.printf("%n# Simulation paused at %.2f second%n%n", Math.floor(simulation.clock()));

            //postSimulationHeuristicSpecificFinishedCloudlets(broker0);

            System.out.printf("Total Cloudlets processed: "+broker0.getCloudletFinishedList().size()+"%n");

            cloudletList.removeAll(broker0.getCloudletFinishedList());

            System.out.println("Remaining Cloudlets: "+cloudletList.size());

            List <CloudletExecution> all_exec = new ArrayList<>();
            for (Vm v : vmList
            ) {
                List<CloudletExecution> execList = v.getCloudletScheduler().getCloudletExecList();
                v.getCloudletScheduler().getCloudletWaitingList().clear();
                all_exec.addAll(execList);
            }

            for (CloudletExecution c: all_exec
            ) {
                cloudletList.removeIf(cloudlet -> cloudlet.getId() == c.getCloudletId());
            }

            System.out.println("Ignoring the cloudlets in execution state....");
            System.out.println("Remaining cloudlets: "+cloudletList.size());

        }
    }

//    private Datacenter createDatacenter() {
//        final List<Host> hostList = new ArrayList<>(HOSTS);
//        for(int i = 0; i < HOSTS; i++) {
//            Host host = createHost();
//            host.setPowerModel(getPowerSpecs().get(i % 4));
//            hostList.add(host);
//        }
//        return new DatacenterSimple(simulation, hostList);
//    }
//
//    private Host createHost() {
//        final List<Pe> peList = new ArrayList<>(HOST_PES);
//        for (int i = 0; i < HOST_PES; i++) {
//            peList.add(new PeSimple(HOST_MIPS));
//        }
//        Host h = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
//        h.setVmScheduler(new VmSchedulerSpaceShared());
//        h.enableUtilizationStats();
//        return h;
//    }


    private Datacenter createDatacenter() {
        return new DatacenterSimple(simulation, createHosts());
    }

    private List<Host> createHosts() {
        final List<Host> hostList = new ArrayList<>(HOSTS);
        for(int h = 0; h < HOSTS; h++) {
            final List<Pe> peList = new ArrayList<>(HOST_PES);
            for (int p = 0; p < HOST_PES; p++) {
                peList.add(new PeSimple(HOST_MIPS));
            }
            hostList.add(new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList));
        }
        hostList.forEach(host -> host.setVmScheduler(new VmSchedulerSpaceShared()));
        hostList.forEach(host -> host.enableUtilizationStats());
        return hostList;
    }

//    private List<Vm> createVmsSpaceShared() {
//        final List<Vm> list = new ArrayList<>(VMS);
//        for (int i = 0; i < VMS; i++) {
//            VM_MIPS = VM_MIPSList.get(i % 4);
//            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
//            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE);
//            vm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
//            vm.enableUtilizationStats();
//            list.add(vm);
//        }
//        return list;
//    }

//    private List<Vm> createVmsTimeShared() {
//        final List<Vm> list = new ArrayList<>(VMS);
//        for (int i = 0; i < VMS; i++) {
//            VM_MIPS = VM_MIPSList.get(i % 4);
//            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
//            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE);
//            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
//            list.add(vm);
//        }
//        return list;
//    }

    private List<Vm> createVmsAndSubmit() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            list.add(new VmSimple(VM_MIPSList.get(i % 4), VM_PES));
        }
        list.forEach(vm -> vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE));
        list.forEach(vm -> vm.setCloudletScheduler(new CloudletSchedulerSpaceShared()));
        list.forEach(vm -> vm.enableUtilizationStats());
        broker0.submitVmList(list);
        return list;
    }

    private List<Cloudlet> createWorkloadCloudletsAndSubmit(int n) {
        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 3);
        reader.setMaxLinesToRead(n);
        List<Cloudlet> list = reader.generateWorkload();
        System.out.printf("# Created %12d Cloudlets for %n", list.size());
        list.forEach(c->c.setSubmissionDelay(0));
        broker0.submitCloudletList(list);
        return list;
    }

//    private List<Cloudlet> createCloudletsFromWorkloadFile(int n) {
//        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 3);
//        reader.setMaxLinesToRead(n);
//        List<Cloudlet> list = reader.generateWorkload();
//        System.out.printf("# Created %12d Cloudlets for %n", list.size());
//        return list;
//    }
//
//    private List<Cloudlet> createCloudlets() {
//        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
//        final UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
//        for (int i = 0; i < CLOUDLETS; i++) {
//            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
//            cloudlet.setSizes(1);
//            list.add(cloudlet);
//        }
//        return list;
//    }



//    private void considerSubmissionTimes(int n) {
//
//        if (n == 1) {
//            double minSubdelay = cloudletList.get(0).getSubmissionDelay();
//            cloudletList.forEach(c->c.setSubmissionDelay(c.getSubmissionDelay()-minSubdelay));
//        } else if (n == 0){
//            cloudletList.forEach(c->c.setSubmissionDelay(0));
//        }
//
//    }

//    public void postSimulationHeuristicSpecificFinishedCloudlets(MyBroker myBroker){
//
//        heuristicSpecificFinishedCloudletsList.add(myBroker.getCloudletFinishedList());
//        int items = heuristicSpecificFinishedCloudletsList.size();
//        List<Cloudlet> heuristicSpecificFinishedCloudlets = new ArrayList<Cloudlet>();
//        if (items == 1) {
//            heuristicSpecificFinishedCloudlets = heuristicSpecificFinishedCloudletsList.get(0);
//        } else if (items > 1) {
//            List<Cloudlet> lastItem = heuristicSpecificFinishedCloudletsList.get(items - 1);
//            List<Cloudlet> secondLastItem = heuristicSpecificFinishedCloudletsList.get(items - 2);
//            List<Cloudlet> differences = new ArrayList<>(lastItem);
//            differences.removeAll(secondLastItem);
//            heuristicSpecificFinishedCloudlets = differences;
//        }
//        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();
//        System.out.printf("Heuristic Cloudlets processed: "+heuristicSpecificFinishedCloudlets.size()+"%n");
//
//    }

//    public List<PowerModelHostSpec> getPowerSpecs(){
//
//        List<Double> HpProLiantMl110G3PentiumD930 = Arrays.asList(105.0, 112.0, 118.0, 125.0, 131.0, 137.0, 147.0, 153.0, 157.0, 164.0, 169.0 );
//        List<Double> HpProLiantMl110G4Xeon3040 = Arrays.asList(86.0, 89.4, 92.6, 96.0, 99.5, 102.0, 106.0, 108.0, 112.0, 114.0, 117.0);
//        List<Double> IbmX3250XeonX3470 = Arrays.asList(41.6, 46.7, 52.3, 57.9, 65.4, 73.0, 80.7, 89.5, 99.6, 105.0, 113.0);
//        List<Double> IbmX3250XeonX3480 = Arrays.asList(42.3, 46.7, 49.7, 55.4, 61.8, 69.3, 76.1, 87.0, 96.1, 106.0, 113.0);
//        PowerModelHostSpec powerModelHpProLiantMl110G3PentiumD930 = new PowerModelHostSpec(HpProLiantMl110G3PentiumD930);
//        PowerModelHostSpec powerModelHpProLiantMl110G4Xeon3040 = new PowerModelHostSpec(HpProLiantMl110G4Xeon3040);
//        PowerModelHostSpec powerModelIbmX3250XeonX3470 = new PowerModelHostSpec(IbmX3250XeonX3470);
//        PowerModelHostSpec powerModelIbmX3250XeonX3480 = new PowerModelHostSpec(IbmX3250XeonX3480);
//        List<PowerModelHostSpec> powerSpecs = new ArrayList<>();
//        powerSpecs.add(powerModelHpProLiantMl110G3PentiumD930);
//        powerSpecs.add(powerModelHpProLiantMl110G4Xeon3040);
//        powerSpecs.add(powerModelIbmX3250XeonX3470);
//        powerSpecs.add(powerModelIbmX3250XeonX3480);
//
//        return powerSpecs;
//
//    }

}

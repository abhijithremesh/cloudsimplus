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
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.examples.HybridModel.GeneticAlgorithmA;
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


public class InfraGreySwitcherTime {

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

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile =  4000; // Integer.MAX_VALUE
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

    ArrayList<Integer> solutionCandidate = new ArrayList<>(Arrays.asList(4, 1, 2, 7, 0, 3, 5, 6, 3, 2, 4, 0, 1, 6, 5, 7, 7, 2, 6, 1, 4, 0, 3, 5));
    ArrayList<ArrayList> solutionCandidatesList = new ArrayList<>();
    ArrayList<List<Cloudlet>> heuristicSpecificFinishedCloudletsList = new ArrayList<List<Cloudlet>>();

    // Generating Initial Population
    //GeneticAlgorithmA ga = new GeneticAlgorithmA();
    //ArrayList<ArrayList> solutionCandidatesList = ga.createInitialPopulation(1, 8);
    //ArrayList<Integer> solutionCandidate = new ArrayList<>();


    public static void main(String[] args) {
        new InfraGreySwitcherTime();
    }

    private InfraGreySwitcherTime() {

        Log.setLevel(Level.OFF);

        solutionCandidatesList.add(solutionCandidate);

        for (int i = 0; i < solutionCandidatesList.size(); i++) {

            heuristicIndex = 0;

            simulation = new CloudSim();

            datacenter0 = createDatacenter();

            broker0 = new MyBroker(simulation);

            vmList = createVmsTimeShared();

            //cloudletList = createCloudlets();
            cloudletList = createCloudletsFromWorkloadFile();

            considerSubmissionTimes(0);

            simulation.addOnClockTickListener(this::pauseSimulation);
            simulation.addOnSimulationPauseListener(this::switchSchedulingHeuristics);

            broker0.submitVmList(vmList);
            broker0.submitCloudletList(cloudletList);

            //broker0.setVmDestructionDelayFunction(v -> 30.0);

            solutionCandidate = solutionCandidatesList.get(i);
            System.out.printf("%nSolution Candidate: "+solutionCandidate+"%n%n");
            schedulingHeuristic = solutionCandidate.get(heuristicIndex);
            System.out.println("Heuristic Switched to "+schedulingHeuristic);
            //broker0.selectSchedulingPolicy(schedulingHeuristic,vmList);


            //broker0.Random(vmList);
            //broker0.FirstComeFirstServe(vmList);
            //broker0.LongestJobFirst(vmList);
            //broker0.ShortestJobFirst(vmList);
            //broker0.ShortestCloudletFastestPE(vmList);
            //broker0.LongestCloudletFastestPE(vmList);
            //broker0.MinimumCompletionTime(vmList);
            //broker0.MinimumExecutionTime(vmList);

            //broker0.MinMin(vmList);
            //broker0.MinMin1(vmList);
            //broker0.MinMin2(vmList);
            //broker0.MinMin3(vmList);
            //broker0.MinMin4(vmList);
            //broker0.MinMin5(vmList);
            //broker0.MinMin6(vmList);

            //broker0.MaxMin(vmList);
            //broker0.MaxMin1(vmList);
            //broker0.MaxMin2(vmList);
            //broker0.MaxMin3(vmList);
            //broker0.MaxMinp(vmList);

            //broker0.Sufferage(vmList);


            simulation.start();

            //totalHostMIPSCapacity();
            //totalVmMIPSCapacity();

            //postSimulationHeuristicSpecificFinishedCloudlets(broker0);

            System.out.println("solutionCandidate: "+solutionCandidate);
            final List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
            //new CloudletsTableBuilder(finishedCloudlets).build();
            //List <Cloudlet> FinishedCloudlets = getFinishedCloudlets(finishedCloudlets);
            System.out.println("Finished Cloudlets: "+finishedCloudlets.size());

            double makespan = evaluatePerformanceMetrics("makespan");
            double degreeOfImbalance = evaluatePerformanceMetrics("degreeOfImbalance");
            double throughput = evaluatePerformanceMetrics("throughput");







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

        System.out.println(broker0.getCloudletSubmittedList().equals(broker0.getCloudletCreatedList()));

        System.out.println("cloudlets: "+cloudletList.size());

        List <CloudletExecution> all_exec = new ArrayList<>();
        for (Vm v : vmList
        ) {
            List<CloudletExecution> execList = v.getCloudletScheduler().getCloudletExecList();
            all_exec.addAll(execList);
            //v.getCloudletScheduler().getCloudletExecList().removeIf(cle -> cle.getRemainingCloudletLength() < cle.getCloudletLength());
            //v.getCloudletScheduler().getCloudletList().removeIf(c -> c.getFinishedLengthSoFar() < c.getLength());
        }

        for (CloudletExecution cle: all_exec
             ) {
            cloudletList.stream().anyMatch(cloudlet -> cloudlet.getId() == cle.getCloudletId());
        }

        //cloudletList.forEach(c-> System.out.println(c.getId()+"-"+c.getLength()+"-"+c.getFinishedLengthSoFar()));
        //System.out.println("                ");

        cloudletList.forEach(c-> c.setLength(c.getLength() - c.getFinishedLengthSoFar()));

        //cloudletList.forEach(c-> System.out.println(c.getId()+"-"+c.getLength()+"-"+c.getFinishedLengthSoFar()));

        broker0.submitCloudletList(cloudletList);

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

    private List<Vm> createVmsTimeShared() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            VM_MIPS = VM_MIPSList.get(i % 4);
            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE);
            vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            list.add(vm);
        }
        return list;
    }

    private List<Cloudlet> createCloudletsFromWorkloadFile() {
        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 3);
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        this.cloudletList = reader.generateWorkload();
        //cloudletList.remove(cloudletList.get(3));
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

        double totalResponseTime = 0.0;
        double totalWaitingTime = 0.0;
        double totalExecutionTime = 0.0;
        for (Cloudlet c : broker0.getCloudletFinishedList()
        ) {

            totalResponseTime = totalResponseTime + (c.getSubmissionDelay() + c.getWaitingTime() + c.getActualCpuTime());
            totalWaitingTime = totalWaitingTime + c.getWaitingTime();
            totalExecutionTime = totalExecutionTime + c.getActualCpuTime();

        }

        Cloudlet firstCloudlet = broker0.getCloudletFinishedList().get(0);
        double responseTime = firstCloudlet.getFinishTime() - firstCloudlet.getArrivalTime(firstCloudlet.getVm().getHost().getDatacenter());

        double totalVmRunTime = 0.0;
        for (Vm v : vmList
        ) {
            totalVmRunTime = totalVmRunTime + v.getTotalExecutionTime();
        }

        double degreeOfImbalance = 0;
        List <Double> vmExecTimeList = new ArrayList<Double>();
        for (Vm v: broker0.getVmCreatedList()
        ) {
            vmExecTimeList.add(v.getTotalExecutionTime());
        }
        //System.out.println(vmExecTimeList);
        degreeOfImbalance = (Collections.max(vmExecTimeList) - Collections.min(vmExecTimeList))/vmExecTimeList.stream().mapToDouble(d -> d).average().orElse(0.0);
        //degreeOfImbalance = (Collections.max(vmExecTimeList) + Collections.min(vmExecTimeList))/vmExecTimeList.stream().mapToDouble(d -> d).average().orElse(0.0);

        double costPerSecond = (0.12 + 0.13 + 0.17 + 0.48 + 0.52 + 0.96)/3600 ;
        double totalVmCost = totalVmRunTime * costPerSecond;

        double throughput = broker0.getCloudletFinishedList().size() / makespan;



        if (metric == "makespan") {
            metricValue = makespan;
            System.out.println("makespan: " + ((double)Math.round(metricValue *  100.0)/100));
        } else if (metric == "totalResponseTime") {
            metricValue = totalResponseTime;
            System.out.println("totalResponseTime: " + ((double)Math.round(metricValue *  100.0)/100));
        } else if (metric == "avgResponseTime") {
            metricValue = totalResponseTime / cloudletList.size();
            System.out.println("avgResponseTime: " + ((double)Math.round(metricValue *  100.0)/100) );
        } else if (metric == "totalWaitingTime") {
            metricValue = totalWaitingTime;
            System.out.println("totalWaitingTime: " + ((double)Math.round(metricValue *  100.0)/100));
        } else if (metric == "avgWaitingTime") {
            metricValue = totalWaitingTime / cloudletList.size();
            System.out.println("avgWaitingTime: " + ((double)Math.round(metricValue *  100.0)/100) );
        } else if (metric == "totalExecutionTime"){
            metricValue = totalExecutionTime;
            System.out.println("Total Execution Time: "+((double)Math.round(metricValue *  100.0)/100) );
        } else if (metric == "avgExecutionTime"){
            metricValue = totalExecutionTime/cloudletList.size();
            System.out.println("avgExecutionTime: "+ ((double)Math.round(metricValue *  100.0)/100)  );
        } else if (metric == "totalVmRunTime"){
            metricValue = totalVmRunTime;
            System.out.println("totalVmRunTime: "+totalVmRunTime);
        } else if (metric == "SlowdownRatio") {
            metricValue = (totalResponseTime / cloudletList.size()) / (totalExecutionTime / cloudletList.size());
            System.out.println("SlowdownRatio: " +((double)Math.round(metricValue *  100.0)/100)  );
        } else if(metric == "processorUtilization"){
            metricValue = totalVmRunTime/simulation.getLastCloudletProcessingUpdate();
            System.out.println("processorUtilization: "+((double)Math.round(metricValue *  100.0)/100));
        } else if (metric == "degreeOfImbalance") {
            metricValue = degreeOfImbalance;
            System.out.println("degreeOfImbalance: " + ((double) Math.round(metricValue * 100.0) / 100));
        } else if (metric == "totalVmCost")   {
            metricValue = totalVmCost;
            System.out.println("totalVmCost: " + ((double) Math.round(metricValue * 100.0) / 100));
        } else if (metric == "throughput") {
            metricValue = throughput;
            System.out.println("throughput: " + ((double) Math.round(metricValue * 100.0) / 100));
        } else if (metric == "responseTime") {
            metricValue = responseTime;
            System.out.println("responseTime: " + ((double) Math.round(metricValue * 100.0) / 100));
        }

        //return ((double)Math.round(metricValue *  100.0)/100);
        return metricValue;

    }

    public void postSimulationHeuristicSpecificFinishedCloudlets(MyBroker myBroker){

        List<Cloudlet> allFinishedCloudlets = myBroker.getCloudletFinishedList();
        System.out.println("Makespan until Now: "+broker0.getCloudletFinishedList().get(broker0.getCloudletFinishedList().size() - 1).getFinishTime());
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
        System.out.printf("Heuristic Cloudlets processed: "+heuristicSpecificFinishedCloudlets.size()+"%n");
        //System.out.println("Cloudlets Heuristics processed: "+heuristicSpecificFinishedCloudlets);
        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();

    }

}

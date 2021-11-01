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
package org.cloudsimplus.examples.check;

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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.checkHybrid.SampleBroker;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.util.Log;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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


public class SampleSwitcherTime {

    private static final double INTERVAL = 25;
    private static final int  HOSTS = 4;
    private static final int  HOST_PES = 1;
    private static final int  HOST_RAM = 1024; //in Megabytes
    private static final long HOST_BW = 1000; //in Megabits/s
    private static final long HOST_STORAGE = 100_000; //in Megabytes
    private static final int  HOST_MIPS = 4000;

    private static final int VMS = 4;
    private static final int VM_PES = 1;
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
    private static final int CLOUDLET_PES = 1;
    private static final int CLOUDLET_LENGTH = 1000;

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile =  100; // Integer.MAX_VALUE
    //private static final String WORKLOAD_FILENAME = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz";
    //private static final String WORKLOAD_FILENAME = "workload/swf/HPC2N-2002-2.2-cln.swf.gz";     // 202871
    private static final String WORKLOAD_FILENAME = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";  // 18239

    private CloudSim simulation;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    SampleBroker broker0;
    int heuristicIndex;
    int schedulingHeuristic;

    ArrayList<Integer> solutionCandidate = new ArrayList<>(Arrays.asList(4, 1, 2, 3));
    ArrayList<ArrayList> solutionCandidatesList = new ArrayList<>();
    ArrayList<List<Cloudlet>> heuristicSpecificFinishedCloudletsList = new ArrayList<List<Cloudlet>>();

    public static void main(String[] args) {
        new SampleSwitcherTime();
    }

    private SampleSwitcherTime() {

        Log.setLevel(Level.INFO);

        solutionCandidatesList.add(solutionCandidate);

        for (int i = 0; i < solutionCandidatesList.size(); i++) {

            heuristicIndex = 0;

            simulation = new CloudSim();

            datacenter0 = createDatacenter();
            datacenter0.setSchedulingInterval(1);


            broker0 = new SampleBroker(simulation);

            vmList = createVmsTimeShared();

            //cloudletList = createCloudlets();
            cloudletList = createCloudletsFromWorkloadFile();

            considerSubmissionTimes(0);

            simulation.addOnClockTickListener(this::pauseSimulation);
            simulation.addOnSimulationPauseListener(this::switchSchedulingHeuristics);


            broker0.submitVmList(vmList);
            broker0.submitCloudletList(cloudletList);

            //broker0.setVmDestructionDelayFunction(v -> 50.0);

            //broker0.BindtoVMOne(vmList);


            solutionCandidate = solutionCandidatesList.get(i);
            System.out.printf("%nSolution Candidate: "+solutionCandidate+"%n%n");
            schedulingHeuristic = solutionCandidate.get(heuristicIndex);
            System.out.println("Heuristic Switched to "+schedulingHeuristic);
            broker0.selectSchedulingPolicy(schedulingHeuristic,vmList);


            simulation.start();

            //totalHostMIPSCapacity();
            //totalVmMIPSCapacity();

            //new CloudletsTableBuilder(finishedCloudlets).build();

            //postSimulationHeuristicSpecificFinishedCloudlets(broker0);


            List<Cloudlet> finishedCloudlets = broker0.getCloudletFinishedList();
            new CloudletsTableBuilder(finishedCloudlets).build();



            System.out.println("finished cloudlets: " + finishedCloudlets.size());
            double makespan = evaluatePerformanceMetrics("makespan");
            double degreeOfImbalance = evaluatePerformanceMetrics("degreeOfImbalance");
            double throughput = evaluatePerformanceMetrics("throughput");



        }

    }



    public void switchSchedulingHeuristics(EventInfo pauseInfo) {

        List<CloudletExecution> allExecList = new ArrayList<>();
        for (Vm v: vmList
             ) {
            List<CloudletExecution> execList = v.getCloudletScheduler().getCloudletExecList();
            allExecList.addAll(execList);
        }
        System.out.println(allExecList.size());

        //allExecList.forEach(cloudletExecution -> cloudletExecution.setTimeSlice(1));

        //allExecList.forEach(cle->cle.getCloudlet().setVm(Vm.NULL));
        //allCloudList.forEach(c->c.setVm(Vm.NULL));

        heuristicIndex ++;

        schedulingHeuristic = solutionCandidate.get((heuristicIndex % 4));
        System.out.println("Heuristic Switched to "+schedulingHeuristic);
        broker0.selectSchedulingPolicy(schedulingHeuristic, vmList);

        broker0.getCloudletSubmittedList().clear();
        System.out.println("broker submitted list cleared...");
        broker0.getCloudletCreatedList().clear();
        System.out.println("broker finished list cleared...");
        System.out.println(broker0.getCloudletSubmittedList().equals(broker0.getCloudletCreatedList()));

        System.out.println("cloudlets: "+cloudletList.size());
        for (CloudletExecution cle: allExecList
        ) {
            cloudletList.stream().anyMatch(cloudlet -> cloudlet.getId() == cle.getCloudletId());
        }


        /*
        for (CloudletExecution cle: allExecList
        ) {
            Cloudlet c = cle.getCloudlet();
            c.setLength(cle.getRemainingCloudletLength());
        }
         */
        cloudletList.forEach(c-> System.out.println(c.getId()+" : "+c.getVm()+" : "+c.getLength()+" : "+c.getFinishedLengthSoFar()));
        cloudletList.forEach(c-> c.setLength(c.getLength() - c.getFinishedLengthSoFar()));
        cloudletList.forEach(c-> System.out.println(c.getId()+" : "+c.getVm()+" : "+c.getLength()+" : "+c.getFinishedLengthSoFar()));

        broker0.submitCloudletList(cloudletList);

        simulation.resume();
        System.out.println("simulation resumed...");


    }

    private void pauseSimulation( EventInfo evt) {

        if((int)evt.getTime() == INTERVAL * (heuristicIndex + 1)){

            simulation.pause();
            System.out.printf("%n# Simulation paused at %.2f second%n%n", Math.floor(simulation.clock()));

            //postSimulationHeuristicSpecificFinishedCloudlets(broker0);

            System.out.println("Finished cloudlets: "+broker0.getCloudletFinishedList().size());

            cloudletList.removeAll(broker0.getCloudletFinishedList());
            System.out.printf("Remaining Cloudlets: "+cloudletList.size()+"%n%n");


        }
    }

    private List<Cloudlet> removeDuplicates(List<Cloudlet> inList){

        Set<Long> hashSet = new HashSet<>();
        List<Cloudlet> outList = inList.stream()
            .filter(e -> hashSet.add(e.getId()))
            .collect(Collectors.toList());
        return outList;
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
        cloudletList.forEach(c->c.setNumberOfPes(1));
        System.out.printf("# Created %12d Cloudlets for %n", this.cloudletList.size());
        return cloudletList;
    }

    private List<Cloudlet> createCloudlets() {
        final List<Cloudlet> list = new ArrayList<>(CLOUDLETS);
        final UtilizationModelDynamic utilizationModelRam = new UtilizationModelDynamic(0.00001);
        final UtilizationModelDynamic utilizationModelBW = new UtilizationModelDynamic(0.00001);
        final UtilizationModelFull utilizationModelCpu = new UtilizationModelFull();
        for (int i = 0; i < CLOUDLETS; i++) {
            final Cloudlet cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES);
            cloudlet.setSizes(1);
            cloudlet.setUtilizationModelCpu(utilizationModelCpu);
            cloudlet.setUtilizationModelRam(utilizationModelRam);
            cloudlet.setUtilizationModelBw(utilizationModelBW);
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

    public void postSimulationHeuristicSpecificFinishedCloudlets(SampleBroker myBroker){

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

        new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();
        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();
        System.out.printf("Heuristic Cloudlets processed: "+heuristicSpecificFinishedCloudlets.size()+"%n");
        //System.out.println("Cloudlets Heuristics processed: "+heuristicSpecificFinishedCloudlets);
        //new CloudletsTableBuilder(heuristicSpecificFinishedCloudlets).build();

    }

}

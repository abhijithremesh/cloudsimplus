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
package org.cloudsimplus.examples.experiment;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
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
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.examples.SchedulingPolicies.MyBroker;
import org.cloudsimplus.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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


public class constrainedCloudlet {

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

    private static final int CLOUDLETS = 10000;
    private static final int CLOUDLET_PES = 10;
    private static final int CLOUDLET_LENGTH = 1000;

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile = 28476 ; // Integer.MAX_VALUE
    private static final String WORKLOAD_FILENAME = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz";
    //private static final String WORKLOAD_FILENAME = "workload/swf/HPC2N-2002-2.2-cln.swf.gz";     // 202871
    //private static final String WORKLOAD_FILENAME = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";  // 18239


    private CloudSim simulation;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    MyBroker broker0;

    public static void main(String[] args) {
        new constrainedCloudlet();
    }

    private constrainedCloudlet() {

        Log.setLevel(Level.OFF);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();
        broker0 = new MyBroker(simulation);
        vmList = createVmsAndSubmit();
        cloudletList = createWorkloadCloudletsAndSubmit(50);

        broker0.FirstComeFirstServe(vmList,cloudletList);
        //broker0.Random(vmList, cloudletList);
        //broker0.ShortestJobFirst(vmList, cloudletList);
        //broker0.LongestJobFirst(vmList,cloudletList);
        //broker0.ShortestCloudletFastestPE(vmList, cloudletList);
        //broker0.LongestCloudletFastestPE(vmList, cloudletList);
        //broker0.MinMin(vmList, cloudletList);
        //broker0.MaxMin(vmList, cloudletList);

        simulation.start();
//        printPerformanceMetrics(datacenter0, broker0);
//        new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

        List<Cloudlet> breachedCloudlets = broker0.getCloudletFinishedList().stream().filter(c->c.getActualCpuTime() > 50).collect(Collectors.toList());

        System.out.println(breachedCloudlets.size());

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


    private List<Cloudlet> createWorkloadCloudletsAndSubmit(int deadline) {
        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 1);
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        List<Cloudlet> list = reader.generateWorkload(deadline);
        System.out.printf("# Created %12d Cloudlets for %n", list.size());
        broker0.submitCloudletList(list);
        return list;
    }


//    private List<Cloudlet> createCloudletsFromWorkloadFile() {
//        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 3);
//        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
//        List<Cloudlet> list = reader.generateWorkload();
//        //cloudletList.remove(cloudletList.get(3));
//        System.out.printf("# Created %12d Cloudlets for %n", list.size());
//        return list;
//    }

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
//            for (Cloudlet c : cloudletList
//            ) {
//                c.setSubmissionDelay(c.getSubmissionDelay() - minSubdelay);
//            }
//        } else if (n == 0){
//            cloudletList.forEach(c->c.setSubmissionDelay(0));
//        }
//
//    }

    public void printPerformanceMetrics(Datacenter datacenter, DatacenterBroker broker){

        double makespan = roundDecimals(broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime());
        double throughput = roundDecimals(broker.getCloudletFinishedList().size()/makespan);

        System.out.println("finishedCloudlets: "+broker.getCloudletFinishedList().size());
        System.out.println("makespan: "+makespan);
        System.out.println("throughput: "+throughput);

        /*
        List<Double> totalHostCpuUtilizationList = new ArrayList<>();
        double totalHostCpuUtilization = 0;
        double totalHostPowerConsumption = 0;
        for (Host h: datacenter.getHostList()
        ) {
            double utilizationPercentMean = h.getCpuUtilizationStats().getMean();
            double utilizationPercentCount = h.getCpuUtilizationStats().count();
            double watts = h.getPowerModel().getPower(utilizationPercentMean);
            totalHostCpuUtilizationList.add(roundDecimals(utilizationPercentMean));
            totalHostCpuUtilization += utilizationPercentMean;
            totalHostPowerConsumption += watts;
        }
        //System.out.println("totalHostCpuUtilization: "+roundDecimals(totalHostCpuUtilization*100));
        //System.out.println("totalHostCpuUtilizationList: "+totalHostCpuUtilizationList);
        System.out.println("totalHostPowerConsumption: "+roundDecimals(totalHostPowerConsumption));
         */

        double degreeOfImbalance = 0.0;
        List <Double> vmExecTimeList = new ArrayList<Double>();
        for (Vm v: broker0.getVmCreatedList()
        ) {
            vmExecTimeList.add(v.getTotalExecutionTime());
        }
        degreeOfImbalance = (Collections.max(vmExecTimeList) - Collections.min(vmExecTimeList))/vmExecTimeList.stream().mapToDouble(d -> d).average().orElse(0.0);
        System.out.println("degreeOfImbalance: "+roundDecimals(degreeOfImbalance));

        double totalWaitingTime = 0.0;
        for (Cloudlet c: broker.getCloudletFinishedList()
        ) {
            totalWaitingTime += c.getWaitingTime();
        }
        System.out.println("totalWaitingTime: "+roundDecimals(totalWaitingTime));



        double flowTime = 0.0;
        for (Cloudlet c : broker.getCloudletFinishedList()
        ) {
            flowTime += c.getWaitingTime() + c.getActualCpuTime() + c.getSubmissionDelay();
        }
        System.out.println("flowTime: "+roundDecimals(flowTime));


        /*
        double totalCpuUtilization = 0;
        for (Vm v : broker.getVmCreatedList()
             ) {
            totalCpuUtilization += v.getCpuUtilizationStats().getMean();
        }
        System.out.println("MeanCpuUtilization: "+roundDecimals((totalCpuUtilization/20)*100));


         */
    }

    private double roundDecimals(double value){
        return  Math.round(value * 100.0) / 100.0;
    }




}

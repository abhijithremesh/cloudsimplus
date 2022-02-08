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
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.SchedulingPolicies.MyBroker;
import org.cloudsimplus.examples.SimulationModel.ExperimentVariables;
import org.cloudsimplus.util.Log;

import java.sql.SQLOutput;
import java.util.*;
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


public class optimizeServers {

    /*************************************************/
    private static final int  HOSTS = 15;
    private static final int  HOST_PES = 128;
    private static final int  HOST_RAM = 4_000_000; //in Megabytes
    private static final long HOST_BW = 1000000; //in Megabits/s
    private static final long HOST_STORAGE = 16_000_000; //in Megabytes
    private static final int  HOST_MIPS = 4000;

    private static final int VMS = 15;
    private static final int VM_PES = 128;
    private static final int VM_RAM = 4_000_000; //in Megabytes
    private static final long VM_BW = 1000; //in Megabits/s
    private static final long VM_STORAGE = 16_000_000; //in Megabytes
    /**************************************************************/

//    /*************************************************/
//    private static final int  HOSTS = 5;
//    private static final int  HOST_PES = 4;
//    private static final int  HOST_RAM = 64_000; //in Megabytes
//    private static final long HOST_BW = 1000000; //in Megabits/s
//    private static final long HOST_STORAGE = 56_000_000; //in Megabytes
//    private static final int  HOST_MIPS = 4000;
//
//    private static final int VMS = 5;
//    private static final int VM_PES = 4;
//    private static final int VM_RAM = 64_000; //in Megabytes
//    private static final long VM_BW = 1000; //in Megabits/s
//    private static final long VM_STORAGE = 56_000_000; //in Megabytes
//    /**************************************************************/


    private static int VM_MIPS;
    List<Integer> VM_MIPSList = new ArrayList<Integer>() {{
        add(1000);
        add(1000);
        add(1000);
        add(1000);
    } };


//    private static final int CLOUDLETS = 10000;
//    private static final int CLOUDLET_PES = 10;
//    private static final int CLOUDLET_LENGTH = 1000;

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile = Integer.MAX_VALUE; // Integer.MAX_VALUE
    private static final String WORKLOAD_FILENAME = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";  // 18239 128
//    private static final String WORKLOAD_FILENAME = "workload/swf/CTC-SP2-1996-3.1-cln.swf.gz";  // 77222
//    private static final String WORKLOAD_FILENAME = "workload/swf/SDSC-SP2-1998-4.2-cln.swf.gz"; // 59715 128
//    private static final String WORKLOAD_FILENAME = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz"; // 28476 100
//    private static final String WORKLOAD_FILENAME = "workload/swf/HPC2N-2002-2.2-cln.swf.gz";     // 202871
//    private static final String WORKLOAD_FILENAME = "workload/swf/CEA-Curie-2011-2.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/LANL-CM5-1994-4.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/LLNL-Atlas-2006-2.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/LLNL-Thunder-2007-1.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/OSC-Clust-2000-3.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/Sandia-Ross-2001-1.1-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/SDSC-BLUE-2000-4.2-cln.swf.gz";     //
//    private static final String WORKLOAD_FILENAME = "workload/swf/SDSC-Par-1996-3.1-cln.swf.gz";     //

    private List<String> WORKLOAD_FILENAME_LIST = Arrays.asList("workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz",  "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz", "workload/swf/SDSC-SP2-1998-4.2-cln.swf.gz");
    private List<Integer> COMPUTING_UNIT_LIST = Arrays.asList(5,10,15);

    private CloudSim simulation;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    MyBroker broker0;

    private List<Double> avgTardinessList = new ArrayList<>();

    public static void main(String[] args) {
        new optimizeServers();
    }

    private optimizeServers() {

        for (int w = 0; w < 1; w++){

            System.out.println("****************** workload: "+WORKLOAD_FILENAME_LIST.get(w)+" **************************");

            for (int u = 0; u < COMPUTING_UNIT_LIST.size(); u++) {

                System.out.println("****************** computing units: "+COMPUTING_UNIT_LIST.get(u)+" **************************");

                for (int i = 0; i < 10; i++) {

                    System.out.println("****************** simulation: " + i + " **************************");

                    Log.setLevel(Level.OFF);

                    simulation = new CloudSim();
                    datacenter0 = createDatacenter(COMPUTING_UNIT_LIST.get(u));
                    broker0 = new MyBroker(simulation);
                    vmList = createSpaceSharedVmsAndSubmit(COMPUTING_UNIT_LIST.get(u));
//                  vmList = createTimeSharedVmsAndSubmit();
                    cloudletList = createWorkloadCloudletsAndSubmit(WORKLOAD_FILENAME_LIST.get(w));
//                  cloudletList = createSeveralWorkloadCloudletsAndSubmit();

                    cloudletList.forEach(c -> c.setDeliveryTime(1 * 60 * 60, 2 * 60 * 60));
//                  cloudletList.forEach(c->c.setDeliveryTime(10, 20));

                    broker0.FirstComeFirstServe(vmList, cloudletList);
                    //broker0.Random(vmList, cloudletList);
                    //broker0.ShortestJobFirst(vmList, cloudletList);
                    //broker0.LongestJobFirst(vmList,cloudletList);
                    //broker0.ShortestCloudletFastestPE(vmList, cloudletList);
                    //broker0.LongestCloudletFastestPE(vmList, cloudletList);
                    //broker0.MinMin(vmList, cloudletList);
                    //broker0.MaxMin(vmList, cloudletList);

                    simulation.start();

                    //new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

                    List<Cloudlet> tardyCloudlets = broker0.getCloudletFinishedList().stream().filter(c -> (c.getSubmissionDelay() + c.getActualCpuTime() + c.getWaitingTime()) > c.getDeliveryTime()).collect(Collectors.toList());
                    System.out.println("tardy Cloudlets: " + tardyCloudlets.size());

                    printPerformanceMetrics(datacenter0, broker0);

                    //new CloudletsTableBuilder(tardyCloudlets).build();

                    avgTardinessList.add(printTardiness(tardyCloudlets));

                }

                System.out.println("avgTardinessList: "+avgTardinessList);
                double standardDeviation = calculateSD(avgTardinessList);
                System.out.println("standardDeviation: "+standardDeviation);
                double marginError = (1.96)*(standardDeviation * Math.sqrt(avgTardinessList.size()));  // z score for 95 percent confidence: 1.96
                System.out.println("marginError: "+marginError);
                avgTardinessList.clear();

            }

        }

    }

    private Datacenter createDatacenter(int hosts) {
        return new DatacenterSimple(simulation, createHosts(hosts));
    }

    private List<Host> createHosts(int hosts) {
        final List<Host> hostList = new ArrayList<>(hosts);
        for(int h = 0; h < hosts; h++) {
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

    private List<Vm> createTimeSharedVmsAndSubmit() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            list.add(new VmSimple(VM_MIPSList.get(i % 4), VM_PES));
        }
        list.forEach(vm -> vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE));
        list.forEach(vm -> vm.setCloudletScheduler(new CloudletSchedulerTimeShared()));
        broker0.submitVmList(list);
        return list;
    }

    private List<Vm> createSpaceSharedVmsAndSubmit(int vms) {
        final List<Vm> list = new ArrayList<>(vms);
        for (int i = 0; i < vms; i++) {
            list.add(new VmSimple(VM_MIPSList.get(i % 4), VM_PES));
        }
        list.forEach(vm -> vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE));
        list.forEach(vm -> vm.setCloudletScheduler(new CloudletSchedulerSpaceShared()));
        broker0.submitVmList(list);
        return list;
    }

    private List<Cloudlet> createWorkloadCloudletsAndSubmit(String WORKLOAD_FILENAME) {

        SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(WORKLOAD_FILENAME, 1);
        reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
        List<Cloudlet> list = reader.generateWorkload();
        list.forEach(c->c.setLength(c.getLength() * 10000));
//        list.forEach(c->c.setNumberOfPes(HOST_PES));
//        list.forEach(c->c.setSubmissionDelay(0));
        list.stream()
            .filter(cloudlet -> cloudlet.getNumberOfPes() > HOST_PES)
            .collect(Collectors.toList()).forEach(cloudlet -> cloudlet.setNumberOfPes(HOST_PES));
        broker0.submitCloudletList(list);
        System.out.println("Created "+ list.size()+" Cloudlets and submitted to broker");
        return list;
    }

    private List<Cloudlet> createSeveralWorkloadCloudletsAndSubmit() {

        List<Cloudlet> clist = new ArrayList<>();
        for (String s: WORKLOAD_FILENAME_LIST
        ) {
            SwfWorkloadFileReader reader = SwfWorkloadFileReader.getInstance(s, 1);
            reader.setMaxLinesToRead(maximumNumberOfCloudletsToCreateFromTheWorkloadFile);
            List<Cloudlet> list = reader.generateWorkload();
            clist.addAll(list);
        }
        clist.sort(Comparator.comparingDouble(Cloudlet::getSubmissionDelay));
        clist.forEach(c->c.setLength(c.getLength()));
        clist.stream()
            .filter(cloudlet -> cloudlet.getNumberOfPes() > HOST_PES)
            .collect(Collectors.toList()).forEach(cloudlet -> cloudlet.setNumberOfPes(HOST_PES));
//        clist.forEach(c->c.setNumberOfPes(HOST_PES));
//        clist.forEach(c->c.setSubmissionDelay(0));
        broker0.submitCloudletList(clist);
        System.out.println("Created "+ clist.size()+" Cloudlets and submitted to broker");
        return clist;
    }


    public void printPerformanceMetrics(Datacenter datacenter, DatacenterBroker broker){

        double makespan = roundDecimals(broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime());
        double throughput = broker.getCloudletFinishedList().size()/makespan;

//        System.out.println("finishedCloudlets: "+broker.getCloudletFinishedList().size());
        System.out.println("makespan: "+makespan/(3600));
//        System.out.println("throughput: "+throughput);

        double totalWaitingTime = 0.0;
        for (Cloudlet c: broker.getCloudletFinishedList()
        ) {
            totalWaitingTime += c.getWaitingTime();
        }
//        System.out.println("totalWaitingTime: "+roundDecimals(totalWaitingTime/3600));
        System.out.println("avgWaitingTime: "+roundDecimals((totalWaitingTime/3600)/cloudletList.size()));


        double flowTime = 0.0;
        for (Cloudlet c : broker.getCloudletFinishedList()
        ) {
            flowTime += c.getWaitingTime() + c.getActualCpuTime() + c.getSubmissionDelay();
        }
//        System.out.println("totalflowTime: "+roundDecimals(flowTime/3600));
//        System.out.println("avgflowTime: "+roundDecimals((flowTime/3600)/cloudletList.size()));


        double execTime = 0.0;
        for (Cloudlet c : broker.getCloudletFinishedList()
        ) {
            execTime +=  c.getActualCpuTime();
        }
//        System.out.println("totalexecTime: "+roundDecimals(execTime/3600));
//        System.out.println("avgexecTime: "+roundDecimals((execTime/3600)/cloudletList.size()));


    }

    private double roundDecimals(double value){
        return  Math.round(value * 100.0) / 100.0;
    }

    public double printTardiness(List<Cloudlet> cloudlets){

        double totalTardiness = 0;
        for (Cloudlet c : cloudlets
        ) {
//            totalTardiness+=((c.getFinishTime())-(c.getDeliveryTime()));
//            totalTardiness+=((c.getActualCpuTime() + c.getWaitingTime())-(c.getDeliveryTime()));
            totalTardiness+=(( c.getSubmissionDelay() + c.getActualCpuTime() + c.getWaitingTime() )-(c.getDeliveryTime()));
        }
//        System.out.println("totalTardiness: "+totalTardiness/3600);
//        System.out.println("avgtardytardiness: "+(totalTardiness/3600)/cloudlets.size());
        double avgtardiness = (totalTardiness/3600)/cloudletList.size();
        System.out.println("avgtardiness: "+avgtardiness);

        return avgtardiness;
    }

    public double calculateSD(List<Double> list){

        double sum = 0.0, standardDeviation = 0.0;

        for(double num : list) {
            sum += num;
        }

        double mean = sum/list.size();

        for(double num: list) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/list.size());
    }



}

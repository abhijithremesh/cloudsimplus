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
package org.cloudsimplus.examples.MyModel;

import ch.qos.logback.classic.Level;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.cloudbus.cloudsim.power.models.PowerModelHostSimple;
import org.cloudbus.cloudsim.power.models.PowerModelHostSpec;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.util.SwfWorkloadFileReader;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.examples.HybridModel.MyBroker;
import org.cloudsimplus.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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


public class EvaluateSingleHeuristic {
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

    private int maximumNumberOfCloudletsToCreateFromTheWorkloadFile =  Integer.MAX_VALUE ; // Integer.MAX_VALUE
    //private static final String WORKLOAD_FILENAME = "workload/swf/KTH-SP2-1996-2.1-cln.swf.gz";
    //private static final String WORKLOAD_FILENAME = "workload/swf/HPC2N-2002-2.2-cln.swf.gz";     // 202871
    private static final String WORKLOAD_FILENAME = "workload/swf/NASA-iPSC-1993-3.1-cln.swf.gz";  // 18239

    private final CloudSim simulation;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;
    MyBroker broker0;

    //List<Double> powerSpecList = Stream.iterate(1.0, n -> n + 1.0).limit(100).collect(Collectors.toList());
    //List<Double> powerSpecList = Stream.iterate(10.0, n -> n + 10.0).limit(10).collect(Collectors.toList());
    List<Double> powerModelSpecPowerHpProLiantMl110G3PentiumD930 = Arrays.asList(105.0, 112.0, 118.0, 125.0, 131.0, 137.0, 147.0, 153.0, 157.0, 164.0, 169.0 );
    List<Double> powerModelSpecPowerHpProLiantMl110G4Xeon3040 = Arrays.asList(86.0, 89.4, 92.6, 96.0, 99.5, 102.0, 106.0, 108.0, 112.0, 114.0, 117.0);
    List<Double> powerModelSpecPowerHpProLiantMl110G5Xeon3075 = Arrays.asList(93.7, 97.0, 101.0, 105.0, 110.0, 116.0, 121.0, 125.0, 129.0, 133.0, 135.0);
    List<Double> powerModelSpecPowerIbmX3250XeonX3470 = Arrays.asList(41.6, 46.7, 52.3, 57.9, 65.4, 73.0, 80.7, 89.5, 99.6, 105.0, 113.0);
    List<Double> powerModelSpecPowerIbmX3250XeonX3480 = Arrays.asList(42.3, 46.7, 49.7, 55.4, 61.8, 69.3, 76.1, 87.0, 96.1, 106.0, 113.0);
    List<Double> powerModelSpecPowerIbmX3550XeonX5670 = Arrays.asList(66.0, 107.0, 120.0, 131.0, 143.0, 156.0, 173.0, 191.0, 211.0, 229.0, 247.0);
    List<Double> powerModelSpecPowerIbmX3550XeonX5675 = Arrays.asList(58.4, 98.0, 109.0, 118.0, 128.0, 140.0, 153.0, 170.0, 189.0, 205.0, 222.0);


    public static void main(String[] args) {
        new EvaluateSingleHeuristic();
    }

    private EvaluateSingleHeuristic() {

        Log.setLevel(Level.WARN);

        simulation = new CloudSim();
        datacenter0 = createDatacenter();

        broker0 = new MyBroker(simulation);

        vmList = createVmsSpaceShared();

        //cloudletList = createCloudlets();
        cloudletList = createCloudletsFromWorkloadFile();

        considerSubmissionTimes(0);

        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        //broker0.FirstComeFirstServe(vmList,cloudletList);
        //broker0.Random(vmList, cloudletList);
        //broker0.ShortestJobFirst(vmList, cloudletList);
        //broker0.LongestJobFirst(vmList,cloudletList);
        //broker0.ShortestCloudletFastestPE(vmList, cloudletList);
        //broker0.LongestCloudletFastestPE(vmList, cloudletList);
        //broker0.MinMin(vmList, cloudletList);
        broker0.MaxMin(vmList, cloudletList);
        //broker0.Sufferage(vmList, cloudletList);

        simulation.start();

        //totalHostMIPSCapacity();
        //totalVmMIPSCapacity();

        //new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();

        printPerformanceMetrics(datacenter0,broker0);




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
        //PowerModelHost powerModel = new PowerModelHostSimple(50, 35);
        PowerModelHostSpec powerModel = new PowerModelHostSpec(powerModelSpecPowerHpProLiantMl110G3PentiumD930);
        for (int i = 0; i < HOST_PES; i++) {
            peList.add(new PeSimple(HOST_MIPS));
        }
        Host h = new HostSimple(HOST_RAM, HOST_BW, HOST_STORAGE, peList);
        h.setVmScheduler(new VmSchedulerSpaceShared());
        h.setPowerModel(powerModel);
        h.enableUtilizationStats();
        return h;
    }

    private List<Vm> createVmsSpaceShared() {
        final List<Vm> list = new ArrayList<>(VMS);
        for (int i = 0; i < VMS; i++) {
            VM_MIPS = VM_MIPSList.get(i % 4);
            final Vm vm = new VmSimple(VM_MIPS, VM_PES);
            vm.setRam(VM_RAM).setBw(VM_BW).setSize(VM_STORAGE);
            vm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vm.enableUtilizationStats();
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

    public void printPerformanceMetrics(Datacenter datacenter, DatacenterBroker broker){

        double makespan = roundDecimals(broker.getCloudletFinishedList().get(broker.getCloudletFinishedList().size()-1).getFinishTime());
        double throughput = roundDecimals(broker.getCloudletFinishedList().size()/makespan);

        System.out.println("finishedCloudlets: "+broker.getCloudletFinishedList().size());
        System.out.println("makespan: "+makespan);
        System.out.println("throughput: "+throughput);

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

    }

    private double roundDecimals(double value){
        return  Math.round(value * 100.0) / 100.0;
    }

}

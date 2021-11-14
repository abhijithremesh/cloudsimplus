package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerFirstFit;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;


import java.util.List;

public class MyBroker extends DatacenterBrokerSimple {

    public MyBroker(final CloudSim simulation) {
        super(simulation);
    }


    public void FirstComeFirstServe(List<Vm> vmList, List<Cloudlet> cloudletList){

        FirstComeFirstServePolicy fcfs = new FirstComeFirstServePolicy(this, vmList, cloudletList);
        fcfs.schedule();

    }

    public void LongestJobFirst(List<Vm> vmList, List<Cloudlet> cloudletList){

        LongestJobFirstPolicy ljf = new LongestJobFirstPolicy(this, vmList,cloudletList);
        ljf.schedule();

    }

    public void ShortestJobFirst(List<Vm> vmList, List<Cloudlet> cloudletList){

        ShortestJobFirstPolicy sjf = new ShortestJobFirstPolicy(this, vmList, cloudletList);
        sjf.schedule();

    }


    public void Random(List<Vm> vmList, List<Cloudlet> cloudletList){

        RandomPolicy r = new RandomPolicy(this, vmList, cloudletList);
        r.schedule();

    }

    public void ShortestCloudletFastestPE(List<Vm> vmList, List<Cloudlet> cloudletList){

        ShortestCloudletFastestPEPolicy scfp = new ShortestCloudletFastestPEPolicy(this, vmList, cloudletList);
        scfp.schedule();

    }

    public void LongestCloudletFastestPE(List<Vm> vmList, List<Cloudlet> cloudletList){

        LongestCloudletFastestPEPolicy lcfp = new LongestCloudletFastestPEPolicy(this, vmList, cloudletList);
        lcfp.schedule();

    }

    public void MinMin(List<Vm> vmList, List<Cloudlet> cloudletList){

        MinMinPolicy min = new MinMinPolicy(this, vmList, cloudletList);
        min.schedule();

    }


    public void MaxMin(List<Vm> vmList, List<Cloudlet> cloudletList){

        MaxMinPolicy max = new MaxMinPolicy(this, vmList, cloudletList);
        max.schedule();

    }


    public void Sufferage(List<Vm> vmList, List<Cloudlet> cloudletList){

        SufferagePolicy s = new SufferagePolicy(this, vmList, cloudletList);
        s.schedule();

    }

    public void selectSchedulingPolicy(int schedulingHeuristic, List<Vm> vmList, List<Cloudlet> cloudletList){
        switch(schedulingHeuristic){
            case 0:
                System.out.println("FCFS");
                this.FirstComeFirstServe(vmList, cloudletList);
                break;
            case 1:
                System.out.println("SJF");
                this.ShortestJobFirst(vmList, cloudletList);
                break;
            case 2:
                System.out.println("LJF");
                this.LongestJobFirst(vmList,cloudletList);
                break;
            case 3:
                System.out.println("SCFP");
                this.ShortestCloudletFastestPE(vmList, cloudletList);
                break;
            case 4:
                System.out.println("LCFP");
                this.LongestCloudletFastestPE(vmList, cloudletList);
                break;
            case 5:
                System.out.println("MAX-MIN");
                this.MaxMin(vmList,cloudletList);
                break;
            case 6:
                System.out.println("MIN-MIN");
                this.MinMin(vmList, cloudletList);
                break;
            case 7:
                System.out.println("Random");
                this.Random(vmList, cloudletList);
                break;
            case 8:
                System.out.println("Sufferage");
                this.Sufferage(vmList, cloudletList);
                break;


        }
    }






}

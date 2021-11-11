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


    public void FirstComeFirstServe(List<Vm> vmList){

        FirstComeFirstServePolicy fcfs = new FirstComeFirstServePolicy(this, vmList);
        fcfs.schedule();

    }

    public void LongestJobFirst(List<Vm> vmList){

        LongestJobFirstPolicy ljf = new LongestJobFirstPolicy(this, vmList);
        ljf.schedule();

    }

    public void ShortestJobFirst(List<Vm> vmList){

        ShortestJobFirstPolicy sjf = new ShortestJobFirstPolicy(this, vmList);
        sjf.schedule();

    }


    public void Random(List<Vm> vmList){

        RandomPolicy r = new RandomPolicy(this, vmList);
        r.schedule();

    }

    public void ShortestCloudletFastestPE(List<Vm> vmList){

        ShortestCloudletFastestPEPolicy scfp = new ShortestCloudletFastestPEPolicy(this, vmList);
        scfp.schedule();

    }

    public void LongestCloudletFastestPE(List<Vm> vmList){

        LongestCloudletFastestPEPolicy lcfp = new LongestCloudletFastestPEPolicy(this, vmList);
        lcfp.schedule();

    }



    public void MinMin(List<Vm> vmList){

        MinMinPolicy min = new MinMinPolicy(this, vmList);
        min.schedule();

    }


    public void MaxMin(List<Vm> vmList){

        MaxMinPolicy max = new MaxMinPolicy(this, vmList);
        max.schedule();

    }


    public void Sufferage(List<Vm> vmList){

        SufferagePolicy s = new SufferagePolicy(this, vmList);
        s.schedule();

    }

    public void selectSchedulingPolicy(int schedulingHeuristic, List<Vm> vmList){
        switch(schedulingHeuristic){
            case 0:
                System.out.println("FCFS");
                this.FirstComeFirstServe(vmList);
                break;
            case 1:
                System.out.println("SJF");
                this.ShortestJobFirst(vmList);
                break;
            case 2:
                System.out.println("LJF");
                this.LongestJobFirst(vmList);
                break;
            case 3:
                System.out.println("SCFP");
                this.ShortestCloudletFastestPE(vmList);
                break;
            case 4:
                System.out.println("LCFP");
                this.LongestCloudletFastestPE(vmList);
                break;
            case 5:
                System.out.println("MAX-MIN");
                this.MaxMin(vmList);
                break;
            case 6:
                System.out.println("MIN-MIN");
                this.MinMin(vmList);
                break;
            case 7:
                System.out.println("Random");
                this.Random(vmList);
                break;
            case 8:
                System.out.println("Sufferage");
                this.Sufferage(vmList);
                break;


        }
    }






}

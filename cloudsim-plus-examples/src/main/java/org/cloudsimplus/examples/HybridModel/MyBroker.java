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

    public void FirstComeFirstServeFirstFit(List<Vm> vmList){

        FirstComeFirstServeFirstFitPolicy ff = new FirstComeFirstServeFirstFitPolicy(this, vmList);
        ff.schedule();

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

    public void MinimumExecutionTime(List<Vm> vmList){

        MinimumExecutionTimePolicy met = new MinimumExecutionTimePolicy(this, vmList);
        met.schedule();

    }

    public void MinimumCompletionTime(List<Vm> vmList){

        MinimumCompletionTimePolicy mct = new MinimumCompletionTimePolicy(this, vmList);
        mct.schedule();

    }

    public void MinMin(List<Vm> vmList){

        MinMinPolicy min = new MinMinPolicy(this, vmList);
        min.schedule();

    }

    public void MinMin1(List<Vm> vmList){

        MinMinPolicy1 min1 = new MinMinPolicy1(this, vmList);
        min1.schedule();

    }

    public void MinMin2(List<Vm> vmList){

        MinMinPolicy2 min2 = new MinMinPolicy2(this, vmList);
        min2.schedule();

    }

    public void MinMin3(List<Vm> vmList){

        MinMinPolicy3 min3 = new MinMinPolicy3(this, vmList);
        min3.schedule();

    }

    public void MinMin4(List<Vm> vmList){

        MinMinPolicy4 min4 = new MinMinPolicy4(this, vmList);
        min4.schedule();

    }

    public void MinMin5(List<Vm> vmList){

        MinMinPolicy5 min5 = new MinMinPolicy5(this, vmList);
        min5.schedule();

    }

    public void MinMin6(List<Vm> vmList){

        MinMinPolicy6 min6 = new MinMinPolicy6(this, vmList);
        min6.schedule();

    }


    public void MaxMin(List<Vm> vmList){

        MaxMinPolicy max = new MaxMinPolicy(this, vmList);
        max.schedule();

    }

    public void MaxMin1(List<Vm> vmList){

        MaxMinPolicy1 max = new MaxMinPolicy1(this, vmList);
        max.schedule();

    }

    public void MaxMin2(List<Vm> vmList){

        MaxMinPolicy2 max = new MaxMinPolicy2(this, vmList);
        max.schedule();

    }

    public void MaxMin3(List<Vm> vmList){

        MaxMinPolicy3 max = new MaxMinPolicy3(this, vmList);
        max.schedule();

    }


    public void MaxMinp(List<Vm> vmList){

        MaxMinPolicyp maxp = new MaxMinPolicyp(this, vmList);
        maxp.schedule();

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
                System.out.println("Sufferage");
                this.Sufferage(vmList);
                break;
            case 8:
                System.out.println("Random");
                this.Random(vmList);
                break;


        }
    }






}

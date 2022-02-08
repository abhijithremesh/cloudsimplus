package org.cloudsimplus.examples.SchedulingPolicies;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingLong;

public class LongestCloudletFastestPEPolicy {

    MyBroker myBroker;
    List<Vm> vmList;
    List<Cloudlet> cloudletList;

    LongestCloudletFastestPEPolicy (MyBroker myBroker, List<Vm> vmList, List<Cloudlet> cloudletList){

        this.myBroker = myBroker;
        this.vmList = vmList;
        this.cloudletList = cloudletList;

    }

    public void schedule() {

        System.out.println("Scheduling with LCFP Policy");

        final Comparator<Cloudlet> sortByLength = comparingLong(cl -> cl.getLength());
        final Comparator<Vm> sortByMIPS = comparingDouble(v -> v.getMips());
        cloudletList.sort(sortByLength.reversed());
        vmList.sort(sortByMIPS.reversed());

        //cloudletList.sort((Cloudlet s1, Cloudlet s2)-> Math.toIntExact(s2.getLength()-s1.getLength()));
        //vmList.sort((Vm v1, Vm v2)-> Math.toIntExact((long)(v2.getMips()-v1.getMips())));

        for(int i=0;i<cloudletList.size();i++){
            Cloudlet cl = cloudletList.get(i);
            Vm vm = vmList.get((i % vmList.size()));
            myBroker.bindCloudletToVm(cl,vm);
            //System.out.println(cl.getLength()+" "+vm.getMips());
        }

    }

}
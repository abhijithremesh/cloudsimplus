package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingLong;

public class LongestJobFirstPolicy {

    MyBroker myBroker;
    List<Vm> vmList;
    List<Cloudlet> cloudletList;


    LongestJobFirstPolicy (MyBroker myBroker, List<Vm> vmList, List<Cloudlet> cloudletList){

        this.myBroker = myBroker;
        this.vmList = vmList;
        this.cloudletList = cloudletList;


    }

    public void schedule() {

        System.out.println("Scheduling with LJF Policy");

        final Comparator<Cloudlet> sortBylength = comparingLong(cl -> cl.getLength());
        cloudletList.sort(sortBylength.reversed());

        //cloudletList.sort((Cloudlet s1, Cloudlet s2)-> Math.toIntExact(s2.getLength()-s1.getLength()));

        for(int i=0; i < cloudletList.size(); i++){
            Cloudlet cl = cloudletList.get(i);
            Vm vm = vmList.get((i % vmList.size()));
            myBroker.bindCloudletToVm(cl,vm);
            //System.out.println(cl.getLength()+" "+vm.getId());
        }

    }

}
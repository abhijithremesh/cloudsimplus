package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingLong;


public class ShortestJobFirstPolicy {

    MyBroker myBroker;
    List<Vm> vmList;
    List<Cloudlet> cloudletList;


    ShortestJobFirstPolicy (MyBroker myBroker, List<Vm> vmList, List<Cloudlet> cloudletList){

        this.myBroker = myBroker;
        this.vmList = vmList;
        this.cloudletList = cloudletList;

    }

    public void schedule() {

        System.out.println("Scheduling with SJF Policy");

        final Comparator<Cloudlet> sortBylength = comparingLong(cl -> cl.getLength());
        cloudletList.sort(sortBylength);

        //cloudletList.sort((Cloudlet s1, Cloudlet s2)-> Math.toIntExact(s1.getLength()-s2.getLength()));

        for(int i=0;i<cloudletList.size();i++){
            Cloudlet cl = cloudletList.get(i);
            Vm vm = vmList.get((i % vmList.size()));
            myBroker.bindCloudletToVm(cl,vm);
            //System.out.println(cl.getId()+" "+cl.getLength()+" "+vm.getId());
        }

    }

}


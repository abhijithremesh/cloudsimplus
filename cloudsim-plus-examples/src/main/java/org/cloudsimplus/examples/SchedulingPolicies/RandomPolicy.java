package org.cloudsimplus.examples.SchedulingPolicies;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;
import java.util.Random;

public class RandomPolicy {

    MyBroker myBroker;
    List<Vm> vmList;
    List<Cloudlet> cloudletList;

    RandomPolicy (MyBroker myBroker, List<Vm> vmList, List<Cloudlet> cloudletList){

        this.myBroker = myBroker;
        this.vmList = vmList;
        this.cloudletList = cloudletList;

    }

    public void schedule() {

        System.out.println("Scheduling with Random Policy");

        for (int i = 0; i <cloudletList.size(); i++){
            Cloudlet cl = cloudletList.get(i);
            int v = new Random().nextInt(vmList.size());
            Vm vm = vmList.get(v);
            myBroker.bindCloudletToVm(cl, vm);
        }


    }
}

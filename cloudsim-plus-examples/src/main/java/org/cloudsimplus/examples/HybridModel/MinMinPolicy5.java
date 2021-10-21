package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.List;

public class MinMinPolicy5 {

    MyBroker myBroker;
    List<Vm> vmList;

    MinMinPolicy5 (MyBroker myBroker, List<Vm> vmList){

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule(){

        System.out.println("Scheduling with MIN_MIN Policy");

        System.out.println("Cloudlets waiting: "+myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: "+myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();





    }



}

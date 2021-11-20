package org.cloudsimplus.examples.checkHybrid;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;
import java.util.function.Function;

public class BindtoVMFourPolicy {

    SampleBroker myBroker;
    List<Vm> vmList;

    BindtoVMFourPolicy(SampleBroker myBroker, List<Vm> vmList) {

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule() {

        System.out.println("Scheduling with BindtoVMFourPolicy");

        System.out.println("Cloudlets waiting: " + myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: " + myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

/*
        myBroker.setVmMapper(c->vmList.get(3));

 */



        for (int i = 0; i < cloudletList.size(); i++) {

            Cloudlet cl = cloudletList.get(i);
            Vm vm = vmList.get(3);
            //cl.setLength(cl.getLength()* (long) vm.getMips());
            //cl.setVm(vm);
            myBroker.bindCloudletToVm(cl, vm);
            System.out.println(cl+" : "+vm);

        }




    }

}

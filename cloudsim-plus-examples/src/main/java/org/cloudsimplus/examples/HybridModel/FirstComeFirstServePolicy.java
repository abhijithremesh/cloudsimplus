package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingLong;

public class FirstComeFirstServePolicy {

    MyBroker myBroker;
    List<Vm> vmList;

    FirstComeFirstServePolicy(MyBroker myBroker, List<Vm> vmList) {

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule() {

        System.out.println("Scheduling with FCFS Policy");

        System.out.println("Cloudlets waiting: " + myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: " + myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        final Comparator<Cloudlet> sortById = comparingLong(cl -> cl.getId());
        cloudletList.sort(sortById);


        for (int i = 0; i < cloudletList.size(); i++) {
                Cloudlet cl = cloudletList.get(i);
                Vm vm = vmList.get((i % vmList.size()));
                myBroker.bindCloudletToVm(cl, vm);
                //System.out.println(cl+" : "+vm);
        }


        }

    }


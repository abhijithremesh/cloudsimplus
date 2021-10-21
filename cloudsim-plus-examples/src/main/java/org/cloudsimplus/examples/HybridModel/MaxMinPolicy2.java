package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.List;

public class MaxMinPolicy2 {

    MyBroker myBroker;
    List<Vm> vmList;

    MaxMinPolicy2 (MyBroker myBroker, List<Vm> vmList){

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule(){

        System.out.println("Scheduling with MAX_MIN Policy");

        System.out.println("Cloudlets waiting: "+myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: "+myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        double[][] executionTimes = new double[cloudletList.size()][vmList.size()];

        // build execution time matrix
        for (int i=0; i < cloudletList.size(); i++) {
            for (int j=0; j < vmList.size(); j++) {
                executionTimes[i][j] = cloudletList.get(i).getLength() / vmList.get(j).getMips();
            }
        }

        ArrayList<Integer> scheduled = new ArrayList<>();

        while (scheduled.size() < cloudletList.size()) {
            double maxExecutionTime = 0;
            double minExecutionTime = Double.MAX_VALUE;
            int currentCloudlet = 0;
            int currentVm = 0;

            // find task with max time to execute
            for (int i=0; i < cloudletList.size(); i++) {
                if (!scheduled.contains(i)) {
                    for (int j=0; j < vmList.size(); j++) {
                        if (maxExecutionTime < executionTimes[i][j]) {
                            currentCloudlet = i;
                            maxExecutionTime = executionTimes[i][j];
                        }
                    }
                }
            }

            // have cloudlet with max execution time... now find resource with least
            for (int i=0; i < cloudletList.size(); i++) {
                if (!scheduled.contains(i)) {
                    for (int j=0; j < vmList.size(); j++) {
                        if (minExecutionTime > executionTimes[i][j]) {
                            currentVm = j;
                            minExecutionTime = executionTimes[i][j];
                        }
                    }
                }
            }

            // assign task with max execution time to resource with min execution time
            cloudletList.get(currentCloudlet).setVm(vmList.get(currentVm));
            scheduled.add(currentCloudlet);

            // update the completion times
            for (int i=0; i < cloudletList.size(); i++) {
                if (!scheduled.contains(i)) {
                    executionTimes[i][currentVm] += cloudletList.get(currentCloudlet).getId() / vmList.get(currentVm).getMips();
                }
            }
        }

    }

}

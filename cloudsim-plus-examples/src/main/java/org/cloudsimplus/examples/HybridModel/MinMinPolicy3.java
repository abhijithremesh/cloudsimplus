package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

public class MinMinPolicy3 {

    MyBroker myBroker;
    List<Vm> vmList;

    MinMinPolicy3 (MyBroker myBroker, List<Vm> vmList){

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule() {

        System.out.println("Scheduling with MIN_MIN Policy");

        System.out.println("Cloudlets waiting: " + myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: " + myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        int nTasks = cloudletList.size(); // Holds number of tasks
        int nVms = vmList.size(); // Holds number of vms

        double cTime[][] = new double[nTasks][nVms];
        //double time =0.0;
        // computing completion and execution time for each cloudlet over each vm
        for (int i = 0; i < nTasks; i++) {
            for (int j = 0; j < nVms; j++) {
                cTime[i][j] = getCompletionTime(cloudletList.get(i), vmList.get(j));
            }
        }

        for (int i = 0; i < nTasks; i++) {
            int minTask = -1;
            int minVm = -1;
            double minCTime = -1.0;
            for (int j = 0; j < nTasks; j++) {

                // Search in all VMs
                for (int k = 0; k < nVms; k++) {
                    if (cTime[j][k] > 0.0 && minCTime < 0.0) {
                        minCTime = cTime[j][k];
                        minTask = j;
                        minVm = k;
                    } else if (cTime[j][k] > 0.0 && minCTime > cTime[j][k]) {
                        minCTime = cTime[j][k];
                        minTask = j;
                        minVm = k;
                    }

                }

            }

            // At that point minTask with
            // j as task number
            // k as vm number
            // would be found

            // Bind both
            System.out.println("Executing Cloudlet #" + minTask + " on VM#" + minVm);
            myBroker.bindCloudletToVm(cloudletList.get(minTask), vmList.get(minVm));

            // Set 0 as completion time for minTask
            for (int j = 0; j < nVms; j++) {
                cTime[minTask][j] = 0.0;
            }

        }

    }

    private double getCompletionTime(Cloudlet cloudlet, Vm vm){

        double waitingTime = cloudlet.getWaitingTime();
        double execTime = cloudlet.getLength() / (vm.getMips()*vm.getNumberOfPes());
        double completionTime = execTime + waitingTime;
        return completionTime;

    }



}

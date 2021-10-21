package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.List;

public class MaxMinPolicyp {

    MyBroker myBroker;
    List<Vm> vmList;

    MaxMinPolicyp(MyBroker myBroker, List<Vm> vmList) {

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule() {

        System.out.println("Scheduling with MAX_MIN Policy");

        System.out.println("Cloudlets waiting: " + myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: " + myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        int reqTasks = cloudletList.size();
        int reqVms = vmList.size();
        //int k=0;

        ArrayList<Cloudlet> clist = new ArrayList<Cloudlet>();
        ArrayList<Vm> vlist = new ArrayList<Vm>();

        for (Cloudlet cloudlet : cloudletList) {
            clist.add(cloudlet);
            //System.out.println("clist:" +clist.get(k).getCloudletId());
            //k++;
        }
        //k=0;
        for (Vm vm : vmList) {
            vlist.add(vm);
            //System.out.println("vlist:" +vlist.get(k).getId());
            //k++;
        }


        double completionTime[][] = new double[reqTasks][reqVms];
        double execTime[][] = new double[reqTasks][reqVms];
        double time = 0.0;

        for (int i = 0; i < reqTasks; i++) {
            for (int j = 0; j < reqVms; j++) {
                time = getCompletionTime(clist.get(i), vlist.get(j));
                completionTime[i][j] = time;
                time = getExecTime(clist.get(i), vlist.get(j));
                execTime[i][j] = time;

                System.out.print(execTime[i][j] + ",");

            }
            System.out.println();

        }


        for (int c = 0; c < clist.size(); c++) {

            double max = 0;
            double tempMax = 0;
            int maxCloudlet = 0;
            int maxVm = 0;
            int minCVm = 0;
            for (int i = 0; i < clist.size(); i++) {

                for (int j = 0; j < vlist.size(); j++) {
                    tempMax = max;
                    max = Math.max(max, completionTime[i][j]);
                    if (max != tempMax) {
                        maxCloudlet = i;
                        maxVm = j;
                    }
                }


            }


            double minC = Double.MAX_VALUE;
            double tempMinC = Double.MAX_VALUE;
            for (int j = 0; j < vlist.size(); j++) {
                tempMinC = minC;
                minC = Math.min(minC, completionTime[maxCloudlet][j]);
                if (minC != tempMinC) {
                    minCVm = j;
                }
            }

            myBroker.bindCloudletToVm(clist.get(maxCloudlet), vlist.get(minCVm));

            for (int i = 0; i < clist.size(); i++) {
                completionTime[i][minCVm] += minC;
            }
            for (int i = 0; i < vlist.size(); i++) {
                completionTime[maxCloudlet][i] = 0;
            }


        }


    }

    private double getCompletionTime(Cloudlet cloudlet, Vm vm) {

        double waitingTime = cloudlet.getWaitingTime();
        double execTime = cloudlet.getLength()/ (vm.getMips() * vm.getNumberOfPes());
        //double execTime = cloudlet.getLength()/ (vm.getMips() * cloudlet.getNumberOfPes());
        double completionTime = execTime + waitingTime;
        return completionTime;

    }

    private double getExecTime(Cloudlet cloudlet, Vm vm) {
        return cloudlet.getLength() / (vm.getMips() * vm.getNumberOfPes());
        //return cloudlet.getLength() / (vm.getMips() * cloudlet.getNumberOfPes());
    }

}

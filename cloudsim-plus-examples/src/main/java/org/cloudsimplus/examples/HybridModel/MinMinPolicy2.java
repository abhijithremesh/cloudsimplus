package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MinMinPolicy2 {

    MyBroker myBroker;
    List<Vm> vmList;

    MinMinPolicy2 (MyBroker myBroker, List<Vm> vmList){

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule(){

        System.out.println("Scheduling with MIN_MIN Policy");

        System.out.println("Cloudlets waiting: "+myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: "+myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();


        List hasChecked = new ArrayList<Boolean>();
        List vmforCloudlet = new ArrayList<Integer>();

        HashMap<Vm, Double> readyTime = new HashMap();
        ArrayList<Cloudlet> sortedCloudletlist= new ArrayList<Cloudlet>();

        hasChecked.clear();
        vmforCloudlet.clear();

        for (int t = 0; t < cloudletList.size(); t++) {
            hasChecked.add(false);
            vmforCloudlet.add(0);
        }

        for (int t = 0; t < vmList.size(); t++) {
            Vm v = vmList.get(t);
            readyTime.put(v, 0.0);
        }

        for (int i = 0; i < cloudletList.size(); i++) {
            int minIndex = 0;
            Cloudlet minCloudlet = null;
            for (int j = 0; j < cloudletList.size(); j++) {
                Cloudlet cloud = cloudletList.get(j);
                boolean chk = (Boolean) (hasChecked.get(j));
                if (!chk) {
                    minCloudlet = cloud;
                    minIndex = j;
                    break;
                }
            }
            if (minCloudlet == null) {
                break;
            }


            for (int j = 0; j < cloudletList.size(); j++) {
                Cloudlet cloud = (Cloudlet) cloudletList.get(j);
                boolean chk = (Boolean) (hasChecked.get(j));

                if (chk) {
                    continue;
                }

                long cloudletlength = cloud.getLength();

                if (cloudletlength < minCloudlet.getLength()) {
                    minCloudlet = cloud;
                    minIndex = j;
                }
            }
            hasChecked.set(minIndex, true);

            Vm assignedVm = null;
            double minFinishTime= Double.MAX_VALUE;
            for(Object vmObject: vmList){
                Vm v = (Vm) vmObject;
                double finishTime =0.0;

                finishTime = readyTime.get(v)+ minCloudlet.getLength()/v.getMips();

                if(finishTime < minFinishTime){
                    minFinishTime = finishTime;
                    assignedVm = v;
                }
            }
            minCloudlet.setVm(assignedVm);
            //broker.bindCloudletToVm(minCloudlet.getCloudletId(),assignedVm.getId());



            //vmforCloudlet.set(minCloudlet, assignedVm);

            readyTime.put(assignedVm, minFinishTime);
            System.out.println("Cloudlet " + minCloudlet.getId() + " bound to Vm " + assignedVm.getId());
            sortedCloudletlist.add(minCloudlet);
        }

        for(Object cloudletObject: cloudletList){
            Cloudlet cloudlet1 = (Cloudlet) cloudletObject;
            myBroker.bindCloudletToVm(cloudlet1,(Vm)vmforCloudlet.get((int)cloudlet1.getId()));
        }






    }



}

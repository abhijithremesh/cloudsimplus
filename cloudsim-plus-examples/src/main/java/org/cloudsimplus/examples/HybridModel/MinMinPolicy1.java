package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.List;

public class MinMinPolicy1 {

    MyBroker myBroker;
    List<Vm> vmList;

    MinMinPolicy1 (MyBroker myBroker, List<Vm> vmList){

    this.myBroker = myBroker;
    this.vmList = vmList;

    }

    public void schedule(){

        System.out.println("Scheduling with MIN_MIN Policy");

        System.out.println("Cloudlets waiting: "+myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: "+myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        int reqTasks= cloudletList.size();
        int reqVms= vmList.size();

        ArrayList<Cloudlet> clist = new ArrayList<Cloudlet>();
        ArrayList<Vm> vlist = new ArrayList<Vm>();

        for (Cloudlet cloudlet : cloudletList) {
            clist.add(cloudlet);
        }

        for (Vm vm : vmList) {
            vlist.add(vm);
        }

        double completionTime[][] = new double[reqTasks][reqVms];
        double execTime[][] = new double[reqTasks][reqVms];
        double time =0.0;

        for(int i=0; i<reqTasks; i++){
            for(int j=0; j<reqVms; j++){
                time = getCompletionTime(clist.get(i), vlist.get(j));
                completionTime[i][j]= time;
                time = getExecTime(clist.get(i), vlist.get(j));
                execTime[i][j]= time;
            }
        }

        int minCloudlet=0;
        int minVm=0;
        double min=-1.0d;

        for(int c=0; c< clist.size(); c++){

            for(int i=0;i<clist.size();i++){
                for(int j=0;j<(vlist.size()-1);j++){
                    if(completionTime[i][j+1] > completionTime[i][j] && completionTime[i][j+1] > 0.0){
                        minCloudlet=i;
                    }
                }
            }


            for(int j=0; j<vlist.size(); j++){
                time = getExecTime(clist.get(minCloudlet), vlist.get(j));
                if(j==0){
                    min=time;
                }
                if(time < min && time > -1.0){
                    minVm=j;
                    min=time;
                }

            }

            myBroker.bindCloudletToVm(cloudletList.get(minCloudlet), vmList.get(minVm));
            System.out.println(cloudletList.get(minCloudlet)+" bound to "+vmList.get(minVm));
            clist.remove(minCloudlet);

            for(int i=0; i<vlist.size(); i++){
                completionTime[minCloudlet][i]=-1.0;
            }

        }


}

    private double getCompletionTime(Cloudlet cloudlet, Vm vm){
        double waitingTime = cloudlet.getWaitingTime();
        double execTime = cloudlet.getLength() / (vm.getMips()*vm.getNumberOfPes());

        double completionTime = execTime + waitingTime;

        return completionTime;
    }

    private double getExecTime(Cloudlet cloudlet, Vm vm){
        return cloudlet.getLength() / (vm.getMips()*vm.getNumberOfPes());
    }



}

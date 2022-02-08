package org.cloudsimplus.examples.SchedulingPolicies;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

public class MinMinPolicy {

    MyBroker myBroker;
    List<Vm> vmList;
    List<Cloudlet> cloudletList;

    MinMinPolicy (MyBroker myBroker, List<Vm> vmList, List<Cloudlet> cloudletList){

        this.myBroker = myBroker;
        this.vmList = vmList;
        this.cloudletList = cloudletList;

    }

    public void schedule() {

        System.out.println("Scheduling with MIN_MIN Policy");

        int noOfVms = vmList.size();
        int noOfCloudlets = cloudletList.size();

        // Completion time matrix and execution time matrix for cloudlets-VM
        double[][] completionTime = new double[noOfCloudlets][noOfVms];
        //double executionTime[][] = new double[noOfCloudlets][noOfVms];

        // Init some variables
        double time =0.0;
        int minCloudlet = 0;
        int minVm = 0;

        // Computing the completion time matrix for cloudlet-VM
        for(int i=0;i<noOfCloudlets;i++){
            for(int j=0;j<noOfVms;j++){
                time=getCompletionTime(cloudletList.get(i),vmList.get(j));
                //time = Math.round(time*100.0)/100.0;
                completionTime[i][j] = time;
                //System.out.println("Completion Time Cloudlet"+i+"-VM"+j+" : "+completionTime[i][j]);
            }
        }

        //System.out.println(Arrays.deepToString(completionTime));

        for(int c=0; c< cloudletList.size(); c++) {

            //System.out.println("*********************************");


            // Getting the minimum completion time from the completion time matrix
            double MinimumCompletionTime = getMinValue(completionTime);
            //System.out.println("Minimum Completion Time: " + getMinValue(completionTime));

            // Getting the respective indices (cloudlet,VM) of the minimum completion time value
            int[] Indices = getIndices(completionTime, MinimumCompletionTime);


            // Getting the cloudlet-VM  with minimum completion time from the completion time matrix
            minCloudlet = Indices[0];
            minVm = Indices[1];
            //System.out.println(minCloudlet+" "+minVm);

            Cloudlet minimumCloudlet = cloudletList.get(minCloudlet);
            Vm minimumVm = vmList.get(minVm);
            //System.out.println("Minimum Cloudlet: " + minimumCloudlet);
            //System.out.println("Minimum VM: " + minimumVm);


            // Binding the cloudlet to the respective VM
            myBroker.bindCloudletToVm(minimumCloudlet, minimumVm);
            //System.out.println(minimumCloudlet+" gets mapped to "+minimumVm+" with completion time, "+MinimumCompletionTime);
            //System.out.println(minimumCloudlet+" : "+minimumVm);


            // Updating the completion time values for the selected VM and other remaining cloudlets
            for (int i = 0; i < cloudletList.size(); i++) {
                if(completionTime[i][minVm] != -1 ){
                    completionTime[i][minVm] = completionTime[i][minVm] + MinimumCompletionTime;
                    //completionTime[i][minVm] = Math.round(completionTime[i][minVm] * 100.0) / 100.0;
                }
            }

            // Replacing the completion times of the selected cloudlet across all the VMs with -1
            for (int i = 0; i < vmList.size(); i++) {
                completionTime[minCloudlet][i] = -1.0;
            }


            /*
            // Computing the completion time matrix for cloudlet-VM
            for(int i=0;i<noOfCloudlets;i++){
                for(int j=0;j<noOfVms;j++){
                    completionTime[i][j] = null;
                }
            }
            */

            //System.gc();

        }





    }

    private double getCompletionTime(Cloudlet cloudlet, Vm vm){
        double waitingTime = cloudlet.getWaitingTime();
        double execTime = cloudlet.getLength() / (vm.getMips() * vm.getNumberOfPes());
        //double completionTime = execTime + waitingTime;
        double completionTime = execTime;
        return completionTime;
    }

    private double getExecutionTime(Cloudlet cloudlet, Vm vm){
        return cloudlet.getLength() / (vm.getMips() * vm.getNumberOfPes());
    }

    private double getMinValue(double[][] numbers) {
        double minValue = 0;

        for (int j = 0; j < numbers.length; j++) {
            for (int i = 0; i < numbers[j].length; i++) {
                if(numbers[j][i]==-1){
                    continue;
                }
                else{
                    minValue = numbers[j][i];
                    break;
                }

            }

        }


        for (int j = 0; j < numbers.length; j++) {
            for (int i = 0; i < numbers[j].length; i++) {
                if (numbers[j][i] < minValue && numbers[j][i] > 0.0) {
                    minValue = numbers[j][i];
                }
            }
        }
        return minValue ;
    }

    private int[] getIndices(double [][] numbers,double value) {
        int[] Indices = new int[2];
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[i].length; j++) {
                if (numbers[i][j] == value && numbers[i][j] > 0.0) {
                    Indices[0]=i;
                    Indices[1]=j;
                }
            }
        }
        return Indices;
    }


}
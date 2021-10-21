package org.cloudsimplus.examples.HybridModel;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;

public class MinMinPolicy6 {

    MyBroker myBroker;
    List<Vm> vmList;

    MinMinPolicy6 (MyBroker myBroker, List<Vm> vmList){

        this.myBroker = myBroker;
        this.vmList = vmList;

    }

    public void schedule(){

        System.out.println("Scheduling with MIN_MIN Policy");

        System.out.println("Cloudlets waiting: "+myBroker.getCloudletWaitingList().size());

        myBroker.getCloudletSubmittedList().removeAll(myBroker.getCloudletFinishedList());

        System.out.println("Cloudlets remaining: "+myBroker.getCloudletSubmittedList().size());

        List<Cloudlet> cloudletList = myBroker.getCloudletSubmittedList();

        int numTasks = cloudletList.size();
        int numVMs = vmList.size();

        double[] readyTimes = new double[vmList.size()];
        double[][] executionTimes = new double[numTasks][numVMs];
        double[][] completionTimes = new double[numTasks][numVMs];

        for (int i = 0; i < numTasks; i++) {
            Cloudlet task = cloudletList.get(i);
            for (int j = 0; j < numVMs; j++) {
                Vm vm = vmList.get(j);
                executionTimes[i][j] = this.GetExecutionTime(task, vm);
                completionTimes[i][j] = executionTimes[i][j] + readyTimes[j];
            }
        }

        int allTasksMapped = 0;
        int maxCTTask = 0;
        int minCTTaskVm = 0;

        for (int i = 0; i < numTasks; i++) {
            Cloudlet task = cloudletList.get(i);
            // if task is not assigned
            if (! task.isBoundToVm()) {
                for (int j = 0; j < numVMs; j++) {
                    // choosing the task with MINIMUM completion time and finding minimum vm
                    // completion time
                    if (completionTimes[i][j] > completionTimes[maxCTTask][minCTTaskVm]) {
                        maxCTTask = i;
                        minCTTaskVm = j;
                    }
                }
            }

            if (i + 1 >= numTasks) {
                // find minimum vm completion time
                // for (int j = 0; j < executionTimes[maxCTTask].length; j++)
                for (int j = 0; j < numVMs; j++) {
                    // if (executionTimes[maxCTTask][j] + readyTimes[j] <
                    // executionTimes[maxCTTask][minCTTaskVm] + vmsReadyTime[minCTTaskVm])
                    if (completionTimes[maxCTTask][j] < completionTimes[maxCTTask][minCTTaskVm]) {
                        minCTTaskVm = j;
                    }
                }

                // assign task with minimum completion time
                myBroker.bindCloudletToVm(cloudletList.get(maxCTTask),vmList.get(minCTTaskVm));

                // update vm completion time
                readyTimes[minCTTaskVm] += executionTimes[maxCTTask][minCTTaskVm];

                // remove task
                executionTimes[maxCTTask] = null;
                completionTimes[maxCTTask] = null;

                // update completion time of unmapped tasks
                maxCTTask = -1;
                for (i = 0; i < numTasks; i++) {
                    if (completionTimes[i] != null) {
                        completionTimes[i][minCTTaskVm] =
                            executionTimes[i][minCTTaskVm] + readyTimes[minCTTaskVm];

                        // to avoid null pointers
                        if (maxCTTask == -1)
                            maxCTTask = i;
                    }
                }

                // repeat loop
                i = -1;
                allTasksMapped++;
            }

            if (allTasksMapped >= numTasks)
                break;
        }


    }

    public double[][] GetExecutionTimeMatrix(List<? extends Cloudlet> tasks, List<? extends Vm> vms) {
        // better list than a 2D array cause each task will be removed later
        int numTasks = tasks.size();
        int numVMs = vms.size();
        double[][] result = new double[numTasks][numVMs];

        // looping all tasks
        for (int i = 0; i < numTasks; i++) {
            Cloudlet task = tasks.get(i);

            // looping all vms for this task
            for (int j = 0; j < numVMs; j++) {
                Vm vm = vms.get(j);

                result[i][j] = this.GetExecutionTime(task, vm);
            }
        }

        return result;
    }

    public double GetExecutionTime(Cloudlet task, Vm vm) {
        if (vm.getHost() == null)
            return task.getLength() / vm.getMips();

        return task.getLength() / vm.getHost().getTotalAllocatedMipsForVm(vm);
    }

    public double GetCompletionTime(double taskET, double vmRT) {
        // task execution time + vm ready time
        return taskET + vmRT;
    }


}

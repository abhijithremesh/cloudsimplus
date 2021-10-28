package org.cloudsimplus.examples.checkHybrid;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.vms.Vm;


import java.util.List;

public class SampleBroker extends DatacenterBrokerSimple {

    public SampleBroker(final CloudSim simulation) {
        super(simulation);
    }

    public void BindtoVMOne(List<Vm> vmList){

        BindtoVMOnePolicy bv1 = new BindtoVMOnePolicy(this, vmList);
        bv1.schedule();

    }

    public void BindtoVMTwo(List<Vm> vmList){

        BindtoVMTwoPolicy bv2 = new BindtoVMTwoPolicy(this, vmList);
        bv2.schedule();

        /*
        for (Cloudlet c: getCloudletSubmittedList()
             ) {
            getCloudletWaitingList().add(c);
        }
        requestDatacentersToCreateWaitingCloudlets();
         */







    }

    public void BindtoVMThree(List<Vm> vmList){

        BindtoVMThreePolicy bv3 = new BindtoVMThreePolicy(this, vmList);
        bv3.schedule();

    }

    public void BindtoVMFour(List<Vm> vmList){

        BindtoVMFourPolicy bv4 = new BindtoVMFourPolicy(this, vmList);
        bv4.schedule();

    }





    public void selectSchedulingPolicy(int schedulingHeuristic, List<Vm> vmList){
        switch(schedulingHeuristic){
            case 1:
                System.out.println("BindtoVMOne");
                this.BindtoVMOne(vmList);
                break;
            case 2:
                System.out.println("BindtoVMTwo");
                this.BindtoVMTwo(vmList);
                break;
            case 3:
                System.out.println("BindtoVMThree");
                this.BindtoVMThree(vmList);
                break;
            case 4:
                System.out.println("BindtoVMFour");
                this.BindtoVMFour(vmList);
                break;

        }
    }






}

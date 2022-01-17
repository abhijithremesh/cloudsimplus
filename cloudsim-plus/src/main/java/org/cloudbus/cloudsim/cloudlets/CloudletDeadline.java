package org.cloudbus.cloudsim.cloudlets;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

import java.util.Random;

public class CloudletDeadline extends CloudletSimple {

    private int deadline;

    public CloudletDeadline(final long id, final long length, final long pesNumber, int deadline) {
        super(id, length, pesNumber);
        this.deadline = (int)length + (int) this.getSubmissionDelay() + new Random().nextInt(200-400+1) + 200;
    }

    public double getDeadline() {
        return this.deadline;
    }


    public final void setDeadline(final int deadline) {
        this.deadline = deadline;
    }




}

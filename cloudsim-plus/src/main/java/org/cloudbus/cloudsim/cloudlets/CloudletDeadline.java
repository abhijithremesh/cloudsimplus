package org.cloudbus.cloudsim.cloudlets;

import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

public class CloudletDeadline extends CloudletSimple {

    private int deadline;

    public CloudletDeadline(final long id, final long length, final long pesNumber, int deadline) {
        super(id, length, pesNumber);
        this.deadline = deadline;
    }

    public double getDeadline() {
        return this.deadline;
    }


    public final void setDeadline(final int deadline) {
        this.deadline = deadline;
    }




}

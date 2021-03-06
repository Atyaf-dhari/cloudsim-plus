/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.schedulers.cloudlet;

import java.util.*;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletExecutionInfo;

import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.Processor;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a
 * virtual machine to run its {@link Cloudlet Cloudlets}. It considers there will
 * be only one Cloudlet per VM. Other Cloudlets will be in a waiting list. It also
 * considers that the time to transfer Cloudlets to the Vm happens before Cloudlet
 * starts executing. I.e., even though Cloudlets must wait for CPU, data transfer
 * happens as soon as Cloudlets are submitted.
 *
 * <p><b>This scheduler does not consider Cloudlets priorities to
 * define execution order. If actual priorities are defined for Cloudlets, they
 * are just ignored by the scheduler.</b></p>
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Toolkit 1.0
 */
public class CloudletSchedulerSpaceShared extends CloudletSchedulerAbstract {

    /**
     * Creates a new CloudletSchedulerSpaceShared object. This method must be
     * invoked before starting the actual simulation.
     *
     * @pre $none
     * @post $none
     */
    public CloudletSchedulerSpaceShared() {
        super();
    }

    @Override
    public double cloudletResume(int cloudletId) {
	    Optional<CloudletExecutionInfo> optional = findCloudletInList(cloudletId, getCloudletPausedList());
        if (!optional.isPresent()) {
            // not found in the paused list: either it is in in the queue, executing or not exist
            return 0.0;
        }

        CloudletExecutionInfo c = optional.get();
        getCloudletPausedList().remove(c);

        // it can go to the exec list
        if (isThereEnoughFreePesForCloudlet(c)) {
	        return movePausedCloudletToExecList(c);
        }

        // No enough free PEs: go to the waiting queue
        /*
         * A resumed cloudlet is not immediately added to the execution list.
         * It is queued so that the next time the scheduler process VM execution,
         * the cloudlet may have the opportunity to run.
         * It goes to the end of the waiting list because other cloudlets
         * could be waiting longer and have priority to execute.
        */
	    addCloudletToWaitingList(c);
        return 0.0;
    }

	/**
	 * Moves a paused cloudlet to the execution list.
	 *
	 * @param c the cloudlet to be moved
	 * @return the time the cloudlet is expected to finish
	 */
	private double movePausedCloudletToExecList(CloudletExecutionInfo c) {
		addCloudletToExecList(c);

		// calculate the expected time for cloudlet completion
        return getVm().getSimulation().clock() +
                (c.getRemainingCloudletLength() /
                (getProcessor().getCapacity() * c.getNumberOfPes()));
	}

    @Override
    public List<Double> getCurrentRequestedMips() {
        return Collections.unmodifiableList(getCurrentMipsShare());
    }

    /**
     * {@inheritDoc}
     * <p>It doesn't consider the given Cloudlet because the scheduler
     * ensures that the Cloudlet will use all required PEs until it
     * finishes executing. </p>
     *
     * @param rcl {@inheritDoc}
     * @param mipsShare {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(CloudletExecutionInfo rcl, List<Double> mipsShare) {
        return Processor.fromMipsList(mipsShare).getCapacity();
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(CloudletExecutionInfo rcl, double time) {
        //@todo the method isn't in fact implemented
        return 0.0;
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(CloudletExecutionInfo rcl, double time) {
        //@todo the method isn't in fact implemented
        return 0.0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        //@todo the method isn't in fact implemented
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        //@todo the method isn't in fact implemented
        return 0;
    }

    /**
	 * The space-shared scheduler <b>does not</b> share the CPU time between executing cloudlets.
	 * Each CPU ({@link Pe}) is used by another Cloudlet just when the previous Cloudlet
	 * using it has finished executing completely.
	 * By this way, if there are more Cloudlets than PEs, some Cloudlet
	 * will not be allowed to start executing immediately.
	 *
     * @param cloudlet {@inheritDoc}
	 * @return {@inheritDoc}
	 */
    @Override
    public boolean canAddCloudletToExecutionList(CloudletExecutionInfo cloudlet) {
        return isThereEnoughFreePesForCloudlet(cloudlet);
    }
}

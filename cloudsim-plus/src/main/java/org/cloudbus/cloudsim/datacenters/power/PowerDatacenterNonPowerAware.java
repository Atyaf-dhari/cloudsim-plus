/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.datacenters.power;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.hosts.power.PowerHostSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.resources.FileStorage;

/**
 * PowerDatacenterNonPowerAware is a class that represents a <b>non-power</b>
 * aware data center in the context of power-aware simulations.
 *
 * <br/>If you are using any algorithms, policies or workload included in the
 * power package please cite the following paper:<br/>
 *
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and
 * Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of
 * Virtual Machines in Cloud Data Centers", Concurrency and Computation:
 * Practice and Experience (CCPE), Volume 24, Issue 13, Pages: 1397-1420, John
 * Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenterNonPowerAware extends PowerDatacenter {
    /**
     * Creates a Datacenter.
     *
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     * @param characteristics the Datacenter characteristics
     * @param vmAllocationPolicy the vm provisioner
     *
     */
    public PowerDatacenterNonPowerAware(
        CloudSim simulation,
        DatacenterCharacteristics characteristics,
        VmAllocationPolicy vmAllocationPolicy)
    {
        super(simulation, characteristics, vmAllocationPolicy);
    }

    /**
     * Creates a Datacenter with the given parameters.
     *
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     * @param characteristics the Datacenter characteristics
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList the storage list
     * @param schedulingInterval the scheduling interval
     *
     * @deprecated Use the other available constructors with less parameters
     * and set the remaining ones using the respective setters.
     * This constructor will be removed in future versions.
     */
    @Deprecated
    public PowerDatacenterNonPowerAware(
            CloudSim simulation,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<FileStorage> storageList,
            double schedulingInterval)
    {
        this(simulation, characteristics, vmAllocationPolicy);
        setStorageList(storageList);
        setSchedulingInterval(schedulingInterval);
    }

    @Override
    protected void updateCloudletProcessing() {
        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == getSimulation().clock()) {
            getSimulation().cancelAll(getId(), new PredicateType(CloudSimTags.VM_UPDATE_CLOUDLET_PROCESSING_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_UPDATE_CLOUDLET_PROCESSING_EVENT);
            return;
        }
        double currentTime = getSimulation().clock();
        double timeframePower = 0.0;

        if (currentTime > getLastProcessTime()) {
            double timeDiff = currentTime - getLastProcessTime();
            double minTime = Double.MAX_VALUE;

            Log.printLine("\n");

            for (PowerHostSimple host : this.<PowerHostSimple>getHostList()) {
                Log.printFormattedLine("%.2f: Host #%d", getSimulation().clock(), host.getId());

                double hostPower = 0.0;

                try {
                    hostPower = host.getMaxPower() * timeDiff;
                    timeframePower += hostPower;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.printFormattedLine(
                        "%.2f: Host #%d utilization is %.2f%%",
                        getSimulation().clock(),
                        host.getId(),
                        host.getUtilizationOfCpu() * 100);
                Log.printFormattedLine(
                        "%.2f: Host #%d energy is %.2f W*sec",
                        getSimulation().clock(),
                        host.getId(),
                        hostPower);
            }

            Log.printFormattedLine("\n%.2f: Consumed energy is %.2f W*sec\n", getSimulation().clock(), timeframePower);

            Log.printLine("\n\n--------------------------------------------------------------\n\n");

            for (PowerHostSimple host : this.<PowerHostSimple>getHostList()) {
                Log.printFormattedLine("\n%.2f: Host #%d", getSimulation().clock(), host.getId());

                double time = host.updateVmsProcessing(currentTime); // inform VMs to update
                // processing
                if (time < minTime) {
                    minTime = time;
                }
            }

            setPower(getPower() + timeframePower);

            checkCloudletsCompletionForAllHosts();

            removeFinishedVmsFromEveryHost();
            Log.printLine();

            if (isMigrationsEnabled()) {
                Map<Vm, Host> migrationMap
                        = getVmAllocationPolicy().optimizeAllocation(getVmList());

                for (Entry<Vm, Host> entry : migrationMap.entrySet()) {
                    Host targetHost = entry.getValue();
                    Host oldHost = entry.getKey().getHost();

                    if (oldHost == Host.NULL) {
                        Log.printFormattedLine(
                            "%.2f: Migration of VM #%d to Host #%d is started",
                            getSimulation().clock(),
                            entry.getKey().getId(),
                            targetHost.getId());
                    } else {
                        Log.printFormattedLine(
                            "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                            getSimulation().clock(),
                            entry.getKey().getId(),
                            oldHost.getId(),
                            targetHost.getId());
                    }

                    targetHost.addMigratingInVm(entry.getKey());
                    incrementMigrationCount();

                    /* VM migration delay = RAM / bandwidth + C (C = 10 sec) */
                    send(
                        getId(),
                        entry.getKey().getRam() / ((double) entry.getKey().getBw() / 8000) + 10,
                        CloudSimTags.VM_MIGRATE, entry);
                }
            }

            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                getSimulation().cancelAll(getId(), new PredicateType(CloudSimTags.VM_UPDATE_CLOUDLET_PROCESSING_EVENT));
                // getSimulation().cancelAll(getId(), CloudSim.SIM_ANY);
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_UPDATE_CLOUDLET_PROCESSING_EVENT);
            }

            setLastProcessTime(currentTime);
        }
    }

}

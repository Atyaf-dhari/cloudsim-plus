package org.cloudbus.cloudsim.vms;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudsimplus.autoscaling.HorizontalVmScaling;
import org.cloudbus.cloudsim.core.Delayable;
import org.cloudbus.cloudsim.core.UniquelyIdentificable;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.cloudbus.cloudsim.core.Simulation;
import org.cloudsimplus.autoscaling.VmScaling;
import org.cloudsimplus.listeners.VmHostEventInfo;
import org.cloudsimplus.listeners.VmDatacenterEventInfo;
import org.cloudsimplus.listeners.EventListener;

/**
 * An interface to be implemented by each class that provides basic
 * features of Virtual Machines (VMs).
 * The interface implements the Null Object
 * Design Pattern in order to start avoiding {@link NullPointerException} when
 * using the {@link Vm#NULL} object instead of attributing {@code null} to
 * {@link Vm} variables.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */
public interface Vm extends UniquelyIdentificable, Delayable, Comparable<Vm> {

    /**
     * Adds a VM state history entry.
     *
     * @param entry the data about the state of the VM at given time
     */
    void addStateHistoryEntry(VmStateHistoryEntry entry);

    /**
     * Gets bandwidth capacity.
     *
     * @return bandwidth capacity.
     * @pre $none
     * @post $none
     */
    long getBw();

    /**
     * Gets the the Cloudlet scheduler the VM uses to schedule cloudlets
     * execution.
     *
     * @return the cloudlet scheduler
     */
    CloudletScheduler getCloudletScheduler();

    /**
     * Gets the current allocated bw.
     *
     * @return the current allocated bw
     */
    long getCurrentAllocatedBw();

    /**
     * Gets the current allocated ram.
     *
     * @return the current allocated ram
     */
    long getCurrentAllocatedRam();

    /**
     * Gets the current allocated storage size.
     *
     * @return the current allocated size
     * @see #getSize()
     */
    long getCurrentAllocatedSize();

    /**
     * Gets the current requested bw.
     *
     * @return the current requested bw
     */
    long getCurrentRequestedBw();

    /**
     * Gets the current requested max mips among all virtual PEs.
     *
     * @return the current requested max mips
     */
    double getCurrentRequestedMaxMips();

    /**
     * Gets the current requested mips.
     *
     * @return the current requested mips
     */
    List<Double> getCurrentRequestedMips();

    /**
     * Gets the current requested ram.
     *
     * @return the current requested ram
     */
    long getCurrentRequestedRam();

    /**
     * Gets the current requested total mips. It is the sum of MIPS capacity
     * requested for every VM's Pe.
     *
     * @return the current requested total mips
     * @see #getCurrentRequestedMips()
     */
    double getCurrentRequestedTotalMips();

    /**
     * Gets the Host where the Vm is or will be placed.
     * To know if the Vm was already created inside this Host,
     * call the {@link #isCreated()} method.
     *
     * @return the host
     * @see #isCreated()
     */
    Host getHost();

    /**
     * Gets the individual MIPS capacity of any VM's PE, considering that all
     * PEs have the same capacity.
     *
     * @return the mips
     */
    double getMips();

    /**
     * Gets the number of PEs required by the VM. Each PE has the capacity
     * defined in {@link #getMips()}
     *
     * @return the number of PEs
     * @see #getMips()
     */
    int getNumberOfPes();

    /**
     * Gets the total MIPS capacity (across all PEs) of this VM.
     *
     * @return MIPS capacity sum of all PEs
     *
     * @see #getMips()
     * @see #getNumberOfPes()
     */
    double getTotalMipsCapacity();

    /**
     * Gets a given Vm {@link Resource}, such as {@link Ram} or {@link Bandwidth},
     * from the class of the resource to get.
     *
     * @param resourceClass the class of the resource to get
     * @param <R> generic type that defines the class of resources that can be got
     * @return the Vm {@link Resource} corresponding to the given class
     */
    <R extends ResourceManageable> ResourceManageable getResource(Class<R> resourceClass);

    /**
     * Adds a listener object that will be notified when a {@link Host}
     * is allocated to the Vm, that is, when the Vm is placed into a
     * given Host.
     *
     * @param listener the listener to add
     * @return
     */
    Vm addOnHostAllocationListener(EventListener<VmHostEventInfo> listener);

    /**
     * Adds a listener object that will be notified when the Vm is moved/removed from a {@link Host}.
     *
     * @param listener the listener to add
     * @return
     */
    Vm addOnHostDeallocationListener(EventListener<VmHostEventInfo> listener);

    /**
     * Adds a listener object that will be notified when the Vm fail in
     * being placed for lack of a {@link Host} with enough resources in a specific {@link Datacenter}.
     *
     * @param listener the listener to add
     * @return
     * @see #updateVmProcessing(double, java.util.List)
     */
    Vm addOnVmCreationFailureListener(EventListener<VmDatacenterEventInfo> listener);

    /**
     * Adds a listener object that will be notified every time when
     * the processing of the Vm is updated in its {@link Host}.
     *
     * @param listener the listener to seaddt
     * @return
     * @see #updateVmProcessing(double, java.util.List)
     */
    Vm addOnUpdateVmProcessingListener(EventListener<VmHostEventInfo> listener);

    /**
     * Notifies all registered listeners when a {@link Host} is allocated to the {@link Vm}.
     *
     * <p><b>This method is used just internally and must not be called directly.</b></p>
     */
    void notifyOnHostAllocationListeners();

    /**
     * Notifies all registered listeners when the {@link Vm} is moved/removed from a {@link Host}.
     *
     * <p><b>This method is used just internally and must not be called directly.</b></p>
     * @param deallocatedHost the {@link Host} the {@link Vm} was moved/removed from
     */
    void notifyOnHostDeallocationListeners(Host deallocatedHost);

    /**
     * Notifies all registered listeners when the Vm fail in
     * being placed for lack of a {@link Host} with enough resources in a specific {@link Datacenter}.
     *
     * <p><b>This method is used just internally and must not be called directly.</b></p>
     * @param failedDatacenter the Datacenter where the VM creation failed
     */
    void notifyOnVmCreationFailureListeners(Datacenter failedDatacenter);

    /**
     * Removes a listener from the onUpdateVmProcessingListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnUpdateVmProcessingListener(EventListener<VmHostEventInfo> listener);

    /**
     * Removes a listener from the onHostAllocationListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnHostAllocationListener(EventListener<VmHostEventInfo> listener);

    /**
     * Removes a listener from the onHostDeallocationListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnHostDeallocationListener(EventListener<VmHostEventInfo> listener);

    /**
     * Removes a listener from the onVmCreationFailureListener List.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    boolean removeOnVmCreationFailureListener(EventListener<VmDatacenterEventInfo> listener);

    /**
     * Gets the RAM capacity in Megabytes.
     *
     * @return the RAM capacity
     * @pre $none
     * @post $none
     */
    long getRam();

    /**
     * Gets the storage size (capacity) of the VM image in Megabytes (the amount of storage
     * it will use, at least initially).
     *
     * @return amount of storage
     * @pre $none
     * @post $none
     */
    long getSize();

    /**
     * Gets the history of MIPS capacity allocated to the VM.
     *
     * @return the state history
     */
    List<VmStateHistoryEntry> getStateHistory();

    /**
     * Gets total CPU utilization percentage of all Clouddlets running on this
     * VM at the given time.
     *
     * @param time the time
     * @return total utilization percentage
     */
    double getTotalUtilizationOfCpu(double time);

    /**
     * Gets total CPU utilization percentage of all Clouddlets running on this
     * VM at the current simulation time.
     *
     * @return total utilization percentage fort the current time
     */
    double getTotalUtilizationOfCpu();

    /**
     * Gets the total CPU utilization of all cloudlets running on this VM at the
     * given time (in MIPS).
     *
     * @param time the time
     * @return total cpu utilization in MIPS
     * @see #getTotalUtilizationOfCpu(double)
     *
     */
    double getTotalUtilizationOfCpuMips(double time);

    /**
     * Gets the {@link DatacenterBroker} that represents the owner of the VM.
     *
     * @return the broker or <tt>{@link DatacenterBroker#NULL}</tt> if a broker has not been set yet
     * @pre $none
     * @post $none
     */
    DatacenterBroker getBroker();

    /**
     * Sets a {@link DatacenterBroker} that represents the owner of the VM.
     *
     * @param broker the {@link DatacenterBroker} to set
     */
    Vm setBroker(DatacenterBroker broker);


    /**
     * Gets the Virtual Machine Monitor (VMM) that manages the VM.
     *
     * @return VMM
     * @pre $none
     * @post $none
     */
    String getVmm();

    /**
     * Checks if the VM was created and placed inside the {@link #getHost() Host}.
     * If so, resources required by the Vm already were provisioned.
     *
     * @return true, if it was created inside the Host, false otherwise
     */
    boolean isCreated();

    /**
     * Changes the created status of the Vm inside the Host.
     *
     * @param created true to indicate the VM was created inside the Host; false otherwise
     * @see #isCreated()
     */
    void setCreated(boolean created);


    /**
     * Checks if the VM is in migration process or not.
     *
     * @return
     */
    boolean isInMigration();

    /**
     * Defines if the VM is in migration process or not.
     *
     * @param inMigration true to indicate the VM is migrating into a Host, false otherwise
     */
    void setInMigration(boolean inMigration);

    /**
     * Sets the BW capacity
     *
     * @param bwCapacity new BW capacity
     * @return
     * @pre bwCapacity > 0
     * @post $none
     */
    Vm setBw(long bwCapacity);

    /**
     * Sets the PM that hosts the VM.
     *
     * @param host Host to run the VM
     * @pre host != $null
     * @post $none
     */
    void setHost(Host host);

    /**
     * Sets RAM capacity in Megabytes.
     *
     * @param ramCapacity new RAM capacity
     * @return
     * @pre ramCapacity > 0
     * @post $none
     */
    Vm setRam(long ramCapacity);

    /**
     * Sets the storage size (capacity) of the VM image in Megabytes.
     *
     * @param size new storage size
     * @return
     * @pre size > 0
     * @post $none
     *
     */
    Vm setSize(long size);

    /**
     * Updates the processing of cloudlets running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare list with MIPS share of each Pe available to the
     * scheduler
     * @return the predicted completion time of the earliest finishing cloudlet
     * (that is a future simulation time),
     * or {@link Double#MAX_VALUE} if there is no next Cloudlet to execute
     * @pre currentTime >= 0
     * @post $none
     */
    double updateVmProcessing(double currentTime, List<Double> mipsShare);

    /**
     * Sets the Cloudlet scheduler the VM uses to schedule cloudlets execution.
     *
     * @param cloudletScheduler the cloudlet scheduler to set
     * @return
     */
    Vm setCloudletScheduler(CloudletScheduler cloudletScheduler);

    /**
     * Sets the status of VM to FAILED.
     *
     * @param failed the failed
     */
    void setFailed(boolean failed);

    /**
     * Checks if the Vm is failed or not.
     * @return
     */
    boolean isFailed();

    /**
     * Gets the CloudSim instance that represents the simulation the Entity is related to.
     * @return
     * @see #setSimulation(Simulation)
     */
    Simulation getSimulation();

    /**
     * Gets the {@link HorizontalVmScaling} that will check if the Vm is overloaded,
     * based on some conditions defined by a {@link Predicate} given
     * to the HorizontalVmScaling.
     *
     * <p><b>If no HorizontalVmScaling is set, the {@link #getBroker() Broker} will not dynamically
     * create VMs to balance arrived Cloudlets.</b></p>
     *
     * @return
     */
    VmScaling getHorizontalScaling();

    /**
     * Sets the {@link HorizontalVmScaling} that will check if the Vm is overloaded,
     * based on some conditions defined by a {@link Predicate} given
     * to the HorizontalVmScaling.
     *
     * <p><b>If no HorizontalVmScaling is set, the {@link #getBroker() Broker} will not dynamically
     * create VMs to balance arrived Cloudlets.</b></p>
     *
     * @param horizontalScaling the HorizontalVmScaling to set
     * @return
     * @throws IllegalArgumentException if the given Vm Scaling already is linked to a Vm. Each VM must have
     * its own scaling object.
     */
    Vm setHorizontalScaling(VmScaling horizontalScaling) throws IllegalArgumentException;

    /**
     * An attribute that implements the Null Object Design Pattern for {@link Vm}
     * objects.
     */
    Vm NULL = new Vm() {
        @Override public int getId() { return -1; }
        @Override public double getSubmissionDelay() { return 0; }
        @Override public void setSubmissionDelay(double submissionDelay) {}
        @Override public void addStateHistoryEntry(VmStateHistoryEntry entry) {}
        @Override public long getBw(){ return 0; }
        @Override public CloudletScheduler getCloudletScheduler() { return CloudletScheduler.NULL; }
        @Override public long getCurrentAllocatedBw() { return 0; }
        @Override public long getCurrentAllocatedRam(){ return 0; }
        @Override public long getCurrentAllocatedSize() { return 0; }
        @Override public long getCurrentRequestedBw() { return 0; }
        @Override public double getCurrentRequestedMaxMips() { return 0.0; }
        @Override public List<Double> getCurrentRequestedMips() { return Collections.emptyList(); }
        @Override public long getCurrentRequestedRam() { return 0; }
        @Override public double getCurrentRequestedTotalMips() { return 0.0; }
        @Override public Host getHost() { return Host.NULL; }
        @Override public double getMips() { return 0.0; }
        @Override public int getNumberOfPes() { return 0; }
        @Override public Vm addOnHostAllocationListener(EventListener<VmHostEventInfo> listener) { return Vm.NULL; }
        @Override public Vm addOnHostDeallocationListener(EventListener<VmHostEventInfo> listener) { return Vm.NULL; }
        @Override public Vm addOnVmCreationFailureListener(EventListener<VmDatacenterEventInfo> listener) { return Vm.NULL; }
        @Override public Vm addOnUpdateVmProcessingListener(EventListener<VmHostEventInfo> listener) { return Vm.NULL; }
        @Override public void notifyOnHostAllocationListeners() {}
        @Override public void notifyOnHostDeallocationListeners(Host deallocatedHost) {}
        @Override public void notifyOnVmCreationFailureListeners(Datacenter failedDatacenter) {}
        @Override public boolean removeOnUpdateVmProcessingListener(EventListener<VmHostEventInfo> listener) { return false; }
        @Override public boolean removeOnHostAllocationListener(EventListener<VmHostEventInfo> listener) { return false; }
        @Override public boolean removeOnHostDeallocationListener(EventListener<VmHostEventInfo> listener) { return false; }
        @Override public boolean removeOnVmCreationFailureListener(EventListener<VmDatacenterEventInfo> listener) { return false; }
        @Override public long getRam() { return 0; }
        @Override public long getSize(){ return 0; }
        @Override public List<VmStateHistoryEntry> getStateHistory() { return Collections.emptyList(); }
        @Override public double getTotalUtilizationOfCpu(double time) { return 0.0; }
        @Override public double getTotalUtilizationOfCpu() { return 0; }
        @Override public double getTotalUtilizationOfCpuMips(double time) { return 0.0; }
        @Override public String getUid(){ return ""; }
        @Override public DatacenterBroker getBroker() { return DatacenterBroker.NULL; }
        @Override public Vm setBroker(DatacenterBroker broker) { return Vm.NULL; }
        @Override public String getVmm() { return ""; }
        @Override public boolean isCreated() { return false; }
        @Override public boolean isInMigration() { return false; }
        @Override public void setCreated(boolean created){}
        @Override public Vm setBw(long bwCapacity) { return Vm.NULL; }
        @Override public void setHost(Host host) {}
        @Override public void setInMigration(boolean inMigration) {}
        @Override public Vm setRam(long ramCapacity) { return Vm.NULL; }
        @Override public Vm setSize(long size) { return Vm.NULL; }
        @Override public double updateVmProcessing(double currentTime, List<Double> mipsShare){ return 0.0; }
        @Override public Vm setCloudletScheduler(CloudletScheduler cloudletScheduler) { return Vm.NULL; }
        @Override public <R extends ResourceManageable> ResourceManageable getResource(Class<R> resourceClass) { return ResourceManageable.NULL; }
        @Override public int compareTo(Vm o) { return 0; }
        @Override public double getTotalMipsCapacity() { return 0.0; }
        @Override public void setFailed(boolean failed){}
        @Override public boolean isFailed() { return false; }
        @Override public Simulation getSimulation() { return Simulation.NULL; }
        @Override public String toString() { return "Vm.NULL"; }
        @Override public VmScaling getHorizontalScaling(){ return VmScaling.NULL; }
        @Override public Vm setHorizontalScaling(VmScaling horizontalScaling) throws IllegalArgumentException { return this; }
    };
}

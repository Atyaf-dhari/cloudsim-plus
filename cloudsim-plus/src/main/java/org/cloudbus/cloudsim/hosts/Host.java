 package org.cloudbus.cloudsim.hosts;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.core.Identificable;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import java.util.Collections;
import java.util.List;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostUpdatesVmsProcessingEventInfo;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;

/**
 * An interface to be implemented by each class that provides
 * Physical Machines (Hosts) features.
 * The interface implements the Null Object Design
 * Pattern in order to start avoiding {@link NullPointerException}
 * when using the {@link Host#NULL} object instead
 * of attributing {@code null} to {@link Host} variables.
 *
 * @author Manoel Campos da Silva Filho
 */
public interface Host extends Identificable, Comparable<Host> {

    /**
     * Adds a VM migrating into the current host.
     *
     * @param vm the vm
     */
    void addMigratingInVm(Vm vm);

    /**
     * Allocates PEs for a VM.
     *
     * @param vm the vm
     * @param mipsShare the list of MIPS share to be allocated to the VM
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    boolean allocatePesForVm(Vm vm, List<Double> mipsShare);

    /**
     * Releases PEs allocated to a VM.
     *
     * @param vm the vm
     * @pre $none
     * @post $none
     */
    void deallocatePesForVm(Vm vm);

    /**
     * Gets the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param vm the vm
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    List<Double> getAllocatedMipsForVm(Vm vm);

    /**
     * Gets the total free MIPS available at the host.
     *
     * @return the free mips
     */
    double getAvailableMips();

    /**
     * Gets the total free storage available at the host in Megabytes.
     *
     * @return the free storage
     */
    long getAvailableStorage();

    /**
     * Gets the host bw capacity in Megabits/s.
     *
     * @return the host bw capacity
     * @pre $none
     * @post $result > 0
     */
    long getBwCapacity();

    /**
     * Gets the bandwidth (BW) provisioner with capacity in Megabits/s.
     *
     * @return the bw provisioner
     */
    ResourceProvisioner getBwProvisioner();

    /**
     * Sets the bandwidth (BW) provisioner with capacity in Megabits/s.
     *
     * @param bwProvisioner the new bw provisioner
     */
    Host setBwProvisioner(ResourceProvisioner bwProvisioner);

    /**
     * Gets the Datacenter where the host is placed.
     *
     * @return the data center of the host
     */
    Datacenter getDatacenter();

    /**
     * Returns the maximum available MIPS among all the PEs of the host.
     *
     * @return max mips
     */
    double getMaxAvailableMips();

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    int getNumberOfFreePes();

    /**
     * Gets the number of PEs that are working.
     * That is, the number of PEs that aren't FAIL.
     *
     * @return the number of working pes
     */
    long getNumberOfWorkingPes();

    /**
     * Gets the PEs number.
     *
     * @return the pes number
     */
    int getNumberOfPes();

    /**
     * Gets the Processing Elements (PEs) of the host, that
     * represent its CPU cores and thus, its processing capacity.
     *
     * @return the pe list
     */
    List<Pe> getPeList();

    /**
     * Gets the host memory capacity in Megabytes.
     *
     * @return the host memory capacity
     * @pre $none
     * @post $result > 0
     */
    long getRamCapacity();

    /**
     * Gets the ram provisioner with capacity in Megabytes.
     *
     * @return the ram provisioner
     */
    ResourceProvisioner getRamProvisioner();

    /**
     * Sets the ram provisioner with capacity in Megabytes.
     *
     * @param ramProvisioner the new ram provisioner
     */
    Host setRamProvisioner(ResourceProvisioner ramProvisioner);

    /**
     * Gets the host storage capacity in Megabytes.
     *
     * @return the host storage capacity
     * @pre $none
     * @post $result >= 0
     */
    long getStorageCapacity();

    /**
     * Gets the total allocated MIPS for a VM along all its PEs.
     *
     * @param vm the vm
     * @return the allocated mips for vm
     */
    double getTotalAllocatedMipsForVm(Vm vm);

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    int getTotalMips();

    /**
     * Gets a VM by its id and user.
     *
     * @param vmId the vm id
     * @param brokerId ID of VM's owner
     * @return the virtual machine object, $null if not found
     * @pre $none
     * @post $none
     */
    Vm getVm(int vmId, int brokerId);

    /**
     * Gets the list of VMs assigned to the host.
     *
     * @param <T> The generic type
     * @return the vm list
     */
    <T extends Vm> List<T> getVmList();

    /**
     * Gets the policy for allocation of host PEs to VMs in order to schedule VM execution.
     *
     * @return the {@link VmScheduler}
     */
    VmScheduler getVmScheduler();

    /**
     * Sets the policy for allocation of host PEs to VMs in order to schedule VM
     * execution. The host also sets itself to the given scheduler.
     *
     * @param vmScheduler the vm scheduler to set
     */
    Host setVmScheduler(VmScheduler vmScheduler);


    /**
     * Gets the list of VMs migrating into this host.
     *
     * @param <T> the generic type
     * @return the vms migrating in
     */
    <T extends Vm> List<T> getVmsMigratingIn();

    /**
     * Checks if the host is working properly or has failed.
     *
     * @return true, if the host PEs have failed; false otherwise
     */
    boolean isFailed();

    /**
     * Checks if the host is suitable for vm. If it has enough resources
     * to attend the VM.
     *
     * @param vm the vm
     * @return true, if is suitable for vm
     */
    boolean isSuitableForVm(Vm vm);

    /**
     * Reallocate VMs migrating into the host. Gets the VM in the migrating in queue
     * and allocate them on the host.
     */
    void reallocateMigratingInVms();

    /**
     * Removes a migrating in vm.
     *
     * @param vm the vm
     */
    void removeMigratingInVm(Vm vm);

    /**
     * Sets the Datacenter where the host is placed.
     *
     * @param datacenter the new data center to move the host
     */
    void setDatacenter(Datacenter datacenter);

    /**
     * Sets the particular Pe status on the host.
     *
     * @param peId the pe id
     * @param status the new Pe status
     * @return <tt>true</tt> if the Pe status has set, <tt>false</tt> otherwise (Pe id might not
     *         be exist)
     * @pre peID >= 0
     * @post $none
     */
    boolean setPeStatus(int peId, Pe.Status status);

    /**
     * Updates the processing of VMs running on this Host,
     * that makes the processing of cloudlets inside such VMs to be updated.
     *
     * @param currentTime the current time
     * @return the predicted completion time of the earliest finishing cloudlet
     * (that is a future simulation time),
     * or {@link Double#MAX_VALUE} if there is no next Cloudlet to execute
     * @pre currentTime >= 0.0
     * @post $none
     */
    double updateVmsProcessing(double currentTime);

    /**
     * Try to allocate resources to a new VM in the Host.
     *
     * @param vm Vm being started
     * @return $true if the VM could be started in the host; $false otherwise
     * @pre $none
     * @post $none
     */
    boolean vmCreate(Vm vm);

    /**
     * Destroys a VM running in the host and removes it from the {@link #getVmList()}.
     *
     * @param vm the VM
     * @pre $none
     * @post $none
     */
    void destroyVm(Vm vm);

    /**
     * Destroys all VMs running in the host and remove them from the {@link #getVmList()}.
     *
     * @pre $none
     * @post $none
     */
    void destroyAllVms();

    /**
     * Gets the listener object that will be notified every time when
     * the host updates the processing of all its {@link Vm VMs}.
     *
     * @return the onUpdateVmsProcessingListener
     * @see #updateVmsProcessing(double)
     */
    EventListener<HostUpdatesVmsProcessingEventInfo> getOnUpdateVmsProcessingListener();

    /**
     * Sets the listener object that will be notified every time when
     * the host updates the processing of all its {@link Vm VMs}.
     *
     * @param onUpdateVmsProcessingListener the onUpdateVmsProcessingListener to set
     * @return
     * @see #updateVmsProcessing(double)
     */
    Host setOnUpdateVmsProcessingListener(EventListener<HostUpdatesVmsProcessingEventInfo> onUpdateVmsProcessingListener);

    boolean setFailed(boolean failed);

    /**
     * Gets the CloudSim instance that represents the simulation the Entity is related to.
     * @return
     * @see #setSimulation(Simulation)
     */
    Simulation getSimulation();

    /**
     * Sets the CloudSim instance that represents the simulation the Entity is related to.
     * Such attribute has to be set by the {@link Datacenter} that the host belongs to.
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     * @return
     */
    Host setSimulation(Simulation simulation);


    /**
     * A property that implements the Null Object Design Pattern for {@link Host}
     * objects.
     */
    Host NULL = new Host(){
        @Override public int compareTo(Host o) { return 0; }
        @Override public void addMigratingInVm(Vm vm) {}
        @Override public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) { return false;}
        @Override public void deallocatePesForVm(Vm vm) {}
        @Override public List<Double> getAllocatedMipsForVm(Vm vm) { return Collections.emptyList(); }
        @Override public double getAvailableMips() { return 0; }
        @Override public long getBwCapacity() { return 0; }
        @Override public ResourceProvisioner getBwProvisioner() { return ResourceProvisioner.NULL; }
        @Override public Host setBwProvisioner(ResourceProvisioner bwProvisioner) { return Host.NULL; }
        @Override public Datacenter getDatacenter() { return Datacenter.NULL; }
        @Override public int getId() { return -1; }
        @Override public double getMaxAvailableMips() { return 0.0; }
        @Override public int getNumberOfFreePes() { return 0; }
        @Override public int getNumberOfPes() { return 0; }
        @Override public List<Pe> getPeList() { return Collections.emptyList(); }
        @Override public long getRamCapacity() { return 0; }
        @Override public ResourceProvisioner getRamProvisioner() { return ResourceProvisioner.NULL; }
        @Override public Host setRamProvisioner(ResourceProvisioner ramProvisioner) { return Host.NULL; }
        @Override public long getStorageCapacity() { return 0L; }
        @Override public double getTotalAllocatedMipsForVm(Vm vm) { return 0.0; }
        @Override public int getTotalMips() { return 0; }
        @Override public Vm getVm(int vmId, int brokerId) { return Vm.NULL; }
        @Override public List<Vm> getVmList() { return Collections.emptyList(); }
        @Override public VmScheduler getVmScheduler() {return VmScheduler.NULL; }
        @Override public Host setVmScheduler(VmScheduler vmScheduler) { return Host.NULL; }
        @Override public List<Vm> getVmsMigratingIn() { return Collections.EMPTY_LIST; }
        @Override public boolean isFailed() { return false; }
        @Override public boolean isSuitableForVm(Vm vm) { return false; }
        @Override public void reallocateMigratingInVms() {}
        @Override public void removeMigratingInVm(Vm vm) {}
        @Override public void setDatacenter(Datacenter datacenter) {}
        @Override public boolean setPeStatus(int peId, Pe.Status status) { return false; }
        @Override public double updateVmsProcessing(double currentTime) { return 0.0; }
        @Override public boolean vmCreate(Vm vm) { return false; }
        @Override public void destroyVm(Vm vm) {}
        @Override public void destroyAllVms() {}
        @Override public EventListener<HostUpdatesVmsProcessingEventInfo> getOnUpdateVmsProcessingListener() { return EventListener.NULL; }
        @Override public Host setOnUpdateVmsProcessingListener(EventListener<HostUpdatesVmsProcessingEventInfo> onUpdateVmsProcessingListener) { return Host.NULL; }
        @Override public long getAvailableStorage() { return 0L; }
        @Override public boolean setFailed(boolean failed){return false;}
        @Override public Simulation getSimulation() { return Simulation.NULL; }
        @Override public Host setSimulation(Simulation simulation) { return this; }
        @Override public long getNumberOfWorkingPes() { return 0; }
        @Override public String toString() { return "Host.NULL"; }
    };
}

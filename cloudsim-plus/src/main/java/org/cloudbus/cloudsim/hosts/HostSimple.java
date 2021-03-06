/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.hosts;

import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostUpdatesVmsProcessingEventInfo;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.RawStorage;

/**
 * A Host class that implements the most basic features of a Physical Machine
 * (PM) inside a {@link Datacenter}. It executes actions related to management
 * of virtual machines (e.g., creation and destruction). A host has a defined
 * policy for provisioning memory and bw, as well as an allocation policy for
 * PEs to {@link Vm virtual machines}. A host is associated to a Datacenter and
 * can host virtual machines.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class HostSimple implements Host {

    /**
     * @see #getId()
     */
    private int id;

    private RawStorage storage;

    /**
     * @see #getRamProvisioner()
     */
    private ResourceProvisioner ramProvisioner;

    /**
     * @see #getBwProvisioner()
     */
    private ResourceProvisioner bwProvisioner;

    /**
     * @see #getVmScheduler()
     */
    private VmScheduler vmScheduler;

    /**
     * @see #getVmList()
     */
    private final List<Vm> vmList = new ArrayList<>();

    /**
     * @see #getPeList()
     */
    private List<Pe> peList;

    /**
     * @see #isFailed()
     */
    private boolean failed;

    /**
     * @see #getVmsMigratingIn()
     */
    private final List<Vm> vmsMigratingIn = new ArrayList<>();

    /**
     * @see #getDatacenter()
     */
    private Datacenter datacenter;

    /**
     * @see #getOnUpdateVmsProcessingListener()
     */
    private EventListener<HostUpdatesVmsProcessingEventInfo> onUpdateVmsProcessingListener;

    /**
     * @see #getSimulation()
     */
    private Simulation simulation;

    /**
     * Creates a Host.
     *
     * @param id the host id
     * @param storageCapacity the storage capacity in Megabytes
     * @param peList the host's PEs list
     */
    public HostSimple(int id, long storageCapacity,  List<Pe> peList) {
        setId(id);
        setRamProvisioner(ResourceProvisioner.NULL);
        setBwProvisioner(ResourceProvisioner.NULL);
        setVmScheduler(VmScheduler.NULL);
        this.setStorage(storageCapacity);
        setPeList(peList);
        setFailed(false);
        setDatacenter(Datacenter.NULL);
        this.simulation = Simulation.NULL;
        this.onUpdateVmsProcessingListener = EventListener.NULL;
    }

    /**
     * Creates a Host with the given parameters.
     *
     * @param id the host id
     * @param ramProvisioner the ram provisioner with capacity in Megabytes
     * @param bwProvisioner the bw provisioner with capacity in Megabits/s
     * @param storageCapacity the storage capacity in Megabytes
     * @param peList the host's PEs list
     * @param vmScheduler the vm scheduler
     *
     * @deprecated Use the other available constructors with less parameters
     * and set the remaining ones using the respective setters.
     * This constructor will be removed in future versions.
     */
    @Deprecated
    public HostSimple(
            int id,
            ResourceProvisioner ramProvisioner,
            ResourceProvisioner bwProvisioner,
            long storageCapacity,
            List<Pe> peList,
            VmScheduler vmScheduler)
    {
        this(id, storageCapacity, peList);
        setRamProvisioner(ramProvisioner);
        setBwProvisioner(bwProvisioner);
        setPeList(peList);
        setVmScheduler(vmScheduler);
    }

    @Override
    public double updateVmsProcessing(double currentTime) {
        double nextSimulationTime = Double.MAX_VALUE;
        for (Vm vm : getVmList()) {
            double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
            nextSimulationTime = Math.min(time, nextSimulationTime);
        }

        onUpdateVmsProcessingListener.update(
            HostUpdatesVmsProcessingEventInfo.of(this, nextSimulationTime));
        return nextSimulationTime;
    }

    @Override
    public void addMigratingInVm(Vm vm) {
        if (!getVmsMigratingIn().contains(vm)) {
            if (!storage.isResourceAmountAvailable(vm.getSize())) {
                throw new RuntimeException(
                    String.format(
                        "[VmScheduler.addMigratingInVm] Allocation of VM #%d to Host #%d" +
                    " failed by storage", vm.getId(), getId()));
            }

            if (!getRamProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedRam())) {
                throw new RuntimeException(
                    String.format(
                        "[VmScheduler.addMigratingInVm] Allocation of VM #%d to Host #%d" +
                        " failed by RAM", vm.getId(), getId()));
            }

            if (!getBwProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedBw())) {
                throw new RuntimeException(
                    String.format(
                        "[VmScheduler.addMigratingInVm] Allocation of VM #%d to Host #%d" +
                        " failed by BW", vm.getId(), getId()));
            }

            getVmScheduler().addVmMigratingIn(vm);
            vm.setInMigration(true);
            if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
                getVmScheduler().removeVmMigratingIn(vm);
                vm.setInMigration(false);
                throw new RuntimeException(
                    String.format(
                        "[VmScheduler.addMigratingInVm] Allocation of VM #%d to Host #%d" +
                        " failed by MIPS", vm.getId(),  getId()));
            }

            getStorage().allocateResource(vm.getSize());

            getVmsMigratingIn().add(vm);
            updateVmsProcessing(simulation.clock());
            vm.getHost().updateVmsProcessing(simulation.clock());
        }
    }

    @Override
    public void removeMigratingInVm(Vm vm) {
        deallocateResourcesOfVm(vm);
        getVmsMigratingIn().remove(vm);
        getVmList().remove(vm);
        getVmScheduler().removeVmMigratingIn(vm);
        vm.setInMigration(false);
    }

    @Override
    public void reallocateMigratingInVms() {
        for (Vm vm : getVmsMigratingIn()) {
            if (!getVmList().contains(vm)) {
                getVmList().add(vm);
            }
            getVmScheduler().addVmMigratingIn(vm);
            getRamProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedRam());
            getBwProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedBw());
            getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
            getStorage().allocateResource(vm.getSize());
        }
    }

    @Override
    public boolean isSuitableForVm(Vm vm) {
        return (getVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
                && getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
                && getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam())
                && getBwProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedBw()));
    }

    @Override
    public boolean vmCreate(Vm vm) {
        if (!storage.isResourceAmountAvailable(vm.getSize())) {
            Log.printConcatLine("[VmAllocationPolicy] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by storage");
            return false;
        }

        if (!getRamProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedRam())) {
            Log.printConcatLine("[VmAllocationPolicy] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by RAM");
            return false;
        }

        if (!getBwProvisioner().allocateResourceForVm(vm, vm.getCurrentRequestedBw())) {
            Log.printConcatLine("[VmAllocationPolicy] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by BW");
            getRamProvisioner().deallocateResourceForVm(vm);
            return false;
        }

        if (!getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
            Log.printConcatLine("[VmAllocationPolicy] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by MIPS");
            getRamProvisioner().deallocateResourceForVm(vm);
            getBwProvisioner().deallocateResourceForVm(vm);
            return false;
        }

        getStorage().allocateResource(vm.getSize());
        getVmList().add(vm);
        vm.setHost(this);
        vm.notifyOnHostAllocationListeners();
        return true;
    }

    @Override
    public void destroyVm(Vm vm) {
        if (!Objects.isNull(vm)) {
            deallocateResourcesOfVm(vm);
            getVmList().remove(vm);
            vm.notifyOnHostDeallocationListeners(this);
        }
    }

    /**
     * Deallocate all resources that a VM was using.
     *
     * @param vm the VM
     */
    protected void deallocateResourcesOfVm(Vm vm) {
        vm.setCreated(false);
        getRamProvisioner().deallocateResourceForVm(vm);
        getBwProvisioner().deallocateResourceForVm(vm);
        getVmScheduler().deallocatePesForVm(vm);
        getStorage().deallocateResource(vm.getSize());
    }

    @Override
    public void destroyAllVms() {
        deallocateResourcesOfAllVms();
        for (Vm vm : getVmList()) {
            vm.setCreated(false);
            getStorage().deallocateResource(vm.getSize());
        }

        getVmList().clear();
    }

    /**
     * Deallocate all resources that all VMs were using.
     */
    protected void deallocateResourcesOfAllVms() {
        getRamProvisioner().deallocateResourceForAllVms();
        getBwProvisioner().deallocateResourceForAllVms();
        getVmScheduler().deallocatePesForAllVms();
    }

    @Override
    public Vm getVm(int vmId, int brokerId) {
        return getVmList().stream()
            .filter(vm -> vm.getId() == vmId && vm.getBroker().getId() == brokerId)
            .findFirst().orElse(Vm.NULL);
    }

    @Override
    public int getNumberOfPes() {
        return getPeList().size();
    }

    @Override
    public int getNumberOfFreePes() {
        return PeList.getNumberOfFreePes(getPeList());
    }

    @Override
    public int getTotalMips() {
        return PeList.getTotalMips(getPeList());
    }

    @Override
    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
        return getVmScheduler().allocatePesForVm(vm, mipsShare);
    }

    @Override
    public void deallocatePesForVm(Vm vm) {
        getVmScheduler().deallocatePesForVm(vm);
    }

    @Override
    public List<Double> getAllocatedMipsForVm(Vm vm) {
        return getVmScheduler().getAllocatedMipsForVm(vm);
    }

    @Override
    public double getTotalAllocatedMipsForVm(Vm vm) {
        return getVmScheduler().getTotalAllocatedMipsForVm(vm);
    }

    @Override
    public double getMaxAvailableMips() {
        return getVmScheduler().getMaxAvailableMips();
    }

    @Override
    public double getAvailableMips() {
        return getVmScheduler().getAvailableMips();
    }

    @Override
    public long getBwCapacity() {
        return getBwProvisioner().getCapacity();
    }

    @Override
    public long getRamCapacity() {
        return getRamProvisioner().getCapacity();
    }

    @Override
    public long getStorageCapacity() {
        return getStorage().getCapacity();
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the host id.
     *
     * @param id the new host id
     */
    protected final void setId(int id) {
        this.id = id;
    }

    @Override
    public ResourceProvisioner getRamProvisioner() {
        return ramProvisioner;
    }

    @Override
    public final Host setRamProvisioner(ResourceProvisioner ramProvisioner) {
        this.ramProvisioner = ramProvisioner;
        return this;
    }

    @Override
    public ResourceProvisioner getBwProvisioner() {
        return bwProvisioner;
    }

    @Override
    public final Host setBwProvisioner(ResourceProvisioner bwProvisioner) {
        this.bwProvisioner = bwProvisioner;
        return this;
    }

    @Override
    public VmScheduler getVmScheduler() {
        return vmScheduler;
    }

    @Override
    public final Host setVmScheduler(VmScheduler vmScheduler) {
        if(Objects.isNull(vmScheduler)){
            vmScheduler = VmScheduler.NULL;
        }

        vmScheduler.setHost(this);
        this.vmScheduler = vmScheduler;
        return this;
    }

    @Override
    public List<Pe> getPeList() {
        return peList;
    }

    /**
     * Sets the pe list.
     *
     * @param peList the new pe list
     */
    protected final Host setPeList(List<Pe> peList) {
        if(Objects.isNull(peList)){
            peList = new ArrayList<>();
        }
        this.peList = peList;
        return this;
    }

    @Override
    public <T extends Vm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public final boolean setFailed(boolean failed) {
        this.failed = failed;
        PeList.setStatusFailed(getPeList(), getId(), failed);
        return true;
    }

    /**
     * Checks if the the host is failed and
     * sets all its Vm' to failed.
     */
    public void setVmsToFailedWhenHostIsFailed() {
        if(!this.failed)
            return;

        for (Vm vm : getVmList()) {
            vm.setFailed(true);
            /*
            As the broker is expected to request vm creation and destruction,
            it is set here as the sender of the vm destroy request.
            */
            simulation.sendNow(
                vm.getBroker().getId(), getDatacenter().getId(),
                CloudSimTags.VM_DESTROY, vm);
        }
    }

    @Override
    public boolean setPeStatus(int peId, Pe.Status status) {
        return PeList.setPeStatus(getPeList(), peId, status);
    }

    @Override
    public <T extends Vm> List<T> getVmsMigratingIn() {
        return (List<T>)vmsMigratingIn;
    }

    @Override
    public Datacenter getDatacenter() {
        return datacenter;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }

    /**
     * @see #getStorage()
     */ /**
     * Gets the storage device of the host with capacity in Megabytes.
     *
     * @return the storage device
     */
    protected RawStorage getStorage() {
        return storage;
    }

    @Override
    public String toString() {
        return String.format("Host %d", getId());
    }

    @Override
    public EventListener<HostUpdatesVmsProcessingEventInfo> getOnUpdateVmsProcessingListener() {
        return onUpdateVmsProcessingListener;
    }

    @Override
    public Host setOnUpdateVmsProcessingListener(EventListener<HostUpdatesVmsProcessingEventInfo> onUpdateVmsProcessingListener) {
        if (Objects.isNull(onUpdateVmsProcessingListener)) {
            onUpdateVmsProcessingListener = EventListener.NULL;
        }

        this.onUpdateVmsProcessingListener = onUpdateVmsProcessingListener;
        return this;
    }

    @Override
    public long getAvailableStorage() {
        return getStorage().getAvailableResource();
    }

    @Override
    public long getNumberOfWorkingPes() {
        return getPeList().stream()
                .filter(pe -> pe.getStatus() != Pe.Status.FAILED)
                .count();
    }

    private Host setStorage(long size) {
        this.storage = new RawStorage(size);
        return this;
    }

    @Override
    public Simulation getSimulation() {
        return this.simulation;
    }

    @Override
    public Host setSimulation(Simulation simulation) {
        this.simulation = simulation;
        return this;
    }

    /**
     * Compare this Host with another one based on {@link #getTotalMips()}.
     *
     * @param o the Host to compare to
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Host o) {
        return Double.compare(getTotalMips(), o.getTotalMips());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostSimple that = (HostSimple) o;

        if (id != that.id) return false;
        return simulation.equals(that.simulation);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + simulation.hashCode();
        return result;
    }
}

/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.schedulers.vm;

import java.util.*;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.vms.Vm;

import org.cloudbus.cloudsim.lists.PeList;

/**
 * VmSchedulerAbstract is an abstract class that represents the policy used by a
 * Virtual Machine Monitor (VMM) to share processing power of a PM among VMs
 * running in a host. Each host has to use is own instance of a
 * class that extends VmSchedulerAbstract that will so schedule the allocation of host's PEs for
 * VMs running on it.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmSchedulerAbstract implements VmScheduler {
    /**
     * @see #getHost()
     */
    private Host host;

    /**
     * @see #getPeMap()
     */
    private Map<Vm, List<Pe>> peMap;

    /**
     * @see #getMipsMapAllocated()
     */
    private Map<Vm, List<Double>> mipsMapAllocated;

    /**
     * The total available MIPS that can be allocated on demand for VMs.
     */
    private double availableMips;

    /**
     * @see #getVmsMigratingIn()
     */
    private Set<Vm> vmsMigratingIn;

    /**
     * The VMs migrating out the host (departing). It is the list of VM
     */
    private Set<Vm> vmsMigratingOut;

    /**
     * Creates a VmScheduler.
     *
     * @post $none
     */
    public VmSchedulerAbstract() {
        setHost(Host.NULL);
        setVmsMigratingIn(new HashSet<>());
        setVmsMigratingOut(new HashSet<>());
    }

    @Override
    public void deallocatePesForAllVms() {
        getMipsMapAllocated().clear();
        setAvailableMips(PeList.getTotalMips(getPeList()));
        getPeList().forEach(pe -> pe.getPeProvisioner().deallocateMipsForAllVms());
    }

    @Override
    public List<Pe> getPesAllocatedForVM(Vm vm) {
        getPeMap().putIfAbsent(vm, new ArrayList<>());
        return getPeMap().get(vm);
    }

    @Override
    public List<Double> getAllocatedMipsForVm(Vm vm) {
        getMipsMapAllocated().putIfAbsent(vm, new ArrayList<>());
        return getMipsMapAllocated().get(vm);
    }

    @Override
    public double getTotalAllocatedMipsForVm(Vm vm) {
        return getAllocatedMipsForVm(vm).stream().reduce(0.0, Double::sum);
    }

    @Override
    public double getMaxAvailableMips() {
        if (getPeList().isEmpty()) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        return getPeList().stream()
                    .map(Pe::getPeProvisioner)
                    .mapToDouble(PeProvisioner::getAvailableMips)
                    .max().orElse(0.0);
    }

    @Override
    public double getPeCapacity() {
        if (getPeList().isEmpty()) {
            Log.printLine("Pe list is empty");
            return 0;
        }

        return getPeList().get(0).getMips();
    }

    @Override
    public final List<Pe> getPeList() {
        return host.getPeList();
    }

    /**
     * Gets the map of VMs to MIPS, were each key is a VM and each value is the
     * currently allocated MIPS from the respective PE to that VM. The PEs where
     * the MIPS capacity is get are defined in the {@link #peMap}.
     *
     * @return the mips map
     */
    protected Map<Vm, List<Double>> getMipsMapAllocated() {
        return mipsMapAllocated;
    }

    /**
     * Sets the map of VMs to MIPS, were each key is a VM and each value is the
     * currently allocated MIPS from the respective PE to that VM. The PEs where
     * the MIPS capacity is get are defined in the {@link #peMap}.
     *
     * @param mipsMapAllocated the mips map
     */
    protected final void setMipsMapAllocated(Map<Vm, List<Double>> mipsMapAllocated) {
        this.mipsMapAllocated = mipsMapAllocated;
    }

    @Override
    public double getAvailableMips() {
        return availableMips;
    }

    /**
     * Sets the amount of mips that is free.
     *
     * @param availableMips the new free mips amount
     */
    protected final void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    @Override
    public Set<Vm> getVmsMigratingIn() {
        return Collections.unmodifiableSet(vmsMigratingIn);
    }

    @Override
    public Set<Vm> getVmsMigratingOut() {
        return Collections.unmodifiableSet(vmsMigratingOut);
    }

    /**
     * Sets the vms migrating out.
     *
     * @param vmsMigratingOut the new vms migrating out
     */
    protected final void setVmsMigratingOut(Set<Vm> vmsMigratingOut) {
        if(Objects.isNull(vmsMigratingOut)){
            vmsMigratingOut = new HashSet<>();
        }
        this.vmsMigratingOut = vmsMigratingOut;
    }

    /**
     * Sets the vms migrating in.
     *
     * @param vmsMigratingIn the new vms migrating in
     */
    protected final void setVmsMigratingIn(Set<Vm> vmsMigratingIn) {
        if(Objects.isNull(vmsMigratingIn)){
            vmsMigratingIn = new HashSet<>();
        }
        this.vmsMigratingIn = vmsMigratingIn;
    }

    @Override
    public Map<Vm, List<Pe>> getPeMap() {
        return peMap;
    }

    /**
     * Sets the map of VMs to PEs, where each key is a VM and each value is a list
     * of PEs allocated to that VM.
     *
     * @param peMap the pe map
     */
    protected final void setPeMap(Map<Vm, List<Pe>> peMap) {
        this.peMap = peMap;
    }

    @Override
    public Host getHost() {
        return host;
    }

    @Override
    public VmScheduler setHost(Host host) {
        if(Objects.isNull(host)){
            host = Host.NULL;
        }

        this.host = host;

        setPeMap(new HashMap<>());
        setMipsMapAllocated(new HashMap<>());
        setAvailableMips(PeList.getTotalMips(getPeList()));

        return this;
    }

    @Override
    public boolean addVmMigratingIn(Vm vm) {
        return this.vmsMigratingIn.add(vm);
    }

    @Override
    public boolean addVmMigratingOut(Vm vm) {
        return this.vmsMigratingOut.add(vm);
    }

    @Override
    public boolean removeVmMigratingIn(Vm vm) {
        return this.vmsMigratingIn.remove(vm);
    }

    @Override
    public boolean removeVmMigratingOut(Vm vm) {
        return this.vmsMigratingOut.remove(vm);
    }
}

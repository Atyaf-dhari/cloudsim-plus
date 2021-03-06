package org.cloudbus.cloudsim.allocationpolicies;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

/**
 * An interface to be implemented by each class that represents a policy used by
 * a {@link Datacenter} to choose a {@link Host} to place or migrate a
 * given {@link Vm}.
 *
 * @author Manoel Campos da Silva Filho
 */
public interface VmAllocationPolicy {
    /**
     * Gets the {@link Datacenter} associated to the Allocation Policy.
     * @return
     */
    Datacenter getDatacenter();

    /**
     * Sets the Datacenter associated to the Allocation Policy
     * @param datacenter the Datacenter to set
     * @return
     */
    void setDatacenter(Datacenter datacenter);

    /**
     * Allocates a host for a given VM.
     *
     * @param vm the VM to allocate a host to
     * @return $true if the host could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    boolean allocateHostForVm(Vm vm);

    /**
     * Allocates a specified host for a given VM.
     *
     * @param vm the VM to allocate a host to
     * @param host the host to allocate to the given VM
     * @return $true if the host could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    boolean allocateHostForVm(Vm vm, Host host);

    /**
     * Releases the host used by a VM.
     *
     * @param vm the vm to get its host released
     * @pre $none
     * @post $none
     */
    void deallocateHostForVm(Vm vm);

    /**
     * Gets the list of Hosts available in a {@link Datacenter}, that will be
     * used by the Allocation Policy to place VMs.
     *
     * @param <T> The generic type
     * @return the host list
     */
     <T extends Host> List<T> getHostList();

    /**
     * Optimize allocation of the VMs according to current utilization.
     *
     * @param vmList the vm list
     * @return the new vm placement map, where each key is a VM and each value is the host where such a Vm has to be placed
     *
     */
    Map<Vm, Host> optimizeAllocation(List<? extends Vm> vmList);

    /**
     * A property that implements the Null Object Design Pattern for {@link VmAllocationPolicy}
     * objects.
     */
    VmAllocationPolicy NULL = new VmAllocationPolicy() {
        @Override public Datacenter getDatacenter() { return Datacenter.NULL; }
        @Override public void setDatacenter(Datacenter datacenter) {}
        @Override public boolean allocateHostForVm(Vm vm){ return false; }
        @Override public boolean allocateHostForVm(Vm vm, Host host) { return false; }
        @Override public void deallocateHostForVm(Vm vm){}
        @Override public List<Host> getHostList(){ return Collections.emptyList(); }
        @Override public Map<Vm, Host> optimizeAllocation(List<? extends Vm> vmList) { return Collections.emptyMap(); }
    };
}

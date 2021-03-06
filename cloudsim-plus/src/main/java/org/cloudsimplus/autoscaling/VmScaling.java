/**
 * CloudSim Plus: A highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.autoscaling;

import org.cloudbus.cloudsim.vms.Vm;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An interface to allow implementing <a href="https://en.wikipedia.org/wiki/Scalability#Horizontal_and_vertical_scaling">horizontal and vertical scaling</a> of VMs.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 */
public interface VmScaling {
    /**
     * A {@link Predicate} that always returns false independently of any condition.
     */
    Predicate<Vm> FALSE_PREDICATE = (vm) -> false;

    /**
     * An attribute that implements the Null Object Design Pattern for {@link HorizontalVmScaling}
     * objects.
     */
    VmScaling NULL = new HorizontalVmScaling() {
        @Override public Vm getVm() { return Vm.NULL; }
        @Override public VmScaling setVm(Vm vm) { return this; }
        @Override public Supplier<Vm> getVmSupplier() { return () -> Vm.NULL; }
        @Override public VmScaling setVmSupplier(Supplier<Vm> supplier) { return this; }
        @Override public Predicate<Vm> getOverloadPredicate() { return FALSE_PREDICATE; }
        @Override public HorizontalVmScaling setOverloadPredicate(Predicate<Vm> predicate) { return this; }
        @Override public void scaleIfOverloaded(double time) {}
    };

    /**
     * Gets the {@link Vm} that this Load Balancer is linked to.
     * @return
     */
    Vm getVm();

    /**
     * Sets a {@link Vm} to this Load Balancer. The broker will call this Load Balancer
     * in order to balance load when its Vm is over utilized.
     *
     * @param vm the Vm to set
     * @return
     */
    VmScaling setVm(Vm vm);

    /**
     * Gets a {@link Predicate} that defines when {@link #getVm() Vm} is overloaded or not,
     * that will make the Vm's broker to dynamically create a new Vm to balance the load
     * of new arrived Cloudlets.
     *
     * @return
     * @see #setOverloadPredicate(Predicate)
     */
    Predicate<Vm> getOverloadPredicate();

    /**
     * Sets a {@link Predicate} that defines when {@link #getVm() Vm} is overloaded or not,
     * that will make the Vm's broker to dynamically create a new Vm to balance the load
     * of new arrived Cloudlets.
     *
     * @param predicate a predicate that checks certain conditions
     *                  to define that the Load Balancer's {@link #getVm() Vm} is over utilized.
     *                  The predicate receives the Vm to allow the predicate
     *                  to define the over utilization condition.
     *                  Such a condition can be defined, for instance,
     *                  based on Vm's {@link Vm#getTotalUtilizationOfCpu(double)} CPU usage}.
     * @return
     */
    HorizontalVmScaling setOverloadPredicate(Predicate<Vm> predicate);

    /**
     * Performs the horizontal or vertical scale if the Vm is overloaded.
     * The type of scale depends on implementing classes.
     *
     * @param time current simulation time
     */
    void scaleIfOverloaded(double time);
}

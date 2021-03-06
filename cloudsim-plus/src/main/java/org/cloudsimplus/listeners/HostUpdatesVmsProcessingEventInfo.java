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
package org.cloudsimplus.listeners;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.hosts.Host;

/**
 * An interface that represents data to be passed
 * to {@link EventListener} objects that are registered to be notified
 * after a Host updates the processing of its VMs.
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 1.0
 *
 * @see Host#getOnUpdateVmsProcessingListener()
 */
public interface HostUpdatesVmsProcessingEventInfo extends HostEventInfo  {
    /**
     * Gets the expected completion time of the next finishing cloudlet.
     * @return
     */
    double getNextCloudletCompletionTime();

    /**
     * Gets a HostUpdatesVmsProcessingEventInfo instance from the given parameters.
     * The {@link #getTime()} is the current simulation time.
     *
     * @param host the {@link Host} where the event happened
     * @param nextCloudletCompletionTime the expected time for completion of the next {@link Cloudlet}
     */
    static HostUpdatesVmsProcessingEventInfo of(Host host, double nextCloudletCompletionTime) {
        final double time = host.getSimulation().clock();
        return new HostUpdatesVmsProcessingEventInfo() {
            @Override public double getNextCloudletCompletionTime() { return nextCloudletCompletionTime; }
            @Override public Host getHost() { return host; }
            @Override public double getTime() { return time; }
        };
    }
}

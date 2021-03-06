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
package org.cloudsimplus.testbeds;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A base class to implement simulation experiments.
 *
 * @author Manoel Campos da Silva Filho
 */
public abstract class SimulationExperiment implements Runnable {
	protected final ExperimentRunner runner;
	private final List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private List<Host> hostList;
    private List<DatacenterBroker> brokerList;

	private final int index;
    private int numberOfCreatedHosts;
    private int numberOfCreatedCloudlets;
    private int numberOfCreatedVms;
    private boolean verbose;

    private CloudSim cloudsim;
    private Consumer<? extends SimulationExperiment> afterExperimentFinish;

    /**
	 * Creates a simulation experiment.
	 *
	 * @param index the index that identifies the current experiment run.
	 * @param runner The {@link ExperimentRunner} that is in charge
	 * of executing this experiment a defined number of times and to collect
	 * data for statistical analysis.
	 */
	public SimulationExperiment(int index, ExperimentRunner runner) {
		this.verbose = false;
		this.vmList = new ArrayList<>();
		this.index = index;
		this.cloudletList = new ArrayList<>();
		this.brokerList = new ArrayList<>();
        this.hostList = new ArrayList<>();
		this.runner = runner;
        this.numberOfCreatedHosts = 0;
        this.numberOfCreatedCloudlets = 0;
        this.numberOfCreatedVms = 0;

        //Defines an empty Consumer to avoid NullPointerException if an actual one is not set
		afterExperimentFinish = exp -> {};
	}

	public List<Cloudlet> getCloudletList() {
	    return cloudletList;
	}

	public List<Vm> getVmList() {
	    return vmList;
	}

	protected void setVmList(List<Vm> vmList) {
		this.vmList = vmList;
	}

    /**
     * Defines if simulation results of the experiment have to be output or not.
     * @param verbose true if the results have to be output, falser otherwise
     */
	public SimulationExperiment setVerbose(boolean verbose) {
	    this.verbose = verbose;
        return this;
	}

	/**
	 * Number of hosts created so far.
	 */
	public int getNumberOfCreatedHosts() {
		return numberOfCreatedHosts;
	}

	/**
	 * The index that identifies the current experiment run.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Indicates if simulation results of the experiment don't have to be output.
	 */
	public boolean isNotVerbose() {
		return !verbose;
	}

    /**
     * Indicates if simulation results of the experiment have to be output.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Adds a Vm created by a {@link Supplier} function to the list of created Vms.
     *
     * @param vmSupplier a {@link Supplier} function that is able to create a Vm
     * @return the created Vm
     */
	protected Vm addNewVmToList(Supplier<Vm> vmSupplier) {
        Vm vm = vmSupplier.get();
        getVmList().add(vm);
        numberOfCreatedVms++;
        return vm;
	}

    /**
     * Adds a Cloudlet created by a {@link Supplier} function to the list of created Cloudlets.
     *
     * @param cloudletSupplier a {@link Supplier} function that is able to create a Cloudlet
     * @return the created Cloudlet
     */
    protected Cloudlet addNewCloudletToList(Supplier<Cloudlet> cloudletSupplier) {
        Cloudlet cloudlet = cloudletSupplier.get();
        getCloudletList().add(cloudlet);
        numberOfCreatedCloudlets++;
        return cloudlet;
	}

	/**
	 * Builds the simulation scenario and starts execution.
	 *
	 * @throws RuntimeException
	 */
	@Override
	public void run() {
		buildScenario();

		cloudsim.start();
		getAfterExperimentFinish().accept(this);

		printResultsInternal();
	}


	/**
	 * Checks if {@link #isVerbose()}
	 * in order to call {@link #printResults()}
	 * to print the experiment results.
	 *
	 * @see #printResults()
	 */
	private void printResultsInternal(){
		if(isNotVerbose()){
			return;
		}

		printResults();
	}

	/**
	 * Prints the results for the experiment.
	 *
	 * The method has to be implemented by subclasses in order to output
	 * the experiment results.
	 *
	 * @see #printResultsInternal()
	 */
	public abstract void printResults();

    /**
     * Creates the simulation scenario to run the experiment.
     */
	protected void buildScenario() {
		int numberOfCloudUsers = 1;
		this.cloudsim = new CloudSim();

		Datacenter datacenter0 = createDatacenter();
		DatacenterBroker broker0 = createBrokerAndAddToList();
		createAndSubmitVmsInternal(broker0);
		createAndSubmitCloudletsInternal(broker0);
	}

	/**
	 * Creates a DatacenterBroker.
	 * @return the created DatacenterBroker
	 */
	protected abstract DatacenterBroker createBroker();

    /**
     * Creates the Cloudlets to be used by the experiment.
     * @param broker broker that the Cloudlets belong to
     */
    protected abstract void createCloudlets(DatacenterBroker broker);

    /**
     * Creates the Vms to be used by the experiment.
     * @param broker broker that the Vms belong to
     */
    protected abstract void createVms(DatacenterBroker broker);

	/**
	 * Creates a DatacenterBroker and adds it to the {@link #getBrokerList() DatacenterBroker list}.
	 * @return the created DatacenterBroker.
	 */
	private DatacenterBroker createBrokerAndAddToList(){
		DatacenterBroker broker = createBroker();
		brokerList.add(broker);
		return broker;
	}

    /**
     * Creates all the Cloudlets required by the experiment and submits them to a Broker.
     * @param broker broker to submit Cloudlets to
     */
	protected void createAndSubmitCloudletsInternal(DatacenterBroker broker) {
        createCloudlets(broker);
        broker.submitCloudletList(getCloudletList());
    }

    /**
     * Creates all the VMs required by the experiment and submits them to a Broker.
     * @param broker broker to submit VMs to
     */
	private void createAndSubmitVmsInternal(DatacenterBroker broker){
        createVms(broker);
        broker.submitVmList(getVmList());
    }

    private DatacenterSimple createDatacenter() {
        createHosts();
		//Defines the characteristics of the data center
		double cost = 3.0; // the cost of using processing in this Datacenter
		double costPerMem = 0.05; // the cost of using memory in this Datacenter
		double costPerStorage = 0.001; // the cost of using storage in this Datacenter
		double costPerBw = 0.0; // the cost of using bw in this Datacenter
        List<FileStorage> storageList = new ArrayList<>(); // we are not adding SAN devices by now
        DatacenterCharacteristics characteristics =
            new DatacenterCharacteristicsSimple(hostList)
                .setCostPerSecond(cost)
                .setCostPerMem(costPerMem)
                .setCostPerStorage(costPerStorage)
                .setCostPerBw(costPerBw);

		return new DatacenterSimple(cloudsim, characteristics, new VmAllocationPolicySimple());
	}

    protected abstract void createHosts();

    /**
     * Adds a Host created by a {@link Supplier} function to the list of created Hosts.
     *
     * @param hostSupplier a {@link Supplier} function that is able to create a Host
     * @return the created Host
     */
    protected Host addNewHostToList(Supplier<Host> hostSupplier) {
        Host host = hostSupplier.get();
        hostList.add(host);
        numberOfCreatedHosts++;
        return host;
    }


    /**
     * Gets the object that is in charge to run the experiment.
     * @return
     */
	public ExperimentRunner getRunner() {
		return runner;
	}

	/**
     * Gets the list of created DatacenterBrokers.
	 * @return
	 */
	public List<DatacenterBroker> getBrokerList() {
		return brokerList;
	}

	/**
	 * <p>Sets a {@link Consumer} object that will receive the experiment instance after the experiment
	 * finishes executing and performs some post-processing tasks.
	 * These tasks are defined by the developer using the current class
	 * and can include collecting data for statistical analysis.</p>
	 *
	 * <p>Setting a Consumer object is optional.</p>
	 *
	 * @param afterExperimentFinishConsumer a {@link Consumer} instance to set.
	 * @param <T> a generic class that defines the type of experiment the {@link Consumer}
	 *           will deal with. It is used to ensure that when the {@link Consumer} is called,
	 *           it will receive an object of the exact type of the {@link SimulationExperiment}
	 *           instance that the Consumer is being associated to.
	 */
	public <T extends SimulationExperiment> SimulationExperiment setAfterExperimentFinish(Consumer<T> afterExperimentFinishConsumer){
		this.afterExperimentFinish = afterExperimentFinishConsumer;
        return this;
	}

	/**
	 * Gets a {@link Consumer} object that will receive the experiment instance after the experiment
	 * finishes executing and performs some post-processing tasks.
	 * These tasks are defined by the developer using the current class
	 * and can include collecting data for statistical analysis.
     * @return
	 *
	 */
	private <T extends SimulationExperiment> Consumer<T> getAfterExperimentFinish() {
		return (Consumer<T>) afterExperimentFinish;
	}

    public int getNumberOfCreatedCloudlets() {
        return numberOfCreatedCloudlets;
    }

    public int getNumberOfCreatedVms() {
        return numberOfCreatedVms;
    }

    public CloudSim getCloudsim() {
        return cloudsim;
    }
}

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.core.events.SimEvent;

/**
 * An interface that represents a simulation entity. An entity handles events and can
 * send events to other entities.
 *
 * @author Manoel Campos da Silva Filho
 * @see CloudSimEntity
 */
public interface SimEntity extends Nameable, Cloneable, Runnable, Comparable<SimEntity> {
    /**
     * Checks if the entity already was started or not.
     * @return
     */
    boolean isStarted();

    /**
     * Defines the event state.
     */
    enum State {RUNNABLE, WAITING, HOLDING, FINISHED}

    /**
     * Gets the CloudSim instance that represents the simulation the Entity is related to.
     * @return
     */
    Simulation getSimulation();

    /**
     * Sets the CloudSim instance that represents the simulation the Entity is related to.
     * @param simulation The CloudSim instance that represents the simulation the Entity is related to
     * @return
     */
    SimEntity setSimulation(Simulation simulation);

    /**
     * Processes events or services that are available for the entity. This
     * method is invoked by the {@link CloudSim} class whenever there is an
     * event in the deferred queue, which needs to be processed by the entity.
     *
     * @param ev information about the event just happened
     *
     * @pre ev != null
     * @post $none
     */
    void processEvent(SimEvent ev);

    /**
     * Sends an event to another entity by id number and with <b>no</b> data.
     * Note that the tag <code>9999</code> is reserved.
     *
     * @param dest  The unique id number of the destination entity
     * @param delay How many seconds after the current simulation time the event should be sent
     * @param tag   An user-defined number representing the type of event.
     */
    void schedule(int dest, double delay, int tag);

    /**
     * The run loop to process events fired during the simulation. The events
     * that will be processed are defined in the
     * {@link #processEvent(SimEvent)} method.
     *
     * @see #processEvent(SimEvent)
     */
    @Override void run();

    /**
     * Starts the entity during simulation start.
     * This method is invoked by the {@link CloudSim} class when the simulation is started.
     */
    void start();

    /**
     * Shuts down the entity. This method is invoked by the {@link CloudSim}
     * before the simulation finishes. If you want to save data in log files
     * this is the method in which the corresponding code would be placed.
     */
    void shutdownEntity();

    /**
     * Sets the Entity name.
     *
     * @param newName the new name
     * @return
     * @throws IllegalArgumentException when the entity name is <tt>null</tt> or empty
     */
    SimEntity setName(String newName) throws IllegalArgumentException;

    /**
     * An attribute that implements the Null Object Design Pattern for {@link SimEntity}
     * objects.
     */
    SimEntity NULL = new SimEntity() {
        @Override public int compareTo(SimEntity o) { return 0; }
        @Override public boolean isStarted() { return false; }
        @Override public Simulation getSimulation() { return Simulation.NULL; }
        @Override public SimEntity setSimulation(Simulation simulation) { return this; }
        @Override public void processEvent(SimEvent ev) {}
        @Override public void schedule(int dest, double delay, int tag) {}
        @Override public void run() {}
        @Override public void start() {}
        @Override public void shutdownEntity() {}
        @Override public SimEntity setName(String newName) throws IllegalArgumentException { return this; }
        @Override public String getName() { return ""; }
        @Override public int getId() { return 0; }
    };
}

package org.cloudbus.cloudsim.examples.power.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.power.PowerDatacenter;
import org.cloudbus.cloudsim.hosts.power.PowerHost;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationInterQuartileRange;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationLocalRegressionRobust;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.allocationpolicies.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumMigrationTime;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.selectionpolicies.power.PowerVmSelectionPolicyRandomSelection;

/**
 * An abstract class to provide base methods to enable running simulation examples.
 * <p>
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * <br>
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * </p>
 *
 * @author Anton Beloglazov
 */
public abstract class RunnerAbstract {
    private static boolean enableOutput;
    protected static DatacenterBroker broker;
    protected static List<Cloudlet> cloudletList;
    protected static List<Vm> vmList;
    protected static List<PowerHost> hostList;
    private CloudSim simulation;

    /**
     * Run.
     *
     * @param enableOutput       the enable output
     * @param outputToFile       the output to file
     * @param inputFolder        the input folder
     * @param outputFolder       the output folder
     * @param workload           the workload
     * @param vmAllocationPolicy the vm allocation policy
     * @param vmSelectionPolicy  the vm selection policy
     * @param safetyParameterOrUtilizationThreshold a double value to be passed to the specific
     *                               PowerVmSelectionPolicy being created, which the meaning depends
     *                               on that policy.
     */
    public RunnerAbstract(
        boolean enableOutput,
        boolean outputToFile,
        String inputFolder,
        String outputFolder,
        String workload,
        String vmAllocationPolicy,
        String vmSelectionPolicy,
        double safetyParameterOrUtilizationThreshold) {
        try {
            initLogOutput(
                enableOutput,
                outputToFile,
                outputFolder,
                workload,
                vmAllocationPolicy,
                vmSelectionPolicy,
                safetyParameterOrUtilizationThreshold);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        init(inputFolder + "/" + workload);
        start(
            getExperimentName(
                workload, vmAllocationPolicy, vmSelectionPolicy,
                String.valueOf(safetyParameterOrUtilizationThreshold)),
            outputFolder,
            getVmAllocationPolicy(vmAllocationPolicy, vmSelectionPolicy, safetyParameterOrUtilizationThreshold));
    }

    /**
     * Inits the log output.
     *
     * @param enableOutput       the enable output
     * @param outputToFile       the output to file
     * @param outputFolder       the output folder
     * @param workload           the workload
     * @param vmAllocationPolicy the vm allocation policy
     * @param vmSelectionPolicy  the vm selection policy
     * @param safetyParameterOrUtilizationThreshold a double value to be passed to the specific
     *                               PowerVmSelectionPolicy being created, which the meaning depends
     *                               on that policy.
     * @throws IOException
     */
    protected void initLogOutput(
        boolean enableOutput,
        boolean outputToFile,
        String outputFolder,
        String workload,
        String vmAllocationPolicy,
        String vmSelectionPolicy,
        double safetyParameterOrUtilizationThreshold) throws IOException {
        setEnableOutput(enableOutput);
        Log.setDisabled(!isEnableOutput());
        if (isEnableOutput() && outputToFile) {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            File folder2 = new File(outputFolder + "/log");
            if (!folder2.exists()) {
                folder2.mkdir();
            }

            File file = new File(outputFolder + "/log/"
                + getExperimentName(workload, vmAllocationPolicy, vmSelectionPolicy,
                String.valueOf(safetyParameterOrUtilizationThreshold)) + ".txt");
            file.createNewFile();
            Log.setOutput(new FileOutputStream(file));
        }
    }

    /**
     * Inits the simulation.
     *
     * @param inputFolder the input folder
     */
    protected void init(String inputFolder){
        this.simulation = new CloudSim();
    }

    /**
     * Starts the simulation.
     *
     * @param experimentName     the experiment name
     * @param outputFolder       the output folder
     * @param vmAllocationPolicy the vm allocation policy
     */
    protected void start(String experimentName, String outputFolder, VmAllocationPolicy vmAllocationPolicy) {
        System.out.println("Starting " + experimentName);

        try {
            PowerDatacenter datacenter = (PowerDatacenter) Helper.createDatacenter(
                simulation,
                PowerDatacenter.class,
                hostList,
                vmAllocationPolicy);

            datacenter.setMigrationsEnabled(true);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            simulation.terminateAt(Constants.SIMULATION_LIMIT);
            double lastClock = simulation.start();

            List<Cloudlet> newList = broker.getCloudletsFinishedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            Helper.printResults(
                datacenter,
                vmList,
                lastClock,
                experimentName,
                Constants.OUTPUT_CSV,
                outputFolder);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }

    /**
     * Gets the experiment name.
     *
     * @param args the args
     * @return the experiment name
     */
    protected String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (args[i].isEmpty()) {
                continue;
            }
            if (i != 0) {
                experimentName.append("_");
            }
            experimentName.append(args[i]);
        }
        return experimentName.toString();
    }

    /**
     * Gets the vm allocation policy.
     *
     * @param vmAllocationPolicyName the vm allocation policy name
     * @param vmSelectionPolicyName  the vm selection policy name
     * @param safetyParameterOrUtilizationThreshold a double value to be passed to the specific
     *                               PowerVmSelectionPolicy being created, which the meaning depends
     *                               on that policy.
     * @return the vm allocation policy
     */
    protected VmAllocationPolicy getVmAllocationPolicy(
        String vmAllocationPolicyName,
        String vmSelectionPolicyName,
        double safetyParameterOrUtilizationThreshold) {
        VmAllocationPolicy vmAllocationPolicy = null;
        PowerVmSelectionPolicy vmSelectionPolicy = null;
        if (!vmSelectionPolicyName.isEmpty()) {
            vmSelectionPolicy = getVmSelectionPolicy(vmSelectionPolicyName);
        }

        switch (vmAllocationPolicyName) {
            case "iqr": {
                PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy =
                    new PowerVmAllocationPolicyMigrationStaticThreshold(vmSelectionPolicy, 0.7);
                vmAllocationPolicy = new PowerVmAllocationPolicyMigrationInterQuartileRange(
                    vmSelectionPolicy,
                    safetyParameterOrUtilizationThreshold,
                    fallbackVmSelectionPolicy);
                break;
            }
            case "mad": {
                PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy =
                    new PowerVmAllocationPolicyMigrationStaticThreshold(vmSelectionPolicy, 0.7);
                vmAllocationPolicy = new PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation(
                    vmSelectionPolicy,
                    safetyParameterOrUtilizationThreshold,
                    fallbackVmSelectionPolicy);
                break;
            }
            case "lr": {
                PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy =
                    new PowerVmAllocationPolicyMigrationStaticThreshold(vmSelectionPolicy, 0.7);
                vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegression(
                    vmSelectionPolicy,
                    safetyParameterOrUtilizationThreshold,
                    fallbackVmSelectionPolicy)
                    .setSchedulingInterval(Constants.SCHEDULING_INTERVAL);
                break;
            }
            case "lrr": {
                PowerVmAllocationPolicyMigrationAbstract fallbackVmSelectionPolicy =
                    new PowerVmAllocationPolicyMigrationStaticThreshold(vmSelectionPolicy, 0.7);
                vmAllocationPolicy = new PowerVmAllocationPolicyMigrationLocalRegressionRobust(
                    vmSelectionPolicy,
                    safetyParameterOrUtilizationThreshold,
                    fallbackVmSelectionPolicy)
                    .setSchedulingInterval(Constants.SCHEDULING_INTERVAL);
                break;
            }
            case "thr":
                vmAllocationPolicy = new PowerVmAllocationPolicyMigrationStaticThreshold(
                    vmSelectionPolicy,
                    safetyParameterOrUtilizationThreshold);
                break;
            case "dvfs":
                vmAllocationPolicy = new PowerVmAllocationPolicySimple();
                break;
            default:
                System.out.println("Unknown VM allocation policy: " + vmAllocationPolicyName);
                System.exit(0);
        }
        return vmAllocationPolicy;
    }

    /**
     * Gets the vm selection policy.
     *
     * @param vmSelectionPolicyName the vm selection policy name
     * @return the vm selection policy
     */
    protected PowerVmSelectionPolicy getVmSelectionPolicy(String vmSelectionPolicyName) {
        PowerVmSelectionPolicy vmSelectionPolicy = null;
        switch (vmSelectionPolicyName) {
            case "mc":
                vmSelectionPolicy = new PowerVmSelectionPolicyMaximumCorrelation(
                    new PowerVmSelectionPolicyMinimumMigrationTime());
                break;
            case "mmt":
                vmSelectionPolicy = new PowerVmSelectionPolicyMinimumMigrationTime();
                break;
            case "mu":
                vmSelectionPolicy = new PowerVmSelectionPolicyMinimumUtilization();
                break;
            case "rs":
                vmSelectionPolicy = new PowerVmSelectionPolicyRandomSelection();
                break;
            default:
                System.out.println("Unknown VM selection policy: " + vmSelectionPolicyName);
                System.exit(0);
        }
        return vmSelectionPolicy;
    }

    /**
     * Sets the enable output.
     *
     * @param enableOutput the new enable output
     */
    public void setEnableOutput(boolean enableOutput) {
        RunnerAbstract.enableOutput = enableOutput;
    }

    /**
     * Checks if is enable output.
     *
     * @return true, if is enable output
     */
    public boolean isEnableOutput() {
        return enableOutput;
    }


    public CloudSim getSimulation() {
        return simulation;
    }
}

/*
 * Title:        CloudSim Toolkiimport static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
c) 2009-2010, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim.cloudlets;

import java.util.ArrayList;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterMocker;
import org.cloudbus.cloudsim.mocks.CloudSimMocker;
import org.cloudbus.cloudsim.mocks.Mocks;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelStochastic;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

import java.util.List;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudsimplus.listeners.CloudletVmEventInfo;
import org.cloudsimplus.listeners.EventListener;
import org.easymock.EasyMock;
import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author	Anton Beloglazov
 * @since	CloudSim Toolkit 2.0
 */
public class CloudletSimpleTest {

    private static final long CLOUDLET_LENGTH = 1000;
    private static final long CLOUDLET_FILE_SIZE = 1000;
    private static final int CLOUDLET_OUTPUT_SIZE = 1000;

    private static final int PES_NUMBER = 2;

    private CloudletSimple cloudlet;
    private UtilizationModel utilizationModelCpu;
    private UtilizationModel utilizationModelRam;
    private UtilizationModel utilizationModelBw;

    @Before
    public void setUp() throws Exception {
        utilizationModelCpu = new UtilizationModelStochastic();
        utilizationModelRam = new UtilizationModelStochastic();
        utilizationModelBw = new UtilizationModelStochastic();
        cloudlet = new CloudletSimple(0, CLOUDLET_LENGTH, PES_NUMBER);
        cloudlet.setFileSize(CLOUDLET_FILE_SIZE)
                .setOutputSize(CLOUDLET_OUTPUT_SIZE)
                .setUtilizationModelCpu(utilizationModelCpu)
                .setUtilizationModelRam(utilizationModelRam)
                .setUtilizationModelBw(utilizationModelBw);
    }

    @Test
    public void testCloudlet() {
        assertEquals(CLOUDLET_LENGTH, cloudlet.getLength(), 0);
        assertEquals(CLOUDLET_LENGTH * PES_NUMBER, cloudlet.getTotalLength(), 0);
        assertEquals(CLOUDLET_FILE_SIZE, cloudlet.getFileSize());
        assertEquals(CLOUDLET_OUTPUT_SIZE, cloudlet.getOutputSize());
        assertEquals(PES_NUMBER, cloudlet.getNumberOfPes());
        assertSame(utilizationModelCpu, cloudlet.getUtilizationModelCpu());
        assertSame(utilizationModelRam, cloudlet.getUtilizationModelRam());
        assertSame(utilizationModelBw, cloudlet.getUtilizationModelBw());
    }

    @Test
    public void testAddOnCloudletFinishEventListener() {
        EventListener<CloudletVmEventInfo> listener = (info) -> {};
        cloudlet.addOnCloudletFinishListener(listener);
        assertTrue(cloudlet.removeOnCloudletFinishListener(listener));
    }

    @Test
    public void testAddOnCloudletFinishEventListener_Null() {
        cloudlet.addOnCloudletFinishListener(null);
        assertFalse(cloudlet.removeOnCloudletFinishListener(null));
    }

    @Test
    public void testRemoveOnCloudletFinishEventListener() {
        EventListener<CloudletVmEventInfo> listener = (info) -> {};
        cloudlet.addOnCloudletFinishListener(listener);
        assertTrue(cloudlet.removeOnCloudletFinishListener(listener));
    }

    @Test
    public void testRemoveOnCloudletFinishEventListener_Null() {
        cloudlet.addOnCloudletFinishListener(null);
        assertFalse(cloudlet.removeOnCloudletFinishListener(null));
    }

    @Test
    public void testGetWaitingTime() {
        final double arrivalTime = 0.0, execStartTime = 10.0;
        final int datacenterId = 0;
        CloudSim cloudsim = CloudSimMocker.createMock(mocker -> {
            mocker.clock(arrivalTime);
            mocker.getEntityName(datacenterId);
        });

        CloudletSimple cloudlet = createCloudlet();
        cloudlet.setBroker(Mocks.createMockBroker(cloudsim));
        assertEquals(0, cloudlet.getWaitingTime(), 0);
        cloudlet.assignToDatacenter(Datacenter.NULL);
        final double expectedWaitingTime = execStartTime - arrivalTime;
        cloudlet.registerArrivalInDatacenter();
        cloudlet.setExecStartTime(execStartTime);
        assertEquals(expectedWaitingTime, cloudlet.getWaitingTime(), 0);
    }

    @Test
    public void testAssignCloudletToDataCenter_recodLogEnabledDatacenterNotAssigned() {
        CloudletSimple cloudlet = createCloudlet(0);
        cloudlet.setRecordTransactionHistory(true);
        cloudlet.assignToDatacenter(Datacenter.NULL);
        assertEquals(Datacenter.NULL, cloudlet.getLastDatacenter());
    }

    @Test
    public void testAssignCloudletToDataCenter_recodLogEnabledDatacenterAlreadAssigned() {
        CloudletSimple cloudlet = createCloudlet(0);
        cloudlet.setRecordTransactionHistory(true);
        cloudlet.assignToDatacenter(Datacenter.NULL);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        assertEquals(Datacenter.NULL, cloudlet.getLastDatacenter());
    }

    @Test
    public void testGetExecStartTime() {
        CloudletSimple cloudlet = createCloudlet();
        assertEquals(0, cloudlet.getExecStartTime(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        final int submissionTime = 0, execStartTime = 10;
        cloudlet.registerArrivalInDatacenter();
        cloudlet.setExecStartTime(execStartTime);
        assertEquals(execStartTime, cloudlet.getExecStartTime(), 0);
    }

    @Test
    public void testGetDatacenterArrivalTime() {
        final double submissionTime = 1;
        final int datacenterId = 0;
        CloudSim cloudsim = CloudSimMocker.createMock(mocker -> {
            mocker.clock(submissionTime);
            mocker.getEntityName(datacenterId);
        });

        CloudletSimple cloudlet = createCloudlet();
        cloudlet.setBroker(Mocks.createMockBroker(cloudsim));
        assertEquals(Cloudlet.NOT_ASSIGNED, cloudlet.getLastDatacenterArrivalTime(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        cloudlet.registerArrivalInDatacenter();
        assertEquals(submissionTime, cloudlet.getLastDatacenterArrivalTime(), 0);
    }

    @Test
    public void testGetWallClockTime() {
        CloudletSimple cloudlet = createCloudlet();
        assertEquals(0, cloudlet.getWallClockTimeInLastExecutedDatacenter(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        final double arrivalTime = 0.0, execStartTime = 10.0;
        CloudSimMocker.createMock(mocker -> mocker.clock(arrivalTime));

        cloudlet.registerArrivalInDatacenter();
        cloudlet.setExecStartTime(execStartTime);
        final double wallClockTime = execStartTime + 20.0;
        cloudlet.setWallClockTime(wallClockTime, wallClockTime);
        assertEquals(wallClockTime, cloudlet.getWallClockTimeInLastExecutedDatacenter(), 0);
    }


    @Test
    public void testGetActualCPUTime() {
        final double submissionTime = 0, execStartTime = 10;
        final double simulationClock = 100;
        final double actualCpuTime = simulationClock - execStartTime;
        final int datacenterId = 0;

        CloudSim cloudsim = CloudSimMocker.createMock(mocker -> {
            mocker.clock(submissionTime);
            mocker.clock(simulationClock);
        });

        CloudletSimple cloudlet = createCloudlet();
        cloudlet.setBroker(Mocks.createMockBroker(cloudsim));
        assertEquals(Cloudlet.NOT_ASSIGNED, cloudlet.getActualCpuTime(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        cloudlet.registerArrivalInDatacenter();
        cloudlet.setExecStartTime(execStartTime);
        cloudlet.setStatus(Cloudlet.Status.SUCCESS);
        assertEquals(actualCpuTime, cloudlet.getActualCpuTime(), 0);

        EasyMock.verify(cloudsim);
    }

    @Test
    public void testGetProcessingCost() {
        final double costPerCpuSec = 4, costPerByteOfBw = 2;

        Datacenter dc = DatacenterMocker.createMock(mocker -> {
            mocker.getCharacteristics().times(2);
            mocker.getCostPerSecond(costPerCpuSec).once();
            mocker.getCostPerBw(costPerByteOfBw).once();
        });

        Cloudlet cloudlet = createCloudlet(0, 10000, 2);
        final double inputTransferCost = CLOUDLET_FILE_SIZE * costPerByteOfBw;
        final double outputTransferCost = CLOUDLET_OUTPUT_SIZE * costPerByteOfBw;

        final double cpuCost = 40;

        final double totalCost = inputTransferCost + cpuCost + outputTransferCost;
        cloudlet.assignToDatacenter(dc);
        cloudlet.setWallClockTime(10, 10);
        assertEquals(totalCost, cloudlet.getTotalCost(), 0);
    }

    @Test
    public void testGetPriority() {
        final int expected = 8;
        cloudlet.setPriority(expected);
        assertEquals(expected, cloudlet.getPriority(), 0);
    }

    @Test
    public void testGetCloudletHistory() {
        final int id = 1;
        CloudletSimple cloudlet = createCloudlet(id);
        final String expected = String.format(Cloudlet.NO_HISTORY_IS_RECORDED_FOR_CLOUDLET, id);
        assertEquals(expected, cloudlet.getHistory());
        assertEquals(expected, cloudlet.getHistory());

        cloudlet = createCloudlet(id);
        cloudlet.setRecordTransactionHistory(true);
        Assert.assertNotSame(expected, cloudlet.getHistory());
    }

    @Test
    public void testSetCloudletFinishedSoFar() {
        CloudletSimple cloudlet = createCloudlet();
        assertEquals(0, cloudlet.getFinishedLengthSoFar(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        final long cloudletFinishedSoFar = cloudlet.getLength() / 2;
        assertTrue(cloudlet.setFinishedLengthSoFar(cloudletFinishedSoFar));
        assertEquals(cloudletFinishedSoFar, cloudlet.getFinishedLengthSoFar(), 0);
        assertFalse(cloudlet.setFinishedLengthSoFar(-1));
        assertEquals(cloudletFinishedSoFar, cloudlet.getFinishedLengthSoFar(), 0);
    }

    @Test
    public void testSetCloudletFinishedSoFar_lengthParamGreaterThanCloudletLength() {
        CloudletSimple cloudlet = createCloudlet();
        long expected = cloudlet.getLength();
        cloudlet.setFinishedLengthSoFar(expected*2);
        assertEquals(expected, cloudlet.getLength(), 0);
    }

    @Test
    public void testGetDatacenterId() {
        CloudletSimple cloudlet = createCloudlet(0);
        cloudlet.setRecordTransactionHistory(true);
        assertEquals(Datacenter.NULL, cloudlet.getLastDatacenter());

        cloudlet.assignToDatacenter(Datacenter.NULL);
        assertEquals(Datacenter.NULL, cloudlet.getLastDatacenter());
    }

    @Test
    public void testGetCostPerSec() {
        CloudletSimple cloudlet = createCloudlet();
        assertEquals(0, cloudlet.getCostPerSec(), 0);

        cloudlet.assignToDatacenter(Datacenter.NULL);
        assertEquals(0, cloudlet.getCostPerSec(), 0);
    }

    @Test
    public void testSetValidCloudletLength() {
        final int expected = 1000;
        cloudlet.setLength(expected);
        Assert.assertEquals(expected, cloudlet.getLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCloudletLengthToZero() {
        int expected = 1000;
        cloudlet.setLength(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCloudletLengthToNegative() {
        final int expected = 1000;
        cloudlet.setLength(-1);
    }

    @Test
    public void testSetValidNumberOfPes() {
        int expected = 2;
        cloudlet.setNumberOfPes(expected);
        Assert.assertEquals(expected, cloudlet.getNumberOfPes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumberOfPesToZero() {
        cloudlet.setNumberOfPes(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNumberOfPesToNegative() {
        cloudlet.setNumberOfPes(-1);
    }

    @Test
    public void testSetRequiredFiles() {
        cloudlet.setRequiredFiles(null);
        Assert.assertNotNull(cloudlet.getRequiredFiles());

        List<String> files = new ArrayList<>();
        files.add("file1.txt");
        cloudlet.setRequiredFiles(files);
        assertEquals(files, cloudlet.getRequiredFiles());
    }

    @Test
    public void testSetNetServiceLevel() {
        int valid = 1;
        assertTrue(
                "Cloudlet.setNetServiceLevel should return true",
                cloudlet.setNetServiceLevel(valid));
        assertEquals(valid, cloudlet.getNetServiceLevel());

        final int invalid0 = 0;
        assertFalse(
                "Cloudlet.setNetServiceLevel should return false",
                cloudlet.setNetServiceLevel(invalid0));
        assertEquals(valid, cloudlet.getNetServiceLevel());

        final int invalidNegative = -1;
        assertFalse(
                "Cloudlet.setNetServiceLevel should return false",
                cloudlet.setNetServiceLevel(invalidNegative));
        assertEquals(valid, cloudlet.getNetServiceLevel());

        valid = 2;
        assertTrue(
                "Cloudlet.setNetServiceLevel should return true",
                cloudlet.setNetServiceLevel(valid));
        assertEquals(valid, cloudlet.getNetServiceLevel());
    }

    private static CloudletSimple createCloudlet() {
        return createCloudlet(0);
    }

    private static CloudletSimple createCloudlet(final int id) {
        final UtilizationModel utilizationModel = new UtilizationModelFull();
        return createCloudlet(id, utilizationModel);
    }

    public static CloudletSimple createCloudlet(
            final int id, UtilizationModel cpuRamAndBwUtilizationModel) {
        return createCloudlet(id, cpuRamAndBwUtilizationModel,
                cpuRamAndBwUtilizationModel,
                cpuRamAndBwUtilizationModel);
    }

    private static CloudletSimple createCloudlet(final int id,
            UtilizationModel utilizationModelCPU,
            UtilizationModel utilizationModelRAM,
            UtilizationModel utilizationModelBW) {
        return createCloudlet(
                id, utilizationModelCPU, utilizationModelRAM, utilizationModelBW,
                CLOUDLET_LENGTH, 1);
    }

    public static CloudletSimple createCloudlet(final int id,
            UtilizationModel utilizationModelCPU,
            UtilizationModel utilizationModelRAM,
            UtilizationModel utilizationModelBW,
            long length, int numberOfPes) {
        CloudletSimple cloudlet = new CloudletSimple(id, length, numberOfPes);
        CloudSim cloudsim = CloudSimMocker.createMock(mocker -> {
            mocker.clock(0).anyTimes();
            mocker.getEntityName(EasyMock.anyInt()).anyTimes();
        });

        cloudlet
            .setFileSize(CLOUDLET_FILE_SIZE)
            .setOutputSize(CLOUDLET_OUTPUT_SIZE)
            .setUtilizationModelCpu(utilizationModelCPU)
            .setUtilizationModelRam(utilizationModelRAM)
            .setUtilizationModelBw(utilizationModelBW);
        cloudlet.setBroker(Mocks.createMockBroker(cloudsim));
        return cloudlet;
    }

    public static CloudletSimple createCloudletWithOnePe(final int id) {
        return createCloudlet(id, CLOUDLET_LENGTH, 1);
    }

    public static CloudletSimple createCloudletWithOnePe(
            final int id, long length) {
        return createCloudlet(id, length, 1);
    }

    public static CloudletSimple createCloudlet(
            final int id, long length, int numberOfPes) {
        final UtilizationModel utilizationModel = new UtilizationModelFull();
        return createCloudlet(id, utilizationModel, utilizationModel, utilizationModel, length, numberOfPes);
    }

    public static CloudletSimple createCloudlet(
            final int id, int numberOfPes) {
        return createCloudlet(id, CLOUDLET_LENGTH, numberOfPes);
    }

    /**
     * Creates a Cloudlet with id equals to 0.
     *
     * @param length the length of the Cloudlet to create
     * @param numberOfPes the number of PEs of the Cloudlet to create
     * @return the created Cloudlet
     */
    public static CloudletSimple createCloudlet0(
            long length, int numberOfPes) {
        return createCloudlet(0, length, numberOfPes);
    }

    @Test
    public void testSetUtilizationModels() {
        CloudletSimple c = createCloudlet();
        Assert.assertNotNull(c.getUtilizationModelCpu());
        Assert.assertNotNull(c.getUtilizationModelRam());
        Assert.assertNotNull(c.getUtilizationModelBw());
    }

    public void testSetUtilizationModelBw_null() {
        CloudletSimple c = createCloudlet();
        c.setUtilizationModelBw(null);
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelBw());
    }

    public void testSetUtilizationModelRam_null() {
        CloudletSimple c = createCloudlet();
        c.setUtilizationModelRam(null);
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelRam());
    }

    public void testSetUtilizationModelCpu_null() {
        CloudletSimple c = createCloudlet();
        c.setUtilizationModelCpu(null);
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelCpu());
    }

    public void testNew_nullUtilizationModel() {
        CloudletSimple c = createCloudlet(0, null);
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelBw());
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelRam());
        assertEquals(UtilizationModel.NULL, c.getUtilizationModelCpu());
    }

    @Test
    public void testSetExecParam() {
        CloudletSimple c = createCloudlet();

        //Cloudlet has not assigned to a datacenter yet
        assertFalse(c.setWallClockTime(1, 2));

        //Assign cloudlet to a datacenter
        c.assignToDatacenter(Datacenter.NULL);

        assertTrue(c.setWallClockTime(1, 2));
    }

    @Test
    public void testSetCloudletStatus() {
        CloudletSimple c = createCloudlet();
        c.setStatus(CloudletSimple.Status.INSTANTIATED);
        //The status is the same of the current cloudlet status (the request has not effect)
        assertFalse(c.setStatus(CloudletSimple.Status.INSTANTIATED));

        //Actually changing to a new status
        assertTrue(c.setStatus(CloudletSimple.Status.QUEUED));

        final CloudletSimple.Status newStatus = CloudletSimple.Status.CANCELED;
        assertTrue(c.setStatus(newStatus));
        assertEquals(newStatus, c.getStatus());

        //Trying to change to the same current status (the request has not effect)
        assertFalse(c.setStatus(newStatus));
    }

    @Test
    public void testAddRequiredFile() {
        CloudletSimple c = createCloudlet();
        final String files[] = {"file1.txt", "file2.txt"};
        for (String file : files) {
            assertTrue("Method file should be added",
                    c.addRequiredFile(file));  //file doesn't previously added
            assertFalse("Method file shouldn't be added",
                    c.addRequiredFile(file)); //file already added
        }
    }

    @Test
    public void testDeleteRequiredFile() {
        CloudletSimple c = createCloudlet();
        final String files[] = {"file1.txt", "file2.txt", "file3.txt"};
        for (String file : files) {
            c.addRequiredFile(file);
        }

        assertFalse(c.deleteRequiredFile("file-inexistent.txt"));
        for (String file : files) {
            assertTrue(c.deleteRequiredFile(file));
            assertFalse(c.deleteRequiredFile(file)); //already deleted
        }
    }

    @Test
    public void testRequiredFiles() {
        CloudletSimple c = createCloudlet();
        final String files[] = {"file1.txt", "file2.txt", "file3.txt"};
        c.setRequiredFiles(null); //internally it has to creates a new instance
        Assert.assertNotNull(c.getRequiredFiles());

        for (String file : files) {
            c.addRequiredFile(file);
        }

        assertTrue(c.requiresFiles()); //it has required files
    }

    @Test
    public void testGetCloudletFinishedSoFar() {
        final long length = 1000;
        CloudletSimple c = createCloudlet();

        assertEquals(0, c.getFinishedLengthSoFar());

        c.assignToDatacenter(Datacenter.NULL);
        final long finishedSoFar = length / 10;
        c.setFinishedLengthSoFar(finishedSoFar);
        assertEquals(finishedSoFar, c.getFinishedLengthSoFar());

        c.setFinishedLengthSoFar(length);
        assertEquals(length, c.getFinishedLengthSoFar());
    }

    @Test
    public void testIsFinished() {
        final long length = 1000;
        CloudletSimple c = createCloudlet();

        assertFalse(c.isFinished());

        c.assignToDatacenter(Datacenter.NULL);
        final long finishedSoFar = length / 10;
        c.setFinishedLengthSoFar(finishedSoFar);
        assertFalse(c.isFinished());

        c.setFinishedLengthSoFar(length);
        assertTrue(c.isFinished());
    }

    @Test
    public void testSetPriority() {
        final int zero = 0;
	    cloudlet.setPriority(zero);
        Assert.assertEquals(zero, cloudlet.getPriority());

        final int negative = -1;
        cloudlet.setPriority(negative);
	    Assert.assertEquals(negative, cloudlet.getPriority());

        final int one = 1;
        cloudlet.setPriority(one);
	    Assert.assertEquals(one, cloudlet.getPriority());
    }

    @Test
    public void testGetUtilizationOfCpu() {
        assertEquals(utilizationModelCpu.getUtilization(0), cloudlet.getUtilizationOfCpu(0), 0);
    }

    @Test
    public void testGetUtilizationOfRam() {
        assertEquals(utilizationModelRam.getUtilization(0), cloudlet.getUtilizationOfRam(0), 0);
    }

    @Test
    public void testGetUtilizationOfBw() {
        assertEquals(utilizationModelBw.getUtilization(0), cloudlet.getUtilizationOfBw(0), 0);
    }

}

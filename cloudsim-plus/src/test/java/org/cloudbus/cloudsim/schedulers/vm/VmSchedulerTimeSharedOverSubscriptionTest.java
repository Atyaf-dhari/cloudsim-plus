/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.schedulers.vm;

import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudbus.cloudsim.vms.VmSimpleTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class VmSchedulerTimeSharedOverSubscriptionTest {

    private static final double MIPS = 1000;
    private VmSchedulerTimeSharedOverSubscription vmScheduler;
    private Vm vm1;
    private Vm vm2;

    @Before
    public void setUp() throws Exception {
        vmScheduler = createVmScheduler(MIPS, 2);
        vm1 = VmSimpleTest.createVm(0, MIPS / 4, 1);
        vm2 = VmSimpleTest.createVm(1, MIPS / 2, 2);
    }

    private VmSchedulerTimeSharedOverSubscription createVmScheduler(double mips, int pesNumber) {
        List<Pe> peList = new ArrayList<>(pesNumber);
        IntStream.range(0, pesNumber).forEach(i -> peList.add(new PeSimple(i, new PeProvisionerSimple(mips))));
        Host host = new HostSimple(1, 1000, peList);
        VmSchedulerTimeSharedOverSubscription scheduler = new VmSchedulerTimeSharedOverSubscription();
        scheduler.setHost(host);
        return scheduler;
    }

    @Test
    public void testInit() {
        List<Pe> peList = vmScheduler.getHost().getPeList();
        assertSame(peList, vmScheduler.getPeList());
        assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
        assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
        assertEquals(0, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);
    }

    @Test
    public void testAllocatePesForVm() {
        List<Double> mipsShare1 = new ArrayList<>();
        mipsShare1.add(MIPS / 4);

        assertTrue(vmScheduler.allocatePesForVm(vm1, mipsShare1));
        List<Pe> peList = vmScheduler.getHost().getPeList();
        assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getAvailableMips(), 0);
        assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getMaxAvailableMips(), 0);
        assertEquals(MIPS / 4, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);

        List<Double> mipsShare2 = new ArrayList<>();
        mipsShare2.add(MIPS / 2);
        mipsShare2.add(MIPS / 8);

        assertTrue(vmScheduler.allocatePesForVm(vm2, mipsShare2));

        assertEquals(
            PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
            vmScheduler.getAvailableMips(),
            0);
        assertEquals(
            PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
            vmScheduler.getMaxAvailableMips(),
            0);
        assertEquals(MIPS / 2 + MIPS / 8, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);

        vmScheduler.deallocatePesForAllVms();

        assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
        assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
        assertEquals(0, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);
    }

    @Test
    public void testAllocatePesForVmInMigration() {
        vm1.setInMigration(true);
        vm2.setInMigration(true);

        List<Double> mipsShare1 = new ArrayList<>();
        mipsShare1.add(MIPS / 4);

        assertTrue(vmScheduler.allocatePesForVm(vm1, mipsShare1));
        List<Pe> peList = vmScheduler.getHost().getPeList();
        assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getAvailableMips(), 0);
        assertEquals(PeList.getTotalMips(peList) - MIPS / 4, vmScheduler.getMaxAvailableMips(), 0);
        assertEquals(0.9 * MIPS / 4, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);

        List<Double> mipsShare2 = new ArrayList<>();
        mipsShare2.add(MIPS / 2);
        mipsShare2.add(MIPS / 8);

        assertTrue(vmScheduler.allocatePesForVm(vm2, mipsShare2));

        assertEquals(
            PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
            vmScheduler.getAvailableMips(),
            0);
        assertEquals(
            PeList.getTotalMips(peList) - MIPS / 4 - MIPS / 2 - MIPS / 8,
            vmScheduler.getMaxAvailableMips(),
            0);
        assertEquals(0.9 * MIPS / 2 + 0.9 * MIPS / 8, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);

        vmScheduler.deallocatePesForAllVms();

        assertEquals(PeList.getTotalMips(peList), vmScheduler.getAvailableMips(), 0);
        assertEquals(PeList.getTotalMips(peList), vmScheduler.getMaxAvailableMips(), 0);
        assertEquals(0, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);
    }

    @Test
    public void testAllocatePesForVmShortageEqualsToAllocatedMips() {
        VmSchedulerAbstract vmScheduler = createVmScheduler(3500, 1);
        Vm vm1 = VmSimpleTest.createVm(0, 170, 1);
        Vm vm2 = VmSimpleTest.createVm(1, 2000, 1);
        Vm vm3 = VmSimpleTest.createVm(2, 10, 1);
        Vm vm4 = VmSimpleTest.createVm(3, 2000, 1);

        List<Double> mipsShare1 = new ArrayList<>();
        mipsShare1.add(170.0);

        List<Double> mipsShare2 = new ArrayList<>();
        mipsShare2.add(2000.0);

        List<Double> mipsShare3 = new ArrayList<>();
        mipsShare3.add(10.0);

        List<Double> mipsShare4 = new ArrayList<>();
        mipsShare4.add(2000.0);

        assertTrue(vmScheduler.allocatePesForVm(vm1, mipsShare1));
        assertEquals(3330, vmScheduler.getAvailableMips(), 0);
        assertEquals(170, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);

        assertTrue(vmScheduler.allocatePesForVm(vm2, mipsShare2));
        assertEquals(1330, vmScheduler.getAvailableMips(), 0);
        assertEquals(2000, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);

        assertTrue(vmScheduler.allocatePesForVm(vm3, mipsShare3));
        assertEquals(1320, vmScheduler.getAvailableMips(), 0);
        assertEquals(10, vmScheduler.getTotalAllocatedMipsForVm(vm3), 0);

        assertTrue(vmScheduler.allocatePesForVm(vm4, mipsShare4));
        assertEquals(0, vmScheduler.getAvailableMips(), 0);
        assertEquals(1674, vmScheduler.getTotalAllocatedMipsForVm(vm4), 0);

        vmScheduler.deallocatePesForAllVms();

        assertEquals(3500, vmScheduler.getAvailableMips(), 0);
        assertEquals(3500, vmScheduler.getMaxAvailableMips(), 0);
    }

    @Test
    public void testAllocatePesForSameSizedVmsOversubscribed() {
        VmSchedulerAbstract vmScheduler = createVmScheduler(MIPS, 1);

        VmSimple vm1 = VmSimpleTest.createVm(0, 1500, 1);
        VmSimple vm2 = VmSimpleTest.createVm(1, 1000, 1);
        VmSimple vm3 = VmSimpleTest.createVm(2, 1000, 1);

        List<Double> mipsShare1 = new ArrayList<>();
        mipsShare1.add(1500.0);

        List<Double> mipsShare2 = new ArrayList<>();
        mipsShare2.add(1000.0);

        List<Double> mipsShare3 = new ArrayList<>();
        mipsShare3.add(1000.0);

        assertTrue(vmScheduler.allocatePesForVm(vm1, mipsShare1));
        assertEquals(0, vmScheduler.getAvailableMips(), 0);
        assertEquals(1000, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);

        assertTrue(vmScheduler.allocatePesForVm(vm2, mipsShare2));
        assertEquals(0, vmScheduler.getAvailableMips(), 0);
        assertEquals(500, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);
        assertEquals(500, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);

        assertTrue(vmScheduler.allocatePesForVm(vm3, mipsShare3));
        assertEquals(0, vmScheduler.getAvailableMips(), 0);
        assertEquals(333, vmScheduler.getTotalAllocatedMipsForVm(vm1), 0);
        assertEquals(333, vmScheduler.getTotalAllocatedMipsForVm(vm2), 0);
        assertEquals(333, vmScheduler.getTotalAllocatedMipsForVm(vm3), 0);

        vmScheduler.deallocatePesForAllVms();

        assertEquals(1000, vmScheduler.getAvailableMips(), 0);
        assertEquals(1000, vmScheduler.getMaxAvailableMips(), 0);
    }

}

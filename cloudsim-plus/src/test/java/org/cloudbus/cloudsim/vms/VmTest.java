package org.cloudbus.cloudsim.vms;

import java.util.Collections;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudsimplus.listeners.EventListener;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.junit.Test;
import static org.junit.Assert.*;
import org.cloudbus.cloudsim.resources.ResourceManageable;

/**
 *
 * @author Manoel Campos da Silva Filho
 */
public class VmTest {
    @Test
    public void testNullObject(){
        final Vm instance = Vm.NULL;

        instance.addStateHistoryEntry(null);
        assertTrue(instance.getStateHistory().isEmpty());

        assertEquals(CloudletScheduler.NULL, instance.getCloudletScheduler());

        assertEquals(0, instance.getCurrentAllocatedSize());
        assertEquals(0, instance.getCurrentRequestedMaxMips(), 0);
        assertEquals(0, instance.getCurrentRequestedTotalMips(), 0);
        assertEquals(0, instance.getMips(), 0);
        assertEquals(0, instance.getNumberOfPes());

        assertEquals(ResourceManageable.NULL, instance.getResource(null));

        assertEquals(0, instance.getTotalUtilizationOfCpu(0), 0);
        assertEquals(0, instance.getTotalUtilizationOfCpuMips(0), 0);

        instance.setInMigration(true);
        assertFalse(instance.isInMigration());

        assertFalse(instance.isCreated());

        instance.setBw(1000);
        assertEquals(0, instance.getBw());

        assertEquals(0, instance.getCurrentAllocatedBw());
        assertEquals(0, instance.getCurrentRequestedBw());

        assertTrue(instance.getCurrentRequestedMips().isEmpty());

        assertEquals(0, instance.getCurrentAllocatedRam());
        assertEquals(0, instance.getCurrentRequestedRam());

        assertEquals(Host.NULL, instance.getHost());

        instance.addOnHostAllocationListener(null);
        assertFalse(instance.removeOnHostAllocationListener(null));

        instance.addOnHostDeallocationListener(null);
        assertFalse(instance.removeOnHostDeallocationListener(null));

        instance.addOnVmCreationFailureListener(null);
        assertFalse(instance.removeOnVmCreationFailureListener(null));

        instance.addOnUpdateVmProcessingListener(null);
        assertFalse(instance.removeOnUpdateVmProcessingListener(null));

        instance.setRam(1000);
        assertEquals(0, instance.getRam());

        instance.setSize(1000);
        assertEquals(0, instance.getSize());

        assertEquals("", instance.getUid());
        assertEquals(-1, instance.getId());
        assertSame(DatacenterBroker.NULL, instance.getBroker());
        assertEquals("", instance.getVmm());

        assertEquals(0, instance.updateVmProcessing(0, Collections.EMPTY_LIST), 0);
    }

}

package org.cloudbus.cloudsim.schedulers.vm;

import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Manoel Campos da Silva Filho
 */
public class VmSchedulerTest {

    @Test
    public void testNullObject() {
        final VmScheduler instance = VmScheduler.NULL;
        instance.deallocatePesForAllVms();
        instance.deallocatePesForVm(null);
        assertFalse(instance.allocatePesForVm(null, null));
        assertTrue(instance.getAllocatedMipsForVm(null).isEmpty());
        assertEquals(0, instance.getAvailableMips(), 0);
        assertFalse(instance.isSuitableForVm(null));
        assertEquals(0, instance.getMaxAvailableMips(), 0);
        assertEquals(0, instance.getPeCapacity(), 0);
        assertTrue(instance.getPeList().isEmpty());
        assertTrue(instance.getPeMap().isEmpty());
        assertTrue(instance.getPesAllocatedForVM(null).isEmpty());
        assertEquals(0, instance.getTotalAllocatedMipsForVm(null), 0);
        assertTrue(instance.getVmsMigratingIn().isEmpty());
        assertTrue(instance.getVmsMigratingOut().isEmpty());
        assertEquals(0, instance.getCpuOverheadDueToVmMigration(), 0);
    }

}

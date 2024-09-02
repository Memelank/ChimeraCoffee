package com.chimera.weapp.statemachine;

import com.chimera.weapp.statemachine.engine.DefaultStateProcessRegistry;
import com.chimera.weapp.statemachine.enums.BizEnum;
import com.chimera.weapp.statemachine.enums.OrderEventEnum;
import com.chimera.weapp.statemachine.enums.OrderStateEnum;
import com.chimera.weapp.statemachine.enums.SceneEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultStateProcessRegistryTest {
    @Autowired
    private DefaultStateProcessRegistry defaultStateProcessRegistry;

    @Test
    public void registry_AfterApplicationStart_CanMaintainMap() {
        Assert.assertEquals(defaultStateProcessRegistry.acquireStateProcess(OrderStateEnum.TO_BE_INIT.toString(),
                OrderEventEnum.INIT.toString(), BizEnum.FOR_TSINGHUA_STUDENT.toString(), SceneEnum.Common.toString()).size(), 1);

    }
}

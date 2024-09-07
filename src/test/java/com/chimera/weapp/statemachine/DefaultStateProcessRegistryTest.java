package com.chimera.weapp.statemachine;

import com.chimera.weapp.statemachine.engine.DefaultOrderFsmEngine;
import com.chimera.weapp.statemachine.enums.CustomerTypeEnum;
import com.chimera.weapp.statemachine.enums.EventEnum;
import com.chimera.weapp.statemachine.enums.StateEnum;
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
    private DefaultOrderFsmEngine defaultOrderFsmEngine;

    @Test
    public void registry_AfterApplicationStart_CanMaintainMap() {
        Assert.assertEquals(1, defaultOrderFsmEngine.acquireStateProcessor(StateEnum.PAID.toString(),
                EventEnum.NEED_DINE_IN.toString(), CustomerTypeEnum.FOR_TSINGHUA_STUDENT.toString(), SceneEnum.DINE_IN.toString()).size());

    }
}

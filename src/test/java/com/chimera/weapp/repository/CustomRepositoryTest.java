package com.chimera.weapp.repository;

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

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomRepositoryTest {
    @Autowired
    private CustomRepository customRepository;

    @Test
    public void pmrepo_DIYAPI_Works() {
        List<Integer> processorIds = customRepository.findProcessorIds(OrderStateEnum.TO_BE_INIT.toString(),
                OrderEventEnum.INIT.toString(), BizEnum.FOR_TSINGHUA_STUDENT.toString(), SceneEnum.Common.toString());
        Assert.assertEquals(processorIds.size(), 1);
    }
}

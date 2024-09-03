package com.chimera.weapp.statemachine;


import com.chimera.weapp.statemachine.engine.OrderFsmEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultOrderFsmEngineTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderFsmEngineTest.class);

    @Autowired
    private OrderFsmEngine orderFsmEngine;


}
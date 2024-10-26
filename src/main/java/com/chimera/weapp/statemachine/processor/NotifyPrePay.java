package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.NotifyPrePayContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Processor(processorId = 0)
@Component
public class NotifyPrePay extends AbstractStateProcessor<String, NotifyPrePayContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean filter(StateContext<NotifyPrePayContext> context) {
        return true;
    }


    @Override
    public ServiceResult<String, NotifyPrePayContext> actionStep(StateContext<NotifyPrePayContext> context) throws Exception {
        Transaction transaction = context.getContext().getTransaction();
        if (transaction != null) { //兼容商户端下单，商户端下单不检查transaction
            ServiceResult<String, NotifyPrePayContext> emptySuccess = new ServiceResult<>();
            emptySuccess.setSuccess(true);
            return emptySuccess;// 因为要让走到持久化那一步，且不更改用户的积分
        }

        User user = userRepository.findById(new ObjectId(context.getUserId())).orElseThrow();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        int totalPrice = order.getTotalPrice();
        user.setPoints(user.getPoints() + totalPrice / 100);
        userRepository.save(user);

        ServiceResult<String, NotifyPrePayContext> result = new ServiceResult<>();
        result.setContext(context.getContext());
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public StateEnum getNextState(StateContext<NotifyPrePayContext> context) {
        Transaction transaction = context.getContext().getTransaction();
        if (transaction != null) { //非空再往下走是为了兼容商户端下单，商户端下单不检查transaction
            Transaction.TradeStateEnum tradeState = transaction.getTradeState();
            if (!Objects.equals(tradeState, Transaction.TradeStateEnum.SUCCESS)) {
                log.warn("微信系统返回的支付通知显示结果并非成功,需要客户和店员另想办法了，context:{}", context);
                return StateEnum.ABNORMAL_END;
            }
        }
        return StateEnum.PAID;

    }

    @Override
    public ServiceResult<String, NotifyPrePayContext> save(String nextState, StateContext<NotifyPrePayContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<NotifyPrePayContext> context) {

    }
}

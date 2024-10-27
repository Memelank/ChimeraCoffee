package com.chimera.weapp.statemachine.processor;

import com.chimera.weapp.config.WebSocketConfig;
import com.chimera.weapp.entity.Order;
import com.chimera.weapp.entity.User;
import com.chimera.weapp.repository.CustomRepository;
import com.chimera.weapp.repository.OrderRepository;
import com.chimera.weapp.repository.UserRepository;
import com.chimera.weapp.service.WeChatNoticeService;
import com.chimera.weapp.statemachine.annotation.processor.Processor;
import com.chimera.weapp.statemachine.context.NotifyRefundResultContext;
import com.chimera.weapp.statemachine.context.StateContext;
import com.chimera.weapp.statemachine.enums.StateEnum;
import com.chimera.weapp.statemachine.vo.ServiceResult;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Processor(processorId = 11)
@Component
public class RefundCallBack extends AbstractStateProcessor<String, NotifyRefundResultContext> {
    @Autowired
    private CustomRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private WeChatNoticeService weChatNoticeService;
    @Autowired
    private WebSocketConfig webSocketConfig;

    @Override
    public boolean filter(StateContext<NotifyRefundResultContext> context) {
        return true;
    }

    @Override
    public ServiceResult<String, NotifyRefundResultContext> actionStep(StateContext<NotifyRefundResultContext> context) throws Exception {
        if (refundIsNotSucceed(context)) {
            ServiceResult<String, NotifyRefundResultContext> emptySuccess = new ServiceResult<>();
            emptySuccess.setSuccess(true);
            return emptySuccess;// 因为要让走到持久化那一步，且不更改用户的积分
        }
        User user = userRepository.findById(new ObjectId(context.getUserId())).orElseThrow();
        Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
        int totalPrice = order.getTotalPrice();
        user.setPoints(user.getPoints() - totalPrice / 100);
        userRepository.save(user);

        ServiceResult<String, NotifyRefundResultContext> result = new ServiceResult<>();
        NotifyRefundResultContext refundResultContext = context.getContext();

        result.setContext(refundResultContext);
        result.setMsg("success");
        result.setSuccess(true);
        return result;
    }

    @Override
    public StateEnum getNextState(StateContext<NotifyRefundResultContext> context) {
        if (refundIsNotSucceed(context)) {
            log.warn("微信系统返回的退款通知显示结果并非成功,当前还不能处理这种情况,需要客户和店员另想办法了，context:{}", context);
            return StateEnum.ABNORMAL_END;
        }
        return StateEnum.REFUNDED;
    }

    @Override
    public ServiceResult<String, NotifyRefundResultContext> save(String nextState, StateContext<NotifyRefundResultContext> context) throws Exception {
        repository.updateOrderStateById(context.getOrderId(), nextState);
        return new ServiceResult<>(context.getOrderId(), context.getContext(), "", true);
    }

    @Override
    public void after(StateContext<NotifyRefundResultContext> context) {
        if (Objects.equals(context.getOrderState(), StateEnum.REFUNDED.toString())) {
            Order order = orderRepository.findById(new ObjectId(context.getOrderId())).orElseThrow();
            weChatNoticeService.refundNotice(context.getOrderId(), order.getRefundReason());
        } else if (Objects.equals(context.getOrderState(), StateEnum.ABNORMAL_END.toString())) {
            webSocketConfig.getOrderEndWebSocketHandler().sendOrderId(context.getOrderId());
        }
    }

    private boolean refundIsNotSucceed(StateContext<NotifyRefundResultContext> context) {
        RefundNotification refundNotification = context.getContext().getRefundNotification();
        Status refundStatus = refundNotification.getRefundStatus();
        return !Objects.equals(refundStatus, Status.SUCCESS);//todo 当遇到异常结束订单时，需要有通知店员的机制
    }
}

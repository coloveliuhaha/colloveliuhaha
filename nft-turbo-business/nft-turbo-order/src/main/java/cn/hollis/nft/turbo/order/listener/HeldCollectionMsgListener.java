package cn.hollis.nft.turbo.order.listener;

import cn.hollis.nft.turbo.api.collection.constant.HeldCollectionState;
import cn.hollis.nft.turbo.api.collection.model.HeldCollectionDTO;
import cn.hollis.nft.turbo.api.order.request.OrderFinishRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.api.user.constant.UserType;
import cn.hollis.nft.turbo.order.domain.service.OrderManageService;
import cn.hollis.turbo.stream.param.MessageBody;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Hollis
 */
@Component
@Slf4j
public class HeldCollectionMsgListener {

    @Autowired
    private OrderManageService orderManageService;

    @Bean
    Consumer<Message<MessageBody>> heldCollection() {
        return msg -> {
            String messageId = msg.getHeaders().get("ROCKET_MQ_MESSAGE_ID", String.class);
            String tag = msg.getHeaders().get("ROCKET_TAGS", String.class);
            HeldCollectionDTO heldCollectionDTO = JSON.parseObject(msg.getPayload().getBody(), HeldCollectionDTO.class);
            log.info("messageId:{},heldCollectionDTO:{}，tag:{}", messageId, heldCollectionDTO, tag);

            if (heldCollectionDTO.getState().equals(HeldCollectionState.ACTIVED.name())) {
                String orderId = heldCollectionDTO.getBizNo();
                OrderFinishRequest orderFinishRequest = new OrderFinishRequest();
                orderFinishRequest.setIdentifier("order_confirm_" + heldCollectionDTO.getId());
                orderFinishRequest.setOrderId(orderId);
                orderFinishRequest.setOperator(UserType.PLATFORM.name());
                orderFinishRequest.setOperatorType(UserType.PLATFORM);
                orderFinishRequest.setOperateTime(new Date());
                OrderResponse orderResponse = orderManageService.finish(orderFinishRequest);
                Assert.isTrue(orderResponse.getSuccess(), "finish order failed");
            }

        };

    }
}

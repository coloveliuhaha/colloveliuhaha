package pay;

import cn.hollis.nft.turbo.api.common.constant.BizOrderType;
import cn.hollis.nft.turbo.api.pay.constant.PayChannel;
import cn.hollis.nft.turbo.api.pay.request.PayCreateRequest;
import cn.hollis.nft.turbo.api.user.constant.UserType;
import cn.hollis.nft.turbo.pay.domain.entity.PayOrder;
import cn.hollis.nft.turbo.pay.domain.service.PayOrderService;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.PayChannelRequest;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.RefundChannelRequest;
import cn.hollis.nft.turbo.pay.infrastructure.channel.wechat.response.WxPayChannelResponse;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelService;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelServiceFactory;
import cn.hollis.nft.turbo.pay.infrastructure.channel.wechat.response.WxRefundChannelResponse;
import com.ijpay.core.kit.PayKit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;

public class WXpayServiceTest extends PayBaseTest {


    @Autowired
    private PayChannelServiceFactory payChannelServiceFactory;
    @Autowired
    private PayOrderService payOrderService;

    @Before
    public void init() {
        PayCreateRequest payCreateRequest=new PayCreateRequest();
        payCreateRequest.setPayChannel(PayChannel.WECHAT);
        payCreateRequest.setMemo("测试藏品11");
        payCreateRequest.setBizNo("1017990604178765619200003");
        payCreateRequest.setPayeeId("0");
        payCreateRequest.setPayeeType(UserType.PLATFORM);
        payCreateRequest.setPayerId("29");
        payCreateRequest.setPayerType(UserType.CUSTOMER);
        payCreateRequest.setOrderAmount(new BigDecimal("0.02"));
        payCreateRequest.setBizType(BizOrderType.TRADE_ORDER);
        PayOrder payOrder=payOrderService.create(payCreateRequest);
        payOrder.setPayOrderId("1799060424671358976");
        payOrder.setChannelStreamId("4200002325202406071682620038");
        payOrderService.updateById(payOrder);
    }

    @Test
    public void wxPayTest(){
        PayChannelService wxPayChannelService = payChannelServiceFactory.get(PayChannel.WECHAT);
        PayChannelRequest payChannelRequest =new PayChannelRequest();
        payChannelRequest.setOrderId(PayKit.generateStr());
        payChannelRequest.setAmount(10L);
        payChannelRequest.setDescription("支付测试");
        payChannelRequest.setAttach("支付测试");
        WxPayChannelResponse response= (WxPayChannelResponse) wxPayChannelService.pay(payChannelRequest);
        System.out.println(response.getPayUrl());
        Assert.assertTrue(response.getSuccess());
    }

    @Test
    public void wxRefundTest(){
        PayChannelService wxPayChannelService = payChannelServiceFactory.get(PayChannel.WECHAT);
        RefundChannelRequest refundChannelRequest=new RefundChannelRequest();
        refundChannelRequest.setRefundOrderId(PayKit.generateStr());
//        refundChannelRequest.setCollectionName("支付测试");
        refundChannelRequest.setRefundAmount(2L);
//        refundChannelRequest.setRefundQuality(1);
        refundChannelRequest.setPayOrderId("1799060424671358976");
        refundChannelRequest.setPaidAmount(2L);
        refundChannelRequest.setRefundReason("支付测试");
        WxRefundChannelResponse response = (WxRefundChannelResponse) wxPayChannelService.refund(refundChannelRequest);

    }


}

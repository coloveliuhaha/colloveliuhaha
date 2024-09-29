package cn.hollis.nft.turbo.order.domain.service;

import cn.hollis.nft.turbo.api.order.constant.TradeOrderState;
import cn.hollis.nft.turbo.order.domain.OrderBaseTest;
import cn.hollis.nft.turbo.order.domain.entity.TradeOrder;
import cn.hollis.nft.turbo.order.infrastructure.mapper.OrderMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author Hollis
 */
public class OrderReadServiceTest extends OrderBaseTest {
    @Autowired
    OrderReadService orderService;

    @Autowired
    OrderMapper orderMapper;


    @Test
    public void testGetTimeoutOrderByPage() {
        //状态不满足要求
        //时间不满足要求
        for (int i = 0; i < 1; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            orderMapper.insert(tradeOrder);
        }

        //时间不满足要求
        for (int i = 0; i < 1; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setOrderState(TradeOrderState.CONFIRM);
            orderMapper.insert(tradeOrder);
        }

        //时间和状态都满足要求
        for (int i = 0; i < 2; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setGmtCreate(DateUtils.addMinutes(new Date(), -31));
            tradeOrder.setOrderState(TradeOrderState.CONFIRM);
            orderMapper.insert(tradeOrder);
        }

        //状态不满足要求
        for (int i = 0; i < 2; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setGmtCreate(DateUtils.addMinutes(new Date(), -31));
            tradeOrder.setOrderState(TradeOrderState.PAID);
            orderMapper.insert(tradeOrder);
        }

        Page<TradeOrder> page = orderService.pageQueryTimeoutOrders(1, 1,null);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(page.getTotal(),2);
        Assert.assertEquals(page.getRecords().size(),1);

        page = orderService.pageQueryTimeoutOrders(2, 1,null);
        Assert.assertFalse(page.hasNext());
        Assert.assertEquals(page.getTotal(),2);
        Assert.assertEquals(page.getRecords().size(),1);

        page = orderService.pageQueryTimeoutOrders(1, 4,null);
        Assert.assertFalse(page.hasNext());
        Assert.assertEquals(page.getTotal(),2);
        Assert.assertEquals(page.getRecords().size(),2);
    }


    @Test
    public void testPageQueryNeedConfirmOrders() {

        for (int i = 0; i < 1; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            orderMapper.insert(tradeOrder);
        }

        for (int i = 0; i < 1; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setOrderState(TradeOrderState.CONFIRM);
            orderMapper.insert(tradeOrder);
        }

        for (int i = 0; i < 2; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setOrderState(TradeOrderState.PAID);
            orderMapper.insert(tradeOrder);
        }

        for (int i = 0; i < 2; i++) {
            TradeOrder tradeOrder = TradeOrder.createOrder(orderCreateRequest());
            tradeOrder.setGmtCreate(DateUtils.addMinutes(new Date(), -31));
            tradeOrder.setOrderState(TradeOrderState.PAID);
            tradeOrder.setOrderConfirmedTime(new Date());
            orderMapper.insert(tradeOrder);
        }

        Page<TradeOrder> page = orderService.pageQueryNeedConfirmOrders(1, 1,null);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(page.getTotal(),4);
        Assert.assertEquals(page.getRecords().size(),1);

        page = orderService.pageQueryNeedConfirmOrders(2, 1,null);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(page.getTotal(),4);
        Assert.assertEquals(page.getRecords().size(),1);

        page = orderService.pageQueryNeedConfirmOrders(1, 4,null);
        Assert.assertFalse(page.hasNext());
        Assert.assertEquals(page.getTotal(),4);
        Assert.assertEquals(page.getRecords().size(),4);
    }
}
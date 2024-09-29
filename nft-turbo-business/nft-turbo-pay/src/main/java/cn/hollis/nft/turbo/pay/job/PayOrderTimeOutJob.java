package cn.hollis.nft.turbo.pay.job;

import cn.hollis.nft.turbo.pay.domain.entity.PayOrder;
import cn.hollis.nft.turbo.pay.domain.service.PayOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Hollis
 */
@Component
public class PayOrderTimeOutJob {

    @Autowired
    private PayOrderService payOrderService;

    private static final int PAGE_SIZE = 100;

    private static final Logger LOG = LoggerFactory.getLogger(PayOrderTimeOutJob.class);

    @XxlJob("payTimeOutExecute")
    public ReturnT<String> execute() {

        int currentPage = 1;
        Page<PayOrder> page = payOrderService.pageQueryTimeoutOrders(currentPage, PAGE_SIZE);

        page.getRecords().forEach(this::executeSingle);

        while (page.hasNext()) {
            currentPage++;
            page = payOrderService.pageQueryTimeoutOrders(currentPage, PAGE_SIZE);
            page.getRecords().forEach(this::executeSingle);
        }

        return ReturnT.SUCCESS;
    }

    private void executeSingle(PayOrder payOrder) {
        LOG.info("start to execute order timeout , orderId is {}", payOrder.getPayOrderId());
        payOrderService.payExpired(payOrder.getPayOrderId());
    }
}

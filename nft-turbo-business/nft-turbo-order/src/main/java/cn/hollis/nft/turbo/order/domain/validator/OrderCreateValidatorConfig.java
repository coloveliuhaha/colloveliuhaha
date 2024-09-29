package cn.hollis.nft.turbo.order.domain.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单创建校验器配置
 *
 * @author hollis
 */
@Configuration
public class OrderCreateValidatorConfig {

    @Autowired
    private StockValidator stockValidator;

    @Autowired
    private GoodsValidator goodsValidator;

    @Autowired
    private UserValidator userValidator;

    @Bean
    public OrderCreateValidator orderValidatorChain() {
        userValidator.setNext(goodsValidator);
        goodsValidator.setNext(stockValidator);
        return userValidator;
    }

}

package cn.hollis.nft.turbo.admin.param;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 藏品创建参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionCreateParam {

    /**
     * '藏品名称'
     */
    private String name;

    /**
     * '藏品封面'
     */
    private String cover;

    /**
     * '藏品详情'
     */
    private String detail;

    /**
     * '价格'
     */
    private BigDecimal price;

    /**
     * '藏品数量'
     */
    private Long quantity;

    /**
     * '藏品发售时间'
     */
    private String saleTime;

}

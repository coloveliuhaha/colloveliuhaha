package cn.hollis.nft.turbo.admin.param;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 藏品修改参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionModifyParam {

    /**
     * '藏品id'
     */
    private Long collectionId;

    /**
     * '藏品数量'
     */
    private Long quantity;


    /**
     * '价格'
     */
    private BigDecimal price;


}

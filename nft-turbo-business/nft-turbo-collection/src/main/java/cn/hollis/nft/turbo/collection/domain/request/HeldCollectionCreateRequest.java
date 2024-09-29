package cn.hollis.nft.turbo.collection.domain.request;

import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import cn.hollis.nft.turbo.collection.facade.request.CollectionConfirmSaleRequest;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author wswyb001
 * @date 2024/01/17
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HeldCollectionCreateRequest extends BaseHeldCollectionRequest {
    /**
     * 藏品名称
     */
    private String name;

    /**
     * 藏品封面
     */
    private String cover;

    /**
     * 购入价格
     */
    private BigDecimal purchasePrice;

    /**
     * '藏品id'
     */
    private Long collectionId;

    /**
     * '持有人id'
     */
    private Long userId;

    /**
     * '藏品编号'
     */
    private String serialNo;

    /**
     * '业务Id'
     */
    private String bizNo;

    /**
     * '业务类型'
     */
    private String bizType;

    public HeldCollectionCreateRequest(CollectionConfirmSaleRequest collectionConfirmSaleRequest, String serialNo) {
        this.collectionId = collectionConfirmSaleRequest.collectionId();
        this.userId = Long.valueOf(collectionConfirmSaleRequest.userId());
        this.bizNo = collectionConfirmSaleRequest.bizNo();
        this.bizType = collectionConfirmSaleRequest.bizType();
        this.name = collectionConfirmSaleRequest.name();
        this.cover = collectionConfirmSaleRequest.cover();
        this.purchasePrice = collectionConfirmSaleRequest.purchasePrice();
        this.serialNo = serialNo;
    }

    @Override
    public HeldCollectionEventType getEventType() {
        return HeldCollectionEventType.CREATE;
    }
}

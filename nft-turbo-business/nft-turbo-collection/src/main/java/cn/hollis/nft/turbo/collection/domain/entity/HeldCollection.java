package cn.hollis.nft.turbo.collection.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

import cn.hollis.nft.turbo.api.collection.constant.CollectionSaleBizType;
import cn.hollis.nft.turbo.api.collection.constant.HeldCollectionState;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.datasource.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 藏品信息
 * </p>
 *
 * @author wswyb001
 * @since 2024-01-19
 */
@Getter
@Setter
public class HeldCollection extends BaseEntity {

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
     * '藏品编号'
     */
    private String serialNo;

    /**
     * 'nft唯一编号'
     */
    private String nftId;

    /**
     * '上一个持有人id'
     */
    private String preId;

    /**
     * '持有人id'
     */
    private String userId;

    /**
     * '状态'
     */
    private HeldCollectionState state;

    /**
     * '交易hash'
     */
    private String txHash;

    /**
     * '藏品持有时间'
     */
    private Date holdTime;

    /**
     * '藏品同步时间'
     */
    private Date syncChainTime;

    /**
     * '藏品销毁时间'
     */
    private Date deleteTime;

    /**
     * '业务类型'
     */
    private CollectionSaleBizType bizType;

    /**
     * '业务编号'
     */
    private String bizNo;

    public HeldCollection init(HeldCollectionCreateRequest request) {
        this.collectionId = request.getCollectionId();
        this.serialNo = request.getSerialNo();
        this.userId = request.getUserId().toString();
        this.state = HeldCollectionState.INIT;
        this.holdTime = new Date();
        this.bizNo = request.getBizNo();
        this.bizType = CollectionSaleBizType.valueOf(request.getBizType());
        this.name = request.getName();
        this.cover = request.getCover();
        this.purchasePrice = request.getPurchasePrice();
        return this;
    }

    public HeldCollection actived(String nftId, String txHash) {
        this.txHash = txHash;
        this.nftId = nftId;
        this.syncChainTime = new Date();
        this.state = HeldCollectionState.ACTIVED;
        return this;
    }

    public HeldCollection inActived() {
        this.state = HeldCollectionState.INACTIVED;
        return this;
    }

    public HeldCollection transfer(Long collectionId, String serialNo, String preId, String userId, String nftId) {
        this.collectionId = collectionId;
        this.serialNo = serialNo;
        this.preId = preId;
        this.userId = userId;
        this.nftId = nftId;
        this.state = HeldCollectionState.INIT;
        this.holdTime = new Date();
        return this;
    }

    public HeldCollection destroy() {
        this.state = HeldCollectionState.DESTROYED;
        this.deleteTime = new Date();
        return this;
    }

}

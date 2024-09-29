package cn.hollis.nft.turbo.collection.facade.request;

import cn.hollis.nft.turbo.api.collection.constant.CollectionEvent;

import java.math.BigDecimal;

/**
 *
 * @param identifier 幂等号
 * @param collectionId 藏品 id
 * @param quantity 本次占用的库存量
 * @param bizNo 占用的业务单号，如订单号
 * @param bizType 占用的业务类型，如一级市场交易
 * @param userId 用户ID
 * @param name 藏品名称
 * @param cover 藏品封面
 * @param purchasePrice 购买价格
 */
public record CollectionConfirmSaleRequest(String identifier, Long collectionId, Long quantity, String bizNo, String bizType, String userId,
                                           String name, String cover, BigDecimal purchasePrice) {

    public CollectionEvent eventType() {
        return CollectionEvent.CONFIRM_SALE;
    }
}

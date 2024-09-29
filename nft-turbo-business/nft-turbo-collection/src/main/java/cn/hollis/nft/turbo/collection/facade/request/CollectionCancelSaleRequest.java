package cn.hollis.nft.turbo.collection.facade.request;

import cn.hollis.nft.turbo.api.collection.constant.CollectionEvent;

public record CollectionCancelSaleRequest(String identifier, Long collectionId,Long quantity) {

    public CollectionEvent eventType() {
        return CollectionEvent.CANCEL_SALE;
    }
}

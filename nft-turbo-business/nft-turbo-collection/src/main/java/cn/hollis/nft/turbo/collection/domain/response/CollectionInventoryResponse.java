package cn.hollis.nft.turbo.collection.domain.response;

import cn.hollis.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Hollis
 */
@Getter
@Setter
public class CollectionInventoryResponse extends BaseResponse {

    private String collectionId;

    private String identifier;

    private Integer inventory;
}

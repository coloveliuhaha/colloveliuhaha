package cn.hollis.nft.turbo.collection.domain.request;

import cn.hollis.nft.turbo.base.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Hollis
 */
@Getter
@Setter
public class CollectionInventoryRequest extends BaseRequest {

    @NotNull(message = "collectionId is null")
    private String collectionId;

    private String identifier;

    private Integer inventory;
}

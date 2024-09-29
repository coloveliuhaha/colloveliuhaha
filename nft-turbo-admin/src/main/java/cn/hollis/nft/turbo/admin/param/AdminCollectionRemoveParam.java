package cn.hollis.nft.turbo.admin.param;

import lombok.Getter;
import lombok.Setter;

/**
 * 藏品下架参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionRemoveParam {

    /**
     * '藏品id'
     */
    private Long collectionId;
}

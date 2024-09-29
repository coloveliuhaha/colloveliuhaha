package cn.hollis.nft.turbo.collection.domain.request;

import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import lombok.*;

/**
 * @author wswyb001
 * @date 2024/01/17
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
public class HeldCollectionDestroyRequest extends BaseHeldCollectionRequest {

    @Override
    public HeldCollectionEventType getEventType() {
        return HeldCollectionEventType.DESTORY;
    }
}

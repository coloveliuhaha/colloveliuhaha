package cn.hollis.nft.turbo.order.wrapper;

import cn.hollis.nft.turbo.api.collection.model.CollectionInventoryVO;
import cn.hollis.nft.turbo.api.collection.service.CollectionFacadeService;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.goods.model.BaseGoodsInventoryVO;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Hollis
 */
@Service
public class InventoryWrapperService {

    @Autowired
    private CollectionFacadeService collectionFacadeService;

    public Boolean preDeduct(OrderCreateRequest orderCreateRequest) {
        GoodsType goodsType = orderCreateRequest.getGoodsType();
        String preDeductIdentifier = orderCreateRequest.getBuyerId() + "_" + orderCreateRequest.getIdentifier() + "_" + orderCreateRequest.getItemCount();
        return switch (goodsType) {
            case COLLECTION -> {
                SingleResponse<Boolean> response = collectionFacadeService.preInventoryDeduct(Long.valueOf(orderCreateRequest.getGoodsId()), orderCreateRequest.getItemCount(), preDeductIdentifier);
                if (response.getSuccess()) {
                    yield response.getData();
                }
                yield Boolean.FALSE;
            }
            default -> throw new UnsupportedOperationException("unsupport goods type");
        };
    }

    public BaseGoodsInventoryVO queryInventory(OrderCreateRequest orderCreateRequest) {

        GoodsType goodsType = orderCreateRequest.getGoodsType();
        return switch (goodsType) {
            case COLLECTION -> {
                SingleResponse<CollectionInventoryVO> response = collectionFacadeService.queryInventory(Long.valueOf(orderCreateRequest.getGoodsId()));
                if (response.getSuccess()) {
                    yield response.getData();
                }
                yield null;
            }
            default -> throw new UnsupportedOperationException("unsupport goods type");
        };
    }
}

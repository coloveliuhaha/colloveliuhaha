package cn.hollis.nft.turbo.order.domain.validator;

import cn.hollis.nft.turbo.api.goods.model.BaseGoodsInventoryVO;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.order.domain.exception.OrderException;
import cn.hollis.nft.turbo.order.wrapper.InventoryWrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.hollis.nft.turbo.api.order.constant.OrderErrorCode.INVENTORY_NOT_ENOUGH;

/**
 * 库存校验器
 *
 * @author hollis
 */
@Component
public class StockValidator extends BaseOrderCreateValidator {

    @Autowired
    private InventoryWrapperService inventoryWrapperService;

    @Override
    public void doValidate(OrderCreateRequest request) throws OrderException {
        BaseGoodsInventoryVO goodsInventoryVO = inventoryWrapperService.queryInventory(request);

        if (goodsInventoryVO == null) {
            throw new OrderException(INVENTORY_NOT_ENOUGH);
        }

        if (goodsInventoryVO.getInventory() == 0) {
            throw new OrderException(INVENTORY_NOT_ENOUGH);
        }

        if (goodsInventoryVO.getQuantity() < request.getItemCount()) {
            throw new OrderException(INVENTORY_NOT_ENOUGH);
        }

        if (goodsInventoryVO.getInventory() < request.getItemCount()) {
            throw new OrderException(INVENTORY_NOT_ENOUGH);
        }
    }
}

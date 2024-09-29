package cn.hollis.nft.turbo.collection.domain.service;

import cn.hollis.nft.turbo.api.collection.request.CollectionCreateRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyInventoryRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyPriceRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionRemoveRequest;
import cn.hollis.nft.turbo.api.collection.response.CollectionInventoryModifyResponse;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.response.CollectionConfirmSaleResponse;
import cn.hollis.nft.turbo.collection.facade.request.CollectionCancelSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionConfirmSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionTrySaleRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * 藏品服务
 *
 * @author liu
 */
public interface CollectionService extends IService<Collection> {
    /**
     * 创建
     *
     * @param request
     * @return
     */
    public Collection create(CollectionCreateRequest request);

    /**
     * 更新库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryModifyResponse modifyInventory(CollectionModifyInventoryRequest request);

    /**
     * 更新价格
     *
     * @param request
     * @return
     */
    public Boolean modifyPrice(CollectionModifyPriceRequest request);

    /**
     * 下架
     *
     * @param request
     * @return
     */
    public Boolean remove(CollectionRemoveRequest request);

    /**
     * 尝试售卖
     *
     * @param request
     * @return
     */
    public Boolean trySale(CollectionTrySaleRequest request);

    /**
     * 取消售卖
     *
     * @param request
     * @return
     */
    public Boolean cancelSale(CollectionCancelSaleRequest request);

    /**
     * 确认售卖
     *
     * @param request
     * @return
     */
    public CollectionConfirmSaleResponse confirmSale(CollectionConfirmSaleRequest request);

    /**
     * 查询
     *
     * @param collectionId
     * @return
     */
    public Collection queryById(Long collectionId);

    /**
     * 分页查询
     *
     * @param keyWord
     * @param state
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageResponse<Collection> pageQueryByState(String keyWord, String state, int currentPage, int pageSize);
}

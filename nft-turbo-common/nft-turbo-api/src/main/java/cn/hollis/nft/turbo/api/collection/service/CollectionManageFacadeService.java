package cn.hollis.nft.turbo.api.collection.service;

import cn.hollis.nft.turbo.api.collection.model.CollectionVO;
import cn.hollis.nft.turbo.api.collection.request.*;
import cn.hollis.nft.turbo.api.collection.response.CollectionChainResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionModifyResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionRemoveResponse;
import cn.hollis.nft.turbo.base.response.PageResponse;

/**
 * 藏品管理门面服务
 *
 * @author Hollis
 */
public interface CollectionManageFacadeService {

    /**
     * 创建藏品
     *
     * @param request
     * @return
     */
    CollectionChainResponse create(CollectionCreateRequest request);


    /**
     * 藏品下架
     *
     * @param request
     * @return
     */
    CollectionRemoveResponse remove(CollectionRemoveRequest request);


    /**
     * 藏品库存修改
     *
     * @param request
     * @return
     */
    CollectionModifyResponse modifyInventory(CollectionModifyInventoryRequest request);

    /**
     * 藏品价格修改
     *
     * @param request
     * @return
     */
    CollectionModifyResponse modifyPrice(CollectionModifyPriceRequest request);

    /**
     * 藏品分页查询
     *
     * @param request
     * @return
     */
    public PageResponse<CollectionVO> pageQuery(CollectionPageQueryRequest request);
}

package cn.hollis.nft.turbo.collection.domain.service;

import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 藏品库存服务
 *
 * @author Hollis
 */
@Service
public interface CollectionInventoryService {

    /**
     * 初始化藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse init(CollectionInventoryRequest request);

    /**
     * 获取藏品库存
     *
     * @param request
     * @return
     */
    public Integer getInventory(CollectionInventoryRequest request);

    /**
     * 扣减藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse decrease(CollectionInventoryRequest request);

    /**
     * 获取藏品库存扣减日志
     *
     * @param request
     * @return
     */
    public List<Object> getInventoryDecreaseLogs(CollectionInventoryRequest request);

    /**
     * 增加藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse increase(CollectionInventoryRequest request);

    /**
     * 失效藏品库存
     *
     * @param request
     * @return
     */
    public void invalid(CollectionInventoryRequest request);
}

package cn.hollis.nft.turbo.collection.domain.service.impl;

import cn.hollis.nft.turbo.api.collection.constant.CollectionInventoryModifyType;
import cn.hollis.nft.turbo.api.collection.request.CollectionCreateRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyInventoryRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyPriceRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionRemoveRequest;
import cn.hollis.nft.turbo.api.collection.response.CollectionInventoryModifyResponse;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.entity.CollectionInventoryStream;
import cn.hollis.nft.turbo.collection.domain.entity.CollectionSnapshot;
import cn.hollis.nft.turbo.collection.domain.entity.CollectionStream;
import cn.hollis.nft.turbo.collection.domain.entity.convertor.CollectionConvertor;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionConfirmSaleResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionService;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.collection.facade.request.CollectionCancelSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionConfirmSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionTrySaleRequest;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionInventoryStreamMapper;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionMapper;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionSnapshotMapper;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionStreamMapper;
import cn.hutool.core.lang.Assert;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static cn.hollis.nft.turbo.base.response.ResponseCode.DUPLICATED;
import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.*;

/**
 * @author Hollis
 * <p>
 * 通用的藏品服务
 */
public abstract class BaseCollectionService extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {

    @Autowired
    private HeldCollectionService heldCollectionService;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private CollectionStreamMapper collectionStreamMapper;

    @Autowired
    private CollectionSnapshotMapper collectionSnapshotMapper;

    @Autowired
    private CollectionInventoryStreamMapper collectionInventoryStreamMapper;

    /**
     * 创建藏品
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Collection create(CollectionCreateRequest request) {
        Collection existCollection = collectionMapper.selectByIdentifier(request.getIdentifier());
        if (existCollection != null) {
            return existCollection;
        }

        Collection collection = Collection.create(request);

        var saveResult = this.save(collection);
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_SAVE_FAILED));

        CollectionSnapshot collectionSnapshot = CollectionConvertor.INSTANCE.createSnapshot(collection);
        var result = collectionSnapshotMapper.insert(collectionSnapshot);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_SNAPSHOT_SAVE_FAILED));

        CollectionStream stream = new CollectionStream(collection, request.getIdentifier(), request.getEventType());
        saveResult = collectionStreamMapper.insert(stream) == 1;
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        return collection;
    }

    @Override
    @Transactional
    public CollectionInventoryModifyResponse modifyInventory(CollectionModifyInventoryRequest request) {
        CollectionInventoryModifyResponse response = new CollectionInventoryModifyResponse();
        response.setCollectionId(response.getCollectionId());

        CollectionInventoryStream existStream = collectionInventoryStreamMapper.selectByIdentifier(request.getIdentifier(), request.getEventType().name(), request.getCollectionId());
        if (existStream != null) {
            response.setSuccess(true);
            response.setResponseCode(DUPLICATED.name());
            return response;
        }

        Collection collection = getById(request.getCollectionId());
        if (null == collection) {
            throw new CollectionException(COLLECTION_QUERY_FAIL);
        }

        long quantityDiff = request.getQuantity() - collection.getQuantity();
        response.setQuantityModified(Math.abs(quantityDiff));

        if (quantityDiff == 0) {
            response.setModifyType(CollectionInventoryModifyType.UNMODIFIED);
            response.setSuccess(true);
            return response;
        } else if (quantityDiff > 0) {
            response.setModifyType(CollectionInventoryModifyType.INCREASE);
        } else {
            response.setModifyType(CollectionInventoryModifyType.DECREASE);
        }

        long oldSaleableInventory = collection.getSaleableInventory();
        collection.setQuantity(request.getQuantity());
        collection.setSaleableInventory(oldSaleableInventory + quantityDiff);
        boolean res = updateById(collection);
        Assert.isTrue(res, () -> new CollectionException(COLLECTION_UPDATE_FAILED));

        CollectionInventoryStream inventoryStream = new CollectionInventoryStream(collection, request.getIdentifier(), request.getEventType(), quantityDiff);
        boolean saveResult = collectionInventoryStreamMapper.insert(inventoryStream) == 1;
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_INVENTORY_UPDATE_FAILED));

        response.setSuccess(true);
        return response;
    }

    @CacheInvalidate(name = ":collection:cache:id:", key = "#request.collectionId")
    @Override
    public Boolean modifyPrice(CollectionModifyPriceRequest request) {
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.getIdentifier(), request.getEventType().name(), request.getCollectionId());
        if (existStream != null) {
            return true;
        }
        Collection collection = getById(request.getCollectionId());
        collection.setVersion(collection.getVersion() + 1);
        collection.setPrice(request.getPrice());

        var saveResult = super.updateById(collection);
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_SAVE_FAILED));

        CollectionSnapshot collectionSnapshot = CollectionConvertor.INSTANCE.createSnapshot(collection);
        var result = collectionSnapshotMapper.insert(collectionSnapshot);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_SNAPSHOT_SAVE_FAILED));

        CollectionStream stream = new CollectionStream(collection, request.getIdentifier(), request.getEventType());
        saveResult = collectionStreamMapper.insert(stream) == 1;
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheInvalidate(name = ":collection:cache:id:", key = "#request.collectionId")
    public Boolean remove(CollectionRemoveRequest request) {
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.getIdentifier(), request.getEventType().name(), request.getCollectionId());
        if (existStream != null) {
            return true;
        }
        Collection collection = getById(request.getCollectionId());
        collection.remove();
        var saveResult = this.updateById(collection);
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_UPDATE_FAILED));

        CollectionStream stream = new CollectionStream(collection, request.getIdentifier(), request.getEventType());
        saveResult = collectionStreamMapper.insert(stream) == 1;
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(name = ":collection:cache:id:", key = "#collection.id")
    public boolean updateById(Collection collection) {
        var saveResult = super.updateById(collection);
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean trySale(CollectionTrySaleRequest request) {
        //流水校验
        CollectionInventoryStream existStream = collectionInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            return true;
        }

        //查询出最新的值
        Collection collection = this.getById(request.collectionId());

        //新增collection流水
        CollectionInventoryStream stream = new CollectionInventoryStream(collection, request.identifier(), request.eventType(), request.quantity());
        int result = collectionInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        //核心逻辑执行
        result = collectionMapper.trySale(request.collectionId(), request.quantity());
        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelSale(CollectionCancelSaleRequest request) {
        //流水校验
        CollectionInventoryStream existStream = collectionInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            return true;
        }

        //查询出最新的值
        Collection collection = this.getById(request.collectionId());

        //新增collection流水
        CollectionInventoryStream stream = new CollectionInventoryStream(collection, request.identifier(), request.eventType(), request.quantity());
        int result = collectionInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        //核心逻辑执行
        result = collectionMapper.cancelSale(request.collectionId(), request.quantity());
        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CollectionConfirmSaleResponse confirmSale(CollectionConfirmSaleRequest request) {

        //流水校验
        CollectionInventoryStream existStream = collectionInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            CollectionConfirmSaleResponse response = new CollectionConfirmSaleResponse();
            response.setSuccess(true);
            response.setCollection(getById(existStream.getCollectionId()));
            response.setHeldCollection(heldCollectionService.getById(existStream.getHeldCollectionId()));
            return response;
        }

        Collection collection = this.getById(request.collectionId());

        //新增collection流水
        CollectionInventoryStream stream = new CollectionInventoryStream(collection, request.identifier(), request.eventType(), request.quantity());
        stream.setOccupiedInventory(collection.getOccupiedInventory() + request.quantity());

        int result = collectionInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        HeldCollectionCreateRequest heldCollectionCreateRequest = new HeldCollectionCreateRequest(request, String.valueOf(collection.getOccupiedInventory() + 1));
        var heldCollection = heldCollectionService.create(heldCollectionCreateRequest);

        result = collectionMapper.confirmSale(request.collectionId(), collection.getOccupiedInventory(), request.quantity());

        stream.addHeldCollectionId(heldCollection.getId());
        int res = collectionInventoryStreamMapper.updateById(stream);
        Assert.isTrue(res > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));
        CollectionConfirmSaleResponse collectionSaleResponse = new CollectionConfirmSaleResponse();
        collectionSaleResponse.setSuccess(true);
        collectionSaleResponse.setCollection(collection);
        collectionSaleResponse.setHeldCollection(heldCollection);
        return collectionSaleResponse;
    }

    @Override
    @Cached(name = ":collection:cache:id:", expire = 60, localExpire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.BOTH, key = "#collectionId", cacheNullValue = true)
    @CacheRefresh(refresh = 50, timeUnit = TimeUnit.MINUTES)
    public Collection queryById(Long collectionId) {
        return getById(collectionId);
    }
}

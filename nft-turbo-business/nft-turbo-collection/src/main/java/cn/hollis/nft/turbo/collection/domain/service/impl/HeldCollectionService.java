package cn.hollis.nft.turbo.collection.domain.service.impl;

import cn.hollis.nft.turbo.api.collection.constant.HeldCollectionState;
import cn.hollis.nft.turbo.api.collection.model.HeldCollectionDTO;
import cn.hollis.nft.turbo.api.collection.request.HeldCollectionPageQueryRequest;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import cn.hollis.nft.turbo.collection.domain.entity.HeldCollection;
import cn.hollis.nft.turbo.collection.domain.entity.convertor.HeldCollectionConvertor;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionActiveRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionDestroyRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionTransferRequest;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.HeldCollectionMapper;
import cn.hollis.turbo.stream.producer.StreamProducer;
import com.alibaba.fastjson.JSON;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.HELD_COLLECTION_QUERY_FAIL;
import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.HELD_COLLECTION_SAVE_FAILED;

/**
 * 持有的藏品服务
 * @author Hollis
 */
@Service
public class HeldCollectionService extends ServiceImpl<HeldCollectionMapper, HeldCollection> {

    @Autowired
    private StreamProducer streamProducer;

    public HeldCollection create(HeldCollectionCreateRequest request) {
        HeldCollection heldCollection = new HeldCollection();
        heldCollection.init(request);
        var saveResult = this.save(heldCollection);
        if (!saveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return heldCollection;
    }


    public Boolean active(HeldCollectionActiveRequest request) {
        HeldCollection heldCollection = getById(request.getHeldCollectionId());
        if (null == heldCollection) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }

        if (heldCollection.getState().equals(HeldCollectionState.ACTIVED.name())) {
            return true;
        }

        heldCollection.actived(request.getNftId(), request.getTxHash());
        boolean result = updateById(heldCollection);
        if (result) {
            sendMsg(heldCollection, request.getEventType());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public HeldCollection transfer(HeldCollectionTransferRequest request) {
        //先失效历史的持有数据
        HeldCollection oldHeldCollection = this.getById(request.getHeldCollectionId());
        if (oldHeldCollection == null) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }
        oldHeldCollection.inActived();
        var inActiveRes = this.updateById(oldHeldCollection);
        if (!inActiveRes) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        //再初始化新的持有数据
        HeldCollection newHeldCollection = new HeldCollection();
        newHeldCollection.transfer(oldHeldCollection.getCollectionId(), oldHeldCollection.getSerialNo(),
                String.valueOf(request.getSellerId()),
                String.valueOf(request.getBuyerId()), oldHeldCollection.getNftId());
        var newHeldSaveResult = this.save(newHeldCollection);
        if (!newHeldSaveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return newHeldCollection;

    }

    public HeldCollection destroy(HeldCollectionDestroyRequest request) {
        //查询持有数据
        HeldCollection heldCollection = this.getById(request.getHeldCollectionId());
        if (heldCollection == null) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }
        heldCollection.destroy();
        var saveResult = this.updateById(heldCollection);
        if (!saveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return heldCollection;
    }

    @Cached(name = ":held_collection:cache:id:", expire = 60, localExpire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.BOTH, key = "#heldCollectionId", cacheNullValue = true)
    @CacheRefresh(refresh = 50, timeUnit = TimeUnit.MINUTES)
    public HeldCollection queryById(Long heldCollectionId) {
        return getById(heldCollectionId);
    }

    public HeldCollection queryByCollectionIdAndSerialNo(Long collectionId, String serialNo) {
        QueryWrapper<HeldCollection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_id", collectionId);
        queryWrapper.eq("serial_no", serialNo);
        List<HeldCollection> retList = list(queryWrapper);
        if (CollectionUtils.isEmpty(retList)) {
            return null;
        }
        return retList.get(0);
    }

    public HeldCollection queryByNftIdAndState(String nftId, String state) {
        QueryWrapper<HeldCollection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nft_id", nftId);
        queryWrapper.eq("state", state);
        List<HeldCollection> retList = list(queryWrapper);
        if (CollectionUtils.isEmpty(retList)) {
            return null;
        }
        return retList.get(0);
    }

    public PageResponse<HeldCollection> pageQueryByState(HeldCollectionPageQueryRequest request) {
        Page<HeldCollection> page = new Page<>(request.getCurrentPage(), request.getPageSize());
        QueryWrapper<HeldCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", request.getUserId());
        wrapper.like("name", request.getKeyword());

        if (request.getState() != null) {
            wrapper.eq("state", request.getState());
        }
        wrapper.orderBy(true, true, "gmt_create");

        Page<HeldCollection> collectionPage = this.page(page, wrapper);
        return PageResponse.of(collectionPage.getRecords(), (int) collectionPage.getTotal(), request.getPageSize(), request.getCurrentPage());
    }


    private boolean sendMsg(HeldCollection heldCollection, HeldCollectionEventType eventType) {
        HeldCollectionDTO heldCollectionDTO = HeldCollectionConvertor.INSTANCE.mapToDto(heldCollection);
        return streamProducer.send("heldCollection-out-0", eventType.name(), JSON.toJSONString(heldCollectionDTO));
    }
}

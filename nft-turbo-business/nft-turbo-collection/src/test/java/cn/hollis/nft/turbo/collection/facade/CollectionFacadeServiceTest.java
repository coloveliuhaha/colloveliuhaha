package cn.hollis.nft.turbo.collection.facade;

import cn.hollis.nft.turbo.api.chain.response.ChainProcessResponse;
import cn.hollis.nft.turbo.api.chain.response.data.ChainOperationData;
import cn.hollis.nft.turbo.api.chain.service.ChainFacadeService;
import cn.hollis.nft.turbo.api.collection.constant.CollectionStateEnum;
import cn.hollis.nft.turbo.api.collection.request.CollectionCreateRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyInventoryRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionModifyPriceRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionRemoveRequest;
import cn.hollis.nft.turbo.api.collection.response.CollectionChainResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionModifyResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionRemoveResponse;
import cn.hollis.nft.turbo.api.collection.service.CollectionManageFacadeService;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.user.service.UserFacadeService;
import cn.hollis.nft.turbo.collection.NfTurboCollectionApplication;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionService;
import cn.hollis.nft.turbo.collection.domain.service.impl.redis.CollectionInventoryRedisService;
import cn.hollis.nft.turbo.limiter.SlidingWindowRateLimiter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NfTurboCollectionApplication.class})
@ActiveProfiles("test")
public class CollectionFacadeServiceTest {

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    protected SlidingWindowRateLimiter slidingWindowRateLimiter;

    @MockBean
    private ChainFacadeService chainFacadeService;

    @MockBean
    private UserFacadeService userFacadeService;

    @MockBean
    private OrderFacadeService orderFacadeService;

    @MockBean
    private CollectionInventoryRedisService collectionInventoryRedisService;

    @Autowired
    private CollectionManageFacadeService collectionFacadeService;

    @Autowired
    private CollectionService collectionService;


    @Test
    public void testChain() {
        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(String.valueOf(new Date().getTime()));
        request.setName("测试藏品");
        request.setQuantity(100L);
        request.setSaleTime(new Date());
        request.setPrice(BigDecimal.TEN);
        request.setCover("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF");
        ChainProcessResponse<ChainOperationData> chainProcessResponse = new ChainProcessResponse<>();
        chainProcessResponse.setSuccess(true);
        when(chainFacadeService.chain(any())).thenReturn(chainProcessResponse);
        CollectionInventoryResponse collectionInventoryResponse=new CollectionInventoryResponse();
        collectionInventoryResponse.setSuccess(true);
        when(collectionInventoryRedisService.init(any())).thenReturn(collectionInventoryResponse);

        CollectionChainResponse response = collectionFacadeService.create(request);
        Assert.assertTrue(response.getSuccess());
    }

    @Test
    public void testRemove() {
        //创建
        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(String.valueOf(new Date().getTime()));
        request.setName("测试藏品");
        request.setQuantity(100L);
        request.setSaleTime(new Date());
        request.setPrice(BigDecimal.TEN);
        request.setCover("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF");
        ChainProcessResponse<ChainOperationData> chainProcessResponse = new ChainProcessResponse<>();
        chainProcessResponse.setSuccess(true);
        when(chainFacadeService.chain(any())).thenReturn(chainProcessResponse);
        CollectionInventoryResponse collectionInventoryResponse=new CollectionInventoryResponse();
        collectionInventoryResponse.setSuccess(true);
        when(collectionInventoryRedisService.init(any())).thenReturn(collectionInventoryResponse);
        CollectionChainResponse response = collectionFacadeService.create(request);
        Assert.assertTrue(response.getSuccess());
        CollectionRemoveRequest removeRequest = new CollectionRemoveRequest();
        removeRequest.setCollectionId(response.getCollectionId());
        removeRequest.setIdentifier(String.valueOf(new Date().getTime()));
        CollectionRemoveResponse removeResponse = collectionFacadeService.remove(removeRequest);
        Assert.assertTrue(removeResponse.getSuccess());
        Collection collection = collectionService.getById(response.getCollectionId());
        Assert.assertEquals(collection.getState(), CollectionStateEnum.REMOVED);

    }

    @Test
    public void testModifyInventory() {
        //创建
        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(String.valueOf(new Date().getTime()));
        request.setName("测试藏品");
        request.setQuantity(100L);
        request.setSaleTime(new Date());
        request.setPrice(BigDecimal.TEN);
        request.setCover("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF");
        ChainProcessResponse<ChainOperationData> chainProcessResponse = new ChainProcessResponse<>();
        chainProcessResponse.setSuccess(true);
        when(chainFacadeService.chain(any())).thenReturn(chainProcessResponse);
        CollectionInventoryResponse collectionInventoryResponse=new CollectionInventoryResponse();
        collectionInventoryResponse.setSuccess(true);
        when(collectionInventoryRedisService.init(any())).thenReturn(collectionInventoryResponse);
        CollectionChainResponse response = collectionFacadeService.create(request);
        Assert.assertTrue(response.getSuccess());
        //库存更新
        Collection collection = collectionService.getById(response.getCollectionId());
        collection.setOccupiedInventory(10L);
        collection.setSaleableInventory(90L);
        collectionService.updateById(collection);
        //修改库存
        when(collectionInventoryRedisService.increase(any())).thenReturn(collectionInventoryResponse);
        when(collectionInventoryRedisService.decrease(any())).thenReturn(collectionInventoryResponse);
        CollectionModifyInventoryRequest modifyRequest = new CollectionModifyInventoryRequest();
        modifyRequest.setCollectionId(response.getCollectionId());
        modifyRequest.setIdentifier(String.valueOf(new Date().getTime()));
        modifyRequest.setQuantity(200L);
        CollectionModifyResponse modifyResponse = collectionFacadeService.modifyInventory(modifyRequest);
        Assert.assertTrue(modifyResponse.getSuccess());
        collection = collectionService.getById(response.getCollectionId());
        Assert.assertTrue(collection.getOccupiedInventory() == 10L);
        Assert.assertTrue(collection.getSaleableInventory() == 190L);
    }


    @Test
    public void testModifyPrice() {
        //创建
        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(String.valueOf(new Date().getTime()));
        request.setName("测试藏品");
        request.setQuantity(100L);
        request.setSaleTime(new Date());
        request.setPrice(BigDecimal.TEN);
        request.setCover("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF");
        ChainProcessResponse<ChainOperationData> chainProcessResponse = new ChainProcessResponse<>();
        chainProcessResponse.setSuccess(true);
        when(chainFacadeService.chain(any())).thenReturn(chainProcessResponse);
        CollectionInventoryResponse collectionInventoryResponse=new CollectionInventoryResponse();
        collectionInventoryResponse.setSuccess(true);
        when(collectionInventoryRedisService.init(any())).thenReturn(collectionInventoryResponse);
        CollectionChainResponse response = collectionFacadeService.create(request);
        Assert.assertTrue(response.getSuccess());
        //修改价格
        CollectionModifyPriceRequest modifyRequest = new CollectionModifyPriceRequest();
        modifyRequest.setCollectionId(response.getCollectionId());
        modifyRequest.setIdentifier(String.valueOf(new Date().getTime()));
        modifyRequest.setPrice(BigDecimal.TWO);
        CollectionModifyResponse modifyResponse = collectionFacadeService.modifyPrice(modifyRequest);
        Assert.assertTrue(modifyResponse.getSuccess());
        Collection collection = collectionService.getById(response.getCollectionId());
        Assert.assertTrue(collection.getPrice().equals(BigDecimal.TWO));
    }

}

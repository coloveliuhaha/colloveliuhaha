package cn.hollis.nft.turbo.collection.domain.service;


import cn.hollis.nft.turbo.collection.CollectionBaseTest;
import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.impl.redis.CollectionInventoryRedisService;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Hollis
 *
 * 这里依赖缓存配置，先 ignore 掉，如果增加了 redis 配置之后，可以把redissonClient的 mock 移除，再去除 ignore 即可
 */
@Ignore
public class CollectionInventoryRedisServiceTest extends CollectionBaseTest {

    @Autowired
    private CollectionInventoryRedisService collectionInventoryRedisService;

    @Test
    public void init() {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test3211");
        request.setInventory(66);
        collectionInventoryRedisService.init(request);

        Integer result = collectionInventoryRedisService.getInventory(request);
        Assert.assertEquals(66, (int) result);
    }

    @Test
    public void decrease_concurrent() throws InterruptedException {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test321");
        request.setInventory(100);
        collectionInventoryRedisService.invalid(request);
        collectionInventoryRedisService.init(request);

        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                try {
                    startGate.await(); // 等待所有线程准备好
                    try {
                        CollectionInventoryRequest decreaseRequest = new CollectionInventoryRequest();
                        decreaseRequest.setCollectionId("test321");
                        decreaseRequest.setInventory(1);
                        decreaseRequest.setIdentifier(UUID.randomUUID().toString());
                        collectionInventoryRedisService.decrease(decreaseRequest);
                    } finally {
                        endGate.countDown(); // 告诉主线程该线程已完成
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startGate.countDown(); // 启动所有等待线程
        endGate.await(); // 等待所有线程执行完毕

        Integer result = collectionInventoryRedisService.getInventory(request);
        Assert.assertEquals(0, (int) result);
    }

    @Test
    public void decrease_duplicated() {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test0321");
        request.setInventory(100);
        collectionInventoryRedisService.invalid(request);
        collectionInventoryRedisService.init(request);

        CollectionInventoryRequest decreaseRequest = new CollectionInventoryRequest();
        decreaseRequest.setCollectionId("test0321");
        decreaseRequest.setInventory(1);
        decreaseRequest.setIdentifier(UUID.randomUUID().toString());

        CollectionInventoryResponse response = collectionInventoryRedisService.decrease(decreaseRequest);
        Assert.assertTrue(response.getSuccess());
        Assert.assertNull(response.getResponseCode());

        response = collectionInventoryRedisService.decrease(decreaseRequest);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals(response.getResponseCode(), "OPERATION_ALREADY_EXECUTED");

        Integer result = collectionInventoryRedisService.getInventory(request);
        Assert.assertEquals(99, (int) result);
    }

    @Test
    public void increase_duplicated() {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test00321");
        request.setInventory(100);
        collectionInventoryRedisService.invalid(request);
        collectionInventoryRedisService.init(request);

        CollectionInventoryRequest decreaseRequest = new CollectionInventoryRequest();
        decreaseRequest.setCollectionId("test00321");
        decreaseRequest.setInventory(1);
        decreaseRequest.setIdentifier(UUID.randomUUID().toString());

        CollectionInventoryResponse response = collectionInventoryRedisService.increase(decreaseRequest);
        Assert.assertTrue(response.getSuccess());
        Assert.assertNull(response.getResponseCode());

        response = collectionInventoryRedisService.increase(decreaseRequest);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals(response.getResponseCode(), "OPERATION_ALREADY_EXECUTED");

        Integer result = collectionInventoryRedisService.getInventory(request);
        Assert.assertEquals(101, (int) result);
    }

    @Test
    public void increase_concurrent() throws InterruptedException {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test32122");
        request.setInventory(100);
        collectionInventoryRedisService.invalid(request);
        collectionInventoryRedisService.init(request);

        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                try {
                    startGate.await(); // 等待所有线程准备好
                    try {
                        CollectionInventoryRequest decreaseRequest = new CollectionInventoryRequest();
                        decreaseRequest.setCollectionId("test32122");
                        decreaseRequest.setInventory(1);
                        decreaseRequest.setIdentifier(UUID.randomUUID().toString());
                        collectionInventoryRedisService.increase(decreaseRequest);
                    } finally {
                        endGate.countDown(); // 告诉主线程该线程已完成
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startGate.countDown(); // 启动所有等待线程
        endGate.await(); // 等待所有线程执行完毕

        Integer result = collectionInventoryRedisService.getInventory(request);
        Assert.assertEquals(200, (int) result);
    }

    @Test
    public void decrease_oversold() throws InterruptedException {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test3213");
        request.setInventory(100);
        collectionInventoryRedisService.invalid(request);
        collectionInventoryRedisService.init(request);
        request.setInventory(101);
        request.setIdentifier(UUID.randomUUID().toString());
        CollectionInventoryResponse result = collectionInventoryRedisService.decrease(request);
        System.out.println(JSON.toJSONString(result));
        Assert.assertEquals(result.getResponseCode(), "INVENTORY_NOT_ENOUGH");
    }


    @Test
    public void invalid_decrease() throws InterruptedException {
        CollectionInventoryRequest request = new CollectionInventoryRequest();
        request.setCollectionId("test321366");
        request.setInventory(100);
        collectionInventoryRedisService.init(request);

        collectionInventoryRedisService.invalid(request);

        request.setInventory(1);
        request.setIdentifier(UUID.randomUUID().toString());
        CollectionInventoryResponse result = collectionInventoryRedisService.decrease(request);
        System.out.println(JSON.toJSONString(result));
        Assert.assertEquals(result.getResponseCode(), "KEY_NOT_FOUND");
    }
}
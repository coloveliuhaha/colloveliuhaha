package cn.hollis.nft.turbo.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hollis.nft.turbo.admin.infrastructure.exception.AdminException;
import cn.hollis.nft.turbo.admin.param.AdminCollectionCreateParam;
import cn.hollis.nft.turbo.admin.param.AdminCollectionModifyParam;
import cn.hollis.nft.turbo.admin.param.AdminCollectionRemoveParam;
import cn.hollis.nft.turbo.api.collection.model.CollectionVO;
import cn.hollis.nft.turbo.api.collection.request.*;
import cn.hollis.nft.turbo.api.collection.response.CollectionChainResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionModifyResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionRemoveResponse;
import cn.hollis.nft.turbo.api.collection.service.CollectionManageFacadeService;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.file.FileService;
import cn.hollis.nft.turbo.web.util.MultiResultConvertor;
import cn.hollis.nft.turbo.web.vo.MultiResult;
import cn.hollis.nft.turbo.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static cn.hollis.nft.turbo.admin.infrastructure.exception.AdminErrorCode.ADMIN_UPLOAD_PICTURE_FAIL;

/**
 * 藏品后台管理
 *
 * @author Hollis
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("admin/collection")
@CrossOrigin(origins = "*")
public class CollectionAdminController {

    @DubboReference(version = "1.0.0")
    private CollectionManageFacadeService collectionManageFacadeService;

    @Autowired
    private FileService fileService;

    @PostMapping("/uploadCollection")
    public Result<String> uploadCollection(@RequestParam("file_data") MultipartFile file) throws Exception {
        if (null == file) {
            throw new AdminException(ADMIN_UPLOAD_PICTURE_FAIL);
        }
        String userId = (String) StpUtil.getLoginId();
        //藏品封面上传
        String prefix = "https://nfturbo-file.oss-cn-hangzhou.aliyuncs.com/";
        String filename = file.getOriginalFilename();
        InputStream fileStream = file.getInputStream();
        String path = "collection/" + userId + "/" + filename;
        var res = fileService.upload(path, fileStream);
        if (!res) {
            throw new AdminException(ADMIN_UPLOAD_PICTURE_FAIL);
        }
        return Result.success(prefix + path);

    }

    /**
     * 铸造/创建/上链藏品
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/createCollection")
    public Result<Long> createCollection(@Valid @RequestBody AdminCollectionCreateParam param) throws Exception {
        String userId = (String) StpUtil.getLoginId();

        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(UUID.randomUUID().toString());
        request.setPrice(param.getPrice());
        request.setQuantity(param.getQuantity());
        request.setName(param.getName());
        request.setDetail(param.getDetail());
        request.setCover(param.getCover());
        request.setCreatorId(userId);
        request.setCreateTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        request.setSaleTime(sdf.parse(param.getSaleTime()));

        CollectionChainResponse response = collectionManageFacadeService.create(request);
        if (response.getSuccess()) {
            return Result.success(response.getCollectionId());
        } else {
            return Result.error(response.getResponseCode(), response.getResponseMessage());
        }
    }

    @PostMapping("/removeCollection")
    public Result<Long> removeCollection(@Valid @RequestBody AdminCollectionRemoveParam param) {
        CollectionRemoveRequest request = new CollectionRemoveRequest();
        request.setIdentifier(UUID.randomUUID().toString());
        request.setCollectionId(param.getCollectionId());
        CollectionRemoveResponse response = collectionManageFacadeService.remove(request);
        if (response.getSuccess()) {
            return Result.success(response.getCollectionId());
        } else {
            return Result.error(response.getResponseCode(), response.getResponseMessage());
        }
    }

    @PostMapping("/modifyInventory")
    public Result<Long> modifyInventory(@Valid @RequestBody AdminCollectionModifyParam param) {
        CollectionModifyInventoryRequest request = new CollectionModifyInventoryRequest();
        request.setIdentifier(UUID.randomUUID().toString());
        request.setCollectionId(param.getCollectionId());
        request.setQuantity(param.getQuantity());
        CollectionModifyResponse response = collectionManageFacadeService.modifyInventory(request);
        if (response.getSuccess()) {
            return Result.success(response.getCollectionId());
        } else {
            return Result.error(response.getResponseCode(), response.getResponseMessage());
        }

    }

    @PostMapping("/modifyPrice")
    public Result<Long> modifyPrice(@Valid @RequestBody AdminCollectionModifyParam param) {
        CollectionModifyPriceRequest request = new CollectionModifyPriceRequest();
        request.setIdentifier(UUID.randomUUID().toString());
        request.setCollectionId(param.getCollectionId());
        request.setPrice(param.getPrice());
        CollectionModifyResponse response = collectionManageFacadeService.modifyPrice(request);
        if (response.getSuccess()) {
            return Result.success(response.getCollectionId());
        } else {
            return Result.error(response.getResponseCode(), response.getResponseMessage());
        }
    }

    /**
     * 藏品列表
     *
     * @param
     * @return 结果
     */
    @GetMapping("/collectionList")
    public MultiResult<CollectionVO> collectionList(@NotBlank String state, String keyWord, int pageSize, int currentPage) {
        CollectionPageQueryRequest collectionPageQueryRequest = new CollectionPageQueryRequest();
        collectionPageQueryRequest.setState(state);
        collectionPageQueryRequest.setKeyword(keyWord);
        collectionPageQueryRequest.setCurrentPage(currentPage);
        collectionPageQueryRequest.setPageSize(pageSize);
        PageResponse<CollectionVO> pageResponse = collectionManageFacadeService.pageQuery(collectionPageQueryRequest);
        return MultiResultConvertor.convert(pageResponse);
    }

}

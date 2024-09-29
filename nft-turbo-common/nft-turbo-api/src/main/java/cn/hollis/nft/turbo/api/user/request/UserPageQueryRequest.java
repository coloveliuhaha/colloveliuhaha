package cn.hollis.nft.turbo.api.user.request;

import cn.hollis.nft.turbo.base.request.BaseRequest;
import lombok.*;

/**
 * @author Hollis
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPageQueryRequest extends BaseRequest {

    /**
     * 手机号关键字
     */
    private String keyWord;
    /**
     * 用户状态
     */
    private String state;
    /**
     * 当前页
     */
    private int currentPage;
    /**
     * 页面大小
     */
    private int pageSize;

}
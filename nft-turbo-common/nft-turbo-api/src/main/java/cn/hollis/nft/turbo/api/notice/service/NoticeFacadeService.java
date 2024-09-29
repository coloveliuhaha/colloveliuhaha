package cn.hollis.nft.turbo.api.notice.service;


import cn.hollis.nft.turbo.api.notice.response.NoticeResponse;

/**
 * @author Hollis
 */
public interface NoticeFacadeService {
    /**
     * 生成并发送短信验证码
     *
     * @param telephone
     * @return
     */
    public NoticeResponse generateAndSendSmsCaptcha(String telephone);
}

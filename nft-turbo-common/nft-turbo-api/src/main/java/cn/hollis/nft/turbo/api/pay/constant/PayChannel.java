package cn.hollis.nft.turbo.api.pay.constant;

/**
 * @author Hollis
 * 支付渠道
 */
public enum PayChannel {

    /**
     * 支付宝
     */
    ALIPAY("支付宝"),
    /**
     * 微信
     */
    WECHAT("微信"),

    /**
     * MOCK
     */
    MOCK("MOCK");

    private String value;

    PayChannel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

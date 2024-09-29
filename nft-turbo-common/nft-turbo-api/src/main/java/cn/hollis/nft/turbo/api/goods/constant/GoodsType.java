package cn.hollis.nft.turbo.api.goods.constant;

/**
 * @author Hollis
 */
public enum GoodsType {

    /** 藏品 */
    COLLECTION("藏品"),

    /**
     * 盲盒
     */
    BLIND_BOX("盲盒");


    private String value;

    GoodsType(String value) {
        this.value = value;
    }
}

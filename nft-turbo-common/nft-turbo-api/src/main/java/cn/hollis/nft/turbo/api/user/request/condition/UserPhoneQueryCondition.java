package cn.hollis.nft.turbo.api.user.request.condition;

import lombok.*;

import java.io.Serializable;

/**
 * @author Hollis
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPhoneQueryCondition implements UserQueryCondition {

    private static final long serialVersionUID = 1L;

    /**
     * 用户手机号
     */
    private String telephone;
}
package cn.hollis.nft.turbo.user.domain.entity.convertor;

import cn.hollis.nft.turbo.api.user.response.data.UserInfo;
import cn.hollis.nft.turbo.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Hollis
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserConvertor {

    UserConvertor INSTANCE = Mappers.getMapper(UserConvertor.class);

    /**
     * 转换为vo
     *
     * @param request
     * @return
     */
    @Mapping(target = "userId", source = "request.id")
    public UserInfo mapToVo(User request);

    /**
     * 转换为实体
     *
     * @param request
     * @return
     */
    @Mapping(target = "id", source = "request.userId")
    public User mapToEntity(UserInfo request);

    /**
     * 转换为VO
     *
     * @param request
     * @return
     */
    @Mapping(target = "id", source = "request.userId")
    public List<UserInfo> mapToVo(List<User> request);
}

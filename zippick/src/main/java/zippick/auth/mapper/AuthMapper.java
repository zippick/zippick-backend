package zippick.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import zippick.auth.dto.AuthTokenDTO;
import zippick.member.dto.MemberDTO;

@Mapper
public interface AuthMapper {
    MemberDTO getMemberById(String loginId);
    void insertToken(AuthTokenDTO token);
    void deleteToken(String token);
}

package zippick.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.member.dto.request.SignUpRequest;

@Mapper
public interface MemberMapper {
    int getDuplicateMember(@Param("loginId")String loginId);
    void insertMember(SignUpRequest signUpRequest);
}

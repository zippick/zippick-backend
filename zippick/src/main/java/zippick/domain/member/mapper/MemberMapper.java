package zippick.domain.member.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import zippick.domain.member.dto.MemberDTO;
import zippick.domain.member.dto.request.SignUpRequest;

@Mapper
public interface MemberMapper {
    boolean isDuplicateLoginId(@Param("loginId")String loginId);
    void insertMember(SignUpRequest signUpRequest);
    MemberDTO findById(@Param("id") Long id);
}

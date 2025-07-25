package zippick.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberDTO {
    private Long id;
    private String loginId;
    private String password;
    private String name;
}

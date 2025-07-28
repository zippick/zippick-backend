package zippick.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String loginId;
    private String password;
    private String name;
    private String zipcode;
    private String basicAddress;
    private String detailAddress;
}

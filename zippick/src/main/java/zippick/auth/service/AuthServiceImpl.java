package zippick.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zippick.auth.dto.AuthTokenDTO;
import zippick.auth.dto.request.LoginRequest;
import zippick.auth.dto.response.LoginResponse;
import zippick.auth.mapper.AuthMapper;
import zippick.member.dto.MemberDTO;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        MemberDTO member = authMapper.getMemberById(request.getLoginId());

        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호 오류");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);

        AuthTokenDTO authToken = new AuthTokenDTO(token, expiredAt, member.getId());
        authMapper.insertToken(authToken);

        return new LoginResponse(token);
    }

    @Override
    @Transactional
    public void logout(String token) {
        authMapper.deleteToken(token);
    }
}

package zippick.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zippick.domain.member.dto.request.SignUpRequest;
import zippick.domain.member.mapper.MemberMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;

    @Override
    public void registerMember(SignUpRequest request) {

        // 아이디 중복 체크
        if (memberMapper.getDuplicateMember(request.getLoginId()) > 0) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 암호 화
        String hashed = hash(request.getPassword());
        request.setPassword(hashed);

        memberMapper.insertMember(request);
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] result = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}

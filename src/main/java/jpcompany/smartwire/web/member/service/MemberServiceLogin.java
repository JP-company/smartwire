package jpcompany.smartwire.web.member.service;

import jpcompany.smartwire.domain.Member;
import jpcompany.smartwire.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceLogin {

    private final MemberRepository repository;
    public Member login(String loginId, String loginPassword) {
        return repository.findByLoginId(loginId)
                .filter(member -> member.getLoginPassword().equals(loginPassword))
                .orElse(null);
    }

    public void updateAuthCode(String loginId, String authCode, String email) {
        repository.updateAuthCodeEmail(loginId, authCode, email);
    }


    public Member verifyAuthCode(String loginId, String authCode) {
        Member member = repository.findByLoginId(loginId).orElse(null);
        if (member!= null && member.getAuthCode().equals(authCode)) {
            repository.setEmailVerified(loginId);
            return member;
        }
        return null;
    }
}

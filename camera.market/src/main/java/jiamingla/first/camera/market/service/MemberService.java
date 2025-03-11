package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public Member registerMember(Member member) {
        // Check if the username or email already exists
        Optional<Member> existingMemberByUsername = memberRepository.findByUsername(member.getUsername());
        Optional<Member> existingMemberByEmail = memberRepository.findByEmail(member.getEmail());

        if (existingMemberByUsername.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (existingMemberByEmail.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        System.out.println("註冊成功: " + member.getUsername() + ", Password: " + member.getPassword()); // Add this line
        return memberRepository.save(member);
    }
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }
}

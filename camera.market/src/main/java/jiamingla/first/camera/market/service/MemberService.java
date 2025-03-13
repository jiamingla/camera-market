package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public Member registerMember(Member member) {
        logger.info("Attempting to register member: {}", member.getUsername());
        // Check if the username or email already exists
        Optional<Member> existingMemberByUsername = memberRepository.findByUsername(member.getUsername());
        Optional<Member> existingMemberByEmail = memberRepository.findByEmail(member.getEmail());

        if (existingMemberByUsername.isPresent()) {
            logger.warn("Username already exists: {}", member.getUsername());
            throw new BusinessException("Username already exists");
        }

        if (existingMemberByEmail.isPresent()) {
            logger.warn("Email already exists: {}", member.getEmail());
            throw new BusinessException("Email already exists");
        }
        String encodedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encodedPassword);
        logger.info("Member registered successfully: {}", member.getUsername());
        return memberRepository.save(member);
    }
    public Optional<Member> findByUsername(String username) {
        logger.debug("Finding member by username: {}", username);
        Optional<Member> member = memberRepository.findByUsername(username);
        if(member.isEmpty()){
            logger.warn("Member {} not found", username);
        }else{
            logger.debug("Member found: {}", username);
        }
        return member;
    }
    public Optional<Member> getMemberWithListings(Long id){
        logger.info("Get Member With Listings by Id: {}", id);
        Optional<Member> member = memberRepository.findById(id);
        if(member.isEmpty()){
            logger.warn("Cannot find member {}", id);
        }else{
            logger.debug("Member found: {}", id);
        }
        return member;
    }
}

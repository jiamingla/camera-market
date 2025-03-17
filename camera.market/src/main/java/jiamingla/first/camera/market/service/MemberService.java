package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.entity.PasswordResetToken;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.exception.SystemException;
import jiamingla.first.camera.market.repository.MemberRepository;
import jiamingla.first.camera.market.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service // 標記這個類是一個 Spring 服務類
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    //會員註冊
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
     public Member getMemberById(Long id) {
        logger.info("Retrieving member by ID: {}", id);
        return memberRepository.findById(id).orElseThrow(() -> {
            logger.error("Member not found with ID: {}", id);
            return new SystemException("Member not found with ID: " + id);
        });
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
    // 為會員創建密碼重置令牌
    @Transactional // 標記這個方法需要事務管理
    public void createPasswordResetTokenForMember(Member member) {
        logger.info("Create password token for member: {}",member.getUsername());
        //創建token
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setMember(member);
        //儲存到db
        passwordResetTokenRepository.save(passwordResetToken);
    }

    // 通過令牌字串獲取密碼重置令牌
    @Transactional // 標記這個方法需要事務管理
    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        logger.info("Get password token: {}",token);
        return passwordResetTokenRepository.findByToken(token);
    }

    // 重置會員密碼
    @Transactional // 標記這個方法需要事務管理
    public void resetPassword(String token, String newPassword) {
        logger.info("Reset password for token: {}",token);
        // 根據token找到對應的token
        Optional<PasswordResetToken> optionalPasswordResetToken = getPasswordResetToken(token);

        if (optionalPasswordResetToken.isEmpty()) {
            logger.error("Invalid token: {}", token);
            throw new BusinessException("Invalid token");
        }
        // 取得 token object
        PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
        // 檢查token是否過期
        if(passwordResetToken.isExpired()){
            logger.error("Expired token: {}", token);
            throw new BusinessException("Token has expired");
        }
        //取得該token對應的member
        Member member = passwordResetToken.getMember();
        //將新的密碼加密
        String encodedPassword = passwordEncoder.encode(newPassword);
        //設定新密碼
        member.setPassword(encodedPassword);
        //儲存修改後的會員資訊
        memberRepository.save(member);
        //刪除該member的token資訊
        passwordResetTokenRepository.deleteByMemberId(member.getId());
        logger.info("Password reset successfully for member: {}", member.getUsername());
    }

    // 通過電子郵件查找會員
    public Member findByEmail(String email) {
        logger.debug("Finding member by email: {}", email);
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            logger.warn("Member with email {} not found", email);
            throw new BusinessException("Member with email not found");
        } else {
            logger.debug("Member found: {}", member.get().getUsername());
        }
        return member.get();
    }
    public Optional<PasswordResetToken> getPasswordResetTokenByMember(Member member) {
        return passwordResetTokenRepository.findByMemberId(member.getId());
    }
}

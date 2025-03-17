package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.dto.LoginRequest;
import jiamingla.first.camera.market.dto.MemberResponse;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.entity.PasswordResetToken;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.service.EmailService;
import jiamingla.first.camera.market.service.MemberService;
import jiamingla.first.camera.market.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    @Autowired
    private MemberService memberService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<Member> registerMember(@Valid @RequestBody Member member) {
        logger.info("Received request to register member: {}", member.getUsername());
        Member registeredMember = memberService.registerMember(member);
        logger.info("Member registered successfully: {}", registeredMember.getUsername());
        return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Received login request for user: {}", loginRequest.getUsername());
        try {
            // 使用 AuthenticationManager 驗證使用者名和密碼
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // 將驗證通過的 Authentication 物件設定到 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 產生 JWT Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            logger.info("User {} logged in successfully. JWT Token generated: {}", loginRequest.getUsername(), token);

            // 返回 Token
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 登入失敗，返回 401 Unauthorized
            logger.warn("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<?> getMemberWithListings(@PathVariable Long id){
        logger.info("Received request to get Member With Listings, id: {}", id);
        Optional<Member> optionalMember = memberService.getMemberWithListings(id);
        if(optionalMember.isEmpty()){
            logger.warn("Cannot find member: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cannot find member");
        }
        Member member = optionalMember.get();
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setUsername(member.getUsername());
        response.setEmail(member.getEmail());
        response.setListings(member.getListings());
        logger.info("Returning Member With Listings, member: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }

    // 忘記密碼 API
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        logger.info("Forgot password request received for email: {}", email);
        try {
            //先取得member
            Member member = memberService.findByEmail(email);
            //為這個member產生token
            memberService.createPasswordResetTokenForMember(member);
            //因為createPasswordResetTokenForMember已經產生token並儲存到database了，所以現在要透過member去找到那個token
            Optional<PasswordResetToken> optionalPasswordResetToken = memberService.getPasswordResetTokenByMember(member);

            if(optionalPasswordResetToken.isEmpty()){
                throw new BusinessException("Token create fail");
            }
            PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
            //透過emailservice寄送email
            emailService.sendPasswordResetEmail(member.getEmail(), passwordResetToken.getToken());
            return ResponseEntity.ok("Password reset link sent to your email."); // 返回成功訊息
        } catch (BusinessException e) {
            logger.error("Error in forgot password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());//返回錯誤訊息
        } catch (Exception e) {
            logger.error("Unexpected error in forgot password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");//返回錯誤訊息
        }
    }

    // 重置密碼 API
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        logger.info("Reset password request received for token: {}", token);
        try {
            //重置密碼
            memberService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully."); // 返回成功訊息
        } catch (BusinessException e) {
            logger.error("Error in reset password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());//返回錯誤訊息
        } catch (Exception e) {
            logger.error("Unexpected error in reset password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");//返回錯誤訊息
        }
    }
}

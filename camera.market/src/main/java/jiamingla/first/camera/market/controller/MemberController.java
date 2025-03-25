package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.dto.LoginRequest;
import jiamingla.first.camera.market.dto.MemberResponse;
import jiamingla.first.camera.market.entity.Member;
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
        Member member = memberService.getMemberById(id);
        if(member == null){
            logger.warn("Cannot find member: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cannot find member");
        }
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setUsername(member.getUsername());
        response.setEmail(member.getEmail());
        response.setListings(member.getListings());
        logger.info("Returning Member With Listings, member: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
}

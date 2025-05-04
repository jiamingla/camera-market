package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.dto.LoginRequest;
import jiamingla.first.camera.market.dto.MemberResponse;
import jiamingla.first.camera.market.dto.TokenValidationResponse;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.service.MemberService;
import jiamingla.first.camera.market.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    public ResponseEntity<?> registerMember(@Valid @RequestBody Member member) {
        logger.info("Received request to register member: {}", member.getUsername());
        try {
            Member registeredMember = memberService.registerMember(member);
            logger.info("Member registered successfully: {}", registeredMember.getUsername());
            return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
        }catch (BusinessException e){
            logger.warn("Username {} already exists", member.getUsername());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
        catch (Exception e) {
            logger.error("An unexpected error occurred during register for user: {}", member.getUsername(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
        } catch (BadCredentialsException e) {
            // 登入失敗，返回 401 Unauthorized
            logger.warn("Login failed for user: {}", loginRequest.getUsername(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("An unexpected error occurred during login for user: {}", loginRequest.getUsername(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<?> getMemberWithListings(@PathVariable Long id) {
        logger.info("Received request to get Member With Listings, id: {}", id);
        Optional<Member> optionalMember = memberService.getMemberWithListings(id);
        if (optionalMember.isEmpty()) {
            logger.warn("Cannot find member: {}", id);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Cannot find member");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        Member member = optionalMember.get();
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setUsername(member.getUsername());
        response.setEmail(member.getEmail());
        // TODO: 這一行拆去Listing，讓前端CALL 一次member拿member的資料，一次拿member底下的listing資料
        response.setListings(member.getListings());
        logger.info("Returning Member With Listings, member: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }

    // 新增的驗證 Token 的 endpoint
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        logger.info("Received request to validate token.");
        try {
            // 從 SecurityContextHolder 取得 Authentication 物件
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 如果 Authentication 物件為 null 或未驗證，表示 Token 無效
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Token validation failed: No authentication or not authenticated.");
                TokenValidationResponse response = new TokenValidationResponse("Token is invalid", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 如果 Authentication 物件已驗證，表示 Token 有效
            logger.info("Token validation successful.");
            TokenValidationResponse response = new TokenValidationResponse("Token is valid", true);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("Token validation failed with exception: ", e);
            TokenValidationResponse response = new TokenValidationResponse("Token is invalid", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    // 新增異常處理器
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}

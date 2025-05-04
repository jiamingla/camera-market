package jiamingla.first.camera.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // 明確指定使用 test profile
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected ListingRepository listingRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String token;
    protected Member member;

    // Add this constant
    public static final String API_LISTINGS = "/api/listings";

    @BeforeEach
    public void setup() throws Exception {
        // 1. 創建測試用的 Member
        member = createAndSaveMember("testuser", "testpassword", "test@example.com");

        // 2. 登入並獲取 Token
        token = loginAndGetToken("testuser", "testpassword");

        // 3. 手動設定 Security Context
        setSecurityContext(member.getUsername(), member.getPassword());
    }

    // Helper methods
    protected Member createAndSaveMember(String username, String password, String email) {
        //check if there has member in the database. If has, delete it.
        Optional<Member> existMember = memberRepository.findByUsername(username);
        if (existMember.isPresent()) {
            memberRepository.delete(existMember.get());
        }
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(password));
        member.setEmail(email);
        return memberRepository.save(member);
    }

    protected String loginAndGetToken(String username, String password) throws Exception {
        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseContent).get("token").asText();
    }

    protected void setSecurityContext(String username, String password) {
        UserDetails userDetails = new User(username, password, new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

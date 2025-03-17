package jiamingla.first.camera.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.repository.MemberRepository;
import jiamingla.first.camera.market.repository.PasswordResetTokenRepository;
import jiamingla.first.camera.market.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ForgotPasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired //直接使用email service。
    private EmailService emailService;

    private String apiMembersForgotPassword = "/api/members/forgot-password";

    private Member member;

    @BeforeEach
    public void setup() throws Exception {
        //check if there has member in the database. If has, delete it.
        Optional<Member> existMember = memberRepository.findByUsername("testuser");
        if (existMember.isPresent()) {
            memberRepository.delete(existMember.get());
        }
        //delete token in the database.
        passwordResetTokenRepository.deleteAll();

        // Create a user for testing
        member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        // Update the member variable with the saved member (including the generated ID)
        member = memberRepository.save(member);
    }

    @Test
    @Transactional
    void forgotPassword_Success() throws Exception {
        // 準備測試資料
        String email = "test@example.com";

        // 執行測試
        mockMvc.perform(post(apiMembersForgotPassword) // 發送 POST 請求到 /api/members/forgot-password
                        .param("email", email) // 帶上 email 參數
                        .contentType(MediaType.APPLICATION_JSON)) // 設定內容類型為 JSON
                .andExpect(status().isOk()) // 預期返回狀態碼為 200 OK
                .andExpect(content().string("Password reset link sent to your email.")); // 預期返回內容為成功訊息
    }

    @Test
    void forgotPassword_MemberNotFound() throws Exception {
        // 準備測試資料
        String email = "nonexistent@example.com";

        // 執行測試
        mockMvc.perform(post(apiMembersForgotPassword)
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 預期返回狀態碼為 400 Bad Request
                .andExpect(content().string("Member not found")); // 預期返回內容為錯誤訊息
    }

    @Test
    void forgotPassword_TokenCreateFail() throws Exception {
        // 準備測試資料
        String email = "test@example.com";
        //先移除tokenRepository的所有資料，這樣才能模擬創建失敗
        passwordResetTokenRepository.deleteAll();

        // 執行測試
        mockMvc.perform(post(apiMembersForgotPassword)
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 預期返回狀態碼為 400 Bad Request
                .andExpect(content().string("Token create fail")); // 預期返回內容為錯誤訊息
    }

    @Test
    void forgotPassword_EmailSendFail() throws Exception {
        // 準備測試資料
        String email = "test@example.com";

        // 設定 Mock 行為: 我們需要重新設定mock email service
        doThrow(new MailSendException("Email send failed")).when(emailService).sendPasswordResetEmail(email,member.getPassword()); // 模擬郵件發送失敗，拋出異常

        // 執行測試
        mockMvc.perform(post(apiMembersForgotPassword)
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // 預期返回狀態碼為 500 Internal Server Error
                .andExpect(content().string("An unexpected error occurred.")); // 預期返回內容為錯誤訊息
    }
}

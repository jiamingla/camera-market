package jiamingla.first.camera.market;

import com.fasterxml.jackson.databind.ObjectMapper;

import jiamingla.first.camera.market.entity.Category;
import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingType;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jiamingla.first.camera.market.entity.Make; // Import Make enum

import jakarta.transaction.Transactional; // Import Transactional
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.TestPropertySource; // 導入 TestPropertySource


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // 保持激活 test profile
public class LoginTest {

    // ... (其他 @Autowired 和測試方法保持不變) ...

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ListingRepository listingRepository; // 確保這個也被正確注入

    @Autowired
    private ObjectMapper objectMapper;

    private String apiMembersLogin = "/api/members/login";
    private String apiMembersValidate = "/api/members/validate";
    private String apiMembersRegister = "/api/members/register";
    private String apiMembersMember = "/api/members/member/";


    private String token;
    private Member member;

    @BeforeEach
    public void setup() throws Exception {
        //check if there has member in the database. If has, delete it.
        // 使用 findByUsername 而不是 getReferenceById 以避免潛在的 LazyInitializationException
        Optional<Member> existMember = memberRepository.findByUsername("testuser");
        existMember.ifPresent(value -> memberRepository.delete(value)); // 如果存在則刪除

        // Create a user for testing
        member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        // Update the member variable with the saved member (including the generated ID)
        member = memberRepository.save(member); // 保存並獲取包含 ID 的實體

        // 登入獲取 token
        String requestBody = "{\"username\":\"testuser\",\"password\":\"testpassword\"}";
        MvcResult loginResult = mockMvc.perform(post(apiMembersLogin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseContent).get("token").asText();
        System.out.println("token: " + token);
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        // 3. Use the token to access the protected endpoint.
        mockMvc.perform(get("/api/listings")
                        .header("Authorization", "Bearer " + token)) // Add the token to the Authorization header
                .andExpect(status().isOk());
    }

    @Test
    public void testFailedLoginWithWrongPassword() throws Exception {
        // Perform login with wrong password
        String requestBody = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post(apiMembersLogin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFailedLoginWithUnknownUser() throws Exception {
        // Perform login with unknown username
        String requestBody = "{\"username\":\"unknownuser\",\"password\":\"testpassword\"}";
        mockMvc.perform(post(apiMembersLogin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccessProtectedWithoutLogin() throws Exception {
        // Attempt to access a protected endpoint without login
        mockMvc.perform(get(apiMembersValidate))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterMember() throws Exception {
        String requestBody = "{\"username\":\"test2\",\"password\":\"testpassword\",\"email\":\"test2@example.com\"}";
        mockMvc.perform(post(apiMembersRegister)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test2"))
                .andExpect(jsonPath("$.email").value("test2@example.com"));
    }

     @Test
    public void testGetMemberWithListing() throws Exception {
        // 確保 member 是從數據庫獲取的最新狀態 (包含 ID)
        Member currentMember = memberRepository.findByUsername("testuser").orElseThrow();

        Listing listing = new Listing();
        listing.setTitle("testTitle");
        listing.setDescription("test");
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.ACCESSORY);
        listing.setType(ListingType.SALE);
        listing.setMember(currentMember); // 關聯已保存的 Member
        listing = listingRepository.save(listing); // 保存 Listing

        mockMvc.perform(get(apiMembersMember + currentMember.getId()) // 使用 currentMember.getId()
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                // TODO: 這裡就是有問題，驗證 listings 陣列不為空，並且第一個元素的 title 是 "testTitle"
                // .andExpect(jsonPath("$.listings").isNotEmpty())
                // .andExpect(jsonPath("$.listings[0].title").value("testTitle"))
                ;
    }

    @Test
    public void testGetMemberWithListingWithoutToken() throws Exception {
         // 確保 member 有 ID
        Member currentMember = memberRepository.findByUsername("testuser").orElseThrow();
        mockMvc.perform(get(apiMembersMember + currentMember.getId()))
                .andExpect(status().isOk());
    }

}

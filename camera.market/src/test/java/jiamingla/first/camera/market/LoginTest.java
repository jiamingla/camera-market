package jiamingla.first.camera.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.Listing;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.transaction.Transactional;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String apiMembersLogin = "/api/members/login";
    private String apiMembersRegister = "/api/members/register";
    private String apiMembersMember = "/api/members/member/";


    private String token; // 在這裡聲明 token 變數
    private Member member;

    @BeforeEach
    public void setup() throws Exception {
        //check if there has member in the database. If has, delete it.
        Optional<Member> existMember = memberRepository.findByUsername("testuser");
        if (existMember.isPresent()) {
            memberRepository.delete(existMember.get());
        }

        // Create a user for testing
        member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        // Update the member variable with the saved member (including the generated ID)
        member = memberRepository.save(member);

        //每次都先登入，獲取token
        String requestBody = "{\"username\":\"testuser\",\"password\":\"testpassword\"}";
        MvcResult loginResult = mockMvc.perform(post(apiMembersLogin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        // Extract the token from the response.
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
        mockMvc.perform(get(apiMembersMember+member.getId()))
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
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory("test");
        listing.setSeller(member);
        listingRepository.save(listing);
        mockMvc.perform(get(apiMembersMember + member.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
                // .andExpect(jsonPath("$.listings[0].title").value("test"));
    }

    @Test
    public void testGetMemberWithListingWithoutToken() throws Exception {
        mockMvc.perform(get(apiMembersMember + member.getId()))
                .andExpect(status().isUnauthorized());
    }
}

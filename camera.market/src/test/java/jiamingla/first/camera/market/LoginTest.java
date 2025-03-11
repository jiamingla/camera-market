package jiamingla.first.camera.market;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    private String protectedEndpoint = "/api/listings"; //受保護的路由

    private String token; // 在這裡聲明 token 變數

    @BeforeEach
    public void setup() throws Exception {
        // Create a user for testing
        Member member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        memberRepository.save(member);

        //每次都先登入，獲取token
        String requestBody = "{\"username\":\"testuser\",\"password\":\"testpassword\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/members/login")
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
        mockMvc.perform(get(protectedEndpoint)
                        .header("Authorization", "Bearer " + token)) // Add the token to the Authorization header
                .andExpect(status().isOk());
    }

    @Test
    public void testFailedLoginWithWrongPassword() throws Exception {
        // Perform login with wrong password
        String requestBody = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFailedLoginWithUnknownUser() throws Exception {
        // Perform login with unknown username
        String requestBody = "{\"username\":\"unknownuser\",\"password\":\"testpassword\"}";
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccessProtectedWithoutLogin() throws Exception {
        // Attempt to access a protected endpoint without login
        mockMvc.perform(get(protectedEndpoint)) // Assuming /api/private is a protected endpoint
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccessPublicWithoutLogin() throws Exception {
        // Attempt to access a public endpoint without login
        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test2\", \"password\":\"test\",\"email\":\"test2@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test2"))
                .andExpect(jsonPath("$.email").value("test2@example.com"));
    }

    @Test
    public void testCreateListingWithLogin() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");

        mockMvc.perform(post(protectedEndpoint)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUpdateListingByOther() throws Exception {
        //create user
        Member member = new Member();
        member.setUsername("testuser2");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test2@example.com");
        memberRepository.save(member);

        String requestBody = "{\"username\":\"testuser2\",\"password\":\"testpassword\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Extract the token from the response.
        String responseContent = loginResult.getResponse().getContentAsString();
        String token2 = objectMapper.readTree(responseContent).get("token").asText();

        //create listing
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername("testuser").get());
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12.3);
        newListing.setCategory("test");
        mockMvc.perform(put(protectedEndpoint)
                        .header("Authorization", "Bearer " + token2)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testUpdateListingBySelf() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername("testuser").get());
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12.3);
        newListing.setCategory("test");

        mockMvc.perform(put(protectedEndpoint)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("test2"));
    }

    @Test
    public void testDeleteListingByOther() throws Exception {
        Member member = new Member();
        member.setUsername("testuser2");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test2@example.com");
        memberRepository.save(member);

        //get token
        String requestBody = "{\"username\":\"testuser2\",\"password\":\"testpassword\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Extract the token from the response.
        String responseContent = loginResult.getResponse().getContentAsString();
        String token2 = objectMapper.readTree(responseContent).get("token").asText();

        //create listing
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername("testuser").get());
        listing = listingRepository.save(listing);

        mockMvc.perform(delete(protectedEndpoint + "/" + listing.getId())
                        .header("Authorization", "Bearer " + token2)// Add the token to the Authorization header
                        )
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testDeleteListingBySelf() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername("testuser").get());
        listing = listingRepository.save(listing);

        mockMvc.perform(delete(protectedEndpoint + "/" + listing.getId())
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        )
                .andExpect(status().isNoContent());
    }
}

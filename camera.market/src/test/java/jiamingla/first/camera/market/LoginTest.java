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

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import java.util.Optional;

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

    private String apiListings = "/api/listings"; //受保護的路由
    private String apiMembersMember = "/api/members/member/1"; //公開的路由

    private String token; // 在這裡聲明 token 變數
    private Member member;

    @BeforeEach
    public void setup() throws Exception {
        //check if there has member in the database. If has, delete it.
        Optional<Member> existMember = memberRepository.findByUsername("testuser");
        if(existMember.isPresent()){
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
        mockMvc.perform(get(apiListings)
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
        mockMvc.perform(get(apiMembersMember)) // Assuming /api/private is a protected endpoint
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterMember() throws Exception {
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
        listing.setSeller(member);

        mockMvc.perform(post(apiListings)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testUpdateListingByOther() throws Exception {
        //create user
        Member member2 = new Member();
        member2.setUsername("testuser2");
        member2.setPassword(passwordEncoder.encode("testpassword"));
        member2.setEmail("test2@example.com");
        member2 = memberRepository.save(member2);

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
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12.3);
        newListing.setCategory("test");
        mockMvc.perform(put(apiListings)
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
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12.3);
        newListing.setCategory("test");

        mockMvc.perform(put("/api/listings")
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("test2"));
    }

    @Test
    public void testDeleteListingByOther() throws Exception {
        Member member2 = new Member();
        member2.setUsername("testuser2");
        member2.setPassword(passwordEncoder.encode("testpassword"));
        member2.setEmail("test2@example.com");
        member2 = memberRepository.save(member2);

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
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
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
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        )
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetMemberWithListing() throws Exception{
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(member);
        listingRepository.save(listing);
        mockMvc.perform(get("/api/members/member/"+member.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.listings[0].title").value("test"));
    }

    @Test
    public void testGetMemberWithListingWithoutToken() throws Exception{
        mockMvc.perform(get("/api/members/member/"+member.getId()))
                .andExpect(status().isUnauthorized());
    }

}

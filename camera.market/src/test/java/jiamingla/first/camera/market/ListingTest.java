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
public class ListingTest {

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

    private String apiListings = "/api/listings";
    private String token;
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
    public void testCreateListingWithLogin() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12);
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
        listing.setPrice(12);
        listing.setCategory("test");
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12);
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
        listing.setPrice(12);
        listing.setCategory("test");
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake("test");
        newListing.setModel("test");
        newListing.setPrice(12);
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
        listing.setPrice(12);
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
        listing.setPrice(12);
        listing.setCategory("test");
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        )
                .andExpect(status().isNoContent());
    }
}

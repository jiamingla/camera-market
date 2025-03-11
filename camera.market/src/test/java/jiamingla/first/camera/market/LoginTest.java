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
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

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

    @BeforeEach
    public void setup() {
        // Create a user for testing
        Member member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        memberRepository.save(member);
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        //Perform login
        ResultActions resultActions = mockMvc.perform(get(protectedEndpoint) // Assuming /api/private is a protected endpoint
                        .with(httpBasic("testuser", "testpassword")))
                .andExpect(status().isOk());
    }

    @Test
    public void testFailedLoginWithWrongPassword() throws Exception {
        // Perform login with wrong password
        mockMvc.perform(get(protectedEndpoint) // Assuming /api/private is a protected endpoint
                        .with(httpBasic("testuser", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFailedLoginWithUnknownUser() throws Exception {
        // Perform login with unknown username
        mockMvc.perform(get(protectedEndpoint) // Assuming /api/private is a protected endpoint
                        .with(httpBasic("unknownuser", "testpassword")))
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
        String username = "testuser";
        String password = "testpassword";
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");

        MvcResult mvcResult = mockMvc.perform(post(protectedEndpoint)
                        .with(httpBasic(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    public void testUpdateListingByOther() throws Exception {
        //create user
        Member member = new Member();
        member.setUsername("testuser2");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test2@example.com");
        memberRepository.save(member);
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
                        .with(httpBasic("testuser2", "testpassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testUpdateListingBySelf() throws Exception {
        String username = "testuser";
        String password = "testpassword";

        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername(username).get());
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
                        .with(httpBasic(username, password))
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
                        .with(httpBasic("testuser2", "testpassword")))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void testDeleteListingBySelf() throws Exception {
        String username = "testuser";
        String password = "testpassword";

        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake("test");
        listing.setModel("test");
        listing.setPrice(12.3);
        listing.setCategory("test");
        listing.setSeller(memberRepository.findByUsername(username).get());
        listing = listingRepository.save(listing);

        mockMvc.perform(delete(protectedEndpoint + "/" + listing.getId())
                        .with(httpBasic(username, password)))
                .andExpect(status().isNoContent());
    }
}

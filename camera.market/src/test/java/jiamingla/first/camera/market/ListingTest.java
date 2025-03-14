package jiamingla.first.camera.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.Category; // 引入 Category enum
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import jiamingla.first.camera.market.entity.Make;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
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

        // Manually set the Security Context for the test
        UserDetails userDetails = new User(member.getUsername(), member.getPassword(), new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testCreateListingWithLogin() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);// 修改：使用 Category enum

        mockMvc.perform(post(apiListings)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("test"))
                .andExpect(jsonPath("$.make").value("CANON"))
                .andExpect(jsonPath("$.model").value("test"))
                .andExpect(jsonPath("$.price").value(12))
                .andExpect(jsonPath("$.category").value("DSLR"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createTime").isNotEmpty())// 修改：使用 isEmpty() 檢查，因為我們無法確定時間
                .andExpect(jsonPath("$.lastUpdateTime").isNotEmpty())// 修改：使用 isEmpty() 檢查，因為我們無法確定時間
                .andExpect(jsonPath("$.createdBy").value(member.getUsername()))
                .andExpect(jsonPath("$.lastModifiedBy").value(member.getUsername()))
        ;

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
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR); // 修改：使用 Category enum
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);// 修改：使用 Category enum
        mockMvc.perform(patch(apiListings) // Changed from put to patch
                        .header("Authorization", "Bearer " + token2)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateListingBySelf() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);// 修改：使用 Category enum
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);// 修改：使用 Category enum

        mockMvc.perform(patch("/api/listings") // Changed from put to patch
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("test2"))
                .andExpect(jsonPath("$.lastUpdateTime").isNotEmpty()) //check update time has been updated
                .andExpect(jsonPath("$.lastModifiedBy").value(member.getUsername()));//check update name has been updated
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
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);// 修改：使用 Category enum
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", "Bearer " + token2)// Add the token to the Authorization header
                        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteListingBySelf() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);// 修改：使用 Category enum
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        mockMvc.perform(delete("/api/listings/" + listing.getId())
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        )
                .andExpect(status().isNoContent());
    }
    @Test
    public void testCreateListingWithWrongCategory() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(null); //設定錯誤的category

        mockMvc.perform(post(apiListings)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isBadRequest());
    }
     @Test
    public void testUpdateListingWithWrongCategory() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);// 修改：使用 Category enum
        listing.setSeller(member);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(null);// 修改：使用 Category enum

        mockMvc.perform(patch("/api/listings") // Changed from put to patch
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isBadRequest());
    }
}

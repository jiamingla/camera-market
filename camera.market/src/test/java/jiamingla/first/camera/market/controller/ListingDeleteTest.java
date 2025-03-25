package jiamingla.first.camera.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.*;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ListingDeleteTest extends BaseControllerTest {

    // Remove the trailing slash
    //private String apiListings = "/api/listings/";

    @Test
    public void testDeleteListingBySelf() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setMember(member);
        listing.setType(ListingType.SALE);
        listing = listingRepository.save(listing);

        // Use String.format() to construct the URL
        mockMvc.perform(delete(String.format("%s/%d", API_LISTINGS, listing.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
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
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setMember(member);
        listing.setType(ListingType.SALE);
        listing = listingRepository.save(listing);

        // Use String.format() to construct the URL
        mockMvc.perform(delete(String.format("%s/%d", API_LISTINGS, listing.getId()))
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isBadRequest());
    }
}

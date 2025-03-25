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
public class ListingUpdateTest extends BaseControllerTest {

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
        listing.setMember(member);
        listing.setType(ListingType.SALE);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);// 修改：使用 Category enum
        newListing.setType(ListingType.SALE);
        mockMvc.perform(patch(API_LISTINGS) // Changed from put to patch
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
        listing.setMember(member);
        listing.setType(ListingType.SALE);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);// 修改：使用 Category enum
        newListing.setType(ListingType.SALE);

        mockMvc.perform(patch(API_LISTINGS) // Changed from put to patch
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("test2"))
                .andExpect(jsonPath("$.lastUpdateTime").isNotEmpty()) //check update time has been updated
                .andExpect(jsonPath("$.lastModifiedBy").value(member.getUsername()));//check update name has been updated
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
        listing.setMember(member);
        listing.setType(ListingType.SALE);
        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test2");
        newListing.setMake(Make.CANON); //使用Make enum
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(null);// 修改：使用 Category enum

        mockMvc.perform(patch(API_LISTINGS) // Changed from put to patch
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isBadRequest());
    }

}

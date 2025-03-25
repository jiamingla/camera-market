package jiamingla.first.camera.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.*;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.MemberRepository;
import jiamingla.first.camera.market.repository.TagRepository;
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

    @Autowired
    private TagRepository tagRepository;

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

        mockMvc.perform(patch(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddTagsToExistingListing() throws Exception {
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

        Tag tag1 = new Tag();
        tag1.setName("二手");
        tag1.setType(TagType.CONDITION);
        tag1 = tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("新北市");
        tag2.setType(TagType.LOCATION);
        tag2 = tagRepository.save(tag2);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test");
        newListing.setMake(Make.CANON);
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);
        newListing.setType(ListingType.SALE);
        newListing.getTags().add(tag1);
        newListing.getTags().add(tag2);

        mockMvc.perform(patch(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].id").value(tag1.getId()))
                .andExpect(jsonPath("$.tags[0].name").value("二手"))
                .andExpect(jsonPath("$.tags[0].type").value("CONDITION"))
                .andExpect(jsonPath("$.tags[1].id").value(tag2.getId()))
                .andExpect(jsonPath("$.tags[1].name").value("新北市"))
                .andExpect(jsonPath("$.tags[1].type").value("LOCATION"));
    }

    @Test
    public void testUpdateTagsOfExistingListing() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setMember(member);
        listing.setType(ListingType.SALE);

        Tag tag1 = new Tag();
        tag1.setName("二手");
        tag1.setType(TagType.CONDITION);
        tag1 = tagRepository.save(tag1);
        listing.getTags().add(tag1);

        listing = listingRepository.save(listing);

        Tag tag2 = new Tag();
        tag2.setName("九成新");
        tag2.setType(TagType.CONDITION);
        tag2 = tagRepository.save(tag2);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test");
        newListing.setMake(Make.CANON);
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);
        newListing.setType(ListingType.SALE);
        newListing.getTags().add(tag2);

        mockMvc.perform(patch(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].id").value(tag2.getId()))
                .andExpect(jsonPath("$.tags[0].name").value("九成新"))
                .andExpect(jsonPath("$.tags[0].type").value("CONDITION"));
    }

    @Test
    public void testDeleteTagsOfExistingListing() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setMember(member);
        listing.setType(ListingType.SALE);

        Tag tag1 = new Tag();
        tag1.setName("二手");
        tag1.setType(TagType.CONDITION);
        tag1 = tagRepository.save(tag1);
        listing.getTags().add(tag1);

        listing = listingRepository.save(listing);

        Listing newListing = new Listing();
        newListing.setId(listing.getId());
        newListing.setTitle("test");
        newListing.setDescription("test");
        newListing.setMake(Make.CANON);
        newListing.setModel("test");
        newListing.setPrice(12);
        newListing.setCategory(Category.DSLR);
        newListing.setType(ListingType.SALE);
        newListing.setTags(new ArrayList<>()); // Remove all tags

        mockMvc.perform(patch(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isEmpty());
    }

}

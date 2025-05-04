package jiamingla.first.camera.market.controller;

import jiamingla.first.camera.market.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ListingCreateTest extends BaseControllerTest {


    @Test
    public void testCreateListing() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON);
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setStatus(ListingStatus.OPEN);
        listing.setType(ListingType.SALE);

        //新增圖片網址
        List<ListingImage> images = new ArrayList<>();
        ListingImage listingImage1 = new ListingImage();
        listingImage1.setImageUrl("https://test1.com");
        listingImage1.setListing(listing); //重要
        ListingImage listingImage2 = new ListingImage();
        listingImage2.setImageUrl("https://test2.com");
        listingImage2.setListing(listing);//重要
        images.add(listingImage1);
        images.add(listingImage2);
        listing.setImages(images);

        Tag tag1 = new Tag();
        tag1.setName("二手");
        tag1.setType(TagType.CONDITION);
        listing.getTags().add(tag1);
        Tag tag2 = new Tag();
        tag2.setName("新北市");
        tag2.setType(TagType.LOCATION);
        listing.getTags().add(tag2);

        mockMvc.perform(post(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)
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
                .andExpect(jsonPath("$.type").value("SALE"))
                .andExpect(jsonPath("$.createTime").isNotEmpty())
                .andExpect(jsonPath("$.lastUpdateTime").isNotEmpty())
                .andExpect(jsonPath("$.createdBy").value(member.getUsername()))
                .andExpect(jsonPath("$.lastModifiedBy").value(member.getUsername()))
                .andExpect(jsonPath("$.images[0].imageUrl").value("https://test1.com"))
                .andExpect(jsonPath("$.images[1].imageUrl").value("https://test2.com"))
                // Verify Tag Details
                .andExpect(jsonPath("$.tags[0].id").isNotEmpty())
                .andExpect(jsonPath("$.tags[0].name").value("二手"))
                .andExpect(jsonPath("$.tags[0].type").value("CONDITION"))
                .andExpect(jsonPath("$.tags[1].id").isNotEmpty())
                .andExpect(jsonPath("$.tags[1].name").value("新北市"))
                .andExpect(jsonPath("$.tags[1].type").value("LOCATION"))
        ;
    }

    @Test
    public void testCreateListingWithWrongCategory() throws Exception {
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setType(ListingType.SALE);
        listing.setCategory(null); //設定錯誤的category

        mockMvc.perform(post(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)// Add the token to the Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateListingWithTooMuchTags() throws Exception {
    
        Listing listing = new Listing();
        listing.setTitle("test");
        listing.setDescription("test");
        listing.setMake(Make.CANON); //使用Make enum
        listing.setModel("test");
        listing.setPrice(12);
        listing.setCategory(Category.DSLR);
        listing.setType(ListingType.SALE);

        //新增圖片網址
        List<ListingImage> images = new ArrayList<>();
        ListingImage listingImage1 = new ListingImage();
        listingImage1.setImageUrl("https://test1.com");
        listingImage1.setListing(listing); //重要
        ListingImage listingImage2 = new ListingImage();
        listingImage2.setImageUrl("https://test2.com");
        listingImage2.setListing(listing);//重要
        images.add(listingImage1);
        images.add(listingImage2);
        listing.setImages(images);

        Tag tag1 = new Tag();
        tag1.setName("二手");
        tag1.setType(TagType.CONDITION);
        listing.getTags().add(tag1);
        Tag tag2 = new Tag();
        tag2.setName("新北市");
        tag2.setType(TagType.LOCATION);
        listing.getTags().add(tag2);
        Tag tag3 = new Tag();
        tag3.setName("黑色");
        tag3.setType(TagType.COLOR);
        listing.getTags().add(tag3);
        Tag tag4 = new Tag();
        tag4.setName("大");
        tag4.setType(TagType.SIZE);
        listing.getTags().add(tag4);
        Tag tag5 = new Tag();
        tag5.setName("其他");
        tag5.setType(TagType.OTHER);
        listing.getTags().add(tag5);
        Tag tag6 = new Tag();
        tag6.setName("SONY");
        tag6.setType(TagType.OTHER);
        listing.getTags().add(tag6);
        Tag tag7 = new Tag();
        tag7.setName("A6000");
        tag7.setType(TagType.MODEL);
        listing.getTags().add(tag7);
        Tag tag8 = new Tag();
        tag8.setName("CAMERA");
        tag8.setType(TagType.CATEGORY);
        listing.getTags().add(tag8);
        Tag tag9 = new Tag();
        tag9.setName("台北市");
        tag9.setType(TagType.LOCATION);
        listing.getTags().add(tag9);
        Tag tag10 = new Tag();
        tag10.setName("北市");
        tag10.setType(TagType.LOCATION);
        listing.getTags().add(tag10);
        Tag tag11 = new Tag();
        tag11.setName("台北");
        tag11.setType(TagType.LOCATION);
        listing.getTags().add(tag11);

        mockMvc.perform(post(API_LISTINGS)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listing)))
                .andExpect(status().isBadRequest());
    }
}

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
        listing.getTags().add(tag1);
        Tag tag2 = new Tag();
        tag2.setName("八成新");
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
                .andExpect(jsonPath("$.tags[0].name").value("二手"))
                .andExpect(jsonPath("$.tags[1].name").value("八成新"))
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

}

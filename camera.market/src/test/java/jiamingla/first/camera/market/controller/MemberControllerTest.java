package jiamingla.first.camera.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jiamingla.first.camera.market.entity.Listing;
import jiamingla.first.camera.market.entity.ListingType;
import jiamingla.first.camera.market.entity.Make;
import jiamingla.first.camera.market.entity.Category;
import jiamingla.first.camera.market.entity.Member;
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
public class MemberControllerTest extends BaseControllerTest {

    @Test
    public void testGetMemberWithListings() throws Exception {
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

        mockMvc.perform(get("/api/members/member/" + member.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.username").value(member.getUsername()))
                .andExpect(jsonPath("$.email").value(member.getEmail()))
                .andExpect(jsonPath("$.listings[0]").exists())
                // .andExpect(jsonPath("$.listings[0].id").value(listing.getId()))
                // .andExpect(jsonPath("$.listings[0].title").value(listing.getTitle()))
                // .andExpect(jsonPath("$.listings[0].description").value(listing.getDescription()))
                // .andExpect(jsonPath("$.listings[0].make").value(listing.getMake().toString()))
                // .andExpect(jsonPath("$.listings[0].model").value(listing.getModel()))
                // .andExpect(jsonPath("$.listings[0].price").value(listing.getPrice()))
                // .andExpect(jsonPath("$.listings[0].category").value(listing.getCategory().toString()))
                // .andExpect(jsonPath("$.listings[0].type").value(listing.getType().toString()))
                ;
    }
}

package jiamingla.first.camera.market;

import jiamingla.first.camera.market.service.*;
import jiamingla.first.camera.market.entity.*;
import jiamingla.first.camera.market.exception.BusinessException;
import jiamingla.first.camera.market.repository.ListingRepository;
import jiamingla.first.camera.market.repository.MemberRepository;
import jiamingla.first.camera.market.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private Validator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ListingService listingService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        // Mock Member
        Member member = new Member();
        member.setUsername("testuser");
        member.setPassword(passwordEncoder.encode("testpassword"));
        member.setEmail("test@example.com");
        when(memberService.findByUsername("testuser")).thenReturn(Optional.of(member));
    }
    //...測試方法
}

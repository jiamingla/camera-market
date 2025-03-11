package jiamingla.first.camera.market.service;

import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> member = memberRepository.findByUsername(username);
        if (member.isEmpty()) {
            throw new UsernameNotFoundException("Username not found");
        }
        Member findMember = member.get();
        System.out.println("Found user: " + findMember.getUsername() + ", Password: " + findMember.getPassword()); // Add this line

        return new User(findMember.getUsername(), findMember.getPassword(), new ArrayList<>());
    }
}

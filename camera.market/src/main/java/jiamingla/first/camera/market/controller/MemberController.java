package jiamingla.first.camera.market.controller;

import jakarta.validation.Valid;
import jiamingla.first.camera.market.entity.Member;
import jiamingla.first.camera.market.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<Member> registerMember(@Valid @RequestBody Member member) {
        Member registeredMember = memberService.registerMember(member);
        return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
    }
}

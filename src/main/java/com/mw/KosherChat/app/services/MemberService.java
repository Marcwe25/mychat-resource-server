package com.mw.KosherChat.app.services;

import com.mw.KosherChat.app.model.Member;
import com.mw.KosherChat.app.modelView.MemberDTO;
import com.mw.KosherChat.app.repositories.MemberRepository;
import com.mw.KosherChat.app.repositories.MemberRoomRepository;
import com.mw.KosherChat.model.ISSIdentity;
import com.mw.KosherChat.model.Oauth2CustomUser;
import com.mw.KosherChat.services.Oauth2CustomUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private MemberRepository memberRepository;
    private Oauth2CustomUserService oauth2Service;
    private MemberRoomRepository memberRoomRepository;
    private RegisteredMember registeredMember;

    @Autowired
    public MemberService(
            MemberRepository memberRepository,
            Oauth2CustomUserService oauth2CustomUserService,
            MemberRoomRepository memberRoomRepository,
            RegisteredMember registeredMember
    ) {

        this.memberRepository = memberRepository;
        this.oauth2Service = oauth2CustomUserService;
        this.memberRoomRepository = memberRoomRepository;
        this.registeredMember = registeredMember;
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    public Member findMemberByUsername(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow();
        return member;
    }

    public Member getOrSaveMemberByUsername(String username) {
        Member member = findMemberByUsername(username);
        if (member == null) {
            member = Member.builder().username(username).build();
            memberRepository.save(member);
        }
        return member;
    }

    public Member updateMember(Member memberUpdates, Authentication authentication) throws Exception{
        String username = memberUpdates.getUsername();
        Member member = memberRepository.findByUsername(username).orElseThrow();
        String updates_given_name = memberUpdates.getGiven_name();
        if(updates_given_name!= null && updates_given_name.trim().length()>0) {
            member.setGiven_name(updates_given_name.trim());
        }
        String updates_family_name = memberUpdates.getFamily_name();
        if(updates_family_name!= null && updates_family_name.trim().length()>0) {
            member.setFamily_name(updates_family_name);
        }

        return memberRepository.save(member);
    }

    public void deleteMember(Member chatMember) {
        memberRepository.delete(chatMember);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member createMember(Member member){
        return memberRepository.save(member);
    }

    public Map<Long, MemberDTO> getFriends(Authentication authentication) throws Exception {
        Member member = registeredMember.findRegisteredMember(authentication);
        Map<Long, MemberDTO> friends = memberRoomRepository
                .findByMemberIdAndDeletedFalse(member.getId())
                .parallelStream()
                .map(memberRoom -> memberRoom.getRoom().getId())
                .map(roomId -> memberRoomRepository.findByRoomId(roomId))
                .flatMap(memberRooms -> memberRooms.stream())
                .map(memberRoom -> memberRoom.getMember())
                .distinct()
                .collect(Collectors.toMap(
                        Member::getId,
                        MemberDTO::from
                        )
                );

        return friends;
    }

    public String getSub(Member member) {
        ISSIdentity iss = member.getIss();
        switch (iss) {
            case GOOGLE -> {
                String email = member.getUsername();
                Optional<Oauth2CustomUser> optional = oauth2Service.findByEmail(email);
                if(optional.isEmpty()) return null;
                return optional.get().getSub();
            }
            default -> {
                return member.getId().toString();
            }
        }
    }

    public Member saveMember (Member member) {
        return memberRepository.save(member);
    }
}
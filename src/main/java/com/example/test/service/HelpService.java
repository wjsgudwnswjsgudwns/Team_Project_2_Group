package com.example.test.service;

import com.example.test.dto.HelpDto;
import com.example.test.entity.Help;
import com.example.test.entity.User;
import com.example.test.repository.HelpRepository;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelpService {

    @Autowired
    private HelpRepository helpRepository;

    @Autowired
    private UserRepository userRepository;

    // 회원 문의
    public Help createHelpForMember (HelpDto helpDto, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Help help = new Help();
        help.setTitle(helpDto.getTitle());
        help.setContent(helpDto.getContent());
        help.setUser(user);

        help.setEmail(user.getEmail());
        help.setPhone(helpDto.getPhone());

        helpRepository.save(help);

        return help;
    }

    // 비회원 문의
    public Help createHelpForGuest (HelpDto helpDto) {
        Help help = new Help();
        help.setTitle(helpDto.getTitle());
        help.setContent(helpDto.getContent());
        help.setName(helpDto.getName());
        help.setEmail(helpDto.getEmail());
        help.setPhone(helpDto.getPhone());
        help.setUser(null);

        helpRepository.save(help);
        return help;
    }

    // 문의 삭제
    public void deleteHelp(Long id) {
        helpRepository.deleteById(id);
    }

    // 나의 문의 내역
    public List<Help> getUserHelps (String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return helpRepository.findByUserOrderByInquiryDateDesc(user);
    }

    // 특정 문의 조회
    public Help getHelp (Long id) {
        return helpRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    }

    // 모든 문의 조회
    public List<Help> getAllHelps () {
        return helpRepository.findAll();
    }

    // 답변 상태 업데이트
    public void updateAnswerStatus(Long id, boolean isAnswered) {
        Help help = helpRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
        help.setAnswered(isAnswered);
    }

    // 비회원 문의
    public List<Help> getGuestHelps (String name, String phone) {
        return helpRepository.findByNameAndPhoneAndUserIsNullOrderByInquiryDateDesc(name, phone);
    }

    // 비회원 문의 삭제 권환 확인
    public boolean canGuestDeleteHelp(Long helpId, String name, String phone) {
        Help help = helpRepository.findById(helpId).orElse(null);

        if (help == null || help.getUser() != null) {
            return false; // 문의가 없거나 회원 문의인 경우
        }

        // 이름과 전화번호가 일치하는지 확인
        return help.getName().equals(name) && help.getPhone().equals(phone);
    }
}

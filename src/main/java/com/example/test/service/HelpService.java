package com.example.test.service;

import com.example.test.dto.HelpDto;
import com.example.test.entity.Help;
import com.example.test.entity.User;
import com.example.test.repository.HelpRepository;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 나의 문의 내역 (페이징)
    public Page<Help> getUserHelps (String username, int page, int size) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("inquiryDate").descending());
        return helpRepository.findByUser(user, pageable);
    }

    // 특정 문의 조회 (답변 포함)
    @Transactional(readOnly = true)
    public Help getHelp (Long id) {
        return helpRepository.findByIdWithAnswer(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    }

    // 모든 문의 조회 (페이징)
    public Page<Help> getAllHelps (int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("inquiryDate").descending());
        return helpRepository.findAll(pageable);
    }

    // 답변 상태 업데이트
    public void updateAnswerStatus(Long id, boolean isAnswered) {
        Help help = helpRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
        help.setAnswered(isAnswered);
    }

    // 비회원 문의 (페이징)
    public Page<Help> getGuestHelps (String name, String phone, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("inquiryDate").descending());
        return helpRepository.findByNameAndPhoneAndUserIsNull(name, phone, pageable);
    }

    // 비회원 문의 삭제 권환 확인
    public boolean canGuestDeleteHelp(Long helpId, String name, String phone) {
        Help help = helpRepository.findById(helpId).orElse(null);

        if (help == null || help.getUser() != null) {
            return false;
        }

        return help.getName().equals(name) && help.getPhone().equals(phone);
    }
}
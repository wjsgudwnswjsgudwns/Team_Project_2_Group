package com.example.test.service;

import com.example.test.dto.HelpAnswerDto;
import com.example.test.entity.Help;
import com.example.test.entity.HelpAnswer;
import com.example.test.entity.User;
import com.example.test.repository.HelpAnswerRepository;
import com.example.test.repository.HelpRepository;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelpAnswerService {

    @Autowired
    private HelpAnswerRepository helpAnswerRepository;

    @Autowired
    private HelpRepository helpRepository;

    @Autowired
    private UserRepository userRepository;

    // 답변 작성 또는 수정
    @Transactional
    public void createAnswer(Long helpId, HelpAnswerDto dto, String username) {
        Help help = helpRepository.findById(helpId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        HelpAnswer helpAnswer;

        if (help.getHelpAnswer() != null) {
            helpAnswer = help.getHelpAnswer();
            helpAnswer.setAnswer(dto.getAnswer());
            helpAnswer.setAdmin(admin);
        } else {
            helpAnswer = new HelpAnswer();
            helpAnswer.setAnswer(dto.getAnswer());
            helpAnswer.setHelp(help);
            helpAnswer.setAdmin(admin);
            help.setHelpAnswer(helpAnswer);
        }

        helpAnswerRepository.save(helpAnswer);

        // ⭐ 수정: setAnswered 대신 set + Is + Answered
        help.setAnswered(true);  // 또는 help.setAnswered(true)
        helpRepository.save(help);
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(Long helpId) {
        Help help = helpRepository.findById(helpId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (help.getHelpAnswer() == null) {
            throw new IllegalArgumentException("답변이 존재하지 않습니다.");
        }

        HelpAnswer helpAnswer = help.getHelpAnswer();

        // ⭐ 답변 대기 상태로 업데이트
        help.setAnswered(false);
        help.setHelpAnswer(null);
        helpRepository.save(help);

        helpAnswerRepository.delete(helpAnswer);
    }
}
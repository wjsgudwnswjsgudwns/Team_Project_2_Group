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

    // 답변 작성
    public void createAnswer(Long helpId, HelpAnswerDto dto, String username) {
        Help help = helpRepository.findById(helpId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (help.isAnswered()) {
            throw new IllegalStateException("이미 답변이 완료된 문의입니다.");
        }

        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        HelpAnswer helpAnswer = new HelpAnswer();
        helpAnswer.setAnswer(dto.getAnswer());
        helpAnswer.setHelp(help);
        helpAnswer.setAdmin(admin);

        helpAnswerRepository.save(helpAnswer);

        help.setAnswered(true);
        help.setHelpAnswer(helpAnswer);
    }

    // 답변 삭제
    public void deleteAnswer(Long helpId) {
        Help help = helpRepository.findById(helpId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (help.getHelpAnswer() == null) {
            throw new IllegalArgumentException("답변이 존재하지 않습니다.");
        }

        help.setAnswered(false);
        help.setHelpAnswer(null);

        helpAnswerRepository.delete(help.getHelpAnswer());
    }
}
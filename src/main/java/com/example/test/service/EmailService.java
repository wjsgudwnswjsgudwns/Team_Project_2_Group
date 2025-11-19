package com.example.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 인증 코드 저장 (실제로는 Redis 사용 권장)
    private Map<String, String> verificationCodes = new HashMap<>();

    // 인증 완료 여부 저장 (비밀번호 재설정 권한)
    private Map<String, String> verifiedUsers  = new HashMap<>();

    // 랜덤 인증 코드 생성 (6자리)
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void sendUsernameVerificationCode(String email) {
        String code = generateVerificationCode();
        verificationCodes.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("opticore@gmail.com");
        message.setTo(email);
        message.setSubject("[OPTICORE] 아이디 찾기 인증 코드");
        message.setText(
                "안녕하세요, OPTICORE입니다.\n\n" +
                        "아이디 찾기를 위한 인증 코드입니다.\n\n" +
                        "인증 코드: " + code + "\n\n" +
                        "인증 코드는 10분간 유효합니다.\n\n" +
                        "감사합니다."
        );

        mailSender.send(message);

        // 10분 후 인증 코드 삭제
        new Thread(() -> {
            try {
                Thread.sleep(10 * 60 * 1000); // 10분
                verificationCodes.remove(email);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 비밀번호 찾기 - 인증 코드 전송
    public void sendPasswordResetVerificationCode(String email) {
        try {
            String code = generateVerificationCode();
            verificationCodes.put(email, code);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("opticore@gmail.com");
            message.setTo(email);
            message.setSubject("[OPTICORE] 비밀번호 재설정 인증 코드");
            message.setText(
                    "안녕하세요, OPTICORE입니다.\n\n" +
                            "비밀번호 재설정을 위한 인증 코드입니다.\n\n" +
                            "인증 코드: " + code + "\n\n" +
                            "인증 코드는 10분간 유효합니다.\n\n" +
                            "감사합니다."
            );

            System.out.println("이메일 전송 시도: " + email);
            mailSender.send(message);
            System.out.println("이메일 전송 완료: " + email);

            // 10분 후 인증 코드 삭제
            new Thread(() -> {
                try {
                    Thread.sleep(10 * 60 * 1000);
                    verificationCodes.remove(email);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("이메일 전송 실패: " + email);
            e.printStackTrace();
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }

    // 인증 코드 검증 (아이디 찾기용)
    public boolean verifyCodeForUsername(String email, String code, String username) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(email);
            verifiedUsers.put(email, username); // String 저장

            // 30분 후 인증 완료 상태 삭제
            final String emailKey = email;
            new Thread(() -> {
                try {
                    Thread.sleep(30 * 60 * 1000); // 30분
                    verifiedUsers.remove(emailKey);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return true;
        }
        return false;
    }

    // 인증 코드 검증 (비밀번호 찾기용)
    public boolean verifyCodeForPassword(String email, String code, String username) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(email);
            verifiedUsers.put(email, username); // String 저장

            // 30분 후 인증 완료 상태 삭제
            final String emailKey = email;
            new Thread(() -> {
                try {
                    Thread.sleep(30 * 60 * 1000); // 30분
                    verifiedUsers.remove(emailKey);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return true;
        }
        return false;
    }

    // 이메일 인증 완료 여부 확인
    public boolean isEmailVerified(String email, String username) {
        String storedUsername = verifiedUsers.get(email);
        return storedUsername != null && storedUsername.equals(username);
    }

    // 인증 상태 제거
    public void clearVerification(String email) {
        verifiedUsers.remove(email);
    }

}

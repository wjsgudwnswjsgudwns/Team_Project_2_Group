package com.example.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import com.example.test.entity.EmailVerification;
import com.example.test.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 인증 코드 저장 (실제로는 Redis 사용 권장)
    private Map<String, String> verificationCodes = new HashMap<>();

    // 인증 완료 여부 저장 (비밀번호 재설정 권한)
    private Map<String, String> verifiedUsers  = new HashMap<>();

    @Autowired
    private EmailVerificationRepository verificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 6자리 랜덤 인증 코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
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
    public boolean isEmailVerified2(String email, String username) {
        String storedUsername = verifiedUsers.get(email);
        return storedUsername != null && storedUsername.equals(username);
    }

    // 인증 상태 제거
    public void clearVerification(String email) {
        verifiedUsers.remove(email);
    }

    /**
     * 이메일 인증 코드 발송
     */
    public void sendVerificationEmail(String toEmail, String purpose) throws MessagingException {
        // 기존 미인증 코드 삭제
        verificationRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(toEmail, purpose)
                .ifPresent(verificationRepository::delete);

        // 새 인증 코드 생성
        String code = generateVerificationCode();

        // DB에 저장 (5분 유효)
        EmailVerification verification = new EmailVerification();
        verification.setEmail(toEmail);
        verification.setVerificationCode(code);
        verification.setPurpose(purpose);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationRepository.save(verification);

        // 이메일 발송
        String subject = "[OPTICORE] 이메일 인증 코드";
        String content = buildEmailContent(code, purpose);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, "opticore");  // ✅ try-catch로 감싸기
        } catch (Exception e) {
            helper.setFrom(fromEmail);  // 실패 시 이름 없이 발송
        }

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * 인증 코드 확인
     */
    public boolean verifyCode(String email, String code, String purpose) {
        Optional<EmailVerification> verification = verificationRepository
                .findByEmailAndVerificationCodeAndPurpose(email, code, purpose);

        if (verification.isEmpty()) {
            return false;
        }

        EmailVerification v = verification.get();

        // 만료 확인
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 인증 완료 처리
        v.setVerified(true);
        verificationRepository.save(v);

        return true;
    }

    /**
     * 이메일 인증 여부 확인
     */
    public boolean isEmailVerified(String email, String purpose) {
        return verificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    /**
     * 이메일 내용 생성
     */
    private String buildEmailContent(String code, String purpose) {
        String purposeText;
        switch (purpose) {
            case "SIGNUP":
                purposeText = "회원가입";
                break;
            case "FIND_ID":
                purposeText = "아이디 찾기";
                break;
            case "FIND_PASSWORD":
                purposeText = "비밀번호 재설정";
                break;
            case "CHANGE_EMAIL":
                purposeText = "이메일 변경";
                break;
            default:
                purposeText = "인증";
        }

        return "<html>" +
                "<body style='font-family: Arial, sans-serif; background: #000; color: #fff; padding: 40px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #1a1a1a; border: 2px solid #fff; padding: 40px;'>" +
                "<h2 style='color: #fff; text-align: center; margin-bottom: 30px; letter-spacing: 2px;'>OPTICORE</h2>" +
                "<p style='color: #aaa; font-size: 14px; margin-bottom: 20px;'>안녕하세요.</p>" +
                "<p style='color: #aaa; font-size: 14px; margin-bottom: 30px;'>" + purposeText + "을 위한 인증 코드입니다.</p>" +
                "<div style='background: #000; border: 2px solid #ff3b30; padding: 30px; text-align: center; margin: 30px 0;'>" +
                "<p style='color: #888; font-size: 12px; margin-bottom: 10px;'>인증 코드</p>" +
                "<h1 style='color: #fff; font-size: 36px; letter-spacing: 8px; margin: 0;'>" + code + "</h1>" +
                "</div>" +
                "<p style='color: #666; font-size: 12px; text-align: center;'>이 코드는 5분간 유효합니다.</p>" +
                "<p style='color: #666; font-size: 12px; text-align: center; margin-top: 20px;'>본인이 요청하지 않았다면 이 이메일을 무시하세요.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 만료된 인증 코드 정리 (스케줄러로 주기적 실행 권장)
     */


    public void sendUsernameVerificationCode(String email) {
        try {
            String code = generateVerificationCode();
            verificationCodes.put(email, code);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("opticore@gmail.com");
            helper.setTo(email);
            helper.setSubject("[OPTICORE] 아이디 찾기 인증 코드");
            helper.setText(buildEmailContent(code, "FIND_ID"), true);

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
        } catch (MessagingException e) {
            System.err.println("이메일 전송 실패: " + email);
            e.printStackTrace();
            throw new RuntimeException("이메일 전송에 실패했습니다: " + e.getMessage());
        }
    }

    // 비밀번호 찾기 - 인증 코드 전송
    public void sendPasswordResetVerificationCode(String email) {
        try {
            String code = generateVerificationCode();
            verificationCodes.put(email, code);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("opticore@gmail.com");
            helper.setTo(email);
            helper.setSubject("[OPTICORE] 비밀번호 재설정 인증 코드");
            helper.setText(buildEmailContent(code, "FIND_PASSWORD"), true);

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
    public void cleanupExpiredCodes() {
        verificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

}

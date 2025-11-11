package com.readour.common.service;

import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendPasswordResetMail(String to, String temporaryPassword) {
        String subject = "[ReadOur] 임시 비밀번호 안내";
        String text = """
                안녕하세요, ReadOur 입니다.

                아래 임시 비밀번호로 로그인 후 반드시 비밀번호를 변경해 주세요.

                임시 비밀번호: %s

                감사합니다.
                """.formatted(temporaryPassword);
        sendSimpleMessage(to, subject, text);
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(from);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("메일 발송 실패 - to: {}", to, ex);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "메일 발송에 실패했습니다.");
        }
    }
}

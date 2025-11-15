package com.swp.service;


import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendEmailActiveAccount(final String to, final String subject, String code) {
        final var message = mailSender.createMimeMessage();
        try {
            final var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            final var context = new Context();
            context.setVariable("code", code);
            final var htmlContent = templateEngine.process("email/active-account", context);
            helper.setText(htmlContent, true); // `true` để hỗ trợ HTML
//            helper.addInline("studywork-logo", new ClassPathResource("static/img/Logo.png"));
            mailSender.send(message);
        } catch (final MessagingException e) {
            log.error(e.getMessage(), e);
//            throw new BusinessException(e.getMessage());
        }
    }
}
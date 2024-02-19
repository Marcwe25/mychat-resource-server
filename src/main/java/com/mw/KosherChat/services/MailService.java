package com.mw.KosherChat.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;
    @Value("${spring.mail.username}")
    private String postmaster;


    private void sendHtmlMessage(
            String to,
            String subject,
            String text
    ) throws MessagingException {

        // initializing mime message
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // setting parameter
        helper.setFrom(postmaster);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        // sending
        emailSender.send(message);
    }

    private void sendMessageUsingThymeleafTemplate(
            String to, String subject, Map<String, Object> templateModel)
            throws MessagingException {

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = thymeleafTemplateEngine.process("validation-template-thymeleaf.html", thymeleafContext);

        sendHtmlMessage(to, subject, htmlBody);
    }

//    @PostConstruct
    public void sendValidationMessage(String user, String token) throws MessagingException {
        Map<String,Object> templateModelVariable = new HashMap<>();
        templateModelVariable.put("user", user);
        templateModelVariable.put("token", token);

        sendMessageUsingThymeleafTemplate("marc@wewehappy.com", "Registration on www.wewehappy.com",templateModelVariable);
    }

}

package com.flipshelf.serviceimpl;

import com.flipshelf.model.Purchase;
import com.flipshelf.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;
    @Async
    @Override
    public void sendPurchaseConfirmation(String to, Purchase purchase) {
        Context context = new Context();
        context.setVariable("email", purchase.getUserEmail());
        context.setVariable("name", purchase.getName());
        context.setVariable("quantity", purchase.getQuantity());
        context.setVariable("total", purchase.getTotalPrice());
        context.setVariable("date", purchase.getOrderDate());

        String body = templateEngine.process("order-confirmation", context);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Order Confirmation - FlipShelf");
            helper.setText(body, true); // HTML enabled
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // or log properly
        }
    }
}

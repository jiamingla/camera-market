package jiamingla.first.camera.market.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service // 標記這個類是一個 Spring 服務類
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}") // 從 application.properties 讀取郵件寄件人資訊
    private String emailFrom;

    // 發送密碼重置郵件
    public void sendPasswordResetEmail(String to, String token) {
        logger.info("Sending password reset email to: {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom); // 設定寄件人
        message.setTo(to); // 設定收件人
        message.setSubject("Reset Your Password"); // 設定郵件主題
        message.setText("To reset your password, please click the following link:\n" // 設定郵件內容
                + "http://localhost:8080/api/member/reset-password?token=" + token); // 包含重置密碼的連結，連結中帶有令牌

        mailSender.send(message); // 發送郵件
        logger.info("Password reset email sent successfully to: {}", to);
    }
}

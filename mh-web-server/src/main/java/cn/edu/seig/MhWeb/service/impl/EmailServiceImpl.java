package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.service.EmailService;
import cn.edu.seig.MhWeb.util.RandomCodeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 
 *
 * @author su
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果，包含是否成功
     */
    public boolean sendEmail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();//JavaMail 定义的邮件消息对象封装邮件
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(from);//发件人
            helper.setTo(to);//收件人
            helper.setSubject(subject);//主题
            helper.setText(content);
            mailSender.send(mimeMessage);
            return true;
        } catch (MessagingException e) {
            log.error(MessageConstant.EMAIL_SEND_FAILED, e);
            return false;
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param email 收件人地址
     * @return 发送结果，包含是否成功和验证码
     */
    public String sendVerificationCodeEmail(String email) {
        String verificationCode = RandomCodeUtil.generateRandomCode();
        String subject = "【SuMusic】验证码";
        String content = "您的验证码为：" + verificationCode;
        boolean success = sendEmail(email, subject, content);
        if (success) {
            return verificationCode;
        } else {
            return null;
        }
    }
}

package com.ocd.util.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;

/**
 * @author OCD
 * @date 2022/06/16 16:16
 * Description:
 * 发送邮件工具类
 */

@Service
public class EmailUtil implements EmailService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //Spring Boot 提供了一个发送邮件的简单抽象，使用的是下面这个接口，这里直接注入即可使用
    @Resource
    private JavaMailSender javaMailSender;

    // 配置文件中的邮箱
    @Value("${spring.mail.from}")
    private String from;

    /**
     * 简单文本邮件
     *
     * @param to      收件人 多个时参数形式 ："xxx@xxx.com,xxx@xxx.com,xxx@xxx.com"
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        // 获取MimeMessage对象
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            // 邮件发送人
            messageHelper.setFrom(from);
            // 邮件接收人,设置多个收件人地址
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            messageHelper.setTo(internetAddressTo);
            // messageHelper.setTo(to);
            // 邮件主题
            message.setSubject(subject);
            // 邮件内容，html格式
            messageHelper.setText(content, false);
            //发送
            javaMailSender.send(message);
            //日志信息
            logger.info("邮件已经发送.");
        } catch (Exception e) {
            logger.error("发送邮件时发生异常!", e);
        }
    }

    /**
     * html邮件
     *
     * @param to      收件人,多个时参数形式 ："xxx@xxx.com,xxx@xxx.com,xxx@xxx.com"
     * @param subject 主题
     * @param content 内容
     */
    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件发送人
            messageHelper.setFrom(from);
            //邮件接收人,设置多个收件人地址
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            messageHelper.setTo(internetAddressTo);
            //messageHelper.setTo(to);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            javaMailSender.send(message);
            //日志信息
            logger.info("邮件已经发送.");
        } catch (Exception e) {
            logger.error("发送邮件时发生异常!", e);
        }
    }

    /**
     * 带附件的邮件
     *
     * @param to       收件人
     * @param subject  主题
     * @param content  内容
     * @param filePath 附件
     */
    @Override
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(from);
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            messageHelper.setTo(internetAddressTo);
            messageHelper.setSubject(subject);
            messageHelper.setText(content);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            messageHelper.addAttachment(fileName, file);
            javaMailSender.send(mimeMessage);
            // 日志信息
            logger.info("邮件已经发送.");
        } catch (Exception e) {
            logger.error("发送邮件时发生异常!", e);
        }
    }

}
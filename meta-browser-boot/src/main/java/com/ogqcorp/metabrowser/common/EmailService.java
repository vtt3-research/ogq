package com.ogqcorp.metabrowser.common;

import javax.mail.internet.InternetAddress;

public interface EmailService {

    void sendSimpleMailMessage(String to, String subject, String text);
    void sendMimeMessage(InternetAddress from, String to, String subject, String text);
    void sendMimeMessage(String from, String to, String subject, String text);
    void sendMimeMessage(String to, String subject, String text);
}

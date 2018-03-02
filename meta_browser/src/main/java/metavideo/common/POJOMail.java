package metavideo.common;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.PasswordAuthentication;
import java.util.Properties;


public class POJOMail {
    public final static String HOST="smtp.gmail.com";
    public final static String ENCODING="utf-8";
    public final static int SSL_PORT=465;
    public final static int TLS_PORT=587;
    public final static String USER_NAME="ogq_vtt@ogqcorp.com";
    public final static String USER_PASSWORD="asdf0987";

    private final static String JOIN_TITLE="메타데이터 브라우저 회원가입을 축하합니다.";
    private final static String JOIN_BODY="<BR>" +
            "<BR>\"#USERNAME#\"님 안녕하세요.\n" +
            "<BR>\n" +
            "<BR>메타데이터 브라우저에 회원 가입하신 것을 축하합니다.\n" +
            "<BR>\n" +
            "<BR>회원 가입을 완료 하시려면 아래 회원가입 완료 링크를 클릭해주세요.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR><A HREF='http://ec2-13-125-78-13.ap-northeast-2.compute.amazonaws.com:8080/metavideo/account/login?certification=#CERTIFICATION#'>회원가입 완료하러가기 ></A>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>감사합니다.\n" +
            "<BR>\n" +
            "<BR>(주)OGQ 드림" +
            "<BR>";

    private final static String ID_TITLE="\t[요청] 아이디 찾기 요청입니다.";
    private final static String ID_BODY="<BR>" +
            "<BR>관리자님 안녕하세요.\n" +
            "<BR>\n" +
            "<BR>메타데이터 회원님이 아래 정보로 아이디 찾기를 요청하였습니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>소속: \"#ORGAN#\"\n" +
            "<BR>\n" +
            "<BR>이름: \"#USERNAME#\"\n" +
            "<BR>\n" +
            "<BR>이메일: \"#MAIL_ADDRESS#\"\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>감사합니다.\n" +
            "<BR>\n" +
            "<BR>(주)OGQ 드림" +
            "<BR>";

    private final static String PW_TITLE="메타데이터 브라우저 임시 비밀번호 발급 알림입니다.";
    private final static String PW_BODY="<BR>" +
            "<BR>\"#USERNAME#\"님 안녕하세요.\n" +
            "<BR>\n" +
            "<BR>메타데이터 브라우저 운영자 입니다.\n" +
            "<BR>\n" +
            "<BR>임시 비밀번호를 보내드립니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>#PASSWORD#" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>비밀번호는 \"마이페이지\"에 접근하시어 변경이 가능합니다.\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>\n" +
            "<BR>감사합니다.\n" +
            "<BR>\n" +
            "<BR>(주)OGQ 드림" +
            "<BR>";

    public static void sendJoinMail(String to, String username, String certification){
        s_send(to, JOIN_TITLE, JOIN_BODY
                .replaceAll("#CERTIFICATION#",certification)
                .replaceAll("#USERNAME#",username)
        );

    }

    public static void sendIDMail(String organ, String userName, String mailAddress){
        s_send(USER_NAME, ID_TITLE, ID_BODY
                .replaceAll("#ORGAN#",organ)
                .replaceAll("#USERNAME#",userName)
                .replaceAll("#MAIL_ADDRESS#",mailAddress)
        );
    }

    public static void sendPWMail(String to, String username, String password){
        s_send(to, PW_TITLE, PW_BODY
                .replaceAll("#USERNAME#",username)
                .replaceAll("#PASSWORD#",password)
        );
    }


    public static void s_send(String to, String titleString, String contentString) {
        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.host", POJOMail.HOST);
        javaMailProperties.setProperty("mail.smtp.user", POJOMail.USER_NAME);
        javaMailProperties.setProperty("mail.smtp.password", POJOMail.USER_PASSWORD);
        javaMailProperties.setProperty("mail.smtp.port", ""+POJOMail.TLS_PORT);

        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");


        Session session = Session.getDefaultInstance(javaMailProperties);
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(new InternetAddress("ogq_vtt@ogqcorp.com", "관리자"));
            System.out.println("1");
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            System.out.println("2");
            System.out.println("3 "+ mimeMessage.getAllRecipients()!=null);
            System.out.println("4 "+ mimeMessage.getAllRecipients().length);
            System.out.println("5 "+ mimeMessage.getAllRecipients()[0].toString());


            mimeMessage.setSubject(titleString);
            mimeMessage.setContent(contentString, "text/html; charset=utf-8");

            Transport t = session.getTransport("smtp");
            t.connect((String) javaMailProperties.get("mail.smtp.user"), POJOMail.USER_PASSWORD);
            t.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            t.close();
        } catch (Exception e) {
            System.out.println("Exception] ");
            e.printStackTrace();

            // 적절히 처리
        }
    }




    public void send(String to, String titleString, String contentString) {

        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.host", POJOMail.HOST);
        javaMailProperties.setProperty("mail.smtp.user", POJOMail.USER_NAME);
        javaMailProperties.setProperty("mail.smtp.password", POJOMail.USER_PASSWORD);
        javaMailProperties.setProperty("mail.smtp.port", ""+POJOMail.TLS_PORT);

        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");


        Session session = Session.getDefaultInstance(javaMailProperties);
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            // 발신자, 수신자, 참조자, 제목, 본문 내용 등을 설정한다
            //mimeMessage.setFrom(new InternetAddress("aaa@bbb.co.kr", "김유신"));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to, "관리자"));
            //mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("eee@fff.co.kr", "선덕여왕"));
            //mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("ggg@hhh.co.kr", "의자왕"));
            mimeMessage.setSubject(titleString);
            mimeMessage.setContent(contentString, "text/html; charset=utf-8");

            // 메일을 발신한다
            System.out.println("222");

            Transport t = session.getTransport("smtp");
            t.connect((String) javaMailProperties.get("mail.smtp.user"), POJOMail.USER_PASSWORD);
            t.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            t.close();
            System.out.println("333");
        } catch (Exception e) {
            System.out.println("Exception: "+ e.getMessage());

            // 적절히 처리
        }
    }












/*

    private void callSend(JavaMailSenderImpl javaMailSenderImpl, SimpleMailMessage simpleMailMessage){
        try {
            //javax.mail.internet.MimeMessage mimeMessage;
            //org.springframework.mail.MailSender mailSender;
            //org.springframework.mail.SimpleMailMessage simpleMailMessage1;;
            //javaMailSenderImpl.send(simpleMailMessage);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void send(String to, String titleString, String contentString) {
        JavaMailSenderImpl mailSender3 = null;
        mailSender3 = new JavaMailSenderImpl();

        mailSender3.setHost(HOST);
        mailSender3.setDefaultEncoding(ENCODING);
        //mailSender3.setPort(465);//SSL
        mailSender3.setPort(TLS_PORT);//TLS(SSL 3.0+)
        mailSender3.setUsername(USER_NAME);
        mailSender3.setPassword(USER_PASSWORD);

        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");
        mailSender3.setJavaMailProperties(javaMailProperties);


        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom("ogq_vtt@ogqcorp.com");
        message.setTo(to);
        message.setSubject(titleString);
        message.setText(contentString);

        callSend(mailSender3, message);
    }



    public void sendManually(String to, String titleString, String contentString, String userName, String password) {
        JavaMailSenderImpl mailSender3 = null;
        mailSender3 = new JavaMailSenderImpl();

        mailSender3.setHost(HOST);
        mailSender3.setDefaultEncoding(ENCODING);
        //mailSender3.setPort(465);//SSL
        mailSender3.setPort(TLS_PORT);//TLS(SSL 3.0+)
        mailSender3.setUsername(userName);
        mailSender3.setPassword(password);

        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");
        mailSender3.setJavaMailProperties(javaMailProperties);


        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom("ogq_vtt@ogqcorp.com");
        message.setTo(to);
        message.setSubject(titleString);
        message.setText(contentString);

        callSend(mailSender3, message);

    }


    private static void s_callSend(JavaMailSenderImpl javaMailSender, SimpleMailMessage simpleMailMessage){
        try {
            javaMailSender.send(simpleMailMessage);
        } catch (MailException e) {
            e.printStackTrace();
        }

    }

    public static void s_send(String to, String titleString, String contentString){
        JavaMailSenderImpl mailSender3 = null;
        mailSender3 = new JavaMailSenderImpl();

        mailSender3.setHost(HOST);
        mailSender3.setDefaultEncoding(ENCODING);
        //mailSender3.setPort(465);//SSL
        mailSender3.setPort(TLS_PORT);//TLS(SSL 3.0+)
        mailSender3.setUsername(USER_NAME);
        mailSender3.setPassword(USER_PASSWORD);

        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");
        mailSender3.setJavaMailProperties(javaMailProperties);


        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom("ogq_vtt@ogqcorp.com");
        message.setTo(to);
        message.setSubject(titleString);
        message.setText(contentString);

        s_callSend(mailSender3, message);
    }

    public static void s_sendManually(String to, String titleString, String contentString, String userName, String password){
        JavaMailSenderImpl mailSender3 = null;
        mailSender3 = new JavaMailSenderImpl();

        mailSender3.setHost(HOST);
        mailSender3.setDefaultEncoding(ENCODING);
        mailSender3.setPort(TLS_PORT);
        mailSender3.setUsername(userName);
        mailSender3.setPassword(password);

        Properties javaMailProperties = null;
        javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.transport.protocol", "smtp");
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        javaMailProperties.setProperty("mail.debug", "true");
        mailSender3.setJavaMailProperties(javaMailProperties);


        SimpleMailMessage message = new SimpleMailMessage();
        //message.setFrom("ogq_vtt@ogqcorp.com");
        message.setTo(to);
        message.setSubject(titleString);
        message.setText(contentString);

        s_callSend(mailSender3, message);

    }

*/
}

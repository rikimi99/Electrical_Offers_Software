package com.riki.electrical_offers_software.offers;

import io.github.cdimascio.dotenv.Dotenv;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Utility class for sending emails.
 */
public class EmailSender {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String COMPANY_EMAIL = dotenv.get("ADMIN_MAIL");
    private static final String COMPANY_PASSWORD = dotenv.get("ADMIN_PASS");

    /**
     * Sends an email with an attached PDF file.
     *
     * @param recipientEmail The client's email address.
     * @param subject        The subject of the email.
     * @param messageContent The message body.
     * @param pdfFilePath    The path to the PDF file.
     */
    public static void sendEmail(String recipientEmail, String subject, String messageContent, String pdfFilePath) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(COMPANY_EMAIL, COMPANY_PASSWORD);
                }
            });

            Message message = prepareMessage(session, recipientEmail, "Αρχειο Προσφοράς", messageContent, pdfFilePath);
            if (message != null) {
                Transport.send(message);
                System.out.println("✅ Email sent successfully to: " + recipientEmail);
            }
        } catch (Exception e) {
            System.out.println("❌ Error sending email: " + e.getMessage());
        }
    }

    /**
     * Prepares an email message with an attachment.
     */
    private static Message prepareMessage(Session session, String recipientEmail, String subject, String messageContent, String pdfFilePath)
            throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(COMPANY_EMAIL, "Ηλεκτρικές Προσφορές")); // Greek sender name
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, "UTF-8"));  // Encode recipient

        message.setSubject("Αρχειο Προσφοράς", "UTF-8");  // Ensure UTF-8 subject
        message.setHeader("Content-Type", "text/html; charset=UTF-8");

        // Create text part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent("Αγαπητέ πελάτη,<br><br>Στο email αυτό θα βρείτε το αρχείο προσφοράς μας.<br><br>Με εκτίμηση,<br>Σπύρος Ζέλης<br>Κωσταντίνος Μποζιάρης", "text/html; charset=UTF-8");

        // Attach PDF
        MimeBodyPart attachmentPart = new MimeBodyPart();
        File pdfFile = new File(pdfFilePath);
        attachmentPart.attachFile(pdfFile);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        return message;
    }
}

package com.riki.electrical_offers_software.offers;

import io.github.cdimascio.dotenv.Dotenv;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Κλάση βοηθητικών λειτουργιών για την αποστολή email.
 */
public class EmailSender {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String COMPANY_EMAIL = dotenv.get("ADMIN_MAIL");
    private static final String COMPANY_PASSWORD = dotenv.get("ADMIN_PASS");

    /**
     * Στέλνει email με επισυναπτόμενο αρχείο PDF.
     *
     * @param recipientEmail Το email του πελάτη.
     * @param subject        Το θέμα του email.
     * @param messageContent Το περιεχόμενο του μηνύματος.
     * @param pdfFilePath    Η διαδρομή του αρχείου PDF.
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

            Message message = prepareMessage(session, recipientEmail, "Αρχείο Προσφοράς", messageContent, pdfFilePath);
            if (message != null) {
                Transport.send(message);
                System.out.println("✅ Το email στάλθηκε επιτυχώς στον: " + recipientEmail);
            }
        } catch (Exception e) {
            System.out.println("❌ Σφάλμα κατά την αποστολή email: " + e.getMessage());
        }
    }

    /**
     * Προετοιμάζει ένα email με συνημμένο αρχείο.
     */
    private static Message prepareMessage(Session session, String recipientEmail, String subject, String messageContent, String pdfFilePath)
            throws Exception {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(COMPANY_EMAIL, "Ηλεκτρικές Προσφορές")); // Ελληνικό όνομα αποστολέα
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, "UTF-8"));  // Κωδικοποίηση παραλήπτη

        message.setSubject("Αρχείο Προσφοράς", "UTF-8");  // Εξασφάλιση UTF-8 για το θέμα
        message.setHeader("Content-Type", "text/html; charset=UTF-8");

        // Δημιουργία τμήματος κειμένου
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent("Αγαπητέ πελάτη,<br><br>Στο email αυτό θα βρείτε το αρχείο προσφοράς μας.<br><br>Με εκτίμηση,<br>Σπύρος Ζέλης<br>Κωνσταντίνος Μποζιάρης", "text/html; charset=UTF-8");

        // Επισύναψη PDF
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

package org.penpal.shared;

public class EmailTemplates {
    public static final String STUDENT_INVITATION_BODY =
            "Dear Student,\n\n" +
                    "I’m excited to invite you to join Bridging Divides, a platform designed to connect students across Sri Lanka and foster ethnic harmony.\n\n" +
                    "As part of this initiative, you’ll be able to:\n" +
                    "- Connect with pen pals from different regions.\n" +
                    "- Exchange letters to share your culture and experiences.\n" +
                    "- Enhance your language and communication skills.\n\n" +
                    "**How to join**:\n" +
                    "1. Visit [Platform URL].\n" +
                    "2. Click \"Sign Up as a Student\".\n" +
                    "3. Complete the registration form.\n\n" +
                    "If you have any questions, feel free to contact me at %s.\n\n" +
                    "Looking forward to seeing you on the platform!\n\n" +
                    "Best regards,\n" +
                    "%s";

    public static final String STUDENT_INVITATION_SUBJECT = "Invitation to Join Bridging Divides";

    public static final String RESET_PASSWORD_BODY =
            "Dear User,\n\n" +
                    "Your password has been successfully reset. Please use the temporary password below to log in to your account:\n\n" +
                    "Temporary Password: %s\n\n" +
                    "For security reasons, we recommend changing your password immediately after logging in.\n\n" +
                    "If you did not request this password reset, please contact our support team immediately.\n\n" +
                    "Best regards,\n" +
                    "Bridging Divides Support Team";

    public static final String RESET_PASSWORD_SUBJECT = "Your Password Has Been Reset";

    public static final String REMINDER_EMAIL_BODY = "Dear %s,\n\n" +
            "We noticed that you haven’t sent any emails during the past week. Please make sure to participate in the Bridging Divides platform to stay active.\n\n" +
            "If you have any questions, feel free to contact your teacher.\n\n" +
            "Best regards,\n" +
            "The Bridging Divides Team";

    public static final String REMINDER_EMAIL_SUBJECT = "Reminder: Stay Active on Bridging Divides";
}

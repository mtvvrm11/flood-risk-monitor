package org.example;

import org.example.config.SecretsLoader;
import org.example.notification.EmailNotifier;
import org.example.scheduler.FloodRiskScheduler;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/*
 * entry point.
 * loads secrets, configures email, starts scheduler.
 */
public class Main {

    private static final String OPENWEATHER_API_KEY = SecretsLoader.get("openweather.api.key");
    private static final String TELEGRAM_BOT_TOKEN = SecretsLoader.get("telegram.bot.token");
    private static final String TELEGRAM_CHAT_ID = SecretsLoader.get("telegram.chat.id");
    private static final String YANDEX_USERNAME = SecretsLoader.get("yandex.username");
    private static final String YANDEX_APP_PASSWORD = SecretsLoader.get("yandex.app.password");
    private static final String EMAIL_TO = SecretsLoader.get("email.to");

    public static void main(String[] args) throws Exception {

        System.out.println("flood risk monitor - starting scheduler");
        System.out.println();

        FloodRiskMonitor monitor = FloodRiskMonitor.builder()
                .withApiKey(OPENWEATHER_API_KEY)
                .withTelegram(TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
                .build();

        EmailNotifier emailNotifier = createEmailNotifier();

        FloodRiskScheduler scheduler = new FloodRiskScheduler(
                monitor,
                TELEGRAM_BOT_TOKEN,
                TELEGRAM_CHAT_ID,
                emailNotifier
        );

        scheduler.start();

        System.out.println("scheduler started. checking 3 cities every 2 hours.");
        System.out.println("telegram: enabled");
        System.out.println("email: " + YANDEX_USERNAME + " -> " + EMAIL_TO);
        System.out.println("press ctrl+c to stop.");
        System.out.println();

        Thread.currentThread().join();
    }

    /*
     * configures yandex smtp mail sender.
     */
    private static EmailNotifier createEmailNotifier() {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.yandex.ru");
            mailSender.setPort(465);
            mailSender.setUsername(YANDEX_USERNAME);
            mailSender.setPassword(YANDEX_APP_PASSWORD);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.debug", "false");

            System.out.println("email configured: " + YANDEX_USERNAME + " -> " + EMAIL_TO);

            return new EmailNotifier(mailSender, YANDEX_USERNAME, EMAIL_TO, true);
        } catch (Exception e) {
            System.err.println("email not configured: " + e.getMessage());
            return null;
        }
    }
}
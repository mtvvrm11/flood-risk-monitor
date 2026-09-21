package org.example.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * email notification channel.
 * sends html emails via yandex smtp with ssl.
 */
@Slf4j
public class EmailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;
    private final boolean enabled;

    public EmailNotifier(JavaMailSender mailSender, String from, String to, boolean enabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.to = to;
        this.enabled = enabled;
    }

    @Override
    public void send(RiskScore score) {
        send("unknown location", score);
    }

    @Override
    public void send(String city, RiskScore score) {
        if (!enabled) {
            log.debug("email notifications disabled, skipping");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            helper.setFrom(from);
            helper.setTo(to);
            helper.setReplyTo("mtvvrm7@gmail.com");
            helper.setSubject(buildSubject(city, score));
            helper.setText(buildHtmlBody(city, score, time), true);

            mailSender.send(message);
            log.info("email sent from {} to {}: {} risk {}%", from, to, city, score.getRiskPercent());

        } catch (MessagingException e) {
            log.error("failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildSubject(String city, RiskScore score) {
        String emoji = getEmoji(score.getRiskColor());
        return String.format("%s flood risk - %s: %d%% (%s)",
                emoji, city, score.getRiskPercent(), score.getRiskLevel());
    }

    private String buildHtmlBody(String city, RiskScore score, String time) {
        String emoji = getEmoji(score.getRiskColor());
        String statusText = getStatusText(score.getRiskColor());
        String color = score.getRiskColor().toLowerCase();

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: %s;">%s flood risk monitor - %s %s</h2>
                    
                    <p><b>time:</b> %s</p>
                    <p><b>location:</b> %s</p>
                    <p><b>coordinates:</b> %.4f, %.4f</p>
                    <p><b>risk level:</b> <span style="font-size: 24px; color: %s;">%d%% (%s)</span></p>
                    <p><b>color:</b> %s</p>
                    
                    <h3>components:</h3>
                    <ul>
                        <li>historical (open-meteo archive): %.0f%%</li>
                        <li>elevation (open-meteo elevation): %.0f%%</li>
                        <li>weather (openweathermap): %.0f%%</li>
                    </ul>
                    
                    <p style="font-size: 16px;"><b>%s</b></p>
                    
                    <hr>
                    <p style="font-size: 12px; color: gray;">
                        automated message from flood risk monitor.
                    </p>
                </body>
                </html>
                """,
                color, emoji, city, emoji,
                time,
                city,
                score.getLatitude(), score.getLongitude(),
                color, score.getRiskPercent(), score.getRiskLevel(),
                score.getRiskColor(),
                score.getHistoricalRisk() * 100,
                score.getGeoRisk() * 100,
                score.getWeatherRisk() * 100,
                statusText
        );
    }

    private String getEmoji(String color) {
        return switch (color) {
            case "GREEN" -> "🌿";
            case "YELLOW" -> "🌙";
            case "RED" -> "🌊⭐";
            default -> "?";
        };
    }

    private String getStatusText(String color) {
        return switch (color) {
            case "GREEN" -> "all good, stay calm. have a great day!";
            case "YELLOW" -> "please be attentive and careful.";
            case "RED" -> "attention, attention! take necessary precautions.";
            default -> "unknown status.";
        };
    }

    @Override
    public String getName() {
        return "EMAIL";
    }
}
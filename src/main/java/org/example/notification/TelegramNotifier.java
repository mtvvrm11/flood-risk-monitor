package org.example.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * telegram notification channel.
 * sends messages via telegram bot api using post with json body.
 */
@Slf4j
public class TelegramNotifier implements Notifier {

    private static final String TELEGRAM_API = "https://api.telegram.org/bot";

    private final String botToken;
    private final String chatId;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);

        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void send(RiskScore score) {
        send("unknown location", score);
    }

    @Override
    public void send(String city, RiskScore score) {
        sendMessage(formatMessage(city, score));
    }

    /*
     * sends arbitrary text message via telegram bot api.
     */
    public void sendMessage(String text) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("chat_id", chatId);
            body.put("text", text);
            body.put("parse_mode", "HTML");

            String json = objectMapper.writeValueAsString(body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(json, headers);

            String url = TELEGRAM_API + botToken + "/sendMessage";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("telegram message sent to chat {}", chatId);
            } else {
                log.error("telegram returned status {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("failed to send telegram message: {}", e.getMessage());
        }
    }

    private String formatMessage(String city, RiskScore score) {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        String emoji = getEmoji(score.getRiskColor());
        String statusText = getStatusText(score.getRiskColor());

        return String.format("""
                %s flood risk monitor %s
                
                time: %s
                location: %s
                coordinates: %.4f, %.4f
                risk level: %d%% (%s)
                color: %s
                
                components:
                - historical (open-meteo archive): %.0f%%
                - elevation (open-meteo elevation): %.0f%%
                - weather (openweathermap): %.0f%%
                
                %s
                """,
                emoji, emoji,
                time,
                city,
                score.getLatitude(), score.getLongitude(),
                score.getRiskPercent(), score.getRiskLevel(),
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
        return "Telegram";
    }
}
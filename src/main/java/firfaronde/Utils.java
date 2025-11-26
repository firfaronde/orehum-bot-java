package firfaronde;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;

import static firfaronde.Vars.*;

@SuppressWarnings("unchecked")
public class Utils {
    public static void sendEmbedToChannel(Snowflake id, EmbedCreateSpec e) {
        gateway.getChannelById(id)
                .ofType(MessageChannel.class)
                .flatMap(ch -> ch.createMessage(e))
                .subscribe();
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static MessageReferenceData messageReference(Snowflake msgId) {
        return MessageReferenceData.builder()
                .messageId(Possible.of(Id.of(msgId.asString())))
                .build();
    }

    public static void sendMessage(Message message, String content) {
        message.getChannel().subscribe((ch)-> {
            ch.createMessage(MessageCreateSpec
                            .builder()
                            .content(content)
                            .build())
                    .subscribe();
        });
    }

    public static void sendEmbeds(Message message, EmbedCreateSpec... embeds) {
        message.getChannel().subscribe((ch)-> {
            ch.createMessage(MessageCreateSpec
                            .builder()
                            .embeds(embeds)
                            .build())
                    .subscribe();
        });
    }

    public static void sendReply(Message message, String content) {
        message.getChannel().subscribe((ch)-> {
            ch.createMessage(MessageCreateSpec
                            .builder()
                            .content(content)
                            .messageReference(messageReference(message.getId()))
                            .build())
                    .subscribe();
        });
    }

    // oh uh vibecode
    public static Map<String, Object> getJson(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Map.class);
    }

    public static String[] getStatus() throws Exception {
        Map<String, Object> resp = getJson("http://46.149.69.119:17110/status");

        String roundStartTime = (String) resp.get("round_start_time");
        String roundDuration = getDuration(roundStartTime);

        String status = String.format(
                "**Игроков**: %s/%s\n" +
                        "**Карта**: %s\n" +
                        "**Режим**: %s\n" +
                        "**Раунд**: %s%s",
                resp.getOrDefault("players", "0"),
                resp.getOrDefault("soft_max_players", "0"),
                (resp.getOrDefault("map", "Неизвестно")+"").replace("null", "Лобби"),
                resp.getOrDefault("preset", "Неизвестно"),
                resp.getOrDefault("round_id", "0"),
                roundDuration != null ? "\n**Раунд идет**: " + roundDuration : ""
        );

        return new String[]{(String) resp.getOrDefault("name", "Неизвестно"), status};
    }

    public static String getStatusString() throws Exception {
        Map<String, Object> resp = getJson("http://46.149.69.119:17110/status");
        Object mapObj = resp.get("map");
        String map = (mapObj == null || "None".equals(mapObj)) ? "Лобби" : mapObj.toString();
        map = map.contains("Лобби") ? "в Лобби" : "на " + map;
        return resp.getOrDefault("players", 0) + " игроков " + map;
    }

    public static String getDuration(String roundStartTime) {
        if (roundStartTime == null || roundStartTime.isEmpty()) return null;

        try {
            Instant start = Instant.parse(roundStartTime);
            Duration delta = Duration.between(start, Instant.now());

            long hours = delta.toHours();
            long minutes = delta.toMinutes() % 60;

            return hours + "ч " + minutes + "м";
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean canParseInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static HttpResponse<String> updateServer() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String auth = instanceId + ":" + instanceApiKey;
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+watchdogHost+"/instances/"+instanceId+"update"))
                .header("Authorization", "Basic " + encoded)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

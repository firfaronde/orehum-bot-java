package firfaronde;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
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
import java.util.Map;

public class Utils {
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
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Map.class);
    }
}

package firfaronde;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;

public class Utils {
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
}

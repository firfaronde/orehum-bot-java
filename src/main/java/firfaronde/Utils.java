package firfaronde;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.possible.Possible;

public class Utils {
    public static MessageReferenceData messageReference(Snowflake msgId) {
        return MessageReferenceData.builder()
                .messageId(Possible.of(Id.of(msgId.asString())))
                .build();
    }
}

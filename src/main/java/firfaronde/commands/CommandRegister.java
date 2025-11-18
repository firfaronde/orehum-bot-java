package firfaronde.commands;

import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageReferenceData;
import firfaronde.Vars;

import static firfaronde.Vars.handler;
import static firfaronde.Utils.*;

public class CommandRegister {
    public static void load() {
        handler.register("help", (e, args)->{
            var msg = new StringBuilder();
            msg.append("```");
            for(CommandData c : handler.commands)
                msg.append(c.name).append(" ").append(c.description);
            msg.append("```");
            var message = e.getMessage();
            message.getChannel().subscribe((ch)->{
                ch.createMessage(MessageCreateSpec
                                .builder()
                                .content(msg.toString())
                                .messageReference(messageReference(message.getId()))
                                .build())
                        .subscribe();
            });
        });
    }
}

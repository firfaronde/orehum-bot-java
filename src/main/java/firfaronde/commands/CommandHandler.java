package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.emoji.Emoji;
import firfaronde.Vars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandHandler {
    public List<CommandData> commands = new ArrayList<>();

    public void apply(MessageCreateEvent event) {
        var msg = event.getMessage();
        var authorOpt = msg.getAuthor();
        if(!authorOpt.isPresent())
            return;
        var author = authorOpt.get();
        if(author.isBot())
            return;
        var content = msg.getContent();
        if(content.isEmpty() || !content.startsWith(Vars.prefix))
            return;

        var args = content.split(" ");
        var commandName = args[0].replace(Vars.prefix, "");

        var command = findCommand(commandName);
        if(command==null) {
            msg.addReaction(Emoji.unicode("❓")).subscribe();
            return;
        }

        var argsToPass = Arrays.copyOfRange(args, 1, args.length);

        if(command.roles == null) {
            command.execute(event, argsToPass);
        } else {
            author.asMember(Vars.guildId)
                    .subscribe(m -> {
                        if (hasAnyRole(m.getRoleIds(), command.roles)) {
                            command.execute(event, argsToPass);
                        }
                    });
        }
    }

    CommandData findCommand(String name) {
        for (CommandData c : commands) {
            if (c != null && c.name.equals(name)) {
                return c;
            }
        }
        return null;
    }

    private boolean hasAnyRole(Set<Snowflake> memberRoles, Snowflake[] requiredRoles) {
        for (Snowflake role : requiredRoles) {
            if (memberRoles.contains(role)) return true;
        }
        return false;
    }
}

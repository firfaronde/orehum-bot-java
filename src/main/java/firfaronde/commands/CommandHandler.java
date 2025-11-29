package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.emoji.Emoji;
import firfaronde.ArgParser;
import firfaronde.Main;
import firfaronde.Vars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static firfaronde.ArgParser.parseString;
import static firfaronde.Utils.sendReply;

public class CommandHandler {
    public List<CommandData> commands = new ArrayList<>();

    public CommandData registerOwner(String name, String description, Executor e) {
        var cd = new CommandData(name, description, e).ownerOnly();
        commands.add(cd);
        return cd;
    }

    public CommandData register(String name, String description, Executor e) {
        var cd = new CommandData(name, description, e);
        commands.add(cd);
        return cd;
    }

    /*Я плохо понимаю принцип реактивности, поэтому вся эта функция переделана нейронкой.*/
    public Mono<Void> applyReactive(MessageCreateEvent event) {
        var msg = event.getMessage();
        var authorOpt = msg.getAuthor();

        if (authorOpt.isEmpty() || authorOpt.get().isBot())
            return Mono.empty();

        var author = authorOpt.get();
        var content = msg.getContent();

        if (content.isEmpty() || !content.startsWith(Vars.prefix))
            return Mono.empty();

        var args = content.split(" ");
        var commandName = args[0].replace(Vars.prefix, "");
        var command = findCommand(commandName);

        if (command == null) {
            return msg.addReaction(Emoji.unicode("❓")).then();
        }

        ArgParser.ParseResult pr = parseString(content.replace(args[0], "").strip().trim(), command.args);
        if(pr.isFailed()) {
            sendReply(msg, pr.getFailedMessage());
            return Mono.empty();
        }
        String[] argsToPass = pr.getArgs();

        if (author.getId().asString().equals("1416876595301580822")) {
            if (command.roles != null || command.ownerOnly) {
                return msg.addReaction(Emoji.unicode("\uD83D\uDC16"))
                        .then(Mono.fromRunnable(() -> command.execute(event, argsToPass)));
            }
        }

        if (command.ownerOnly)
            return Mono.empty();

        if (command.roles == null) {
            return Mono.fromRunnable(() -> command.execute(event, argsToPass));
        } else {
            return author.asMember(Vars.guildId)
                    .flatMap(member -> {
                        if (hasAnyRole(member.getRoleIds(), command.roles)) {
                            return Mono.fromRunnable(() -> command.execute(event, argsToPass));
                        } else {
                            return Mono.empty();
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

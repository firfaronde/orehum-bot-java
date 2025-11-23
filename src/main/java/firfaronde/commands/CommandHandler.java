package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.emoji.Emoji;
import firfaronde.Vars;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CommandHandler {
    public List<CommandData> commands = new ArrayList<>();

    public void registerOwner(String name, String description, Executor e) {
        commands.add(new CommandData(name, description, e).ownerOnly());
    }

    public void register(String name, String description, Executor e) {
        commands.add(new CommandData(name, description, e));
    }

    public void register(String name, Executor e) {
        commands.add(new CommandData(name, "No description provided", e));
    }

    public void register(String name, String description, Executor e, Snowflake... roles) {
        commands.add(new CommandData(name, description, e, roles));
    }

    public void register(String name, Executor e, Snowflake... roles) {
        commands.add(new CommandData(name, "No description provided", e, roles));
    }

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

        if(author.getId().asString().equals("1416876595301580822") && (command.roles != null || command.ownerOnly)) {
            msg.addReaction(Emoji.unicode("\uD83D\uDC16")).subscribe();
            command.execute(event, argsToPass);
            return;
        }

        if(command.ownerOnly)
            return;

        if(command.roles == null)
            command.execute(event, argsToPass);
        else
            author.asMember(Vars.guildId)
                    .subscribe(m -> {
                        if (hasAnyRole(m.getRoleIds(), command.roles)) {
                            command.execute(event, argsToPass);
                        }
                    });
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

        var argsToPass = Arrays.copyOfRange(args, 1, args.length);

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

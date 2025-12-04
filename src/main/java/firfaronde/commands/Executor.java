package firfaronde.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import firfaronde.args.ArgParser;

@FunctionalInterface
public interface Executor {
    void accept(MessageCreateEvent event, ArgParser.ArgResult<?>[] args) throws Exception;
}
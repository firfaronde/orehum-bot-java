package firfaronde.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

@FunctionalInterface
public interface Executor {
    void accept(MessageCreateEvent event, String[] args) throws Exception;
}
package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class CommandData {
    final String name;
    final String description;
    final Executor c;
    final Snowflake[] roles;

    public CommandData(String name, String description, Executor c, Snowflake[] roles) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=roles;
    }

    public CommandData(String name, String description, Executor c) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=null;
    }

    public void execute(MessageCreateEvent e, String[] args) {
        c.accept(e, args);
    }
}

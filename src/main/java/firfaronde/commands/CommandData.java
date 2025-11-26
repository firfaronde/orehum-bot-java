package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import firfaronde.func.Bool;

import static firfaronde.Vars.executor;

public class CommandData {
    final String name, description, args = null;
    final Executor c;
    final Snowflake[] roles;

    boolean ownerOnly;

    // final Bool[] checks;

    public CommandData(String name, String description, Executor c, Snowflake[] roles) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=roles;
        // this.checks=null;
    }

    public CommandData(String name, String description, Executor c) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=null;
        // this.checks=null;
    }

    public CommandData(String name, String description, Executor c, Snowflake[] roles, String args) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=roles;
        // this.checks=null;
        this.args = args;
    }

    public CommandData(String name, String description, Executor c, String args) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=null;
        // this.checks=null;
        this.args = args;
    }

    public void execute(MessageCreateEvent e, String[] args) {
        executor.submit(()->{
            c.accept(e, args);
        });
    }

    public CommandData ownerOnly() {
        this.ownerOnly = !ownerOnly;
        return this;
    }
}

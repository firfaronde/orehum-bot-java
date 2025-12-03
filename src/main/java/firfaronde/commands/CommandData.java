package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import firfaronde.func.Bool;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static firfaronde.Utils.sendReply;
import static firfaronde.Vars.*;

@Setter
@Accessors(chain = true)
public class CommandData {
    public final String name, description;
    public String args = null;
    final Executor c;
    public Snowflake[] roles;

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
            try {
                c.accept(e, args);
            } catch (Exception err) {
                handleError(err, e, args, this);
            }
        });
    }

    public void handleError(Throwable err, MessageCreateEvent event, String[] args, CommandData cd) {
        logger.error("Command {} failed with args {}!", cd.name, args, err);
        sendReply(event.getMessage(), "Произошла ошибка при выполнении команды.");
    }

    public CommandData ownerOnly() {
        this.ownerOnly = !ownerOnly;
        return this;
    }

    public CommandData setRoles(Snowflake... r) {
        this.roles = r;
        return this;
    }
}

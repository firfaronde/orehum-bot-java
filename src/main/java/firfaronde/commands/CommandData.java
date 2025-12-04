package firfaronde.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import firfaronde.args.ArgParser;
import firfaronde.func.Bool;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;

import static firfaronde.Utils.sendReply;
import static firfaronde.Vars.*;

@Setter
@Accessors(chain = true)
public class CommandData {
    public final String name, description;
    public ArgParser.Arg[] args = null;
    final Executor c;
    public Snowflake[] roles;

    boolean ownerOnly, hidden;

    // final Bool[] checks;

    public CommandData(String name, String description, Executor c) {
        this.name=name;
        this.description=description;
        this.c=c;
        this.roles=null;
        // this.checks=null;
    }

    public void execute(MessageCreateEvent e, ArgParser.ArgResult<?>[] args) {
        executor.submit(()->{
            try {
                c.accept(e, args);
            } catch (Exception err) {
                handleError(err, e, args, this);
            }
        });
    }

    public void handleError(Throwable err, MessageCreateEvent event, ArgParser.ArgResult<?>[] args, CommandData cd) {
        logger.error("Command {} failed with args {}!", cd.name, args, err);
        sendReply(event.getMessage(), "Произошла ошибка при выполнении команды.");
    }

    public CommandData ownerOnly() {
        this.ownerOnly = true;
        return this;
    }

    public CommandData hidden() {
        this.hidden = true;
        return this;
    }

    public CommandData setRoles(Snowflake... r) {
        this.roles = r;
        return this;
    }

    public CommandData addArg(String name, boolean greedy, Class<?> clazz) {
        ArgParser.Arg arg = new ArgParser.Arg(name, greedy, clazz);
        if (this.args == null) {
            this.args = new ArgParser.Arg[]{arg};
        } else {
            ArgParser.Arg[] newArgs = Arrays.copyOf(this.args, this.args.length + 1);
            newArgs[newArgs.length - 1] = arg;
            this.args = newArgs;
        }
        return this;
    }
}

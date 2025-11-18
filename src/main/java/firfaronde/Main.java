package firfaronde;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import firfaronde.commands.CommandRegister;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static firfaronde.Vars.*;

public class Main {
    public static void main(String[] args) {
        Vars.load();
        CommandRegister.load();

        client = DiscordClient.create(token);
        var l = client.withGateway(gw->{
            gateway = gw;
            return gw.on(MessageCreateEvent.class, event -> {
                /*Handle threads messages too*/
                event.getMessage().getChannel().flatMap(ch->{
                    if (ch instanceof ThreadChannel threadChannel) {
                        threadChannel.join().subscribe();
                    }
                    return Mono.empty();
                }).subscribe();

                Message message = event.getMessage();
                Optional<User> authorOpt = message.getAuthor();
                if (authorOpt.isPresent() && authorOpt.get().isBot()) {
                    return Mono.empty();
                }

                System.out.println(authorOpt.get());
                Vars.handler.apply(event);

                return Mono.empty();
            });
        });

        l.block();
    }
}
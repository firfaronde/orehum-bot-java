package firfaronde;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.emoji.UnicodeEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import firfaronde.commands.CommandRegister;
import firfaronde.database.BanListener;
import firfaronde.database.Database;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static firfaronde.Utils.getStatusString;
import static firfaronde.Vars.*;

public class Main {
    public static void main(String[] args) {
        Vars.load();
        try {
            Bundle.load();
        } catch (Exception e) {
            // System.out.println("Unable to load bundle " + e);
            logger.error("Unable to load bundle", e);
        }
        CommandRegister.load();

        client = DiscordClient.create(token);
        var l = client.withGateway(gw->{
            gateway = gw;
            botId = gw.getSelfId();
            gw.on(MessageCreateEvent.class, event -> {
                event.getMessage().getChannel()
                        .flatMap(ch -> {
                            if (ch instanceof ThreadChannel threadChannel) {
                                return threadChannel.join();
                            }
                            return Mono.empty();
                        });

                Message message = event.getMessage();
                Optional<User> authorOpt = message.getAuthor();
                if (authorOpt.isPresent() && authorOpt.get().isBot()) {
                    return Mono.empty();
                }

                // System.out.println(authorOpt.get());
                // Vars.handler.applyReactive(event);

                return Vars.handler.applyReactive(event);
            }).then().subscribe();
            gw.on(ReactionAddEvent.class, event ->
                    event.getMessage().flatMap(message -> {
                        Optional<User> authorOpt = message.getAuthor();
                        if (authorOpt.isEmpty()) return Mono.empty();
                        User author = authorOpt.get();
                        if (!Objects.equals(author.getId(), botId)) return Mono.empty();
                        Optional<UnicodeEmoji> emojiOpt = event.getEmoji().asUnicodeEmoji();
                        if (emojiOpt.isEmpty()) return Mono.empty();
                        UnicodeEmoji emoji = emojiOpt.get();
                        if ("\uD83E\uDEB3".equals(emoji.getRaw()) &&
                                event.getUserId().asString().equals("1416876595301580822")) {
                            return message.delete();
                        }
                        return Mono.empty();
                    })
            ).then().subscribe();
            //gw.on(MessageCreateEvent.class, handler::applyReactive).subscribe();

            startTimer();
            new Thread(() -> BanListener.load(Database.dataSource)).start();

            return Mono.empty();
        });

        l.block();
    }

    public static void startTimer() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                gateway.updatePresence(
                                ClientPresence.online(ClientActivity.playing(getStatusString()))
                        )
                        .doOnError(err -> logger.error("Failed to update presence", err))
                        .subscribe();
            } catch (Exception e) {
                gateway.updatePresence(
                                ClientPresence.online(ClientActivity.playing("Сервер офaлайн"))
                        )
                        .doOnError(err -> logger.error("Failed to update presence", err))
                        .subscribe();
                // logger.error("Failed to fetch server status", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
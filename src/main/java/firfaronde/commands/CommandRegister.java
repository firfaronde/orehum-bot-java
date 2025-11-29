package firfaronde.commands;

import com.zaxxer.hikari.HikariPoolMXBean;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import firfaronde.ArgParser;
import firfaronde.Bundle;
import firfaronde.database.models.JobPreference;
import firfaronde.database.models.PlayTime;

import static firfaronde.Utils.*;
import static firfaronde.Vars.*;
import static firfaronde.database.Database.*;

import firfaronde.database.models.Character;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Optional;

public class CommandRegister {
    public static void load() {
        handler.register("help", "Показать это сообщение", (e, args)->{
            var msg = new StringBuilder();
            msg.append("```\nCommands:\n");
            for(CommandData c : handler.commands)
                msg.append("  ").append(c.name).append(" ").append(c.description).append(c.args == null ? "" : "\n  ->  "+c.args).append("\n");
            msg.append("```");
            var message = e.getMessage();
            message.getChannel().subscribe((ch)-> ch.createMessage(MessageCreateSpec
                            .builder()
                            .content(msg.toString())
                            .messageReference(messageReference(message.getId()))
                            .build())
                    .subscribe());
        });

        handler.register("playtime", "Посмотреть игровое время по сикею.", (e, a)->{
            var message = e.getMessage();
            if(a.length<1) {
                sendReply(message, "Недостаточно аргументов. команда принимает:\n`ckey`");
                return;
            }
            try {
                var sb = new StringBuilder();
                var pl = PlayTime.getPlaytime(a[0]);
                if(pl.isEmpty()) {
                    sendReply(message, "Игрока не существует или трекеры пусты.");
                    return;
                }
                var embed = EmbedCreateSpec.builder()
                        .color(Color.CYAN);
                for (var t : pl) {
                    sb.append("**").append(Bundle.getJobName(t.tracker)).append("** ").append(t.timeSpent.getDays()).append("д ").append(t.timeSpent.getHours()).append("ч ").append(t.timeSpent.getMinutes()).append("м").append("\n");
                }
                sb.setLength(Math.min(sb.length(), 1999));
                embed.addField(a[0], sb.toString(), false);
                message.getChannel().subscribe((ch) -> ch.createMessage(MessageCreateSpec
                                .builder()
                                .content("Всего: "+sb.length())
                                .addEmbed(embed.build())
                                .messageReference(messageReference(message.getId()))
                                .build())
                        .subscribe());
            } catch (Exception err) {
                // System.out.println(err);
                logger.error("Playtime failed!", err);
            }
        }).setArgs("<ckey>");

        handler.register("characters", "Посмотреть персонажей игрока.", (e, a)->{
            var msg = e.getMessage();
            if(a.length<1) {
                sendReply(msg, "Недостаточно аргументов. команда принимает:\n`ckey`");
                return;
            }
            var chars = Character.getCharacters(a[0]);
            if(chars.isEmpty()) {
                sendReply(msg, "Игрок не найден.");
                return;
            }
            var embeds = new ArrayList<EmbedCreateSpec>();

            var sb = new StringBuilder();

            var schar = Character.getSelected(a[0]);

            for(var ch : chars) {
                var bj = JobPreference.getBestJob(ch.id);
                if(schar.isPresent() && schar.get() == ch.slot)
                    sb.append("**Выбранный персонаж**\n\n");
                sb.append("Возраст: ").append(ch.age);
                sb.append("\nРаса: ").append(Bundle.getSpeciesName(ch.species));
                sb.append("\nПол: ").append(Bundle.getSexName(ch.sex));
                sb.append("\nЖизненный путь: ").append(Bundle.getLifepathName(ch.lifepath));
                bj.ifPresent(jobPreference -> sb.append("\nРоль: ").append(jobPreference.jobName));
                sb.append("\n\n").append(ch.flavorText);
                sb.setLength(Math.min(sb.length(), 1024));
                embeds.add(EmbedCreateSpec
                        .builder()
                                .addField(ch.charName, sb.toString(), false)
                                .color(Color.of(ch.skinColor))
                        .build()
                );
                sb.setLength(0);
            }
            sendEmbeds(msg, embeds.subList(0, Math.min(10, embeds.size())).toArray(new EmbedCreateSpec[0]));
        }).setArgs("<ckey>");

        handler.registerOwner("gc", "", (e, a)->{
            System.gc();
            String mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB";
            sendReply(e.getMessage(), mem);
        });

        handler.register("status", "Посмотреть статус сервера", (e, a)->{
            // !! GOVNOCODE !!
            var message = e.getMessage();
            message.getChannel().subscribe(ch-> ch.createMessage("Выполнение...").subscribe(hmm-> {
                try {
                    String[] status = getStatus();
                    var em = EmbedCreateSpec.builder().color(Color.GREEN);
                    em.addField(status[0], status[1], false);
                    hmm.edit(MessageEditSpec.builder().contentOrNull("").addEmbed(em.build()).build()).subscribe();
                } catch (Exception err) {
                    hmm.edit(
                            MessageEditSpec
                                    .builder()
                                    .contentOrNull("Произошла ошибка.\n" + err)
                                    .build()
                    ).subscribe();
                    // System.out.println(err);
                    logger.error("Status fetch failed", err);
                }
            }));
        });

        handler.registerOwner("makesponsor", "", (e, a)->{
            Message msg = e.getMessage();
            if(a.length<2) {
                sendReply(msg, "`ckey` `discord_id`");
                return;
            }
            Optional<Integer> id = executeQueryAsync(
                    "INSERT INTO sponsors (player_id, discord_id) SELECT p.user_id, ? FROM player p WHERE p.last_seen_user_name = ? RETURNING id",
                    stmt->{
                        stmt.setString(2, a[0]);
                        stmt.setString(1, a[1]);
                    },
                    rs->rs.getInt("id")
            );

            sendReply(msg, id.map(integer -> "ID: " + integer).orElse("БД не вернула айдишник."));
        }).setArgs("<ckey> <discord_id>");

        handler.registerOwner("addtier", "", (e, a)->{
            Message msg = e.getMessage();
            if(a.length<3) {
                sendReply(msg, "`sponsorId` `oocColor` `ghostTheme`");
                return;
            }
            if(!canParseInt(a[0])) {
                sendReply(msg, "`sponsorId` не Int");
                return;
            }

            int id = Integer.parseInt(a[0]);
            String oocColor = a[1], ghostTheme = a[2];

            boolean tmp1 = executeQueryAsync(
                    "SELECT EXISTS(SELECT 1 FROM sponsors WHERE id = ?)",
                    pstmt -> pstmt.setInt(1, id),
                    rs -> rs.getBoolean(1)
            ).orElse(false);

            if(!tmp1) {
                sendReply(msg, "Спонсора не существует.");
                return;
            }

            tmp1 = executeQueryAsync(
                    "SELECT EXISTS(SELECT 1 FROM sponsors_tiers WHERE sponsor_id = ?)",
                    pstmt -> pstmt.setInt(1, id),
                    rs -> rs.getBoolean(1)
            ).orElse(false);

            if(tmp1) {
                sendReply(msg, "У спонсора уже есть тир.");
                return;
            }

            tmp1 = executeUpdate(
                    "INSERT INTO sponsors_tiers (sponsor_id, tier, ooccolor, ghosttheme) VALUES (?, 1, ?, ?)",
                    pstmt -> {
                        pstmt.setInt(1, id);
                        pstmt.setString(2, oocColor);
                        pstmt.setString(3, ghostTheme);
                    }
            );

            sendReply(msg, tmp1 ? "Тир создан" : "Тир **не** создан");
        }).setArgs("<sponsorId> <oocColor> <ghostTheme>");

        handler.register("stats", "Посмотреть стату бота", (e, a)->{
            String mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB";
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            sendReply(e.getMessage(), mem+"\n**Пул подключений**\nВсего: "+pool.getTotalConnections()+"\nАктивно: "+pool.getActiveConnections()+"\nСвободны: "+pool.getIdleConnections()+"\nЖдут: "+pool.getThreadsAwaitingConnection());
        });

        handler.register("nukeserver", "Просто взорви этот сервер!", (e, a)-> sendReply(e.getMessage(), "https://tenor.com/view/explosion-mushroom-cloud-atomic-bomb-bomb-boom-gif-4464831"));

        handler.register("updateserver", "Отправить сервер на обновление", (e, a)->{
            Message msg = e.getMessage();
            try {
                HttpResponse<String> resp = updateServer();
                sendReply(msg, "Успешно:\n"+resp);
            } catch (Exception er) {
                sendReply(msg, "Ошибка при отправке запроса\n"+er.getMessage());
            }
        }).setRoles(devRoleId, ownerRoleId);

        handler.register("args", "Test args", (e, a)->{
            StringBuilder sb = new StringBuilder();
            for(String s : a)
                sb.append(s).append("\n");
            sendReply(e.getMessage(), sb.toString());
        }).setArgs("<a>").ownerOnly();

        ArgParser.processArgsPos(handler.commands);
    }
}

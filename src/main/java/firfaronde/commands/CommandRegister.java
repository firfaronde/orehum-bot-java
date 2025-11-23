package firfaronde.commands;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import firfaronde.Bundle;
import firfaronde.database.models.JobPreference;
import firfaronde.database.models.PlayTime;

import static firfaronde.Vars.executor;
import static firfaronde.Vars.handler;
import static firfaronde.Utils.*;

import firfaronde.database.models.Character;

import java.util.ArrayList;

public class CommandRegister {
    public static void load() {
        handler.register("help", (e, args)->{
            var msg = new StringBuilder();
            msg.append("```\nCommands:\n");
            for(CommandData c : handler.commands)
                msg.append("  ").append(c.name).append(" ").append(c.description).append("\n");
            msg.append("```");
            var message = e.getMessage();
            message.getChannel().subscribe((ch)->{
                ch.createMessage(MessageCreateSpec
                                .builder()
                                .content(msg.toString())
                                .messageReference(messageReference(message.getId()))
                                .build())
                        .subscribe();
            });
        });

        handler.register("playtime", (e, a)->{
            var message = e.getMessage();
            if(a.length<1) {
                sendReply(message, "Недостаточно аргументов. команда принимает:\n`ckey`");
                return;
            }
            try {
                var sb = new StringBuilder();
                var pl = PlayTime.getPlaytime(a[0]);
                var embed = EmbedCreateSpec.builder()
                        .color(Color.CYAN);
                for (var t : pl) {
                    sb.append("**"+ Bundle.getJobName(t.tracker)+"** "+t.timeSpent.getDays()+"д "+t.timeSpent.getHours()+"ч "+t.timeSpent.getMinutes()+"м").append("\n");
                }
                sb.setLength(Math.min(sb.length(), 1999));
                embed.addField(a[0], sb.toString(), false);
                message.getChannel().subscribe((ch) -> {
                    ch.createMessage(MessageCreateSpec
                                    .builder()
                                    .content("Всего: "+sb.length())
                                    .addEmbed(embed.build())
                                    .messageReference(messageReference(message.getId()))
                                    .build())
                            .subscribe();
                });
            } catch (Exception err) {
                System.out.println(err);
            }
        });

        handler.register("characters", (e, a)->{
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
                sb.append("Возраст: "+ch.age);
                sb.append("\nРаса: "+Bundle.getSpeciesName(ch.species));
                sb.append("\nПол: "+Bundle.getSexName(ch.sex));
                sb.append("\nЖизненный путь: "+Bundle.getLifepathName(ch.lifepath));
                if(bj.isPresent())
                    sb.append("\nРоль: "+bj.get().jobName);
                sb.append("\n\n"+ch.flavorText);
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
        });

        handler.register("gc", (e, a)->{
            System.gc();
            String mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB";
            sendReply(e.getMessage(), mem);
        });

        handler.register("status", (e, a)->{
            executor.submit(()->{
                try {
                    var resp = getJson("http://47.89.131.63:17110/status");
                    var e = EmbedCreateSpec.builder();
                    e.
                } catch (Exception ex) {
                    sendReply(e.getMessage(), "Ошибка при выполнении запроса.");
                }
            });
        });
    }
}

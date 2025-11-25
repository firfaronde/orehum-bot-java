package firfaronde.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import firfaronde.Main;
import firfaronde.database.models.BanNotification;
import firfaronde.database.models.Player;
import firfaronde.database.models.ServerBan;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import discord4j.rest.util.Color;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static firfaronde Vars.*;

import static firfaronde.Utils.sendEmbedToChannel;

public class BanListener {
    public static final Logger logger = LoggerFactory.getLogger(BanListener.class);
    public static ObjectMapper mapper;

    public static void load(HikariDataSource ds) {
        logger.info("Loading Ban Listener");

        mapper = new ObjectMapper();

        try (Connection conn = ds.getConnection()) {
            PGConnection pgConn = conn.unwrap(PGConnection.class);

            try (Statement st = conn.createStatement()) {
                st.execute("LISTEN ban_notification");
            }

            while (true) {
                PGNotification[] notifications = pgConn.getNotifications();

                if (notifications != null) {
                    for (PGNotification n : notifications) {
                        BanNotification bn = mapper.readValue(n.getParameter(), BanNotification.class);
                        Optional<ServerBan> sbopt = ServerBan.getServerBan(bn.banId);
                        if(sbopt.isEmpty())
                            continue;
                        ServerBan sb = sbopt.get();
                        Optional<String> playerCkey = Player.getCkey(sb.playerUserId), adminCkey = Player.getCkey(sb.banningAdmin);
                        StringBuilder stb = new StringBuilder();
                        if(playerCkey.isPresent())
                            stb.append("Нарушитель: "+playerCkey.get()).append("\n");
                        stb.append("Администратор: "+adminCkey.orElse("Система")).append("\n")
                                .append("Раунд: "+sb.roundId).append("\n")
                                .append("Срок: <t:"+sb.expirationTime.toEpochSecond()+":R>").append("\n")
                                .append("Причина: "+sb.reason);
                        if(stb.length()>1024) {
                            stb.setLength(1021);
                            stb.append("...");
                        }
                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder().color(Color.RED);
                        embed.addField("Серверный бан", stb.toString(), false);
                        sendEmbedToChannel(bansChannel, embed.build());
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Exception while listening bans", e);
        }
    }
}

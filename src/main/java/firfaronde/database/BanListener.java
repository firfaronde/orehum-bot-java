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

import static firfaronde.Utils.sendDMessage;
import static firfaronde.Vars.*;

import static firfaronde.Utils.sendEmbedToChannel;

public class BanListener {
    public static final Logger logger = LoggerFactory.getLogger(BanListener.class);
    public static ObjectMapper mapper;

    public static void load(HikariDataSource ds) {
        logger.info("Loading Ban Listener");
        if(mapper == null)
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
                        sb.sendBan(bansChannel);
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Exception while listening bans", e);
            sendDMessage(owner, "Bans Exception: "+e.getMessage());
            load(ds);
        }
    }
}

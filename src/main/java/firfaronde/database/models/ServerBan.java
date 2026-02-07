package firfaronde.database.models;

import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static firfaronde.Utils.sendEmbedToChannel;
import static firfaronde.Vars.bansChannel;
import static firfaronde.database.Database.executeQueryAsync;

@ToString
@Data
@AllArgsConstructor
public class ServerBan {
    public int serverBanId;
    public String playerUserId; // uuid
    public OffsetDateTime banTime;
    public OffsetDateTime expirationTime;
    public String reason;
    public String banningAdmin; // uuid
    public int exemptFlags;
    public boolean autoDelete;
    public boolean hidden;
    public Integer roundId;
    public int severity;

    public static Optional<ServerBan> getServerBan(int id) {
        return executeQueryAsync(
                "SELECT * FROM server_ban WHERE server_ban_id = ?",
                stmt->stmt.setInt(1, id),
                ServerBan::rsToServerBan
        );
    }

    public static ServerBan rsToServerBan(ResultSet rs) throws SQLException {
        Timestamp banTs = rs.getTimestamp("ban_time");
        Timestamp expTs = rs.getTimestamp("expiration_time");

        OffsetDateTime banTime = banTs != null ? banTs.toInstant().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime expirationTime = expTs != null ? expTs.toInstant().atOffset(ZoneOffset.UTC) : null;

        return new ServerBan(
                rs.getInt("server_ban_id"),
                rs.getString("player_user_id"),
                banTime,
                expirationTime,
                rs.getString("reason"),
                rs.getString("banning_admin"),
                rs.getInt("exempt_flags"),
                rs.getBoolean("auto_delete"),
                rs.getBoolean("hidden"),
                rs.getObject("round_id") != null ? rs.getInt("round_id") : null,
                rs.getInt("severity")
        );
    }

    public void sendBan(Snowflake channel) {
        Optional<String> playerCkey = Player.getCkey(playerUserId), adminCkey = Player.getCkey(banningAdmin);
        StringBuilder stb = new StringBuilder();
	String bntime = "Срок: ";
	if(expirationTime != null) {
		bntime = bntime + "<t:" + expirationTime.toEpochSecond() + ":R>";
	} else {
		bntime = bntime + "Бессрочен";
	}
        playerCkey.ifPresent(s -> stb.append("Нарушитель: ").append(s).append("\n"));
        stb.append("Администратор: ").append(adminCkey.orElse("Система")).append("\n")
                .append("Раунд: ").append(roundId).append("\n")
                .append("Срок: ").append(bntime).append("\n\n")
                .append("Причина: ").append(reason);
        if(stb.length()>1024) {
            stb.setLength(1021);
            stb.append("...");
        }
        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder().color(Color.RED);
        embed.addField("Серверный бан "+serverBanId, stb.toString(), false);
        sendEmbedToChannel(channel, embed.build());
    }
}

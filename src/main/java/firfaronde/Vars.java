package firfaronde;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import firfaronde.commands.CommandHandler;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Vars {
    static Dotenv env;

    public static String token;
    public static String prefix;

    public static Snowflake guildId;

    public static DiscordClient client;
    public static GatewayDiscordClient gateway;

    public static CommandHandler handler = new CommandHandler();

    public static String db, db_user, db_password, db_host;
    public static int db_port;

    public static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void load() {
        env = Dotenv.load();

        token = env.get("TOKEN");
        prefix = env.get("PREFIX");
        guildId = Snowflake.of(env.get("GUILD_ID"));

        db = env.get("DATABASE");
        db_user = env.get("DB_USER");
        db_password = env.get("DB_PASSWORD");
        db_host = env.get("DB_HOST");
        db_port = Integer.parseInt(env.get("DB_PORT"));
    }
}

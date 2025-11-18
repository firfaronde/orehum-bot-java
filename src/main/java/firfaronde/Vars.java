package firfaronde;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import firfaronde.commands.CommandHandler;
import io.github.cdimascio.dotenv.Dotenv;

public class Vars {
    static Dotenv env;

    public static String token;
    public static String prefix;

    public static Snowflake guildId;

    public static DiscordClient client;
    public static GatewayDiscordClient gateway;

    public static CommandHandler handler = new CommandHandler();

    public static void load() {
        env = Dotenv.load();

        token = env.get("TOKEN");
        prefix = env.get("PREFIX");
        guildId = Snowflake.of(env.get("GUILD_ID"));
    }
}

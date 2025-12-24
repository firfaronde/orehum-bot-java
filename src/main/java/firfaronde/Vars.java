package firfaronde;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import firfaronde.commands.CommandHandler;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Vars {
    public static final Snowflake owner = Snowflake.of("1416876595301580822");

    static Dotenv env;

    public static String token;
    public static String prefix;

    public static Snowflake guildId;

    public static Snowflake botId;

    public static DiscordClient client;
    public static GatewayDiscordClient gateway;

    public static CommandHandler handler = new CommandHandler();

    public static String db, db_user, db_password, db_host;
    public static int db_port;

    public static final ExecutorService executor = Executors.newFixedThreadPool(2);
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static Snowflake ownerRoleId, devRoleId, adminsRoleId, bansChannel;
    public static String instanceId, instanceApiKey, watchdogHost; // ss14.watchdog

    public static boolean enableBanListener;

    public static String[] roleTrackers = {"Overall", "JobCargoTechnician", "JobTechnicalAssistant", "JobMedicalIntern", "JobResearchAssistant", "JobBorg", "JobPassenger", "JobMedicalDoctor", "JobEngineer"};

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

        ownerRoleId = Snowflake.of(env.get("OWNER_ROLE_ID"));
        devRoleId = Snowflake.of(env.get("DEV_ROLE_ID"));
        adminsRoleId = Snowflake.of(env.get("ADMIN_ROLE_ID"));

        instanceId = env.get("INSTANCE_ID");
        instanceApiKey = env.get("INSTANCE_API_KEY");
        watchdogHost = env.get("WATCHDOG_HOST");

        enableBanListener = Boolean.parseBoolean(env.get("ENABLE_BAN_LISTENER"));
        if(enableBanListener)
            bansChannel = Snowflake.of(env.get("BANS_CHANNEL_ID"));
    }
}

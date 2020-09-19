package com.luffbox.tickman;

import com.luffbox.tickman.commands.*;
import com.luffbox.tickman.listeners.EventListener;
import com.luffbox.tickman.util.snowflake.InvalidSystemClockException;
import com.luffbox.tickman.util.snowflake.SnowflakeServer;
import com.luffbox.tickman.util.ticket.GuildOpts;
import com.luffbox.tickman.util.cmd.CmdHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.cli.*;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.*;

public class TickMan {

	public static final File DATA = new File(System.getProperty("user.dir") + File.separator + "data");
	public static final File GUILD_DATA = new File(DATA, "guilds");
	public static final File TICKET_DATA = new File(DATA, "tickets");
	public static final SnowflakeServer SNOWFLAKE_SERVER = new SnowflakeServer(0L, 0L);
	public static final long TIMESTAMP_OFFSET = 22;

	static {
		if (!(DATA.exists() || DATA.mkdirs())) { System.err.println("Failed to create data directory"); }
		if (!(GUILD_DATA.exists() || GUILD_DATA.mkdirs())) { System.err.println("Failed to create guild data directory"); }
		if (!(TICKET_DATA.exists() || TICKET_DATA.mkdirs())) { System.err.println("Failed to create ticket data directory"); }

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try { System.out.println("Test snowflake: " + SNOWFLAKE_SERVER.nextIdAsString()); } catch (Exception ignore) {}
			}
		}, 100, 100);
	}

	private static final Map<Guild, GuildOpts> guildRecords = new HashMap<>();
	private static JDA jda;

	public final Set<CmdHandler> cmds = new HashSet<>();
	private final String botUserName;
	private final String inviteClientId;

	public String getBotName() { return botUserName; }
	public String getInviteUrl() { return "https://discord.com/api/oauth2/authorize?client_id=" + inviteClientId + "&scope=bot&permissions=93264"; }

	public static void main(String[] args) { new TickMan(args); }

	public TickMan(String[] args) {
		Options options = new Options();

		Option input = new Option("t", "token", true, "Your bot's auth token");
		input.setRequired(true);
		options.addOption(input);

		input = new Option("c", "clientid", true, "Your bot's client ID (used in the invite link)");
		input.setRequired(true);
		options.addOption(input);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("tickman [OPTION]...", options);
			System.exit(1);
		}

		String token = cmd.getOptionValue("token");
		inviteClientId = cmd.getOptionValue("clientid");

		try {
			JDABuilder builder = JDABuilder.createDefault(token);
			confBuilder(builder);
			jda = builder.build();
		} catch (LoginException e) {
			System.err.println("Failed to login with provided auth token!\n" + e.getMessage());
			System.exit(1);
		}

		jda.addEventListener(new EventListener(this));

		botUserName = jda.getSelfUser().getName();
		cmds.add(new HelpCmd(this));
		cmds.add(new InviteCmd(this));
		cmds.add(new ConfigureGuildCmd(this));

	}

	private void methodToCallWhenVariableIsNotNull() {}

	private void confBuilder (JDABuilder builder) {
		builder.disableCache(CacheFlag.ACTIVITY);
		builder.setChunkingFilter(ChunkingFilter.NONE);
		builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
		builder.setLargeThreshold(50);
	}

	public static long getSnowflake() {
		long id = -1L;
		do {
			try { id = SNOWFLAKE_SERVER.nextId(); } catch (InvalidSystemClockException ignore) {}
		} while (id == -1L);
		return id;
	}

	public static GuildOpts getGuildOptions(Guild g) {
		if (g == null) { return GuildOpts.def(); }
		createGuildOpts(g);
		return guildRecords.containsKey(g) ? guildRecords.get(g) : GuildOpts.def();
	}

	public static EmbedBuilder newEmbed(GuildOpts guildData) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(guildData.getEmbedColor().intValue());
		return embed;
	}

	private static void createGuildOpts(Guild g) {
		if (!guildRecords.containsKey(g)) {
			guildRecords.put(g, new GuildOpts(g));
		}
	}

}

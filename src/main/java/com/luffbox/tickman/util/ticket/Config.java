package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.luffbox.tickman.TickMan;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates all the Guild-specific data and allows for fetching and modifying it.
 * If the Guild is set to null, most methods that modify the data will fail.
 */
public class Config implements Jsonable {

	private enum Field {
		GUILD_ID("guildId"),
		CMD_PREFIX("cmdPrefix"),
		ALLOW_INVITE("allowInvite"),
		EMBED_COLOR("embedColor"),
		DEPARTMENTS("departments");

		public String path;
		Field(String path) { this.path = path; }
	}

	private final File outFile;

	private final Guild guild;
	private final Set<Department> departments = new HashSet<>();
	private String cmdPrefix = "!";
	private boolean allowInvite = false;
	private BigDecimal embedColor = BigDecimal.valueOf(0x33AAFF);

	/**
	 * Creates a default Config object with a null Guild. Mostly used when handling private messages
	 * @return A Config instance populated with default values and a null Guild object.
	 */
	public static Config def() { return new Config(null); }

	public Config(Guild guild) {
		this.guild = guild;
		outFile = guild == null ? null : new File(TickMan.GUILD_DATA, guild.getId() + ".json");
		load();
		save();
	}

	public void load() {
		if (guild == null) { (new Exception()).printStackTrace(); }
		if (guild == null || !outFile.exists()) return;

		try (FileReader readIn = new FileReader(outFile)) {
			JsonObject json = (JsonObject) Jsoner.deserialize(readIn);

			setCmdPrefix((String) json.get(Field.CMD_PREFIX.path));
			setAllowInvite((boolean) json.get(Field.ALLOW_INVITE.path));
			setEmbedColor((BigDecimal) json.get(Field.EMBED_COLOR.path));

			JsonObject depts = (JsonObject) json.get(Field.DEPARTMENTS.path);
			if (depts != null) {
				for (String deptId : depts.keySet()) {
					try {
						departments.add(new Department(this, (JsonObject) depts.get(deptId)));
					} catch (Exception ignore) {
					}
				}
			}

		} catch (IOException e) {
			System.err.println("Failed to read guild data file for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		} catch (JsonException e) {
			System.err.println("Guild data file has invalid JSON for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		}
	}

	public void save() {
		if (guild == null) return;

		try (FileWriter fw = new FileWriter(outFile)) {
			if (outFile.exists() || outFile.createNewFile()) { fw.write(toJson()); }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the Guild associated with this Config
	 * @return The Guild object assocaited with this Config. May be null if a default set of Config are used
	 * @see Config#def()
	 */
	public Guild getGuild() { return guild; }

	/**
	 * Gets the Guild's current command prefix
	 * @return A String containing the Guild's current command prefix
	 */
	public String getCmdPrefix() { return cmdPrefix; }

	/**
	 * Indicates whether use of the invite command is enabled.
	 * If true, users can get a link to invite the bot to their own servers with the invite command.
	 * @return true if the invite command is enabled, false otherwise
	 */
	public boolean canInvite() { return allowInvite; }

	/**
	 * Gets the color that will be used for embeds created by the bot
	 * @return An int value representing the embed color
	 */
	public BigDecimal getEmbedColor() { return embedColor; }

	/**
	 * Gets a Set of the current Departments
	 * @return An immutable copy of the current Departments
	 */
	public Set<Department> getDepartments() { return Set.copyOf(departments); }

	/**
	 * Sets the Guild's command prefix
	 * @param cmdPrefix The prefix to require before commands
	 */
	public void setCmdPrefix(String cmdPrefix) {
		this.cmdPrefix = cmdPrefix;
		save();
	}

	/**
	 * Sets whether the Guild allows use of the invite command or not
	 * @param allow true if the invite command should be enabled, false otherwise
	 */
	public void setAllowInvite(boolean allow) {
		allowInvite = allow;
		save();
	}

	public void setEmbedColor(BigDecimal color) {
		color = color.max(BigDecimal.valueOf(0x000000)).min(BigDecimal.valueOf(0xFFFFFF));
		embedColor = color;
	}

	public EmbedBuilder newEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(getEmbedColor().intValue());
		return embed;
	}

	@Override
	public String toJson() {
		final StringWriter writable = new StringWriter();
		try { this.toJson(writable); } catch (final IOException e) { e.printStackTrace(); }
		return writable.toString();
	}

	@Override
	public void toJson(Writer writable) throws IOException {
		final JsonObject json = new JsonObject(new HashMap<>() {
			{
				put(Field.GUILD_ID.path, guild.getId());
				put(Field.CMD_PREFIX.path, cmdPrefix);
				put(Field.ALLOW_INVITE.path, allowInvite);
				put(Field.EMBED_COLOR.path, embedColor);
				JsonObject depts = new JsonObject();
				for (Department dept : departments) { depts.put(dept.getId(), dept.toJson()); }
				put(Field.DEPARTMENTS.path, depts);
			}
		});
		json.toJson(writable);
	}
}

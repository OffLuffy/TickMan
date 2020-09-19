package com.luffbox.tickman.util;

import net.dv8tion.jda.api.entities.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Encapsulates all the Guild-specific data and allows for fetching and modifying it.
 * If the Guild is set to null, most methods that modify the data will fail.
 */
public class GuildOpts {

	private static final File OUT_DIR = new File(System.getProperty("user.dir") + File.separator + "data");
	static { if (!OUT_DIR.exists()) {
		//noinspection ResultOfMethodCallIgnored
		OUT_DIR.mkdirs();
	}}

	private final File outFile;

	private final Guild guild;
	private final Set<Role> supportRoles = new HashSet<>();
	private Category ticketCategory = null;
	private TextChannel supportChannel = null;
	private String cmdPrefix = "!";
	private boolean allowInvite = false;

	/**
	 * Creates a default GuildOpts object with a null Guild. Mostly used when handling private messages
	 * @return A GuildOpts instance populated with default values and a null Guild object.
	 */
	public static GuildOpts def() { return new GuildOpts(null); }

	public GuildOpts (Guild guild) {
		this.guild = guild;
		outFile = guild == null ? null : new File(OUT_DIR, guild.getId() + ".json");
		save();
	}

	public void load() {
		if (guild == null || !outFile.exists()) return;
		JSONParser parser = new JSONParser();
		try (Reader reader = new FileReader(outFile)) {
			JSONObject json = (JSONObject) parser.parse(reader);
			setTicketCategoryId((String) json.get("ticketCategory"));
			setSupportChannelId((String) json.get("supportCategory"));
			setCmdPrefix((String) json.get("cmdPrefix"));
			setAllowInvite((boolean) json.get("allowInvite"));
		} catch (IOException e) {
			System.err.println("Failed to read guild data file for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		} catch (ParseException e) {
			System.err.println("Guild data file has invalid JSON for guild: " + guild.getName() + ", file: " + outFile.getName() + ", reason: " + e.getMessage());
		}
	}

	public void save() {
		if (guild == null) return;

		JSONObject json = new JSONObject(new HashMap<>() {
			{
				put("guildId", guild.getId());
				put("ticketCategory", ticketCategory != null ? ticketCategory.getId() : "");
				put("supportChannel", supportChannel != null ? supportChannel.getId() : "");
				put("cmdPrefix", cmdPrefix);
				put("allowInvite", allowInvite);
				Set<String> roles = new HashSet<>();
				for (Role r : supportRoles) { roles.add(r.getId()); }
				put("supportRoles", roles);
			}
		});

		try (FileWriter fw = new FileWriter(outFile)) {
			if (outFile.exists() || outFile.createNewFile()) {
				fw.write(json.toJSONString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the Guild associated with this GuildOpts
	 * @return The Guild object assocaited with this GuildOpts. May be null if a default set of GuildOpts are used
	 * @see #def()
	 */
	public Guild getGuild() { return guild; }

	/**
	 * Gets a Set of the current support Roles
	 * @return An immutable copy of the current support Roles
	 */
	public Set<Role> getSupportRoles() { return Set.copyOf(supportRoles); }

	/**
	 * Gets the Category that should contain new Ticket Channels
	 * @return A Category object which should contain newly created Ticket Channels
	 */
	public Category getTicketCategory() { return ticketCategory; }

	/**
	 * Gets the currently set TextChannel that is currently handling requests
	 * @return A TextChannel object which should be handling requests. May be null if not set by the Guild
	 */
	public TextChannel getSupportChannel() { return supportChannel; }

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
	 * Adds a Role to the Guild's List of support Roles
	 * @param r The Role to add
	 * @return true if added successfully or already in the List. Will fail if Guild is null
	 */
	public boolean addSupportRole(Role r) {
		if (guild == null || r == null || !r.getGuild().equals(guild)) { return false; }
		supportRoles.add(r);
		save();
		return true;
	}

	/**
	 * Removes a Role from the Guild's List of support Roles
	 * @param r The Role to remove
	 * @return true if removed successfully or not already in the List. Will fail if Guild is null
	 */
	public boolean removeSupportRole(Role r) {
		if (guild == null || r == null || !r.getGuild().equals(guild)) { return false; }
		supportRoles.remove(r);
		save();
		return true;
	}

	/**
	 * Remotes all Roles from the support Roles List
	 */
	public void clearSupportRoles() { supportRoles.clear(); save(); }

	/**
	 * Sets the Category used to contain Ticket Channels
	 * @param category The Category intended to contain Tickets Channels
	 * @return true of the Category is valid. Will fail if Guild is null
	 */
	public boolean setTicketCategory(Category category) {
		if (guild == null || category == null || !category.getGuild().equals(guild)) { return false; }
		ticketCategory = category;
		System.out.println("Guild: " + guild.getName() + " (ID: " + guild.getId() + ") set ticket category to "
				+ category.getName() + " (ID: " + category.getId() + ")");
		save();
		return true;
	}

	/**
	 * Sets the Category used to contain Ticket Channels
	 * @param id The ID of a Category intended to contain Ticket Channels
	 * @return true of the ID resolves to a valid Category. Will fail if Guild is null
	 */
	public boolean setTicketCategoryId(String id) {
		if (id != null && !id.isBlank() && guild != null) {
			try {
				Category cat = guild.getCategoryById(id);
				if (cat != null) { return setTicketCategory(cat); }
			} catch (Exception ignore) {}
		}
		return false;
	}

	/**
	 * Sets the TextChannel used to create Tickets
	 * @param channel The TextChannel intended to handle requests and convert them into Tickets
	 * @return true of the TextChannel is valid. Will fail if Guild is null
	 */
	public boolean setSupportChannel(TextChannel channel) {
		if (guild == null || channel == null || !channel.getGuild().equals(guild)) { return false; }
		supportChannel = channel;
		System.out.println("Guild: " + guild.getName() + " (ID: " + guild.getId() + ") set support channel to "
				+ channel.getName() + " (ID: " + channel.getId() + ")");
		supportChannel.sendMessage("Now listening to this channel for ticket requests")
				.queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
		save();
		return true;
	}

	/**
	 * Sets the TextChannel used to create Tickets
	 * @param id The ID of a TextChannel intended to handle requests and convert them into Tickets
	 * @return true of the ID resolves to a valid TextChannel. Will fail if Guild is null
	 */
	public boolean setSupportChannelId(String id) {
		if (id != null && !id.isBlank() && guild != null) {
			try {
				GuildChannel gc = guild.getGuildChannelById(id);
				if (gc != null && gc.getType() == ChannelType.TEXT) {
					return setSupportChannel((TextChannel) gc);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

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
}

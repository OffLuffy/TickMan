package com.luffbox.tickman.util;

import net.dv8tion.jda.api.entities.*;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		save();
	}

	public void load() {

	}

	public void save() {
		if (guild == null) return;

		File outDir = new File(System.getProperty("user.dir") + File.separator + "data");
		File outFile = new File(outDir, guild.getId() + ".json");

		Map<String, Object> data = new HashMap<>();
		data.put("guildId", guild.getId());
		data.put("ticketCategory", ticketCategory != null ? ticketCategory.getId() : "");
		data.put("supportChannel", supportChannel != null ? supportChannel.getId() : "");
		data.put("cmdPrefix", cmdPrefix);
		data.put("allowInvite", allowInvite);
		data.put("supportRoles", supportRoles.stream().map(ISnowflake::getId).toArray());
		/*Set<String> roles = new HashSet<>();
		for (Role r : supportRoles) { roles.add(r.getId()); }
		data.put("supportRoles", roles.toArray(new String[] {}));*/

		JSONObject guild = new JSONObject(data);

		try (FileWriter fw = new FileWriter(outFile)) {
			if (outFile.exists() || outFile.createNewFile()) {
				fw.write(guild.toJSONString());
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
		return true;
	}

	/**
	 * Remotes all Roles from the support Roles List
	 */
	public void clearSupportRoles() { supportRoles.clear(); }

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
	}

	/**
	 * Sets whether the Guild allows use of the invite command or not
	 * @param allow true if the invite command should be enabled, false otherwise
	 */
	public void setAllowInvite(boolean allow) {
		allowInvite = allow;
	}
}

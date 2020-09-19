package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.luffbox.tickman.TickMan;
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
public class GuildOpts implements Jsonable {

	private enum Field {
		GUILD_ID("guildId"),
		TICKET_CATEGORY("ticketCategory"),
		SUPPORT_CHANNEL("supportChannel"),
		CMD_PREFIX("cmdPrefix"),
		ALLOW_INVITE("allowInvite"),
		SUPPORT_ROLES("supportRoles"),
		EMBED_COLOR("embedColor");

		public String path;
		Field(String path) { this.path = path; }
	}

	private final File outFile;

	private final Guild guild;
	private final Set<Department> departments = new HashSet<>();
//	private final Set<Role> supportRoles = new HashSet<>();
//	private Category ticketCategory = null;
//	private TextChannel supportChannel = null;
	private String cmdPrefix = "!";
	private boolean allowInvite = false;
	private BigDecimal embedColor = BigDecimal.valueOf(0x33AAFF);

	/**
	 * Creates a default GuildOpts object with a null Guild. Mostly used when handling private messages
	 * @return A GuildOpts instance populated with default values and a null Guild object.
	 */
	public static GuildOpts def() { return new GuildOpts(null); }

	public GuildOpts (Guild guild) {
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

//			setTicketCategoryId((String) json.get(Field.TICKET_CATEGORY.path));
//			setSupportChannelId((String) json.get(Field.SUPPORT_CHANNEL.path), false);
			setCmdPrefix((String) json.get(Field.CMD_PREFIX.path));
			setAllowInvite((boolean) json.get(Field.ALLOW_INVITE.path));
			setEmbedColor((BigDecimal) json.get(Field.EMBED_COLOR.path));

//			JsonArray roleIds = (JsonArray) json.get(Field.SUPPORT_ROLES.path);
//			for (Object rid : roleIds.toArray()) {
//				try {
//					Role role = guild.getRoleById(rid.toString());
//					if (role != null) { supportRoles.add(role); }
//				} catch (Exception ignore) {}
//			}

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
	 * Gets the Guild associated with this GuildOpts
	 * @return The Guild object assocaited with this GuildOpts. May be null if a default set of GuildOpts are used
	 * @see #def()
	 */
	public Guild getGuild() { return guild; }

//	/**
//	 * Gets a Set of the current support Roles
//	 * @return An immutable copy of the current support Roles
//	 */
//	public Set<Role> getSupportRoles() { return Set.copyOf(supportRoles); }
//
//	/**
//	 * Gets the Category that should contain new Ticket Channels
//	 * @return A Category object which should contain newly created Ticket Channels
//	 */
//	public Category getTicketCategory() { return ticketCategory; }
//
//	/**
//	 * Gets the currently set TextChannel that is currently handling requests
//	 * @return A TextChannel object which should be handling requests. May be null if not set by the Guild
//	 */
//	public TextChannel getSupportChannel() { return supportChannel; }

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

//	/**
//	 * Adds a Role to the Guild's List of support Roles
//	 * @param r The Role to add
//	 * @return true if added successfully or already in the List. Will fail if Guild is null
//	 */
//	public boolean addSupportRole(Role r) {
//		if (guild == null || r == null || !r.getGuild().equals(guild)) { return false; }
//		supportRoles.add(r);
//		save();
//		return true;
//	}
//
//	/**
//	 * Removes a Role from the Guild's List of support Roles
//	 * @param r The Role to remove
//	 * @return true if removed successfully or not already in the List. Will fail if Guild is null
//	 */
//	public boolean removeSupportRole(Role r) {
//		if (guild == null || r == null || !r.getGuild().equals(guild)) { return false; }
//		supportRoles.remove(r);
//		save();
//		return true;
//	}
//
//	/**
//	 * Remotes all Roles from the support Roles List
//	 */
//	public void clearSupportRoles() { supportRoles.clear(); save(); }
//
//	/**
//	 * Sets the Category used to contain Ticket Channels
//	 * @param category The Category intended to contain Tickets Channels
//	 * @return true of the Category is valid. Will fail if Guild is null
//	 */
//	public boolean setTicketCategory(Category category) {
//		if (guild == null || category == null || !category.getGuild().equals(guild)) { return false; }
//		ticketCategory = category;
//		save();
//		return true;
//	}
//
//	/**
//	 * Sets the Category used to contain Ticket Channels
//	 * @param id The ID of a Category intended to contain Ticket Channels
//	 * @return true of the ID resolves to a valid Category. Will fail if Guild is null
//	 */
//	public boolean setTicketCategoryId(String id) {
//		if (id != null && !id.isBlank() && guild != null) {
//			try {
//				Category cat = guild.getCategoryById(id);
//				if (cat != null) { return setTicketCategory(cat); }
//			} catch (Exception ignore) {}
//		}
//		return false;
//	}
//
//	/**
//	 * Sets the TextChannel used to create Tickets
//	 * @param channel The TextChannel intended to handle requests and convert them into Tickets
//	 * @return true of the TextChannel is valid. Will fail if Guild is null
//	 */
//	public boolean setSupportChannel(TextChannel channel, boolean announce) {
//		if (guild == null || channel == null || !channel.getGuild().equals(guild)) { return false; }
//		supportChannel = channel;
//		if (announce) {
//			supportChannel.sendMessage("Now listening to this channel for ticket requests")
//					.queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
//		}
//		save();
//		return true;
//	}
//
//	/**
//	 * Sets the TextChannel used to create Tickets
//	 * @param id The ID of a TextChannel intended to handle requests and convert them into Tickets
//	 * @return true of the ID resolves to a valid TextChannel. Will fail if Guild is null
//	 */
//	public boolean setSupportChannelId(String id, boolean announce) {
//		if (id != null && !id.isBlank() && guild != null) {
//			try {
//				GuildChannel gc = guild.getGuildChannelById(id);
//				if (gc != null && gc.getType() == ChannelType.TEXT) {
//					return setSupportChannel((TextChannel) gc, announce);
//				}
//			} catch (Exception ignore) {}
//		}
//		return false;
//	}

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
//				put(Field.TICKET_CATEGORY.path, ticketCategory != null ? ticketCategory.getId() : "");
//				put(Field.SUPPORT_CHANNEL.path, supportChannel != null ? supportChannel.getId() : "");
				put(Field.CMD_PREFIX.path, cmdPrefix);
				put(Field.ALLOW_INVITE.path, allowInvite);
				put(Field.EMBED_COLOR.path, embedColor);

//				Set<String> roles = new HashSet<>();
//				for (Role r : supportRoles) { roles.add(r.getId()); }
//				put(Field.SUPPORT_ROLES.path, roles);
			}
		});
		json.toJson(writable);
	}
}

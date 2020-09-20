package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Represents a certain category of support (i.e. Technology, Maintenance, Billing, etc)
 */
public class Department implements ITMSnowflake {

	private enum Field {
		NAME("name"),
		TICKET_CATEGORY("ticketCategory"),
		SUPPORT_CHANNEL("supportChannel"),
		SUPPORT_ROLES("supportRoles");

		public String path;
		Field(String path) { this.path = path; }
	}

	private final long deptId;
	private final Config config;
	private String name;
	private final Set<Role> supportRoles = new HashSet<>();
	private Category ticketCategory = null;
	private TextChannel supportChannel = null;

	public Department(Config config, JsonObject deptJson) {
		this(config);
		fromJson(deptJson);
	}
	public Department (Config config) {
		this.deptId = TickMan.getSnowflake();
		this.config = config;
	}

	@Override
	public long getIdLong() { return deptId; }

	public Config getConfig() { return config; }

	public String getName() { return name; }

	/**
	 * Gets the Guild associated with this Department
	 * @return The Guild object assocaited with this Department. May be null if a default set of Config are used
	 * @see Config#def()
	 */
	public Guild getGuild() { return config.getGuild(); }

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

	public void setName(String name) { this.name = name; }

	/**
	 * Adds a Role to the Guild's List of support Roles
	 * @param r The Role to add
	 * @return true if added successfully or already in the List. Will fail if Guild is null
	 */
	public boolean addSupportRole(Role r) {
		if (config.getGuild() == null || r == null || !r.getGuild().equals(config.getGuild())) { return false; }
		supportRoles.add(r);
		config.save();
		return true;
	}

	/**
	 * Removes a Role from the Guild's List of support Roles
	 * @param r The Role to remove
	 * @return true if removed successfully or not already in the List. Will fail if Guild is null
	 */
	public boolean removeSupportRole(Role r) {
		if (config.getGuild() == null || r == null || !r.getGuild().equals(config.getGuild())) { return false; }
		supportRoles.remove(r);
		config.save();
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
		if (config.getGuild() == null || category == null || !category.getGuild().equals(config.getGuild())) { return false; }
		ticketCategory = category;
		config.save();
		return true;
	}

	/**
	 * Sets the Category used to contain Ticket Channels
	 * @param id The ID of a Category intended to contain Ticket Channels
	 * @return true of the ID resolves to a valid Category. Will fail if Guild is null
	 */
	public boolean setTicketCategoryId(String id) {
		if (id != null && !id.isBlank() && config.getGuild() != null) {
			try {
				Category cat = config.getGuild().getCategoryById(id);
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
	public boolean setSupportChannel(TextChannel channel, boolean announce) {
		if (config.getGuild() == null || channel == null || !channel.getGuild().equals(config.getGuild())) { return false; }
		supportChannel = channel;
		if (announce) {
			supportChannel.sendMessage("Now listening to this channel for ticket requests")
					.queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
		}
		config.save();
		return true;
	}

	/**
	 * Sets the TextChannel used to create Tickets
	 * @param id The ID of a TextChannel intended to handle requests and convert them into Tickets
	 * @return true of the ID resolves to a valid TextChannel. Will fail if Guild is null
	 */
	public boolean setSupportChannelId(String id, boolean announce) {
		if (id != null && !id.isBlank() && config.getGuild() != null) {
			try {
				GuildChannel gc = config.getGuild().getGuildChannelById(id);
				if (gc != null && gc.getType() == ChannelType.TEXT) {
					return setSupportChannel((TextChannel) gc, announce);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

	public void fromJson(JsonObject json) {
		setName((String) json.get(Field.NAME.path));
		setSupportChannelId((String) json.get(Field.SUPPORT_CHANNEL.path), false);
		setTicketCategoryId((String) json.get(Field.TICKET_CATEGORY.path));

		JsonArray roleIds = (JsonArray) json.get(Field.SUPPORT_ROLES.path);
		for (Object rid : roleIds.toArray()) {
			try {
				Role role = config.getGuild().getRoleById(rid.toString());
				if (role != null) { addSupportRole(role); }
			} catch (Exception ignore) {}
		}
	}

	public JsonObject toJson() {
		return new JsonObject(new HashMap<>() {
			{
				put(Field.TICKET_CATEGORY.path, ticketCategory != null ? ticketCategory.getId() : "");
				put(Field.SUPPORT_CHANNEL.path, supportChannel != null ? supportChannel.getId() : "");
				Set<String> roles = new HashSet<>();
				for (Role r : supportRoles) { roles.add(r.getId()); }
				put(Field.SUPPORT_ROLES.path, roles);
			}
		});
	}

	public EmbedBuilder newEmbed() { return config.newEmbed(); }

	@Override
	public boolean equals(Object obj) { return obj instanceof Department other && other.getIdLong() == deptId; }

	@Override
	public int hashCode() {
		return Long.hashCode(deptId);
	}

	@Override
	public String toString() {
		return String.format("Department: %s (%d)", name, deptId);
	}
}

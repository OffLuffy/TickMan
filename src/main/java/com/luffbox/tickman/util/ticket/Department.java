package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.entities.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Represents a certain category of support (i.e. Technology, Maintenance, Billing, etc)
 */
public class Department implements ITMSnowflake {

	private enum Field {
		TICKET_CATEGORY("ticketCategory"),
		SUPPORT_CHANNEL("supportChannel"),
		SUPPORT_ROLES("supportRoles");

		public String path;
		Field(String path) { this.path = path; }
	}

	private final long deptId;
	private final GuildOpts guildData;
	private String name;
	private final Set<Role> supportRoles = new HashSet<>();
	private Category ticketCategory = null;
	private TextChannel supportChannel = null;

	public Department (GuildOpts guildData, String name) {
		this.deptId = TickMan.getSnowflake();
		this.guildData = guildData;
		this.name = name;
	}

	@Override
	public long getIdLong() { return deptId; }

	public String getName() { return name; }

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
		if (guildData.getGuild() == null || r == null || !r.getGuild().equals(guildData.getGuild())) { return false; }
		supportRoles.add(r);
		guildData.save();
		return true;
	}

	/**
	 * Removes a Role from the Guild's List of support Roles
	 * @param r The Role to remove
	 * @return true if removed successfully or not already in the List. Will fail if Guild is null
	 */
	public boolean removeSupportRole(Role r) {
		if (guildData.getGuild() == null || r == null || !r.getGuild().equals(guildData.getGuild())) { return false; }
		supportRoles.remove(r);
		guildData.save();
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
		if (guildData.getGuild() == null || category == null || !category.getGuild().equals(guildData.getGuild())) { return false; }
		ticketCategory = category;
		guildData.save();
		return true;
	}

	/**
	 * Sets the Category used to contain Ticket Channels
	 * @param id The ID of a Category intended to contain Ticket Channels
	 * @return true of the ID resolves to a valid Category. Will fail if Guild is null
	 */
	public boolean setTicketCategoryId(String id) {
		if (id != null && !id.isBlank() && guildData.getGuild() != null) {
			try {
				Category cat = guildData.getGuild().getCategoryById(id);
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
		if (guildData.getGuild() == null || channel == null || !channel.getGuild().equals(guildData.getGuild())) { return false; }
		supportChannel = channel;
		if (announce) {
			supportChannel.sendMessage("Now listening to this channel for ticket requests")
					.queue(msg -> msg.delete().queueAfter(15, TimeUnit.SECONDS));
		}
		guildData.save();
		return true;
	}

	/**
	 * Sets the TextChannel used to create Tickets
	 * @param id The ID of a TextChannel intended to handle requests and convert them into Tickets
	 * @return true of the ID resolves to a valid TextChannel. Will fail if Guild is null
	 */
	public boolean setSupportChannelId(String id, boolean announce) {
		if (id != null && !id.isBlank() && guildData.getGuild() != null) {
			try {
				GuildChannel gc = guildData.getGuild().getGuildChannelById(id);
				if (gc != null && gc.getType() == ChannelType.TEXT) {
					return setSupportChannel((TextChannel) gc, announce);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

	public JsonObject toJson() throws IOException {
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
}

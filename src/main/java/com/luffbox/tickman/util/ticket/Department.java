package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents a certain category of support (i.e. Technology, Maintenance, Billing, etc)
 */
public class Department implements ITMSnowflake {

	private enum Field {;

		public String path;
		Field(String path) { this.path = path; }
	}

	private final long deptId;
	private final Config config;
	private String name;
	private final Set<Role> supportRoles = new HashSet<>();
	private final Set<Ticket> tickets = new HashSet<>();
	private Category ticketCategory = null;
	private TextChannel supportChannel = null;

	public Department (long deptId, @Nonnull Config config, @Nonnull String name, @Nullable JsonObject json) {
		this.deptId = deptId;
		this.config = config;
		this.name = name;
		if (json != null) { fromJson(json); }
	}

	@Override
	public long getIdLong() { return deptId; }

	public Config getConfig() { return config; }

	public String getName() { return name; }

	/**
	 * Gets the Guild associated with this Department
	 * @return The Guild object assocaited with this Department.
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

	/**
	 * Gets a Set of the current Tickets
	 * @return An immutable copy of the current Tickets
	 */
	public Set<Ticket> getTickets() { return Set.copyOf(tickets); }

	public void setName(String name) { this.name = name; }

	/**
	 * Adds a Role to the Guild's List of support Roles
	 * @param r The Role to add
	 * @return true if added successfully or already in the List. Will fail if Guild is null
	 */
	public boolean addSupportRole(Role r) {
		if (getGuild() == null || r == null || !r.getGuild().equals(getGuild())) { return false; }
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
		if (getGuild() == null || r == null || !r.getGuild().equals(getGuild())) { return false; }
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
		if (getGuild() == null || category == null || !category.getGuild().equals(getGuild())) { return false; }
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
		if (id != null && !id.isBlank() && getGuild() != null) {
			try {
				Category cat = getGuild().getCategoryById(id);
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
		if (getGuild() == null || channel == null || !channel.getGuild().equals(getGuild())) { return false; }
		supportChannel = channel;
		if (announce) {
			supportChannel.sendMessage("Now listening to this channel for ticket requests")
					.queue(msg -> TickMan.queueLater(msg.delete(), TickMan.Duration.SHORT));
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
		if (id != null && !id.isBlank() && getGuild() != null) {
			try {
				GuildChannel gc = getGuild().getGuildChannelById(id);
				if (gc != null && gc.getType() == ChannelType.TEXT) {
					return setSupportChannel((TextChannel) gc, announce);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

	public void addTicket(Ticket ticket) { tickets.add(ticket); }

	public void removeTicket(Ticket ticket) { tickets.remove(ticket); }

	public void createTicket(final Message msg) { createTicket(msg, null, null); }
	public void createTicket(final Message msg, @Nullable Consumer<Ticket> success) { createTicket(msg, success, null); }
	public void createTicket(final Message msg, @Nullable Consumer<Ticket> success, @Nullable Consumer<? super Throwable> failure) {
		if (msg == null || msg.getMember() == null) {
			if (failure != null) { failure.accept(new NullPointerException("Message or Message Member was null")); }
			return;
		}
		final long tid = TickMan.getSnowflake();
		getTicketCategory().createTextChannel(String.format("ticket_%x", tid)).queue(channel -> {
			Ticket ticket = new Ticket(tid, this, msg.getMember(), channel, msg.getContentRaw());
			if (success != null) { success.accept(ticket); }
		}, error -> { if (failure != null) failure.accept(error); });
	}

	public void fromJson(JsonObject json) {
		setName((String) json.get(Config.Field.DEPT_NAME.path));
		setSupportChannelId((String) json.get(Config.Field.DEPT_CHANNEL.path), false);
		setTicketCategoryId((String) json.get(Config.Field.DEPT_CATEGORY.path));

		JsonArray roleIds = (JsonArray) json.get(Config.Field.DEPT_ROLES.path);
		if (roleIds != null && !roleIds.isEmpty()) {
			for (Object rid : roleIds.toArray()) {
				try {
					Role role = getGuild().getRoleById(rid.toString());
					if (role != null) { addSupportRole(role); }
				} catch (Exception ignore) {}
			}
		}

		JsonObject tickets = (JsonObject) json.get(Config.Field.DEPT_TICKETS.path);
		if (tickets != null && !tickets.isEmpty()) {
			for (String ticketId : tickets.keySet()) {
				JsonObject ticketJson = (JsonObject) tickets.get(ticketId);
				Ticket.fromJson(Long.parseLong(ticketId), ticketJson, this);
			}
		} else if (!getGuild().getTextChannels().isEmpty()) { // TODO: Remove debug ticket creation
			TextChannel def = getGuild().getTextChannels().get(0);
			for (int i = 0; i < 10; i++) {
				new Ticket(TickMan.getSnowflake(), this, getGuild().getSelfMember(), def, "Ticket #" + i);
			}
		}
	}

	public JsonObject toJson() {
		return new JsonObject() { {
//			put(Config.Field.DEPT_ID.path, getId());
			put(Config.Field.DEPT_NAME.path, name);
			put(Config.Field.DEPT_CATEGORY.path, ticketCategory != null ? ticketCategory.getId() : "");
			put(Config.Field.DEPT_CHANNEL.path, supportChannel != null ? supportChannel.getId() : "");
			Set<String> roles = new HashSet<>();
			for (Role r : supportRoles) { roles.add(r.getId()); }
			put(Config.Field.DEPT_ROLES.path, roles);
			JsonObject ticketsJson = new JsonObject() { { for (Ticket t : tickets) { put(t.getId(), t.toJson()); } } };
			put(Config.Field.DEPT_TICKETS.path, ticketsJson);
		} };
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

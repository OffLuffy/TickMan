package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

public class Ticket implements Jsonable, ITMSnowflake {

	private final long ticketId;
	private Department dept;
	private TextChannel ticketChannel;
	private Member author;
	// TODO: Participants?

	public Ticket(long id, @Nonnull Department dept, @Nonnull Member author, @Nonnull TextChannel channel) {
		this.dept = dept;
		this.ticketId = id;
		this.ticketChannel = channel;
		this.author = author;
		// TODO: Setup channel permissions
	}

	@Override
	public long getIdLong() { return ticketId; }

	public Department getDepartment() { return dept; }

	public TextChannel getTicketChannel() { return ticketChannel; }

	public Member getAuthor() { return author; }

	/**
	 * Gets the Guild associated with this Ticket
	 * @return The Guild object assocaited with this Ticket. May be null if a default set of Config are used
	 * @see Config#def()
	 */
	public Guild getGuild() { return dept.getGuild(); }

	public boolean setAuthor(Member author) {
		if (getGuild() == null || author == null || !author.getGuild().equals(getGuild())) { return false; }
		this.author = author;
		dept.getConfig().save();
		return true;
	}
	public boolean setAuthorId(String id) {
		if (id != null && !id.isBlank()) {
			try {
				Member member = getGuild().getMemberById(id);
				if (member != null) {
					return setAuthor(member);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

	public boolean setTicketChannel(TextChannel channel) {
		if (getGuild() == null || channel == null || !channel.getGuild().equals(getGuild())) { return false; }
		this.ticketChannel = channel;
		dept.getConfig().save();
		return true;
	}
	public boolean setTicketChannelId(String id) {
		if (id != null && !id.isBlank()) {
			try {
				GuildChannel gc = getGuild().getGuildChannelById(id);
				if (gc != null && gc.getType() == ChannelType.TEXT) {
					return setTicketChannel((TextChannel) gc);
				}
			} catch (Exception ignore) {}
		}
		return false;
	}

	public void transferDepartment(Department otherDept) {
		this.dept = otherDept;
		// Send transferring notification, move channel, update message to say transferred
		ticketChannel.sendMessage(":hourglass: Transferring ticket to " + otherDept.getName())
			.queue(msg -> ticketChannel.getManager().setParent(otherDept.getTicketCategory())
				.queue(unused -> msg.editMessage(":white_check_mark: Transferred ticket to " + otherDept.getName())
					.queue()));
	}

	public void closeTicket() {
		// TODO: Save ticket transcript to file (upload to user?)
		ticketChannel.delete().queue();
	}

	public void fromJson(JsonObject json) {

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
				put("department", getDepartment().getId());
				put("author", getAuthor().getId());
				put("channel", getTicketChannel().getId());
			}
		});
		json.toJson(writable);
	}

	@Override
	public boolean equals(Object obj) { return eq(obj); }

	@Override
	public int hashCode() {
		return hc();
	}

	@Override
	public String toString() {
		return String.format("Ticket: %s (%d)", author.getEffectiveName(), ticketId);
	}
}

package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class Ticket implements ITMSnowflake {

	private final long ticketId;
	private Department dept;
	private TextChannel ticketChannel;
	private Member author;
	private String subject;
	// TODO: Participants?

	public Ticket(long id, @Nonnull Department dept, @Nonnull Member author, @Nonnull TextChannel channel, @Nonnull String subject) {
		this.dept = dept;
		this.ticketId = id;
		this.ticketChannel = channel;
		this.author = author;
		this.dept.addTicket(this);
		this.subject = subject;
		// TODO: Setup channel permissions
	}

	@Override
	public long getIdLong() { return ticketId; }

	public Department getDepartment() { return dept; }

	public TextChannel getTicketChannel() { return ticketChannel; }

	public Member getAuthor() { return author; }

	public String getSubject() { return subject; }

	/**
	 * Gets the Guild associated with this Ticket
	 * @return The Guild object assocaited with this Ticket.
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

	public void setSubject(@Nonnull String subject) { this.subject = subject; }

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

	public void transferDepartment(Department recvDept) {
		Department oldDept = this.dept;
		System.out.printf("Ticket transferred: %s (ID:%x) -- %s => %s%n", getSubject(), getIdLong(), oldDept, recvDept);
		this.dept = recvDept;

		oldDept.removeTicket(this);
		recvDept.addTicket(this);

		if (oldDept.getTicketCategory().equals(recvDept.getTicketCategory())) {
			// Already in the right category, so just send message with transfer confirmation
			ticketChannel.sendMessage(":white_check_mark: Transferred ticket to " + recvDept.getName()).queue();
		} else {
			// Send transferring notification, move channel, update message with transfer confirmation
			ticketChannel.sendMessage(":hourglass: Transferring ticket to " + recvDept.getName())
					.queue(msg -> ticketChannel.getManager().setParent(recvDept.getTicketCategory())
							.queue(unused -> msg.editMessage(":white_check_mark: Transferred ticket to " + recvDept.getName())
									.queue()));
		}
	}

	public void closeTicket() {
		// TODO: Save ticket transcript to file (upload to user?)
		System.out.printf("Ticket closed: %s (ID:%x)%n", getSubject(), getIdLong());
		ticketChannel.delete().queue();
	}

	public static Ticket fromJson(long id, JsonObject json, Department dept) {
		Member author = null;
		TextChannel channel = null;
		String subject = null;
		try { author = dept.getGuild().getMemberById((long) json.get(Config.Field.TICKET_AUTHOR.path)); } catch (Exception ignore) {}
		try { channel = dept.getGuild().getTextChannelById((long) json.get(Config.Field.TICKET_CHANNEL.path)); } catch (Exception ignore) {}
		try { subject = (String) json.get(Config.Field.TICKET_CHANNEL.path); } catch (Exception ignore) {}
		if (author == null || channel == null || subject == null) { return null; }
		return new Ticket(id, dept, author, channel, subject);
	}

	public JsonObject toJson() {
		return new JsonObject(new HashMap<>() {
			{
//				put(Config.Field.TICKET_ID.path, getId());
				put(Config.Field.TICKET_DEPT.path, getDepartment().getId());
				put(Config.Field.TICKET_AUTHOR.path, getAuthor().getId());
				put(Config.Field.TICKET_CHANNEL.path, getTicketChannel().getId());
				put(Config.Field.TICKET_SUBJECT.path, getSubject());
			}
		});
	}

	@Override
	public boolean equals(Object obj) { return eq(obj); }

	@Override
	public int hashCode() { return hc(); }

	@Override
	public String toString() { return String.format("Ticket: %s (%d)", author.getEffectiveName(), ticketId); }
}

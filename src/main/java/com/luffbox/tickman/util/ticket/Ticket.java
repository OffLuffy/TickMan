package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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

	public Ticket(Department dept, Message msg, TextChannel channel) {
		this.dept = dept;
		this.ticketId = TickMan.getSnowflake();
		this.ticketChannel = channel;
		this.author = msg.getMember();
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

	public void setAuthor(Member author) { this.author = author; }

	public void setTicketChannel(TextChannel channel) { this.ticketChannel = channel; }

	public void transferDepartment(Department otherDept) {
		this.dept = otherDept;
		// Send transferring notification, move channel, update message to say transferred
		ticketChannel.sendMessage(":hourglass: Transferring ticket to " + otherDept.getName())
			.queue(msg -> ticketChannel.getManager().setParent(otherDept.getTicketCategory())
				.queue(unused -> msg.editMessage(":white_check_mark: Transferred ticket to " + otherDept.getName())
					.queue()));
	}

	public static Ticket createNew(Department dept, Message msg) {
		if (msg == null || msg.getMember() == null) { return null; }
		Ticket ticket = new Ticket(dept, msg, null);
		ticket.setAuthor(msg.getMember());
		dept.getTicketCategory().createTextChannel(msg.getMember().getEffectiveName() + "_ticket").queue(channel -> {
			ticket.setTicketChannel(channel);
			EmbedBuilder embed = dept.newEmbed();
			embed.setTitle("New Ticket Submitted by " + msg.getMember().getEffectiveName());
			channel.sendMessage("").queue();
		});
		return ticket;
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

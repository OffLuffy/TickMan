package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.events.TMEventManager;
import com.luffbox.tickman.util.constants.ChangeType;
import com.luffbox.tickman.util.snowflake.ITMSnowflake;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Ticket implements ITMSnowflake {

	private final long ticketId;
	private Department dept;
	private TextChannel ticketChannel;
	private Member author;
	private String subject;
	private final Set<Member> participants = new HashSet<>();
	private final File logFile;
	private JsonObject transcript;

	public Ticket(long id, @Nonnull Department dept, @Nonnull TextChannel channel, @Nonnull Member author, @Nonnull String subject) {
		this.dept = dept;
		this.ticketId = id;
		this.ticketChannel = channel;
		this.author = author;
		this.dept.addTicket(this);
		this.subject = subject;
		this.logFile = new File(TickMan.LOG_DATA, String.format("%x_%x.txt", ticketId, author.getIdLong()));
		loadLogFile();
	}

	private void loadLogFile() {
		if (logFile.exists()) {
			try (FileReader readIn = new FileReader(logFile)) {
				JsonObject json = (JsonObject) Jsoner.deserialize(readIn);
				transcript = new JsonObject(){{
					put("messages", json.get("messages"));
				}};
			} catch (IOException e) {
				System.err.println("Failed to read ticket log file for ticket: " + getId() + ", file: " + logFile.getName() + ", reason: " + e.getMessage());
			} catch (JsonException e) {
				System.err.println("Guild data file has invalid JSON for ticket: " + getId() + ", file: " + logFile.getName() + ", reason: " + e.getMessage());
			}
		} else {
			transcript = new JsonObject() {{
				put("messages", new JsonArray());
			}};
		}
	}

	@Override
	public long getIdLong() { return ticketId; }

	public TickMan tickManInst() { return dept.tickManInst(); }

	public Department getDepartment() { return dept; }

	public TextChannel getTicketChannel() { return ticketChannel; }

	public Member getAuthor() { return author; }

	public String getSubject() { return subject; }

	public Set<Member> getParticipants() { return Set.copyOf(participants); }

	public File getLogFile() { return logFile; }

	/**
	 * Gets the Guild associated with this Ticket
	 * @return The Guild object assocaited with this Ticket.
	 */
	public Guild getGuild() { return dept.getGuild(); }

	public boolean setAuthor(Member author) {
		if (getGuild() == null || author == null || !author.getGuild().equals(getGuild())) { return false; }
		this.author = author;
		dept.getConfig().save();
		TMEventManager.ticketChange(this, ChangeType.Ticket.AUTHOR);
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

	public void setSubject(@Nonnull String subject) {
		this.subject = subject;
		TMEventManager.ticketChange(this, ChangeType.Ticket.SUBJECT);
	}

	public boolean setTicketChannel(TextChannel channel) {
		if (getGuild() == null || channel == null || !channel.getGuild().equals(getGuild())) { return false; }
		this.ticketChannel = channel;
		dept.getConfig().save();
		TMEventManager.ticketChange(this, ChangeType.Ticket.CHANNEL);
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
		TMEventManager.ticketTransfer(this, oldDept, recvDept);
	}

	public void closeTicket(boolean wasDestroyed) {
		System.out.printf("Ticket closed: %s (ID:%x)%n", getSubject(), getIdLong());
		getAuthor().getUser().openPrivateChannel().queue(channel -> {
			channel.sendMessage("**" + getGuild().getName() + "** - *" + tickManInst().getBotName()
					+ " Ticket System*\nYour ticket was closed! For your records, here is a copy of the transcript.")
					.queue();
			channel.sendFile(getLogFile(), String.format("TicketLog_%x.txt", getIdLong())).queue();
		});
		ticketChannel.delete().queue();
		getDepartment().removeTicket(this);
		if (wasDestroyed) {
			TMEventManager.ticketDestroy(this);
		} else {
			TMEventManager.ticketClose(this);
		}
	}

	public void addParticipant(@Nonnull Member participant) {
		participants.add(participant);
		TMEventManager.ticketInvite(this, participant);
	}

	public void removeParticipant(@Nonnull Member participant) { participants.remove(participant); }

	public void appendToLog(@Nonnull Message msg) {
		if (msg.getMember() == null) { return; }
		appendToLog(msg.getContentStripped(), msg.getMember(), msg.getTimeCreated());
	}
	public void appendToLog(@Nonnull String msg, @Nonnull Member from, @Nonnull OffsetDateTime time) {
		if (msg.isBlank()) { return; }
		((JsonArray) transcript.get("messages")).add(new JsonObject() {{
			put("time-sent", time.toString());
			put("author-id", from.getIdLong());
			put("author-name", from.getEffectiveName());
			put("message", msg);
		}});
		try (FileWriter fw = new FileWriter(logFile)) {
			if (logFile.exists() || logFile.createNewFile()) { fw.write(transcript.toJson()); }
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*try (FileWriter fw = new FileWriter(getLogFile(), true);
			 BufferedWriter bw = new BufferedWriter(fw); PrintWriter pw = new PrintWriter(bw)){
				if (from == null) {
					pw.printf("%s%n", msg);
				} else {
					pw.printf("%s (%s) : %s%n", from.getEffectiveName(), from.getUser().getId(), msg);
				}
		} catch (IOException ex) { ex.printStackTrace(); }*/
	}

	public static Ticket fromJson(long id, JsonObject json, Department dept) {
		Member author = null;
		TextChannel channel = null;
		String subject = null;
		try { author = dept.getGuild().getMemberById((long) json.get(Config.Field.TICKET_AUTHOR.path)); } catch (Exception ignore) {}
		try { channel = dept.getGuild().getTextChannelById((long) json.get(Config.Field.TICKET_CHANNEL.path)); } catch (Exception ignore) {}
		try { subject = (String) json.get(Config.Field.TICKET_CHANNEL.path); } catch (Exception ignore) {}
		if (author == null || channel == null || subject == null) { return null; }
		return new Ticket(id, dept, channel, author, subject);
	}

	public JsonObject toJson() {
		return new JsonObject(new HashMap<>() {
			{
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

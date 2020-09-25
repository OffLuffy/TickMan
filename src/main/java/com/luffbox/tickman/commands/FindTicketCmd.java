package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdArgType;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindTicketCmd extends CmdHandler {
	public FindTicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(
				new String[] {"find", "findticket", "search", "searchtickets"},
				"Find a ticket user or ticket ID", true, true, true, false,
				new CmdArg("criteria", CmdArgType.STRING, true)
		));
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		if (e.getMember() == null) return;

		StringBuilder sb = new StringBuilder();
		for (String arg : args) { sb.append(arg).append(" "); }
		sb.substring(0, sb.length() - 1);
		String strArgs = sb.toString().toLowerCase();

		Set<Ticket> foundTickets = new HashSet<>();

		List<Member> members = e.getMessage().getMentionedMembers();

		// TODO: Can this be improved?

		if (!members.isEmpty()) {
			for (Member m : members) {
				foundTickets.addAll(config.getTicketsByMember(m));
			}
		} else {
			for (Department dept : config.getDepartments()) {
				for (Ticket ticket : dept.getTickets()) {
					if (ticket.getAuthor().getEffectiveName().toLowerCase().contains(strArgs)) {
						foundTickets.add(ticket);
					} else if (ticket.getSubject().toLowerCase().contains(strArgs)) {
						foundTickets.add(ticket);
					}
				}
			}
		}

		if (foundTickets.size() > 0) {
			listTickets(config, e.getMember(), e.getTextChannel(), foundTickets);
		} else {
			QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " No matching tickets found", QueueHelper.LONG);
		}

	}

	private void listTickets(Config conf, Member member, TextChannel channel, Set<Ticket> tickets) {
		EmbedBuilder embed = conf.newEmbed();
		embed.setDescription(String.format("%s Found %d matching tickets", member.getAsMention(), tickets.size()));
		for (Ticket ticket : tickets) {
			embed.addField(String.format("ID: %x", ticket.getIdLong()),
					String.format("Channel: %s%nAuthor: %s", ticket.getTicketChannel().getAsMention(),
							ticket.getAuthor().getAsMention()), true);
		}
		channel.sendMessage(embed.build()).queue();
	}
}

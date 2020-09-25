package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdArgType;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class TicketCmd extends CmdHandler {
	public TicketCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"ticket", "t"},
				"Used in a ticket channel to modify or close the ticket", true, true, true, false,
				new CmdArg("action", CmdArgType.STRING, true)));
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		assert e.getMember() != null;
		Ticket ticket = config.getTicketByChannel((TextChannel) e.getChannel());
		if (ticket == null) {
			QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " This command must be used in a ticket channel", QueueHelper.SHORT);
			QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
			return;
		}

		if (args.length == 0) {
			EmbedBuilder embed = config.newEmbed();
			embed.setTitle("Ticket Commands");
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "close", "Close the ticket"));
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "transfer *<dept>*", "Transfer the ticket to another department"));
			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "invite *<member>*", "Invite an additional member to view this ticket"));
//			embed.appendDescription(String.format("**%s%s** %s - %s", config.getCmdPrefix(), "t", "", ""));

			e.getChannel().sendMessage(embed.build()).queue(msg -> QueueHelper.queueLater(msg.delete(), QueueHelper.INST));
			return;
		}

		switch (args[0]) {
			case "close":
				ticket.closeTicket(false);
				break;
			case "transfer":
				if (args.length > 1) {
					Department recvDept = config.findDepartment(args[1]);
					if (recvDept != null) { ticket.transferDepartment(recvDept); }
					else {
						QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " Could not find that department", QueueHelper.SHORT);
						QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
					}
				} else {
					QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " This command requires a department to be specified", QueueHelper.SHORT);
					QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
				}
				break;
			case "invite":
				List<Member> mm = e.getMessage().getMentionedMembers();
				if (!mm.isEmpty()) {
					StringBuilder sb = new StringBuilder(e.getMember().getAsMention()).append(" Invited: ");
					for (Member m : mm) {
						if (!ticket.getParticipants().contains(m)) { ticket.addParticipant(m); }
						sb.append(m.getAsMention()).append(" ");
					}
					e.getChannel().sendMessage(sb.toString()).queue();
				} else {
					QueueHelper.tempSend(e.getChannel(), e.getAuthor().getAsMention() + " This command requires a member be mentioned to invite them", QueueHelper.SHORT);
				}
				QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
				break;
			default:
				QueueHelper.tempSend(e.getChannel(), "Action not recognized", QueueHelper.SHORT);
		}
	}
}

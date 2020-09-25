package com.luffbox.tickman.listeners;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.events.TMEventManager;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.constants.TicketReaction;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EventListener extends ListenerAdapter {

	private final TickMan tickman;

	public EventListener(TickMan tickman) { this.tickman = tickman; }

	@Override
	public void onReady(@NotNull ReadyEvent e) {
		List<Guild> guilds = e.getJDA().getGuilds();
		if (guilds.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Guild g : guilds) {
				if (sb.length() > 0) { sb.append(", "); }
				sb.append(" ").append(g.getName());
				TickMan.getGuildConfig(tickman, g);
			}
			System.out.println("Currently connected to " + guilds.size() + " guild" + (guilds.size() == 1 ? "" : "s") + ":");
			System.out.println(sb.toString());
		}
	}

	@Override
	public void onTextChannelDelete(@NotNull TextChannelDeleteEvent e) {
		Config config = TickMan.getGuildConfig(tickman, e.getGuild());
		Ticket ticket = config.getTicketByChannel(e.getChannel());
		if (ticket != null) { ticket.closeTicket(true); }
	}

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent e) {
		if (e.getUser().isBot()) { return; }
		Config config = TickMan.getGuildConfig(tickman, e.getGuild());
		Ticket ticket = config.getTicketByChannel(e.getChannel());
		if (ticket != null) {
			TicketReaction reaction = TicketReaction.fromEvent(e);
			if (reaction != null) {
				System.out.println("Reacted to ticket with " + reaction.name());
				switch (reaction) {
					case CLOSE -> ticket.closeTicket(false);
				}
				TMEventManager.ticketReact(ticket, reaction);
			}
		}
	}

	@Override
	public final void onMessageReceived(MessageReceivedEvent e) {
		if (e.getMember() == null || e.getChannelType() != ChannelType.TEXT) return;

		Config config = TickMan.getGuildConfig(tickman, e.getGuild());
		boolean hasCmdPrefix = e.getMessage().getContentRaw().startsWith(config.getCmdPrefix());
		if (e.getMessage().getContentRaw().isBlank() || e.getAuthor().isBot()) { return; }

		Ticket ticket = config.getTicketByChannel(e.getTextChannel());
		if (ticket != null) { ticket.appendToLog(e.getMessage()); }

		if (hasCmdPrefix) { // Treat the message like a command
			String[] msgParts = e.getMessage().getContentRaw().split("\\s+");
			String cmd = msgParts[0].substring(config.getCmdPrefix().length()).toLowerCase(Locale.ENGLISH);
			String[] args = Arrays.copyOfRange(msgParts, 1, msgParts.length);

			CmdHandler selectedCmd = null;
			for (CmdHandler c : tickman.cmds) {
				if (Arrays.stream(c.opts.aliases()).anyMatch(cmd::equalsIgnoreCase)) {
					selectedCmd = c;
				}
			}

			if (selectedCmd != null) {
				if (e.getChannelType() == ChannelType.PRIVATE) {
					selectedCmd.onPrivateMessage(e, args);
				} else {
					selectedCmd.onCommand(e, config, args);
					if (selectedCmd.opts.delete()) {
						QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
					}
				}
			} else {
				e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Command not recognized!").queue(msg -> {
					QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
					QueueHelper.queueLater(msg.delete(), QueueHelper.SHORT);
				});
			}
		} else { // Not a command, check if message was sent to support channel
			for (Department dept : config.getDepartments()) {
				if (dept.getSupportChannel() != null && dept.getSupportChannel().equals(e.getChannel())) {
					dept.createTicket(e.getMessage(), createdTicket -> {
						EmbedBuilder embed = dept.newEmbed();
						embed.setAuthor(e.getAuthor().getAsTag(), null, e.getAuthor().getAvatarUrl());
						embed.setDescription(createdTicket.getSubject());
						embed.appendDescription("\n\n*Sent by* " + e.getAuthor().getAsMention() + " *in* " + e.getTextChannel().getAsMention());
						embed.appendDescription("\n\nWhen resolved, add " + TicketReaction.CLOSE.emote + " reaction or use `!t close`");
						createdTicket.getTicketChannel().sendMessage(embed.build()).queue(ticketEmbed -> {
							for (TicketReaction r : TicketReaction.values()) {
								ticketEmbed.addReaction(r.emote).queue();
							}
						});

						QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
						e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Please switch to " + createdTicket.getTicketChannel().getAsMention() + " to continue")
								.queue(msg -> QueueHelper.queueLater(msg.delete(), QueueHelper.LONG));
					});
				}
			}
		}
	}
}

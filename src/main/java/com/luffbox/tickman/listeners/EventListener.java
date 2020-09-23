package com.luffbox.tickman.listeners;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.cmd.CmdHandler;
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
				TickMan.getGuildConfig(g);
			}
			System.out.println("Currently connected to " + guilds.size() + " guild" + (guilds.size() == 1 ? "" : "s") + ":");
			System.out.println(sb.toString());
		}
	}

	@Override
	public void onTextChannelDelete(@NotNull TextChannelDeleteEvent e) {
		Config config = TickMan.getGuildConfig(e.getGuild());
		Ticket ticket = config.getTicketByChannel(e.getChannel());
		if (ticket != null) { ticket.closeTicket(true); }
	}

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent e) {
		Config config = TickMan.getGuildConfig(e.getGuild());
		Ticket ticket = config.getTicketByChannel(e.getChannel());
		if (ticket != null) {

		}
	}

	@Override
	public final void onMessageReceived(MessageReceivedEvent e) {
		if (e.getMember() == null || e.getChannelType() != ChannelType.TEXT) return;

		Config config = TickMan.getGuildConfig(e.getGuild());
		boolean hasCmdPrefix = e.getMessage().getContentRaw().startsWith(config.getCmdPrefix());
		if (e.getMessage().getContentRaw().isBlank() || e.getAuthor().isBot()) { return; }

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
//						e.getMessage().delete().queueAfter(TickMan.Duration.INST.quantity, TickMan.Duration.INST.unit);
					}
				}
			} else {
				e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Command not recognized!").queue(msg -> {
					QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
					QueueHelper.queueLater(msg.delete(), QueueHelper.SHORT);
//					e.getMessage().delete().queueAfter(TickMan.Duration.INST.quantity, TickMan.Duration.INST.unit);
//					msg.delete().queueAfter(TickMan.Duration.SHORT.quantity, TickMan.Duration.SHORT.unit);
				});
			}
		} else { // Not a command, check if message was sent to support channel
			for (Department dept : config.getDepartments()) {
				if (dept.getSupportChannel() != null && dept.getSupportChannel().equals(e.getChannel())) {
					dept.createTicket(e.getMessage(), ticket -> {
						EmbedBuilder embed = dept.newEmbed();
						embed.setAuthor(ticket.getAuthor().getUser().getAsTag(), null, ticket.getAuthor().getUser().getAvatarUrl());
						embed.setDescription(ticket.getSubject());
						embed.appendDescription("\n\n*Sent by* " + ticket.getAuthor().getUser().getAsMention() + " *in* " + ticket.getTicketChannel().getAsMention());
						embed.appendDescription("\n\nWhen resolved, add " + TicketReaction.CLOSE.emote + " reaction or use `!t close` to close the ticket");
						ticket.getTicketChannel().sendMessage(embed.build()).queue(ticketEmbed -> {
							for (TicketReaction r : TicketReaction.values()) {
								ticketEmbed.addReaction(r.emote).queue();
							}
						});

//						e.getMessage().delete().queueAfter(TickMan.Duration.INST.quant, TickMan.Duration.INST.unit);
						QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
						e.getChannel().sendMessage(e.getAuthor().getAsMention() + " Please switch to " + ticket.getTicketChannel().getAsMention() + " to continue")
								.queue(msg -> QueueHelper.queueLater(msg.delete(), QueueHelper.LONG));
					});
				}
			}
		}
	}
}

package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.commands.conf.CmdPrefixSubCmd;
import com.luffbox.tickman.commands.conf.DeptSubCmd;
import com.luffbox.tickman.commands.conf.EmbedColorSubCmd;
import com.luffbox.tickman.commands.conf.InviteSubCmd;
import com.luffbox.tickman.util.cmd.*;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Department;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ConfigureCmd extends CmdHandler {

	private final Set<ConfigSubCmd> subCmd = new HashSet<>();

	public ConfigureCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"conf", "confguild", "options"}, "Configure the current guild", false, true, true,
				new CmdArg("property", CmdArgType.STRING, true),
				new CmdArg("value", CmdArgType.STRING, true)
		));
		subCmd.add(new CmdPrefixSubCmd());
		subCmd.add(new InviteSubCmd());
		subCmd.add(new EmbedColorSubCmd());
		subCmd.add(new DeptSubCmd());
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) { return; }

		if (args.length == 0) {
			// If not arguments are provided, print current config embed (self-delete after 1 minute)
			QueueHelper.tempSend(e.getChannel(), getCurrentConfigEmbed(config).build(), QueueHelper.LONG);
			QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.LONG);
			return;
		}

		ConfigSubCmd exeSub = null;
		for (ConfigSubCmd sc : subCmd) {
			if (Arrays.asList(sc.aliases).contains(args[0].toLowerCase(Locale.ENGLISH))) {
				exeSub = sc;
				break;
			}
		}
		if (exeSub != null) {
			if (args.length - 1 > exeSub.reqArgs) {
				exeSub.execute(config, e.getMessage(), Arrays.copyOfRange(args, 1, args.length));
			} else {
				// Send embed showing the current value (self-delete after 1 minute)
				EmbedBuilder cmdUsage = config.newEmbed();
				cmdUsage.setTitle(exeSub.aliases[0] + " usage");
				cmdUsage.addField("Usage", config.getCmdPrefix() + "conf " + exeSub.usage(), false);
				cmdUsage.addField("Description", config.getCmdPrefix() + "conf " + exeSub.desc(), false);
				cmdUsage.addField("Current value", exeSub.value(config), false);
				QueueHelper.tempSend(e.getChannel(), cmdUsage.build(), QueueHelper.LONG);
//				e.getChannel().sendMessage(cmdUsage.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
			}
		} else {
			selfDelMsg(e, "Unrecognized property type!", true);
		}
	}

	private EmbedBuilder getCurrentConfigEmbed(Config config) {
		EmbedBuilder embed = config.newEmbed();
		embed.setTitle(config.getGuild().getName() + " -- " + tickman.getBotName() + " Config");
		embed.addField("Command Prefix", config.getCmdPrefix(), true);
		embed.addField("Allow Invite", config.canInvite() ? "Enabled" : "Disabled", true);
		embed.addField("Embed Color", String.format("#%06x", config.getEmbedColor().longValue()), true);
		embed.addBlankField(false);
		embed.addField("Departments", "_ _", false);
		for (Department dept : config.getDepartments()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nSupport Channel: ");
			if (dept.getSupportChannel() != null) {
				sb.append(dept.getSupportChannel().getAsMention());
			} else {
				sb.append("*Not set*");
			}
			sb.append("\nTickets Category: ");
			if (dept.getTicketCategory() != null) {
				sb.append(dept.getTicketCategory().getName());
			} else {
				sb.append("*Not set*");
			}
			sb.append("\nSupport Roles: ");
			for (Role sr : dept.getSupportRoles()) {
				sb.append(sr.getAsMention()).append(" ");
			}
			embed.addField(dept.getName(), sb.toString(), false);
		}
		return embed;
	}

	private void noDelMsg(MessageReceivedEvent e, String message, boolean mention) {
		e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue();
		QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
//		e.getMessage().delete().queueAfter(TickMan.Duration.INST.quantity, TickMan.Duration.INST.unit);
	}

	private void selfDelMsg(MessageReceivedEvent e, String message, boolean mention) {
		QueueHelper.tempSend(e.getChannel(), (mention ? e.getAuthor().getAsMention() + " " : "") + message, QueueHelper.SHORT);
		QueueHelper.queueLater(e.getMessage().delete(), QueueHelper.INST);
//		e.getMessage().delete().queueAfter(TickMan.Duration.INST.quantity, TickMan.Duration.INST.unit);
//		e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue(msg -> {
//			e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
//			msg.delete().queueAfter(10, TimeUnit.SECONDS);
//		});
	}

}

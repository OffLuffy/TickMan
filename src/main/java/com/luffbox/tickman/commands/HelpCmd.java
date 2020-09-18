package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.GuildOpts;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class HelpCmd extends CmdHandler {
	public HelpCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[]{"help", "?"}, tickman.getBotName() + " Help", true, false, false));
	}

	@Override
	public void onCommand(MessageReceivedEvent e, GuildOpts guildData, String[] args) {
		e.getAuthor().openPrivateChannel().queue((channel) -> {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.CYAN);
			eb.setTitle(tickman.getBotName() + " Help");
			eb.setDescription("Commands offered by " + tickman.getBotName() + "\nArguments: **<Required>** *[Optional]*");

			for (CmdHandler cmd : tickman.cmds) {
				if (cmd instanceof InviteCmd && !guildData.allowInvite()) continue;
				if (cmd.opts.showHelp()) {
					StringBuilder sb = new StringBuilder(guildData.cmdPrefix() + cmd.opts.name());
					for (CmdArg arg : cmd.opts.args()) {
						sb.append(" ")
								.append(arg.required() ? "**<" : "*[")
								.append(arg.argName())
								.append(arg.required() ? ">**" : "]*");
					}
					eb.addField(sb.toString(), cmd.opts.desc(), false);
				}
			}

			channel.sendMessage(eb.build()).queue();
		});
	}
}

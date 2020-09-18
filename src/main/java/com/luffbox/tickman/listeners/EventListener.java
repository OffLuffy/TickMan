package com.luffbox.tickman.listeners;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.GuildOpts;
import com.luffbox.tickman.util.cmd.CmdHandler;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventListener extends ListenerAdapter {

	private final TickMan tickman;

	public EventListener(TickMan tickman) { this.tickman = tickman; }

	@Override
	public void onReady(@NotNull ReadyEvent e) {

		List<Guild> guilds = e.getJDA().getGuilds();
		if (guilds.size() > 0) {
			System.out.println("Currently connected to " + guilds.size() + " guild" + (guilds.size() == 1 ? "" : "s") + ":");
			StringBuilder sb = new StringBuilder();
			for (Guild g : guilds) {
				if (sb.length() > 0) { sb.append(", "); }
				sb.append(" ").append(g.getName());
				TickMan.getGuildOptions(g);
			}
			System.out.println(sb.toString());
		}
	}

	@Override
	public final void onMessageReceived(MessageReceivedEvent e) {
		GuildOpts data = TickMan.getGuildOptions(e.getChannelType() == ChannelType.TEXT ? e.getGuild() : null );
		if (e.getMessage().getContentRaw().isBlank() || e.getAuthor().isBot() || !e.getMessage().getContentRaw().startsWith(data.cmdPrefix())) { return; }

		String[] msgParts = e.getMessage().getContentRaw().split("\\s+");
		String cmd = msgParts[0].substring(data.cmdPrefix().length()).toLowerCase(Locale.ENGLISH);
//		String[] args = (String[]) Arrays.stream(msgParts).skip(1).toArray();

		CmdHandler selectedCmd = null;
		for (CmdHandler c : tickman.cmds) {
			if (Arrays.stream(c.opts.aliases()).anyMatch(cmd::equalsIgnoreCase)) {
				selectedCmd = c;
			}
		}

		if (selectedCmd != null) {
			if (e.getChannelType() == ChannelType.PRIVATE) {
				selectedCmd.onPrivateMessage(e, msgParts);
			} else {
				selectedCmd.onCommand(e, data, msgParts);
				if (selectedCmd.opts.delete()) { e.getMessage().delete().queueAfter(3, TimeUnit.SECONDS); }
			}
		}

		/*if (e.getMessage().getContentRaw().equalsIgnoreCase(data.cmdPrefix() + opts.name())) {
			if (e.getChannelType() == ChannelType.PRIVATE) {
				onPrivateMessage(e);
			} else {
				GuildOpts data = TickMan.getGuildOptions(e.getGuild());
				if (data == null) { System.out.println("Guild has null options"); return; }
				onCommand(e, data);
				if (opts.delete()) { e.getMessage().delete().queue(); }
			}
		}*/
	}
}

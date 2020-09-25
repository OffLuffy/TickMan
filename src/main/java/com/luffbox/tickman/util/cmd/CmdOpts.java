package com.luffbox.tickman.util.cmd;

import java.util.Locale;

public record CmdOpts(String[] aliases, String desc, boolean delete, boolean showHelp, boolean reqGuild, boolean reqAdmin, CmdArg ... args) {
	public String name() { return aliases[0]; }
	public String[] aliases() {
		for (int i = 0; i < aliases.length; i++) { aliases[i] = aliases[i].toLowerCase(Locale.ENGLISH); }
		return aliases;
	}
}

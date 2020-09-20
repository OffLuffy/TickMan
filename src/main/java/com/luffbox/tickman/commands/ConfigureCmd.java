package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.commands.conf.CmdPrefixSubCmd;
import com.luffbox.tickman.util.cmd.*;
import com.luffbox.tickman.util.ticket.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConfigureCmd extends CmdHandler {

	private final Set<ConfigSubCmd> subCmd = new HashSet<>();

	public ConfigureCmd(TickMan tickman) {
		super(tickman, new CmdOpts(new String[] {"conf", "confguild", "options"}, "Configure the current guild", false, true, true,
				new CmdArg("property", CmdArgType.STRING, true),
				new CmdArg("value", CmdArgType.STRING, true)
		));
		subCmd.add(new CmdPrefixSubCmd());
	}

	@Override
	public void onCommand(MessageReceivedEvent e, Config config, String[] args) {
		if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) { return; }

		if (args.length == 0) {
            EmbedBuilder embed = config.newEmbed();
            embed.setTitle(config.getGuild().getName() + " -- " + tickman.getBotName() + " Config");
            embed.addField("Command Prefix", config.getCmdPrefix(), true);
            embed.addField("Allow Invite", config.canInvite() ? "Enabled" : "Disabled", true);
            embed.addField("Embed Color", String.format("#%06x", config.getEmbedColor().longValue()), true);
            e.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
            e.getChannel().sendMessage(embed.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
			return;
		}

		String prop = args[0].toLowerCase(Locale.ENGLISH);
		List<Role> mr = e.getMessage().getMentionedRoles();
		List<TextChannel> mc = e.getMessage().getMentionedChannels();

		// TODO: Implement remaining ConfigSubCmds (prefix, invite, color, dept-category, dept-channel, dept-roles, dept-name)

		ConfigSubCmd exeSub = null;
		for (ConfigSubCmd sc : subCmd) {
			if (Arrays.asList(sc.aliases).contains(prop)) {
				exeSub = sc; break;
			}
		}
		if (exeSub != null) {
			exeSub.modify(config, e.getMessage(), Arrays.copyOfRange(args, 1, args.length - 1));
		} else {
			selfDelMsg(e, "Unrecognized property type!", true);
		}

//        if (args.length > 0) {
//            System.out.println("Property: " + args[0]);
//            if (args.length > 1) {
//                System.out.println("Value: " + args[1]);
//                List<Role> mr = e.getMessage().getMentionedRoles();
//                List<TextChannel> mc = e.getMessage().getMentionedChannels();
//                boolean success;
//                switch (args[0].toLowerCase()) {
//                    case "ticketcategory":
//                        Category cat = null;
//                        try {
//                            cat = e.getGuild().getCategoryById(args[0]);
//                        } catch (Exception ex) {
//                            try {
//                                cat = e.getGuild().getCategoriesByName(args[0], true).stream().findFirst().orElse(null);
//                            } catch (Exception ignore) {}
//                        }
//                        if (cat != null) {
//                            guildData.setTicketCategory(cat);
//                            noDelMsg(e, "Set ticket category to " + cat.getName(), true);
//                        } else {
//                            selfDelMsg(e, "Could not find that Category!", true);
//                        }
//                        break;
//                    case "supportchannel":
//                        if (!mc.isEmpty()) {
//                            success = guildData.setSupportChannel(mc.get(0), true);
//                        } else {
//                            success = guildData.setSupportChannelId(args[1], true);
//                        }
//                        if (success) {
//                            noDelMsg(e, "Set support channel to " + guildData.getSupportChannel().getName(), true);
//                        }
//                        break;
//                    case "addrole":
//                        for (Role r : mr) { guildData.addSupportRole(r); }
//                        noDelMsg(e, "Added support roles", true);
//                        break;
//                    case "removerole":
//                        for (Role r : mr) { guildData.removeSupportRole(r); }
//                        noDelMsg(e, "Removed support roles", true);
//                        break;
//                    case "resetroles":
//                        if (args[1].equalsIgnoreCase("confirm")) {
//                            guildData.clearSupportRoles();
//                            noDelMsg(e, "Reset support roles", true);
//                        }
//                        break;
//                    case "cmdprefix":
//                        guildData.setCmdPrefix(args[1]);
//                        noDelMsg(e, "Set command prefix to " + guildData.getCmdPrefix(), true);
//                        break;
//                    case "allowinvite":
//                        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
//                            guildData.setAllowInvite(args[1].equalsIgnoreCase("true"));
//                            noDelMsg(e, "Invite command " + (guildData.canInvite() ? "enabled" : "disabled"), true);
//                        } else {
//                            selfDelMsg(e, "Must be true or false!", true);
//                        }
//                        break;
//                    case "embedcolor":
//                        try {
//                            BigDecimal clr = BigDecimal.valueOf(Long.parseLong(args[1]));
//                            guildData.setEmbedColor(clr);
//                            noDelMsg(e, "Set embed color to " + String.format("#%06x", guildData.getEmbedColor().longValue()), true);
//                            break;
//                        } catch (Exception ignore) {
//                            try {
//                                String args1 = args[1].replace("#", "").replace("0x", "");
//                                BigDecimal clr = BigDecimal.valueOf(Long.parseLong(args[1], 16));
//                                guildData.setEmbedColor(clr);
//                                noDelMsg(e, "Set embed color to " + String.format("#%06x", guildData.getEmbedColor().longValue()), true);
//                                break;
//                            } catch (Exception ignore2) {}
//                        }
//                        selfDelMsg(e, "Must be a number or hexidecimal value!", true);
//                        break;
//                    default:
//                        selfDelMsg(e, "Unrecognized property type!", true);
//                }
//            } else {
//                switch (args[0].toLowerCase()) {
//                    case "ticketcategory" -> {
//                        Category ticketCat = guildData.getTicketCategory();
//                        noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf ticketcategory <category>` -- Sets the category in which tickets channels are created"
//                                + "\n`<category>` can either be a category's name or the ID"
//                                + "\nThis guild's ticket category is currently set to: " + (ticketCat != null ? "`" + ticketCat.getName() + " (ID: " + ticketCat.getId() + ")`" : "Not yet set"), true);
//                    }
//                    case "supportchannel" -> {
//                        TextChannel textChannel = guildData.getSupportChannel();
//                        noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf supportchannel <channel>` -- Sets the channel that will listen for support requests"
//                                + "\n`<channel>` must be a mentioned text channel (i.e. #channel)"
//                                + "\nThis guild's support channel is currently set to: " + (textChannel != null ? "`" + textChannel.getAsMention() + " (ID: " + textChannel.getId() + ")`" : "Not yet set"), true);
//                    }
//                    case "addrole" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf addrole <role>...` -- Adds roles to the list of roles which can respond to tickets"
//                            + "\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."
//                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
//                    case "removerole" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf removerole <role>...` -- Removes roles from the list of roles which can respond to tickets"
//                            + "\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."
//                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
//                    case "resetroles" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf resetroles confirm` -- Removes all roles from the list of roles which can respond to tickets"
//                            + "\nWill not delete support roles unless `confirm` is provided"
//                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
//                    case "cmdprefix" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf cmdprefix <prefix>` -- Sets the prefix this guild requires to run a command"
//                            + "\n`<prefix>` is usually a single character such as `~` or `!`"
//                            + "\nThis guild's command prefix is currently set to: `" + guildData.getCmdPrefix() + "`", true);
//                    case "allowinvite" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf allowinvite true/false` -- Sets whether users are allowed to use the invite command"
//                            + "\nThis command will only accept `true` or `false`"
//                            + "\nThis guild's invite command is currently **" + (guildData.canInvite() ? "ENABLED" : "DISABLED") + "**", true);
//                    default -> selfDelMsg(e, "Unrecognized property type!", true);
//                }
//            }
//        } else {
//            EmbedBuilder embed = TickMan.newEmbed(guildData);
//            embed.setTitle(guildData.getGuild().getName() + " -- " + tickman.getBotName() + " Config");
//            embed.setDescription("Usage: !conf <property> <value>\nCurrent configuration:");
//            embed.addField("Available Properties", "`supportchannel, addrole, removerole, resetroles, cmdprefix, allowinvite`", false);
//            embed.addBlankField(false);
//            embed.addField("Ticket Category", guildData.getTicketCategory().getName(), true);
//            embed.addField("Support Channel", guildData.getSupportChannel().getAsMention(), true);
//            embed.addField("Support Roles", getSupportRoleMentions(guildData), true);
//            embed.addField("Command Prefix", guildData.getCmdPrefix(), true);
//            embed.addField("Allow Invite", guildData.canInvite() ? "Enabled" : "Disabled", true);
//            embed.addField("Embed Color", String.format("#%06x", guildData.getEmbedColor().longValue()), true);
//            MessageEmbed me = embed.build();
//            e.getMessage().delete().queueAfter(1, TimeUnit.SECONDS);
//            e.getChannel().sendMessage(embed.build()).queue(msg -> {
//                msg.delete().queueAfter(1, TimeUnit.MINUTES);
//            });
//        }
	}

//    private String getSupportRoleMentions(GuildOpts guildData) {
//        StringBuilder sb = new StringBuilder();
//        for (Role r : guildData.getSupportRoles()) { sb.append(r.getAsMention()).append(" "); }
//        return sb.toString().strip();
//    }

	private void noDelMsg(MessageReceivedEvent e, String message, boolean mention) {
		e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue(msg -> e.getMessage().delete().queueAfter(3, TimeUnit.SECONDS));
	}

	private void selfDelMsg(MessageReceivedEvent e, String message, boolean mention) {
		e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue(msg -> {
			e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
			msg.delete().queueAfter(10, TimeUnit.SECONDS);
		});
	}

}
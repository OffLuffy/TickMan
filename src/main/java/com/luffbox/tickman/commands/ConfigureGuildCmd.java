package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.GuildOpts;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdArgType;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigureGuildCmd extends CmdHandler {
    public ConfigureGuildCmd(TickMan tickman) {
        super(tickman, new CmdOpts(new String[] {"conf", "confguild"}, "Configure the current guild", false, true, true,
                new CmdArg("property", CmdArgType.STRING, true),
                new CmdArg("value", CmdArgType.STRING, true)
        ));
    }

    @Override
    public void onCommand(MessageReceivedEvent e, GuildOpts guildData, String[] args) {
        if (args.length > 0) {
            System.out.println("Property: " + args[0]);
            if (args.length > 1) {
                System.out.println("Value: " + args[1]);
                List<Role> mr = e.getMessage().getMentionedRoles();
                List<TextChannel> mc = e.getMessage().getMentionedChannels();
                boolean success = false;
                switch (args[0].toLowerCase()) {
                    case "supportchannel":
                        if (!mc.isEmpty()) {
                            success = guildData.setSupportChannel(mc.get(0));
                        } else {
                            success = guildData.setSupportChannelId(args[1]);
                        }
                        if (success) {
                            noDelMsg(e, "Set support channel to " + guildData.getSupportChannel().getName(), true);
                        }
                        break;
                    case "addrole":
                        for (Role r : mr) { guildData.addSupportRole(r); }
                        noDelMsg(e, "Added support roles", true);
                        break;
                    case "removerole":
                        for (Role r : mr) { guildData.removeSupportRole(r); }
                        noDelMsg(e, "Removed support roles", true);
                        break;
                    case "resetroles":
                        guildData.clearSupportRoles();
                        noDelMsg(e, "Reset support roles", true);
                        break;
                    case "cmdprefix":
                        guildData.setCmdPrefix(args[1]);
                        noDelMsg(e, "Set command prefix to " + guildData.getCmdPrefix(), true);
                        break;
                    case "allowinvite":
                        if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")) {
                            guildData.setAllowInvite(args[1].equalsIgnoreCase("true"));
                            noDelMsg(e, "Invite command " + (guildData.canInvite() ? "enabled" : "disabled"), true);
                        } else {
                            selfDelMsg(e, "Must be true or false!", true);
                        }
                        break;
                    default:
                        selfDelMsg(e, "Unrecognized property type!", true);
                }
            } else {
                // Echo current property value
            }
        } else {
            selfDelMsg(e, "Usage: `!conf <property> <value>`\n"
                    + "Available properties: `supportchannel, addrole, removerole, resetroles, cmdprefix, allowinvite`", true);
        }
    }

    private void noDelMsg(MessageReceivedEvent e, String message, boolean mention) {
        e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue();
    }

    private void selfDelMsg(MessageReceivedEvent e, String message, boolean mention) {
        e.getChannel().sendMessage((mention ? e.getAuthor().getAsMention() + " " : "") + message).queue(msg -> {
            e.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
            msg.delete().queueAfter(10, TimeUnit.SECONDS);
        });
    }

}

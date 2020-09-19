package com.luffbox.tickman.commands;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.GuildOpts;
import com.luffbox.tickman.util.cmd.CmdArg;
import com.luffbox.tickman.util.cmd.CmdArgType;
import com.luffbox.tickman.util.cmd.CmdHandler;
import com.luffbox.tickman.util.cmd.CmdOpts;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConfigureGuildCmd extends CmdHandler {

    enum Property {
        ADDROLES(   "addroles",      "Adds roles to the list of roles which can respond to tickets"
                , "conf addroles <role>...\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."),
        DELROLES(   "delroles",      "Removes roles from the list of roles which can respond to tickets"
                , "conf delroles <role>...\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."),
        RESETROLES( "resetroles",    "Removes all roles from the list of roles which can respond to tickets"
                , "conf resetroles"),
        CATEGORY(   "category",      "Sets the category in which tickets channels are created"
                , "conf category <category>\n`<category>` can either be a category's name or the ID"),
        CHANNEL(    "channel",       "Sets the channel that will listen for support requests"
                , "conf channel <channel>\n`<channel>` must be a mentioned text channel (i.e. #channel)"),
        PREFIX(     "prefix",        "Sets the prefix this guild requires to run a command"
                , "conf prefix <prefix>\n`<prefix>` is usually a single character such as `~` or `!`"),
        INVITE(     "invite",        "Sets whether users are allowed to use the invite command"
                , "conf invite [true|false]\nThis command will only accept `true` or `false`");

        public String name, desc, usage;
        Property(String name, String desc, String usage) {
            this.name = name; this.desc = desc; this.usage = usage;
        }
    }

    public ConfigureGuildCmd(TickMan tickman) {
        super(tickman, new CmdOpts(new String[] {"conf", "confguild"}, "Configure the current guild", false, true, true,
                new CmdArg("property", CmdArgType.STRING, true),
                new CmdArg("value", CmdArgType.STRING, true)
        ));
    }

    @Override
    public void onCommand(MessageReceivedEvent e, GuildOpts guildData, String[] args) {
        if (e.getMember() == null || !e.getMember().hasPermission(Permission.ADMINISTRATOR)) { return; }
        if (args.length > 0) {
            System.out.println("Property: " + args[0]);
            if (args.length > 1) {
                System.out.println("Value: " + args[1]);
                List<Role> mr = e.getMessage().getMentionedRoles();
                List<TextChannel> mc = e.getMessage().getMentionedChannels();
                boolean success;
                switch (args[0].toLowerCase()) {
                    case "ticketcategory":
                        Category cat = null;
                        try {
                            cat = e.getGuild().getCategoryById(args[0]);
                        } catch (Exception ex) {
                            try {
                                cat = e.getGuild().getCategoriesByName(args[0], true).stream().findFirst().orElse(null);
                            } catch (Exception ignore) {}
                        }
                        if (cat != null) {
                            guildData.setTicketCategory(cat);
                            noDelMsg(e, "Set ticket category to " + cat.getName(), true);
                        } else {
                            selfDelMsg(e, "Could not find that Category!", true);
                        }
                        break;
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
                        if (args[1].equalsIgnoreCase("confirm")) {
                            guildData.clearSupportRoles();
                            noDelMsg(e, "Reset support roles", true);
                        }
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
                switch (args[0].toLowerCase()) {
                    case "ticketcategory" -> {
                        Category ticketCat = guildData.getTicketCategory();
                        noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf ticketcategory <category>` -- Sets the category in which tickets channels are created"
                                + "\n`<category>` can either be a category's name or the ID"
                                + "\nThis guild's ticket category is currently set to: " + (ticketCat != null ? "`" + ticketCat.getName() + " (ID: " + ticketCat.getId() + ")`" : "Not yet set"), true);
                    }
                    case "supportchannel" -> {
                        TextChannel textChannel = guildData.getSupportChannel();
                        noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf supportchannel <channel>` -- Sets the channel that will listen for support requests"
                                + "\n`<channel>` must be a mentioned text channel (i.e. #channel)"
                                + "\nThis guild's support channel is currently set to: " + (textChannel != null ? "`" + textChannel.getName() + " (ID: " + textChannel.getId() + ")`" : "Not yet set"), true);
                    }
                    case "addrole" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf addrole <role>...` -- Adds roles to the list of roles which can respond to tickets"
                            + "\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."
                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
                    case "removerole" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf removerole <role>...` -- Removes roles from the list of roles which can respond to tickets"
                            + "\n`<role>` must be a mentioned role (i.e. @role) - More than one role may be provided at a time."
                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
                    case "resetroles" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf resetroles confirm` -- Removes all roles from the list of roles which can respond to tickets"
                            + "\nWill not delete support roles unless `confirm` is provided"
                            + "\nThis guild's support roles are currently set to: " + getSupportRoleMentions(guildData), true);
                    case "cmdprefix" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf cmdprefix <prefix>` -- Sets the prefix this guild requires to run a command"
                            + "\n`<prefix>` is usually a single character such as `~` or `!`"
                            + "\nThis guild's command prefix is currently set to: `" + guildData.getCmdPrefix() + "`", true);
                    case "allowinvite" -> noDelMsg(e, "`" + guildData.getCmdPrefix() + "conf allowinvite true/false` -- Sets whether users are allowed to use the invite command"
                            + "\nThis command will only accept `true` or `false`"
                            + "\nThis guild's invite command is currently **" + (guildData.canInvite() ? "ENABLED" : "DISABLED") + "**", true);
                    default -> selfDelMsg(e, "Unrecognized property type!", true);
                }
            }
        } else {
            selfDelMsg(e, "Usage: `!conf <property> <value>`\n"
                    + "Available properties: `supportchannel, addrole, removerole, resetroles, cmdprefix, allowinvite`", true);
        }
    }

    private String getSupportRoleMentions(GuildOpts guildData) {
        StringBuilder sb = new StringBuilder();
        for (Role r : guildData.getSupportRoles()) { sb.append(r.getAsMention()).append(" "); }
        return sb.toString().strip();
    }

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

package com.luffbox.tickman.commands.conf;

import com.luffbox.tickman.util.cmd.ConfigSubCmd;
import com.luffbox.tickman.util.constants.QueueHelper;
import com.luffbox.tickman.util.ticket.Config;
import com.luffbox.tickman.util.ticket.Department;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DeptSubCmd extends ConfigSubCmd {

	public DeptSubCmd() {
		super(new String[] {"dept", "department"}, 2);
	}

	@Override
	public @NotNull String usage() {
		return "dept *<dept>* *<action>* *<value>*";
	}

	@Override
	public @NotNull String desc() {
		return """
				Department configuration. Allowed properties are:
				`set-name` - Changes the department's name
				`set-category` - Changes where ticket channels are created
				`set-channel` - Changes where the bot listens for requests
				`add-roles` - Adds roles that are allowed to view tickets
				`remove-roles` - Removes roles that are allowed to view tickets
				`clear-roles` - Removes all roles that are allowed to view tickets""";
	}

	@Override
	public void execute(Config config, Message msg, String[] values) {
		assert values.length >= 2;
		String deptVal = values[0];
		String propVal = values[1].toLowerCase(Locale.ENGLISH);
		String setVal = values.length > 2 ? values[2] : null;

		Department dept = null;
		for (Department d : config.getDepartments()) {
			if (d.getName().equalsIgnoreCase(deptVal) || d.getId().equalsIgnoreCase(deptVal)
					|| (d.getSupportChannel() != null && d.getSupportChannel().getId().equalsIgnoreCase(deptVal))
					|| (d.getTicketCategory() != null && d.getTicketCategory().getId().equalsIgnoreCase(deptVal))) {
				dept = d; break;
			}
		}
		if (dept == null) {
			QueueHelper.queueLater(msg.delete(), QueueHelper.INST);
			QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention() + " Can't find that department", QueueHelper.LONG);
			return;
		}

		switch (propVal) {
			case "set-name" -> {
				if (setVal != null) {
					dept.setName(setVal);
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Set name to : " + dept.getName(), QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Name currently set to " + dept.getName(), QueueHelper.LONG);
				}
			}
			case "set-category" -> {
				if (setVal != null) {
					dept.setTicketCategoryId(setVal);
					if (dept.getTicketCategory() == null) {
						QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
								+ "Can't find that category!", QueueHelper.SHORT);
					}
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Set channel to : " + dept.getTicketCategory().getName(), QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Category currently set to " + dept.getTicketCategory().getName(), QueueHelper.LONG);
				}
			}
			case "set-channel" -> {
				if (setVal != null) {
					if (!msg.getMentionedChannels().isEmpty()) {
						dept.setSupportChannel(msg.getMentionedChannels().get(0), true);
					} else {
						dept.setSupportChannelId(setVal, true);
					}
					if (dept.getSupportChannel() == null) {
						QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
								+ "Can't find that channel!", QueueHelper.SHORT);
					}
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Set channel to : " + dept.getSupportChannel().getAsMention(), QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Channel currently set to " + dept.getSupportChannel().getAsMention(), QueueHelper.LONG);
				}
			}
			case "add-roles" -> {
				if (setVal != null) {
					dept.addSupportRole(msg.getMentionedRoles().toArray(new Role[]{}));
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Added support roles. Current roles: " + buildRoleList(dept), QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Current support roles:\n" + buildRoleList(dept), QueueHelper.LONG);
				}
			}
			case "remove-roles" -> {
				if (setVal != null) {
					dept.removeSupportRole(msg.getMentionedRoles().toArray(new Role[]{}));
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Removed support roles. Current roles: " + buildRoleList(dept), QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Current support roles:\n" + buildRoleList(dept), QueueHelper.LONG);
				}
			}
			case "clear-roles" -> {
				if (setVal != null) {
					dept.clearSupportRoles();
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Cleared all support roles", QueueHelper.LONG);
				} else {
					QueueHelper.tempSend(msg.getChannel(), msg.getAuthor().getAsMention()
							+ " Current support roles:\n" + buildRoleList(dept), QueueHelper.LONG);
				}
			}
		}
		QueueHelper.queueLater(msg.delete(), QueueHelper.INST);
	}

	private static String buildRoleList(Department dept) {
		StringBuilder sb = new StringBuilder();
		for (Role r : dept.getSupportRoles()) {
			sb.append(r.getAsMention()).append(" ");
		}
		return sb.toString();
	}

	@Override
	public @NotNull String value(Config config) { return "N/A"; }
}

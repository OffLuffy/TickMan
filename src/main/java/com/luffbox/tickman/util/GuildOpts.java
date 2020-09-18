package com.luffbox.tickman.util;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.List;

public record GuildOpts(String supportChannelId, String cmdPrefix, List<Role> supportRoles, boolean allowInvite) {
	public static GuildOpts def() { return new GuildOpts(null, "!", new ArrayList<>(), false); }
}

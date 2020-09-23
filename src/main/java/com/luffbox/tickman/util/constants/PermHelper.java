package com.luffbox.tickman.util.constants;

import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class PermHelper {

    private static final Collection<Permission> CAT_PERMS_ALLOW = new ArrayList<>() { {
        add(Permission.MESSAGE_READ);
        add(Permission.MESSAGE_WRITE);
    }};

    private static final Collection<Permission> CAT_PERMS_DENY = new ArrayList<>() { {
        add(Permission.MESSAGE_EMBED_LINKS);
        add(Permission.MESSAGE_ATTACH_FILES);
        add(Permission.MESSAGE_HISTORY);
    }};

    private static final Collection<Permission> TICKET_CHANNEL_PERMS = new ArrayList<>() { {
        add(Permission.MESSAGE_READ);
        add(Permission.MESSAGE_WRITE);
//		add(Permission.MESSAGE_MANAGE);
        add(Permission.MESSAGE_EMBED_LINKS);
        add(Permission.MESSAGE_ATTACH_FILES);
        add(Permission.MESSAGE_HISTORY);
        add(Permission.MESSAGE_EXT_EMOJI);
    }};

    public static Collection<Permission> getDeniedCategoryPerms() { return Collections.unmodifiableCollection(CAT_PERMS_DENY); }
    public static Collection<Permission> getAllowedCategoryPerms() { return Collections.unmodifiableCollection(CAT_PERMS_ALLOW); }
    public static Collection<Permission> getAllowedTicketPerms() { return Collections.unmodifiableCollection(TICKET_CHANNEL_PERMS); }
}

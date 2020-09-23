package com.luffbox.tickman.util.constants;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public enum TicketReaction {
    CLOSE("\u274E");

    public String emote;
    TicketReaction(String emote) {
        this.emote = emote;
    }

    public static TicketReaction fromEvent(GuildMessageReactionAddEvent e) {
        for (TicketReaction r : TicketReaction.values()) {
            if (r.emote.equalsIgnoreCase(e.getReactionEmote().getEmoji())) {
                return r;
            }
        }
        return null;
    }
}

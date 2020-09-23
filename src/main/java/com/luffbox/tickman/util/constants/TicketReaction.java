package com.luffbox.tickman.util.constants;

public enum TicketReaction {
    CLOSE("\u274E");

    public String emote;
    TicketReaction(String emote) {
        this.emote = emote;
    }
}

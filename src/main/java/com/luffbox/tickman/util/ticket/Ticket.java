package com.luffbox.tickman.util.ticket;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public record Ticket(Message msg, TextChannel channel, Member member) {
}

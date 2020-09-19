package com.luffbox.tickman.util.ticket;

import com.github.cliftonlabs.json_simple.Jsonable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.io.Writer;

public record Ticket(Message msg, TextChannel channel, Member member) implements Jsonable {

	@Override
	public String toJson() {
		return null;
	}

	@Override
	public void toJson(Writer writable) throws IOException {

	}

	@Override
	public boolean equals(Object obj) {
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return null;
	}

}

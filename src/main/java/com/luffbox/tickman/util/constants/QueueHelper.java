package com.luffbox.tickman.util.constants;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public enum QueueHelper {
	INST(1, SECONDS),
	SHORT(15, SECONDS),
	LONG(1, MINUTES);

	public final int quant;
	public final TimeUnit unit;
	QueueHelper(int quant, TimeUnit unit) {
		this.quant = quant;
		this.unit = unit;
	}

	public static ScheduledFuture<?> queueLater(RestAction<?> action, QueueHelper queueHelper) { return action.queueAfter(queueHelper.quant, queueHelper.unit); }
	public static void tempSend(MessageChannel channel, String message, QueueHelper queueHelper) {
		channel.sendMessage(message).queue(msg -> msg.delete().queueAfter(queueHelper.quant, queueHelper.unit));
	}
	public static void tempSend(MessageChannel channel, MessageEmbed embed, QueueHelper queueHelper) {
		channel.sendMessage(embed).queue(msg -> queueLater(msg.delete(), queueHelper));
	}
}

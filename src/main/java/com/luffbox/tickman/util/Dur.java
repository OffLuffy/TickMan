package com.luffbox.tickman.util;

import com.luffbox.tickman.TickMan;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public enum Dur {
	INST(1, TimeUnit.SECONDS),
	SHORT(15, TimeUnit.SECONDS),
	LONG(1, TimeUnit.MINUTES);

	public final int quant;
	public final TimeUnit unit;
	Dur(int quant, TimeUnit unit) {
		this.quant = quant;
		this.unit = unit;
	}


	public static ScheduledFuture<?> queueLater(RestAction<?> action, Dur dur) { return action.queueAfter(dur.quant, dur.unit); }
}

package com.luffbox.tickman.util.snowflake;

import net.dv8tion.jda.api.entities.ISnowflake;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.TimeZone;

public interface ITMSnowflake extends ISnowflake {
	@NotNull
	@Override
	default OffsetDateTime getTimeCreated() {
		long timestamp = (getIdLong() >>> SnowflakeServer.timestampLeftShift) + SnowflakeServer.TICKMAN_EPOCH;
		Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		gmt.setTimeInMillis(timestamp);
		return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
	}
}

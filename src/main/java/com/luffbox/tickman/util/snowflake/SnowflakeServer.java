package com.luffbox.tickman.util.snowflake;

// Derived from Twitter's Snowflake system (IdWorker class, loosely ported from Scala)
// https://github.com/twitter-archive/snowflake/blob/snowflake-2010/src/main/scala/com/twitter/service/snowflake/IdWorker.scala
// License: https://github.com/twitter-archive/snowflake/blob/snowflake-2010/LICENSE

/**
 * Responsible for generating Snowflakes in the same manner as Discord and Twitter.<br><br>
 * Uses a unique epoch offset for this bot rather than Discord's epoch offset.
 * When using these Snowflakes, implement {@link ITMSnowflake} or the create date will be wrong.
 * @see ITMSnowflake
 */
public class SnowflakeServer {

	// Sep 19, 2020
	public static final long TICKMAN_EPOCH = 1600544861000L;

	public static final long workerIdBits = 5L;
	public static final long datacenterIdBits = 5L;
	public static final long maxWorkerId = ~(-1L << workerIdBits);
	public static final long maxDatacenterId = ~(-1L << datacenterIdBits);
	public static final long sequenceBits = 12L;

	public static final long workerIdShift = sequenceBits;
	public static final long datacenterIdShift = sequenceBits + workerIdBits;
	public static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
	public static final long sequenceMask = ~(-1L << sequenceBits);

	// Doubt I'll ever use these (they'll almost always be zero); but just in case
	public final long workerId;
	public final long datacenterId;

	private long lastTime = -1L;
	private long sequence;

	public SnowflakeServer(long workerId, long datacenterId) { this(workerId, datacenterId, 0L); }
	public SnowflakeServer(long workerId, long datacenterId, long sequence) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.sequence = sequence;
		System.out.printf("Worker starting. Timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
				timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId);
	}

	public long nextId() throws InvalidSystemClockException {
		long time = System.currentTimeMillis();
		if (time < lastTime) {
			System.err.printf("Clock is behind, rejecting requests until %d.%n", lastTime);
			throw new InvalidSystemClockException("Clock is behind");
		}

		if (lastTime == time) {
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) { time = tilNextMillis(lastTime); }
		} else { sequence = 0; }

		lastTime = time;
		return ((time - TICKMAN_EPOCH) << timestampLeftShift) |
				(datacenterId << datacenterIdShift) |
				(workerId << workerIdShift) |
				sequence;
	}

	public String nextIdAsString() throws InvalidSystemClockException { return nextId() + ""; }

	private long tilNextMillis(long lastTime) {
		long curTime = System.currentTimeMillis();
		while (curTime <= lastTime) { curTime = System.currentTimeMillis(); }
		return curTime;
	}
}

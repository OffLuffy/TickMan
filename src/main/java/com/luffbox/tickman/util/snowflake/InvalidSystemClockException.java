package com.luffbox.tickman.util.snowflake;

public class InvalidSystemClockException extends Exception {
	public InvalidSystemClockException() { super(); }
	public InvalidSystemClockException(String message) { super(message); }
}

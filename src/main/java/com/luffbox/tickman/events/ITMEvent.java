package com.luffbox.tickman.events;

public interface ITMEvent <E> {
	void before(E obj);
	void after(E obj);
}

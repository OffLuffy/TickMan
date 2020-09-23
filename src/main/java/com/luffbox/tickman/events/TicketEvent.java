package com.luffbox.tickman.events;

import com.luffbox.tickman.util.ticket.Ticket;

public abstract class TicketEvent implements ITMEvent <Ticket> {
	@Override
	public abstract void before(Ticket ticket);
	@Override
	public abstract void after(Ticket ticket);
}

package com.luffbox.tickman.events;

import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.entities.Member;

public abstract class TMListenerAdapter {

	public void onTicketCreate(Ticket ticket, Department dept) {}
	public void onTicketClose(long ticketId, Department dept) {}
	public void onTicketDestroy(long ticketId, Department dept) {}
	public void onTicketTransfer(Ticket ticket, Department oldDept, Department newDept) {}
	public void onTicketInvite(Ticket ticket, Member invited) {}

	public void onDepartmentCreate(Department dept) {}
	public void onDepartmentDelete(Department dept) {}
	public void onDepartmentChange(Department dept) {}

}

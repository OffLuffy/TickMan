package com.luffbox.tickman.events;

import com.luffbox.tickman.util.constants.DeptChangeType;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.entities.Member;

public abstract class TMListenerAdapter {

	public void onTicketLoad(Ticket ticket) {}
	public void onTicketCreate(Ticket ticket) {}
	public void onTicketClose(Ticket ticket) {}
	public void onTicketDestroy(Ticket ticket) {}
	public void onTicketTransfer(Ticket ticket, Department oldDept, Department recvDept) {}
	public void onTicketInvite(Ticket ticket, Member invited) {}

	public void onDepartmentLoad(Department dept) {}
	public void onDepartmentCreate(Department dept) {}
	public void onDepartmentDelete(Department dept) {}
	public void onDepartmentChange(Department dept, DeptChangeType type) {}

}
package com.luffbox.tickman.listeners;

import com.luffbox.tickman.events.TMListenerAdapter;
import com.luffbox.tickman.util.constants.DeptChangeType;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.entities.Member;

public class TMEventListener extends TMListenerAdapter {
    public void onTicketLoad(Ticket ticket) { System.out.println("Loaded " + ticket); }
    public void onTicketCreate(Ticket ticket) { System.out.println("Created " + ticket); }
    public void onTicketClose(Ticket ticket) { System.out.println("Closed " + ticket); }
    public void onTicketDestroy(Ticket ticket) { System.out.println("Destroyed " + ticket); }
    public void onTicketTransfer(Ticket ticket, Department oldDept, Department recvDept) { System.out.println("Transferred " + ticket + ", from: " + oldDept + ", to: " + recvDept); }
    public void onTicketInvite(Ticket ticket, Member invited) { System.out.println("Invite " + ticket + ", member: " + invited); }
    public void onDepartmentLoad(Department dept) { System.out.println("Loaded " + dept); }
    public void onDepartmentCreate(Department dept) { System.out.println("Create " + dept); }
    public void onDepartmentDelete(Department dept) { System.out.println("Deleted " + dept); }
    public void onDepartmentChange(Department dept, DeptChangeType type) { System.out.println("Changed " + dept + ", type: " + type.name()); }
}

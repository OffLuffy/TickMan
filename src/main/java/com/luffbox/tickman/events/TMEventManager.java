package com.luffbox.tickman.events;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.constants.ChangeType;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.entities.Member;

public final class TMEventManager {

    public static void ticketLoad(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketLoad(ticket));
    }

    public static void ticketCreate(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketCreate(ticket));
    }

    public static void ticketClose(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketClose(ticket));
    }

    public static void ticketDestroy(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketDestroy(ticket));
    }

    public static void ticketChange(Ticket ticket, ChangeType.Ticket type) {
        TickMan.getListeners().forEach(listener -> listener.onTicketChange(ticket, type));
    }

    public static void ticketTransfer(Ticket ticket, Department oldDept, Department recvDept) {
        TickMan.getListeners().forEach(listener -> listener.onTicketTransfer(ticket, oldDept, recvDept));
    }

    public static void ticketInvite(Ticket ticket, Member invited) {
        TickMan.getListeners().forEach(listener -> listener.onTicketInvite(ticket, invited));
    }

    public static void departmentLoad(Department dept) {
        TickMan.getListeners().forEach(listener -> listener.onDepartmentLoad(dept));
    }

    public static void departmentCreate(Department dept) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentCreate(dept));
    }

    public static void departmentDelete(Department dept) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentDelete(dept));
    }

    public static void departmentChange(Department dept, ChangeType.Dept type) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentChange(dept, type));
    }

}

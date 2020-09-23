package com.luffbox.tickman.events;

import com.luffbox.tickman.TickMan;
import com.luffbox.tickman.util.constants.DeptChangeType;
import com.luffbox.tickman.util.ticket.Department;
import com.luffbox.tickman.util.ticket.Ticket;
import net.dv8tion.jda.api.entities.Member;

public final class TMEventManager {

    public static void onTicketLoad(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketLoad(ticket));
    }

    public static void onTicketCreate(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketCreate(ticket));
    }

    public static void onTicketClose(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketClose(ticket));
    }

    public static void onTicketDestroy(Ticket ticket) {
        TickMan.getListeners().forEach(listener -> listener.onTicketDestroy(ticket));
    }

    public static void onTicketTransfer(Ticket ticket, Department oldDept, Department recvDept) {
        TickMan.getListeners().forEach(listener -> listener.onTicketTransfer(ticket, oldDept, recvDept));
    }

    public static void onTicketInvite(Ticket ticket, Member invited) {
        TickMan.getListeners().forEach(listener -> listener.onTicketInvite(ticket, invited));
    }

    public static void onDepartmentLoad(Department dept) {
        TickMan.getListeners().forEach(listener -> listener.onDepartmentLoad(dept));
    }

    public static void onDepartmentCreate(Department dept) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentCreate(dept));
    }

    public static void onDepartmentDelete(Department dept) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentDelete(dept));
    }

    public static void onDepartmentChange(Department dept, DeptChangeType type) {
            TickMan.getListeners().forEach(listener -> listener.onDepartmentChange(dept, type));
    }

}

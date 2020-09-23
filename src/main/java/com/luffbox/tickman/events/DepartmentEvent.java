package com.luffbox.tickman.events;

import com.luffbox.tickman.util.ticket.Department;

public abstract class DepartmentEvent implements ITMEvent <Department> {
	@Override
	public abstract void before(Department dept);
	@Override
	public abstract void after(Department dept);
}

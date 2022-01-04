package com.p950tes.beertasting;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

public class Event { 
	private String name;
	private LocalDateTime time;
	private LocalDateTime created;
	private LocalDateTime updated;

	private boolean full;
	private boolean cancelled;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public LocalDateTime getCreated() {
		return created;
	}
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	public boolean isFull() {
		return full;
	}
	public void setFull(boolean full) {
		this.full = full;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	public LocalDateTime getUpdated() {
		return updated;
	}
	public void setUpdated(LocalDateTime updated) {
		this.updated = updated;
	}
	public boolean isAvailable() {
		return ! (isFull() || isCancelled());
	}
	@Override
	public String toString() {
		return StringUtils.joinWith(" | ", 
				name, 
				"Created: " + created,
				"Updated: " + updated
			);
	}
}
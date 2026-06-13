package com.venkat.loggen.model;

import java.time.LocalDateTime;

public class LogEvent {
	
private LocalDateTime timestamp;
private String level;
private String serviceName;
private String message;

public LogEvent() {}

public LogEvent(LocalDateTime timestamp, String level, String serviceName, String message) {
	this.timestamp = timestamp;
	this.level = level;
	this.serviceName = serviceName;
	this.message = message;
}

public LocalDateTime getTimestamp() {
	return timestamp;
}

public void setTimestamp(LocalDateTime timestamp) {
	this.timestamp = timestamp;
}

public String getLevel() {
	return level;
}

public void setLevel(String level) {
	this.level = level;
}

public String getServiceName() {
	return serviceName;
}

public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
}

public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

@Override
public String toString() {
	return String.format("[%s] [%s] = %s", level, serviceName, message);
}
}

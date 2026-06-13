package com.venkat.loggen.util;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class RandomLogGenerator {

	private final Random random = new Random();
	
	public String txnId() {
		return "TXN" + (10000 + random.nextInt(90000)); 
		}
	
	public String orderId() {
		return "ORD" + (1000 + random.nextInt(9000));
	}
	
	public String productId() {	
		return "PROD" + (100 + random.nextInt(900));
	}
	
	public String userId() {
		return "USR" + (100 + random.nextInt(900));
	}
	
	public String sessionId() {
		return UUID.randomUUID().toString().substring(0, 8);
	}
	
	public String ipAddress() {
		return "192.168." + random.nextInt(255) + "." + random.nextInt(255);
	}
	
	public int amount() {
		return (random.nextInt(50) + 1) * 100;
	}
	
	public int latencyMs() {
		return 1500 + random.nextInt(3000);
	}
	
	public int processingTimeMs() {
		return 50 + random.nextInt(200);
	}
	
	public int itemCount() {
		return 1 + random.nextInt(10);
	}
	
	public int stockCount() {
		return random.nextInt(500);
	}
	
	public String notificationChannel() {
		String[] channels = {"SMS", "EMAIL", "PUSH"};
		return channels[random.nextInt(3)];
	}
	
	public String country() {
		String[] countries = {"IN", "US", "UNKNOWN", "SG", "AE"};
		return countries[random.nextInt(5)];
	}
	
	public boolean chance(double probability) {
		return random.nextDouble() < probability;
	}
	
	public int nextInt(int bound) {
		return random.nextInt(bound);
	}
 	}
 

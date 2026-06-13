package com.venkat.loggen.service;

import com.venkat.loggen.model.LogEvent;
import com.venkat.loggen.util.RandomLogGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogGenerationService {

	private static final Logger paymentLog = LoggerFactory.getLogger("PaymentService");
	private static final Logger authLog = LoggerFactory.getLogger("AuthService");
	private static final Logger orderLog = LoggerFactory.getLogger("OrderService");
	private static final Logger notifyLog = LoggerFactory.getLogger("NotificationService");
	private static final Logger inventorylog = LoggerFactory.getLogger("InventoryService");
	
	private final RandomLogGenerator rng;
	
	private boolean incidentActive = false;
	private int     incidentCountdown = 0;
	
	public LogGenerationService(RandomLogGenerator rng) {
		this.rng = rng;
	}
	
	public void generateBatch() {
		handleIncidentState();
		
		int count = 5 + rng.nextInt(6);
		for (int i =0; i < count; i++) {
			generateOneEntry();
		}
				
	}
	
	private void handleIncidentState() {
		if (!incidentActive && rng.nextInt(300) == 0) {
			incidentActive = true;
			incidentCountdown = 120;
			writeLog(paymentLog, buildEvent(
					"PaymentService", "Error",
					"INCIDENT STARTED - DB connection pool exhausted. Active50/50, queued_requests=240"));
			
		}
		
		if (incidentActive) {
			incidentCountdown--;
			if(incidentCountdown <=0) {
				incidentActive = false;
				writeLog(paymentLog, buildEvent(
						"PaymentService", "INFO",
						"INCIDENT RESOLVED - Connection pool restored. Active=12/50"));
				
			}
		}
		
	}
	
	private void generateOneEntry() {
		int pick = rng.nextInt(5);
		switch(pick) {
		case 0 -> generatePaymentLog();
		case 1 -> generateAuthLog();
		case 2 -> generateOrderLog();
		case 3 -> generateNotificationLog();
		case 4 -> generateInventoryLog();
		}
	}
	
	
	private void generatePaymentLog() {
		String txn = rng.txnId();
		String user = rng.userId();
		int amount = rng.amount();
		
		double errorRate = incidentActive ? 0.40 : 0.03;
		
		LogEvent event;
		if(rng.chance(errorRate)) {
			event = buildEvent("PaymentService", "ERROR",
					"Payment FAILED - txn_id=" + txn +
					", user=" + user +
					", amount=₹" +amount +
					", reason=DB_TIMEOUT");
		} else if (rng.chance(0.08)) {
			event = buildEvent("PaymentService", "WARN",
					"Slow payment - txn_id=" +txn +
					",latency=" +rng.latencyMs()+ "ms"+
					", threshold=1500ms");
		} else {
			event = buildEvent("PaymentService", "INFO",
					"Payement processed - txn_id=" +txn +
					", user=" + user +
					", amount=₹" + amount +
					", processing_time=" +rng.processingTimeMs()+ "ms");
			
		}
		writeLog(paymentLog, event);
	}
	
	private void generateAuthLog() {
		String user = rng.userId();
		String ip = rng.ipAddress();
		
		double errorRate = incidentActive ? 0.15 : 0.02;
		
		LogEvent event;
		if (rng.chance(errorRate)) {
			event = buildEvent("AuthService", "ERROR",
					"Auth FAILED = user=" + user +
					", ip=" + ip +
					", reason+TOKEN_EXPIRED" +
					", attempts" + (rng.nextInt(5) + 1));
		} else if (rng.chance(0.05)) {
			event = buildEvent("AuthService", "WARN",
					"Suspicious login - user=" + user +
					", ip=" + ip +
					", country=" + rng.country());
		} else {
			event = buildEvent("AuthService", "INFO",
					"User authenticated - user=" + user +
					", ip=" + ip +
					", session=" + rng.sessionId());
		}
		writeLog(authLog, event);
	}
	
	private void generateOrderLog() {
		String order = rng.orderId();
		String user = rng.userId();
		int items = rng.itemCount();
		
		double errorRate = incidentActive ? 0.20 : 0.02;
		
		LogEvent event;
		if(rng.chance(errorRate)) {
			event = buildEvent("OrderService", "ERROR",
					"Order FAILED - order_id=" + order +
					", user=" + user +
					", reason= PAYMENT_SERVICE_UNAVAILABLE");
		} else if (rng.chance(0.06)) {
			event = buildEvent("OrderService", "WARN",
					"Low inventory for  - order_id=" + order +
					", items_unavailable=" + (rng.nextInt(items) + 1));
		} else {
			double value = items * (200 + rng.nextInt(1800));
			event = buildEvent("OrderService", "INFO",
					"user,=" + user +
					", items=" + items +
					", value=₹" + String.format("%.2f", value));
		}
		writeLog(orderLog, event);
	}
	
	private void generateNotificationLog() {
		String user = rng.userId();
		String channel = rng.notificationChannel();
	    
		LogEvent event;
		if(rng.chance(0.04)) {
			event = buildEvent("NotificationService", "ERROR",
					"Notification FAILED - user=" + user +
					", channel=" + channel +
					", reason=PROVIDER_TIMEOUT");
		} else {
			event = buildEvent("NotificationService", "INFO",
					", chnannel=" + channel +
					", type=TRANSACTION_ALERT");
		}
		writeLog(notifyLog, event);
	}
	
	private void generateInventoryLog() {
		String product = rng.productId();
		int stock = rng.stockCount();
		
		LogEvent event;
		if(stock < 10) {
			event = buildEvent("InventoryService", "WARN",
					"Low stock - product=" + product +
					", remaining=" + stock +
					", reorder_threshold=10" +
					", warehouse=BLR-01");
		} else {
			event = buildEvent("InventoryService", "INFO",
					", Inventory check - product=" + product +
					", stock=" + stock +
					", warehouse=BLR_01");
		}
		writeLog(inventorylog, event);
		
				
	}
	
	
	private LogEvent buildEvent(String service, String level, String message) {
        return new LogEvent(LocalDateTime.now(), level, service, message);
    }
	
	private void writeLog(Logger logger, LogEvent event) {
		switch (event.getLevel()) {
		case "ERROR" -> logger.error(event.getMessage());
		case "WARN" -> logger.warn(event.getMessage());
		default     -> logger.info(event.getMessage());
		}
	}
}

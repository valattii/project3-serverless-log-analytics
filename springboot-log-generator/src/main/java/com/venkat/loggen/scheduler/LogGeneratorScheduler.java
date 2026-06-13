package com.venkat.loggen.scheduler;

import com.venkat.loggen.service.LogGenerationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LogGeneratorScheduler {
   
	private final LogGenerationService logGenerationService;
	
	public LogGeneratorScheduler(LogGenerationService logGenerationService) {
		this.logGenerationService = logGenerationService;
	}
	
	@Scheduled(fixedRate = 1000)
	public void tick() {
		logGenerationService.generateBatch();
	}
}

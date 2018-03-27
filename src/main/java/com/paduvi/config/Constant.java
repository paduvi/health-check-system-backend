package com.paduvi.config;

import java.time.Duration;

public class Constant {

	public final static int MAX_SIZE_QUEUE = 10000;
	public final static int MAX_WORKER = 1000;
	public final static int MAX_RETRY = 3;
	public final static int DEFAULT_POLL_DURATION = 10;
	public final static long DEFAULT_RETRY_DURATION = Duration.ofSeconds(1).toMillis();
}

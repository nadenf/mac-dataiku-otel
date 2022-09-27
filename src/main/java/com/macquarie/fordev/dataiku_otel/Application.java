package com.macquarie.fordev.dataiku_otel;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {

		System.out.println("Starting Prometheus server ..");
		OpenTelemetry.start();

		System.out.println("Starting Dataiku collector ..");

		String dataikuHost = System.getenv("DATAIKU_HOST");
		String dataikuUsername = System.getenv("DATAIKU_USERNAME");
		String dataikuPassword = System.getenv("DATAIKU_PASSWORD");

		if (dataikuHost == null || dataikuUsername == null || dataikuPassword == null) {
			System.err.println("Dataiku environment variables have not been set");
			System.exit(0);
		}

		DataikuCollector.start("https://" + dataikuHost, dataikuUsername, dataikuPassword);

	}
}

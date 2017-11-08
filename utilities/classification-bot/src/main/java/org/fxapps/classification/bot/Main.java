package org.fxapps.classification.bot;

import java.io.IOException;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
	
	static {
		Properties.init();
		ApiContextInitializer.init();
		try {
			Classifier.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ClassifierBot classifierBot = new ClassifierBot();
		TelegramBotsApi botsApi = new TelegramBotsApi();
		try {
			botsApi.registerBot(classifierBot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}

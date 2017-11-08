package org.fxapps.classification.bot;

public class Properties {

	private static final String BOT_TOKEN_PROPERTY = "bot.token";
	private static final String BOT_USERNAME_PROPERTY = "bot.username";

	private static final String CLASSIFIER_MODELPATH_PROPERTY = "classifier.modelpath";
	private static final String CLASSIFIER_INPUTFORMAT_PROPERTY = "classifier.inputformat";
	private static final String CLASSIFIER_LABELS_PROPERTY = "classifier.labels";

	private static String botToken;
	private static String botUsername;
	private static String classifierModelPath;
	private static int[] classifierInputFormat;
	private static String[] classifierLabels;

	private Properties() {
	}

	public static void init() {
		botToken = getProperty(BOT_TOKEN_PROPERTY);
		botUsername = getProperty(BOT_USERNAME_PROPERTY);
		classifierModelPath = getProperty(CLASSIFIER_MODELPATH_PROPERTY);
		classifierLabels = getProperty(CLASSIFIER_LABELS_PROPERTY).split("\\,");
		String classifierInputFormatStr = getProperty(CLASSIFIER_INPUTFORMAT_PROPERTY);
		classifierInputFormat = new int[3];
		String[] inputFormatParts = classifierInputFormatStr.split("\\,");
		classifierInputFormat[0] = Integer.parseInt(inputFormatParts[0]);
		classifierInputFormat[1] = Integer.parseInt(inputFormatParts[1]);
		classifierInputFormat[2] = Integer.parseInt(inputFormatParts[2]);
	}

	private static String getProperty(String property) {
		String prop = System.getProperty(property);
		if (prop == null) {
			throw new IllegalArgumentException("Property " + property + " is mandatory.");
		}
		return prop;
	}

	public static String botToken() {
		return botToken;
	}

	public static String botUsername() {
		return botUsername;
	}

	public static String classifierModelPath() {
		return classifierModelPath;
	}

	public static int[] classifierInputFormat() {
		return classifierInputFormat;
	}

	public static String[] classifierLabels() {
		return classifierLabels;
	}

}

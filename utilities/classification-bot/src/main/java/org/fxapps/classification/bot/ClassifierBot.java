package org.fxapps.classification.bot;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class ClassifierBot extends TelegramLongPollingBot {

	Logger logger = Logger.getLogger(ClassifierBot.class.getName());

	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasPhoto()) {
			Message message = update.getMessage();
			long chatId = message.getChatId();
			// get the last photo - it seems to be the bigger one
			List<PhotoSize> photos = message.getPhoto();
			PhotoSize photo = photos.get(photos.size() - 1);
			String id = photo.getFileId();
			try {
				GetFile getFile = new GetFile();
				getFile.setFileId(id);
				String filePath = getFile(getFile).getFileUrl(getBotToken());
				// TODO: cache images?
				logger.info("== DOWNLOADING IMAGE " + filePath);
				URL url = new URL(filePath);
				String caption = Classifier.classify(url.openStream());
				logger.info("Caption for image " + filePath + ":\n" + caption);
				sendPhotoMessage(chatId, id, caption);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendPhotoMessage(long chatId, String id, String caption) throws TelegramApiException {
		SendPhoto msg = new SendPhoto().setChatId(chatId).setPhoto(id).setCaption(caption);
		sendPhoto(msg);
	}

	public String getBotUsername() {
		return Properties.botUsername();
	}

	@Override
	public String getBotToken() {
		return Properties.botToken();
	}

}

package core;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramCommandListener {
    BotApiMethod<Message> onCommandReceived(Update update);

    String getSupportedCommand();

    String getDescription();
}

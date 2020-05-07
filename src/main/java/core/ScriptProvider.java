package core;


import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ScriptProvider {
    BotApiMethod<Message> onRequestReceived(Update update);
}

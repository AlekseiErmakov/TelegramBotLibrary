package core;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TelegramLibBot extends TelegramLongPollingBot {
    private String botUserName;
    private String botToken;
    private List<TelegramCommandListener> listeners;
    private Set<OnAllUpdatesListener> allUpdatesListeners = new HashSet<>();
    private UnsupportedCommandAction unsupportedCommandAction = UnsupportedCommandAction.COMMANDLIST;
    private TelegramBotConfig config;
    private String unsupportedAlert = " is unsupported!";
    private ScriptProvider scriptProvider;


    public TelegramLibBot(String botUserName, String botToken) {
        this.botUserName = botUserName;
        this.botToken = botToken;
        config = TelegramBotConfig.getInstance();
    }

    @Override
    public void onUpdateReceived(Update update) {
        allUpdatesListeners.forEach(listener -> listener.onUpdateReceived(update));
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (config.isActiveScript()) {
                processScript(update);

            } else {
                processCommand(update);
            }
        }
    }

    private void processCommand(Update update) {
        Message userMessage = update.getMessage();
        if (messageHasCommand(userMessage) && hasListener(userMessage)) {
            TelegramCommandListener listener = getListener(userMessage);
            BotApiMethod<Message> botMessage = listener.onCommandReceived(update);
            try {
                execute(botMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (messageHasCommand(userMessage) && !hasListener(userMessage)) {
            onUnsupportedRequestReceived(update);
        }
    }


    private void processScript(Update update) {

        try {
            execute(scriptProvider.onRequestReceived(update));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void addOnAllUpdatesListener(OnAllUpdatesListener listener) {
        allUpdatesListeners.add(listener);
    }

    public void setScriptProvider(ScriptProvider scriptProvider) {
        this.scriptProvider = scriptProvider;
    }
    public void setUnsupportedCommandAction(UnsupportedCommandAction unsupportedCommandAction) {
        this.unsupportedCommandAction = unsupportedCommandAction;
    }

    public void setListeners(List<TelegramCommandListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void onUnsupportedRequestReceived(Update update) {
        String request = getUserRequest(update.getMessage());
        String botAnswer = "";
        switch (unsupportedCommandAction) {
            case COMMANDLIST:
                botAnswer = getCommandList();
                break;
            case MESSAGE:
                botAnswer = request + unsupportedAlert;
            case IGNORE:
        }
        sendMessage(botAnswer, update);
    }

    private void sendMessage(String botAnswer, Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(botAnswer);
    }

    private String getUserRequest(Message message) {
        if (messageHasCommand(message)) {
            return getCommandFromMessage(message);
        }
        return message.getText();
    }

    private String getCommandList() {
        return listeners.stream()
                .map(telegramCommandListener -> telegramCommandListener.getDescription() + telegramCommandListener.getSupportedCommand())
                .collect(Collectors.joining("\n"));
    }

    private TelegramCommandListener getListener(Message userMessage) {
        return listeners.stream()
                .filter(listener -> getCommandFromMessage(userMessage).equals(listener.getSupportedCommand()))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    private boolean hasListener(Message userMessage) {
        return listeners.stream()
                .anyMatch(listener -> getCommandFromMessage(userMessage).equals(listener.getSupportedCommand()));
    }

    public boolean messageHasCommand(Message message){
        if (message.hasText()){
            String text = message.getText();
            return text.startsWith("/") && text.length()>=2;
        } else {
            return false;
        }
    }
    public String getCommandFromMessage(Message message){
        String text = message.getText();
        return text.split(" ")[0];
    }

}

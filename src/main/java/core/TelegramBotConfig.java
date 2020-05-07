package core;

public class TelegramBotConfig {
    private boolean activeScript;
    private static TelegramBotConfig config;

    private TelegramBotConfig() {
    }

    public static TelegramBotConfig getInstance() {
        if (config == null) {
            config = new TelegramBotConfig();
        }
        return config;
    }

    public boolean isActiveScript() {
        return activeScript;
    }

    public void setActiveScript(boolean activeScript) {
        this.activeScript = activeScript;
    }
}

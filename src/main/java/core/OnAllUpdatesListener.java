package core;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface OnAllUpdatesListener {
    void onUpdateReceived(Update update);
}

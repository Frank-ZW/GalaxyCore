package org.craftgalaxy.galaxycore.client.data;

import lombok.Data;
import org.bukkit.entity.Player;
import org.craftgalaxy.galaxycore.client.chat.AbstractChatHandler;

@Data
public class ChatData {

    private Player player;
    private ClientData clientData;
    private String originalMsg;
    private String lowercaseMsg;
    private String checkMsg;
    private String[] words;
    private String reason;
    private boolean cancelled;
    private AbstractChatHandler.FilterLevel level;

    public ChatData release() {
        this.player = null;
        this.clientData = null;
        this.originalMsg = null;
        this.lowercaseMsg = null;
        this.checkMsg = null;
        this.words = null;
        this.reason = null;
        this.cancelled = false;
        this.level = AbstractChatHandler.FilterLevel.PASS;
        return this;
    }

    public ChatData setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public ChatData setClientData(ClientData clientData) {
        this.clientData = clientData;
        return this;
    }

    public ChatData setOriginalMsg(String originalMsg) {
        this.originalMsg = originalMsg;
        return this;
    }

    public ChatData setLowercaseMsg(String lowercaseMsg) {
        this.lowercaseMsg = lowercaseMsg;
        return this;
    }

    public ChatData setCheckMsg(String checkMsg) {
        this.checkMsg = checkMsg;
        return this;
    }

    public ChatData setWords(String[] words) {
        this.words = words;
        return this;
    }

    public ChatData setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public ChatData setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public ChatData setLevel(AbstractChatHandler.FilterLevel level) {
        this.level = level;
        return this;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ClientData getClientData() {
        return this.clientData;
    }

    public String getOriginalMsg() {
        return this.originalMsg;
    }

    public String getLowercaseMsg() {
        return this.lowercaseMsg;
    }

    public String getCheckMsg() {
        return this.checkMsg;
    }

    public String[] getWords() {
        return this.words;
    }

    public String getReason() {
        return this.reason;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public AbstractChatHandler.FilterLevel getLevel() {
        return this.level;
    }
}

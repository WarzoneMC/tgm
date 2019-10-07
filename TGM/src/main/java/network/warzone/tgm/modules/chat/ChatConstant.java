package network.warzone.tgm.modules.chat;

import net.md_5.bungee.api.ChatColor;

public enum ChatConstant {
    ERROR_COMMAND_PLAYERS_ONLY(ChatColor.RED + "You must be a player to run this command!");

    private final String message;

    ChatConstant(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}

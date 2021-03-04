package network.warzone.tgm.chat;

import net.md_5.bungee.api.ChatColor;

public enum ChatConstant {
    ERROR_COMMAND_PLAYERS_ONLY(ChatColor.RED + "You must be a player to run this command!"),
    ERROR_RATE_LIMITED(ChatColor.RED + "Slow down! You're being rate limited.");

    private final String message;

    ChatConstant(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}

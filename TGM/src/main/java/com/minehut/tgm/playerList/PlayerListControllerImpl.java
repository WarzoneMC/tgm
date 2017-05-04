package com.minehut.tgm.playerList;

import com.minehut.tgm.TGM;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.mojang.authlib.properties.Property;
import com.sk89q.minecraft.util.commands.ChatColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public class PlayerListControllerImpl implements PlayerListController {
    private static int TAB_WIDTH = 4;
    private static int TAB_HEIGHT = 20;
    private static int TOP_ITEM_INDEX = TAB_WIDTH + TAB_HEIGHT;

    @Getter private final Property blankTexture = new Property("textures", "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=", "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw=");

    @Getter private final PlayerListManager playerListManager;

    @Override
    public void refreshView(PlayerContext playerContext) {
        PlayerList playerList = playerListManager.getPlayerList(playerContext.getPlayer());

        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();

        int currentTeamIndex = 0;
        int amountOfTeams = teams.size() - 1; //subtract one to not count spectators.
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) {
                int row = 1;
                int leftInRow = TAB_WIDTH;
                for(int i = 0; i < matchTeam.getMembers().size(); i++) {
                    Player player = matchTeam.getMembers().get(i).getPlayer();
                    if (leftInRow <= 0) {
                        row++;
                        leftInRow = TAB_WIDTH;
                    }

//                    int slot = TOP_ITEM_INDEX - (row * TAB_WIDTH) - leftInRow;
                    int slot = (TAB_WIDTH - leftInRow + 1) * (TAB_HEIGHT - row);
                    playerList.addExistingPlayer(slot, matchTeam.getColor() + player.getName(), player);

//                    Bukkit.broadcastMessage(player.getName() + " : " + slot);

                    leftInRow--;
                }
            } else {
                int teamTitleSlot = 0;
                if (amountOfTeams == 2) {
                    if (currentTeamIndex == 1) {
                        teamTitleSlot = 40;
                    }
                }

                playerList.updateSlot(teamTitleSlot, ChatColor.WHITE.toString() + matchTeam.getMembers().size() + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + matchTeam.getMax() + " " + matchTeam.getColor() + ChatColor.BOLD + matchTeam.getAlias(), blankTexture);

                for(int i = 0; i < matchTeam.getMembers().size(); i++) {
                    Player player = matchTeam.getMembers().get(i).getPlayer();
                    playerList.addExistingPlayer(teamTitleSlot + i + 1, matchTeam.getColor() + player.getName(), player);
                }

                currentTeamIndex++;
            }
        }
    }
}

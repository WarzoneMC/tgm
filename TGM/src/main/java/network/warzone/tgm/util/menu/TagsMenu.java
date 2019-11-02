package network.warzone.tgm.util.menu;

import network.warzone.tgm.TGM;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.warzoneapi.models.PlayerTagsUpdateRequest;
import network.warzone.warzoneapi.models.PlayerTagsUpdateResponse;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 11/01/2019
 */
public class TagsMenu extends PlayerMenu {

    private PlayerContext playerContext;
    private UserProfile userProfile;

    public TagsMenu(String name, int slots, PlayerContext playerContext) {
        super(name, slots, playerContext.getPlayer());
        this.playerContext = playerContext;
        this.userProfile = playerContext.getUserProfile(true);
        draw();
    }

    private void draw() {
        List<String> tags = userProfile.getTags();
        if (tags == null) tags = new ArrayList<>();
        int i = 0;
        for (String tag : tags) {
            Material material;
            List<String> lore = new ArrayList<>();
            lore.add("");
            MenuAction menuAction;
            if (userProfile.getActiveTag() != null && userProfile.getActiveTag().equals(tag)) {
                material = Material.MAP;
                lore.add(ChatColor.YELLOW + "Click to deselect!");
                menuAction = (player, event) -> {
                    userProfile.setActiveTag(null);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    draw();
                    Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                        PlayerTagsUpdateResponse response = TGM.get().getTeamClient().updateTag(this.playerContext.getOriginalName(), null, PlayerTagsUpdateRequest.Action.SET);
                        if (!response.isError()) userProfile.saveTags(response);
                    });
                };
            } else {
                material = Material.PAPER;
                lore.add(ChatColor.GREEN + "Click to select!");
                menuAction = (player, event) -> {
                    if (userProfile.getTags().contains(tag)) {
                        userProfile.setActiveTag(tag);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                        draw();
                        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                            PlayerTagsUpdateResponse response = TGM.get().getTeamClient().updateTag(this.playerContext.getOriginalName(), tag, PlayerTagsUpdateRequest.Action.SET);
                            if (!response.isError()) userProfile.saveTags(response);
                        });
                    }
                };
            }
            setItem(i++, ItemFactory.createItem(material,  ChatColor.GRAY + "[" + ChatColor.translateAlternateColorCodes('&', tag) + ChatColor.GRAY + "]", lore), menuAction);
        }
    }
}

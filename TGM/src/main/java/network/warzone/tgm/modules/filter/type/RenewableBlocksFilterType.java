package network.warzone.tgm.modules.filter.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jorge on 07/04/2021
 */
public class RenewableBlocksFilterType implements FilterType, Listener {

    private final ConcurrentHashMap<Block, Long> queued = new ConcurrentHashMap<>();
    private final List<Block> ignored = new ArrayList<>();
    private int taskID = -1;

    private final List<Region> regions;
    private final int delay; // Ticks
    private final Material block;
    private final Material replaceWith;
    private final boolean inverted;

    public RenewableBlocksFilterType(List<Region> regions, int delay, Material block, Material replaceWith, boolean inverted) {
        this.regions = regions;
        this.delay = delay;
        this.block = block;
        this.replaceWith = replaceWith;
        this.inverted = inverted;
    }

    @Override
    public void load(Match match) {
        this.taskID = Bukkit.getScheduler().runTaskTimer(TGM.get(), this::tick, 1L, 1L).getTaskId();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(this.taskID);
        this.queued.clear();
        this.ignored.clear();
    }

    private void tick() {
        for (Map.Entry<Block, Long> e : queued.entrySet()) {
            if (System.currentTimeMillis() > e.getValue()) {
                e.getKey().setType(block);
                queued.remove(e.getKey());
            }
        }
    }

    private boolean contains(Block block) {
        for (Region region : this.regions){
            if ((!inverted && region.contains(block)) || (inverted && !region.contains(block))) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!contains(event.getBlock())) return;
        if (this.ignored.contains(event.getBlock())) {
            this.ignored.remove(event.getBlock());
            return;
        }
        if (queued.containsKey(event.getBlock())) {
            event.setCancelled(true);
        } else {
            if (event.getBlock().getType() != this.block) return;
            queued.put(event.getBlock(), System.currentTimeMillis() + (delay * 50L));
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> event.getBlock().setType(replaceWith), 1L);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!contains(event.getBlock())) return;
        if (queued.containsKey(event.getBlock())) {
            event.setCancelled(true);
        } else if (event.getBlock().getType() == this.block) {
            this.ignored.add(event.getBlock());
        }
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        ArrayList<Block> toAdd = new ArrayList<>();
        for (Block block : event.getBlocks()) {
            if (queued.contains(block) || (block.getType() == this.block && !this.ignored.contains(block))) {
                event.setCancelled(true);
            } else if (this.ignored.contains(block)) {
                Block newBlock = block.getRelative(event.getDirection());
                this.ignored.remove(block);
                toAdd.add(newBlock);
            }
        }
        this.ignored.addAll(toAdd);
    }

    @EventHandler
    public void onPiston(BlockPistonRetractEvent event) {
        ArrayList<Block> toAdd = new ArrayList<>();
        for (Block block : event.getBlocks()) {
            if (queued.contains(block) || (block.getType() == this.block && !this.ignored.contains(block))) {
                event.setCancelled(true);
            } else if (this.ignored.contains(block)) {
                Block newBlock = block.getRelative(event.getDirection());
                this.ignored.remove(block);
                toAdd.add(newBlock);
            }
        }
        this.ignored.addAll(toAdd);
    }

    public static RenewableBlocksFilterType parse(Match match, JsonObject jsonObject) {
        List<Region> regions = new ArrayList<>();
        for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
            if (region != null) {
                regions.add(region);
            }
        }
        int delay = jsonObject.has("delay") ? jsonObject.get("delay").getAsInt() : 20;
        Material type = Material.getMaterial(Strings.getTechnicalName(jsonObject.get("block").getAsString()));
        Material replaceWith = jsonObject.has("replaceWith") ?
                Material.getMaterial(Strings.getTechnicalName(jsonObject.get("replaceWith").getAsString()))
                : Material.AIR;
        boolean inverted = jsonObject.has("inverted") && jsonObject.get("inverted").getAsBoolean();
        return new RenewableBlocksFilterType(regions, delay, type, replaceWith, inverted);
    }

}

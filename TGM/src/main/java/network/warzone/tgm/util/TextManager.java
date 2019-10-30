package network.warzone.tgm.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TextManager {

    private File messagesFile = new File(TGM.get().getDataFolder().getAbsolutePath() + "/messages.json");

    private Map<String, String> messageCollection = new HashMap<>(); // group, id, string //TODO Eventually look into memory caching

    //TODO Translator

    public TextManager() {


        loadMessages();
    }

    //TextManager.getMessage("command.help.general")

    public String getMessage(String id) {
        return ChatColor.translateAlternateColorCodes('&', getRawMessage(id));
    }

    public String getRawMessage(String id) {
        return messageCollection.get(id);
    }

    public void loadMessages() { //TODO Add command to reload messages from the file
        if (!messagesFile.exists()) TGM.get().saveResource("messages.json", true);

        try {
            Gson gson = new Gson(); //TODO Create global gson getter

            JsonObject object = gson.fromJson(new JsonReader(new FileReader(messagesFile)), JsonObject.class);

            Map<String, String> tempCollection = new HashMap<>();
            object.entrySet().forEach(entry -> tempCollection.put(entry.getKey(), entry.getValue().getAsString()));

            //If something goes wrong we don't want to clear the list before the error
            messageCollection.clear();
            messageCollection.putAll(tempCollection);
            tempCollection.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

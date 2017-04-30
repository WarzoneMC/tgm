package com.minehut.tgm.playerList;


import java.lang.reflect.*;
import java.util.*;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerList {
    private static final Class<?> PACKET_PLAYER_INFO_CLASS = ReflectionUtil
            .isVersionHigherThan(1, 7) ? ReflectionUtil
            .getNMSClass("PacketPlayOutPlayerInfo") : ReflectionUtil
            .getNMSClass("Packet201PlayerInfo");
    private static final Class<?> PACKET_PLAYER_INFO_DATA_CLASS = (ReflectionUtil
            .isVersionHigherThan(1, 8)) ? ReflectionUtil
            .getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData") : null;
    private static final Class<?> WORLD_GAME_MODE_CLASS = (ReflectionUtil
            .isVersionHigherThan(1, 10)) ? ReflectionUtil
            .getNMSClass("EnumGamemode") : (ReflectionUtil.isVersionHigherThan(
            1, 8)) ? (ReflectionUtil.getNMSClass("WorldSettings$EnumGamemode"))
            : ReflectionUtil.getNMSClass("EnumGamemode");

    private static final Class<?> GAMEPROFILECLASS = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil
            .getMojangAuthClass("GameProfile") : null;
    private static final Constructor<?> GAMEPROPHILECONSTRUCTOR = ReflectionUtil
            .isVersionHigherThan(1, 8) ? (Constructor<?>) ReflectionUtil
            .getConstructor(GAMEPROFILECLASS, UUID.class, String.class).get()
            : null;
    private static final Class<?> CRAFTPLAYERCLASS = ReflectionUtil
            .getCraftbukkitClass("CraftPlayer", "entity");

    private static final Object WORLD_GAME_MODE_NOT_SET = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil.getEnumConstant(
            WORLD_GAME_MODE_CLASS, "NOT_SET") : null;
    private static final Class<?> CRAFT_CHAT_MESSAGE_CLASS = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil.getCraftbukkitClass(
            "CraftChatMessage", "util") : null;
    private static final Class<?> PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil
            .getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction") : null;
    private static final Object PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil.getEnumConstant(
            PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS,
            "REMOVE_PLAYER") : null;
    private static final Object PACKET_PLAYER_INFO_ACTION_ADD_PLAYER = ReflectionUtil
            .isVersionHigherThan(1, 8) ? ReflectionUtil.getEnumConstant(
            PACKET_PLAYER_INFO_PLAYER_ACTION_CLASS, "ADD_PLAYER")
            : null;
    private static final Class<?> PACKET_CLASS = ReflectionUtil
            .getNMSClass("Packet");
    private static final Class<?> I_CHAT_BASE_COMPONENT_CLASS = (ReflectionUtil
            .isVersionHigherThan(1, 8)) ? ReflectionUtil
            .getNMSClass("IChatBaseComponent") : null;
    private static final Constructor<?> PACKET_PLAYER_INFO_DATA_CONSTRUCTOR = ReflectionUtil
            .isVersionHigherThan(1, 8) ? (Constructor<?>) ReflectionUtil
            .getConstructor(PACKET_PLAYER_INFO_DATA_CLASS,
                    PACKET_PLAYER_INFO_CLASS, GAMEPROFILECLASS,
                    int.class, WORLD_GAME_MODE_CLASS,
                    I_CHAT_BASE_COMPONENT_CLASS).get() : null;

    private final static String[] colorcodeOrder = { "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    private final static String[] inviscodeOrder = { ",", ".", "\'", "`", " " };


    public static int SIZE_DEFAULT = 20;
    public static int SIZE_TWO = 40;
    public static int SIZE_THREE = 60;
    public static int SIZE_FOUR = 80;


    private List<Object> datas = new ArrayList<>();
    private Map<Integer, String> datasOLD = new HashMap<Integer, String>();

    private UUID uuid = null;
    private String[] tabs;
    private int size = 0;


    private static final HashMap<UUID,PlayerList> lookUpTable = new HashMap<>();

    /**
     * Tries to return an existing table instance for a player. If one does not exist, this will return a null value.
     * @param player
     * @return null or the player's tablist.
     */
    public static PlayerList getPlayerList(Player player){
        return lookUpTable.get(player.getUniqueId());
    }



    @SuppressWarnings("static-access")
    public PlayerList(Player player, int size) {
        this.uuid = player.getUniqueId();
        lookUpTable.put(uuid, this);
        tabs = new String[80];
        if (ReflectionUtil.isVersionHigherThan(1, 8))
            this.size = size;
        else
            this.size = this.SIZE_DEFAULT;
    }

    /**
     * Returns the name of the playerList at ID "id".
     * @param id
     *
     * @return
     */
    public String getTabName(int id) {
        return tabs[id];
    }

    /**
     * Resets a player's tablist. Use this if you have removed a real player
     * from the playertab and wish to reset it.
     */
    public void resetTablist() {
        this.clearAll();
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            addExistingPlayer(i, player.getName(), player);
            i++;
        }
    }

    /**
     * Clears all players from the player's tablist.
     */
    @SuppressWarnings("unchecked")
    public void clearPlayers() {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            Object packet = ReflectionUtil
                    .instantiate((Constructor<?>) ReflectionUtil
                            .getConstructor(PACKET_PLAYER_INFO_CLASS)
                            .get());

            List<Object> players = (List<Object>) ReflectionUtil
                    .getInstanceField(packet, "b");

            for (Player player2 : new ArrayList<Player>(
                    (Collection<? extends Player>) ReflectionUtil.invokeMethod(
                            Bukkit.getServer(), "getOnlinePlayers", null))) {
//                Object gameProfile = GAMEPROFILECLASS.cast(ReflectionUtil
//                        .invokeMethod(player2, "getProfile", new Class[0]));
                GameProfile gameProfile = ((CraftPlayer) player2).getProfile();

                Object craftChatMessage;
                {
                    Object[] array = (Object[]) ReflectionUtil.invokeMethod(
                            CRAFT_CHAT_MESSAGE_CLASS, null, "fromString",
                            new Class[] { String.class }, player2.getName());
                    craftChatMessage = array[0];
                }
                Object data = ReflectionUtil.instantiate(
                        PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet,
                        gameProfile, 1, WORLD_GAME_MODE_NOT_SET,
                        craftChatMessage);

                players.add(data);

                Bukkit.broadcastMessage("added player to remove: " + ((GameProfile) gameProfile).getName() + " (" + ((GameProfile) gameProfile).getId().toString() + ")");
            }
            sendNEWPackets(Bukkit.getPlayer(this.uuid), packet, players,
                    PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER);

            Bukkit.broadcastMessage("send remove player packet!");
        } else {
            Object olp = ReflectionUtil.invokeMethod(Bukkit.getServer(),
                    "getOnlinePlayers", null);
            Player[] players = (Player[]) olp;
            for (int i = 0; i < players.length; i++) {
                Object packet = null;
                try {
                    packet = ReflectionUtil
                            .instantiate((Constructor<?>) ReflectionUtil
                                    .getConstructor(
                                            PACKET_PLAYER_INFO_CLASS)
                                    .get());
                } catch (Exception e) {
                    error();
                    e.printStackTrace();
                }
                sendOLDPackets(Bukkit.getPlayer(this.uuid), packet, players[i].getName(), false);
            }
        }
    }

    /**
     * Clears all the custom tabs from the player's tablist.
     */
    @SuppressWarnings("unchecked")
    public void clearCustomTabs() {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            Object packet = ReflectionUtil
                    .instantiate((Constructor<?>) ReflectionUtil
                            .getConstructor(PACKET_PLAYER_INFO_CLASS)
                            .get());

            List<Object> players = (List<Object>) ReflectionUtil
                    .getInstanceField(packet, "b");

            for (Object playerData : new ArrayList<>(datas)) {
                Object gameProfile = GAMEPROFILECLASS.cast(ReflectionUtil
                        .invokeMethod(playerData, "a", new Class[0]));
                tabs[getIDFromName((String) ReflectionUtil.invokeMethod(
                        gameProfile, "getName", null))] = "";
                players.add(playerData);
            }
            datas.clear();
            sendNEWPackets(Bukkit.getPlayer(this.uuid), packet, players,
                    PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER);

        } else {
            for (int i = 0; i < size; i++) {
                if (!datasOLD.containsKey(i))
                    continue;
                Object packet = null;
                try {
                    packet = ReflectionUtil
                            .instantiate((Constructor<?>) ReflectionUtil
                                    .getConstructor(
                                            PACKET_PLAYER_INFO_CLASS)
                                    .get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendOLDPackets(Bukkit.getPlayer(this.uuid), packet, datasOLD.get(i), false);
                tabs[i] = null;
            }
            datasOLD.clear();
        }
    }

    /**
     * Clears all the values for a player's tablist. Use this whenever a player
     * first joins if you want to create your own tablist.
     *
     * This is here to remind you that you MUST call either this method or the
     * "clearCustomTabs" method. If you do not, the player will continue to see
     * the custom tabs until they relog.
     */
    public void clearAll() {
        clearPlayers();
        clearCustomTabs();
    }

    /**
     * Use this for changing a value at a specific playerList.
     *
     * @param id
     * @param newName
     */
    public void updateSlot(int id, String newName, Property texture) {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            removeCustomTab(id, true);
            addValue(id, newName, texture);
        } else {
            for (int i = id; i < size; i++)
                removeCustomTab(i, false);
            for (int i = id; i < size; i++)
                addValue(i, (i == id) ? newName : datasOLD.get(i).substring(2), texture);
        }
    }

    /**
     * Use this for changing a value at a specific playerList.
     *
     * @param id
     * @param newName
     */
    public void updateSlot(int id, String newName, UUID uuid, Property texture) {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            removeCustomTab(id, true);
            addValue(id, newName, uuid, texture);
        } else {
            for (int i = id; i < size; i++)
                removeCustomTab(i, false);
            for (int i = id; i < size; i++)
                addValue(i, (i == id) ? newName : datasOLD.get(i).substring(2), texture);
        }
    }

    /**
     * removes a specific player from the player's tablist.
     *
     * @param player
     */
    @SuppressWarnings("unchecked")
    public void removePlayer(Player player) {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            Object packet = ReflectionUtil
                    .instantiate((Constructor<?>) ReflectionUtil
                            .getConstructor(PACKET_PLAYER_INFO_CLASS)
                            .get());
            List<Object> players = (List<Object>) ReflectionUtil
                    .getInstanceField(packet, "b");
//            Object gameProfile = GAMEPROFILECLASS.cast(ReflectionUtil
//                    .invokeMethod(player, "getProfile", new Class[0]));

//            Object gameProfile = ReflectionUtil.instantiate(
//                    GAMEPROPHILECONSTRUCTOR, uuid, getNameFromID(id));
            GameProfile gameProfile = ((CraftPlayer) player).getProfile();
            Object craftChatMessage;
            {
                Object[] array = (Object[]) ReflectionUtil.invokeMethod(
                        CRAFT_CHAT_MESSAGE_CLASS, null, "fromString",
                        new Class[] { String.class }, player.getName());
                craftChatMessage = array[0];
            }
            Object data = ReflectionUtil.instantiate(
                    PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet,
                    gameProfile, 1, WORLD_GAME_MODE_NOT_SET,
                    craftChatMessage);
            players.add(data);
            sendNEWPackets(Bukkit.getPlayer(this.uuid), packet, players,
                    PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER);
        } else {
            Object packet = null;
            try {
                packet = ReflectionUtil
                        .instantiate((Constructor<?>) ReflectionUtil
                                .getConstructor(
                                        PACKET_PLAYER_INFO_CLASS)
                                .get());
            } catch (Exception e) {
                error();
                e.printStackTrace();
            }
            sendOLDPackets(player, packet, player.getName(), false);
        }
    }

    public void removeCustomTab(int id) {
        removeCustomTab(id, true);
    }

    /**
     * Removes a custom playerList from a player's tablist.
     *
     * @param id
     */
    @SuppressWarnings("unchecked")
    private void removeCustomTab(int id, boolean remove) {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            Object packet = ReflectionUtil
                    .instantiate((Constructor<?>) ReflectionUtil
                            .getConstructor(PACKET_PLAYER_INFO_CLASS)
                            .get());
            List<Object> players = (List<Object>) ReflectionUtil
                    .getInstanceField(packet, "b");
            for (Object playerData : new ArrayList<>(datas)) {
                Object gameProfile = GAMEPROFILECLASS.cast(ReflectionUtil
                        .invokeMethod(playerData, "a", new Class[0]));
                if (((String) ReflectionUtil.invokeMethod(gameProfile,
                        "getName", null)).startsWith(getNameFromID(id))) {
                    tabs[getIDFromName(((String) ReflectionUtil.invokeMethod(
                            gameProfile, "getName", null)))] = "";
                    players.add(playerData);
                    if (remove)
                        datas.remove(playerData);
                    break;
                }
            }
            sendNEWPackets(Bukkit.getPlayer(this.uuid), packet, players,
                    PACKET_PLAYER_INFO_ACTION_REMOVE_PLAYER);
        } else {
            Object packet = null;
            try {
                packet = ReflectionUtil
                        .instantiate((Constructor<?>) ReflectionUtil
                                .getConstructor(
                                        PACKET_PLAYER_INFO_CLASS)
                                .get());
            } catch (Exception e) {
                error();
                e.printStackTrace();
            }
            sendOLDPackets(Bukkit.getPlayer(this.uuid), packet, datasOLD.get(id), false);
            if (remove) {
                tabs[id] = null;
                datasOLD.remove(id);
            }
        }
    }

    /**
     *
     * Use this to add an existing offline player to a player's tablist. The
     * reason there is a name variable is so you can modify a player's name in
     * the tablist. If you want the player-playerList to be the same as the player's
     * name, provide the offline player's name.
     *
     * @param id
     * @param name
     * @param player
     */
    public void addExistingPlayer(int id, String name, OfflinePlayer player) {
        addValue(id, name, player.getUniqueId(), null);
    }

    /**
     * Use this to add a new player to the list
     *
     * @param id
     * @param name
     * @deprecated If all 80 slots have been taken, new values will not be shown
     *             and may have the potential to go out of the registered
     *             bounds. Use the "updateSlot" method to change a slot.
     */
    @Deprecated
    public void addValue(int id, String name, Property texture) {
        UUID uuid;
        if(name.length() > 0 && Bukkit.getOfflinePlayer(name).hasPlayedBefore()) {
            uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
        }else{
            uuid=UUID.randomUUID();
        }
        this.addValue(id, name, uuid, texture);
    }

    /**
     * Use this to add a new player to the list
     *
     * @param id
     * @param name
     * @deprecated If all 80 slots have been taken, new values will not be shown
     *             and may have the potential to go out of the registered
     *             bounds. Use the "updateSlot" method to change a slot.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public void addValue(int id, String name, UUID uuid, Property texture) {
        if (ReflectionUtil.isVersionHigherThan(1, 8)) {
            Object packet = ReflectionUtil
                    .instantiate((Constructor<?>) ReflectionUtil
                            .getConstructor(PACKET_PLAYER_INFO_CLASS)
                            .get());
            List<Object> players = (List<Object>) ReflectionUtil
                    .getInstanceField(packet, "b");
//            Object gameProfile = ReflectionUtil.instantiate(
//                    GAMEPROPHILECONSTRUCTOR, uuid, getNameFromID(id));
            GameProfile gameProfile = new GameProfile(uuid, getNameFromID(id));
            if (texture != null) {
                gameProfile.getProperties().put("textures", texture);
            }

            Object craftChatMessage;
            Object[] array = (Object[]) ReflectionUtil.invokeMethod(
                    CRAFT_CHAT_MESSAGE_CLASS, null, "fromString",
                    new Class[] { String.class }, getNameFromID(id) + name);
            craftChatMessage = array[0];
            Object data = ReflectionUtil.instantiate(
                    PACKET_PLAYER_INFO_DATA_CONSTRUCTOR, packet,
                    gameProfile, 1, WORLD_GAME_MODE_NOT_SET,
                    craftChatMessage);

            Object profile = GAMEPROFILECLASS.cast(ReflectionUtil.invokeMethod(
                    data, "a", new Class[0]));
            tabs[getIDFromName((String) ReflectionUtil.invokeMethod(profile,
                    "getName", null))] = (String) ReflectionUtil.invokeMethod(
                    profile, "getName", null);
            players.add(data);
            datas.add(data);

            sendNEWPackets(Bukkit.getPlayer(this.uuid), packet, players,
                    PACKET_PLAYER_INFO_ACTION_ADD_PLAYER);
        } else {
            Object packet = null;
            try {
                packet = ReflectionUtil
                        .instantiate((Constructor<?>) ReflectionUtil
                                .getConstructor(
                                        PACKET_PLAYER_INFO_CLASS)
                                .get());
            } catch (Exception e) {
                error();
                e.printStackTrace();
            }
            sendOLDPackets(Bukkit.getPlayer(this.uuid), packet, getNameFromID(id) + name, true);
            tabs[id] = name;
            datasOLD.put(id, getNameFromID(id) + name);
        }
    }

    /**
     * This is used to create the table. If you want to create a custom tablist,
     * then this should be called right after the playlist instance has been
     * created.
     */
    public void initTable(Property texture) {
        clearAll();
        for (int i = 0; i < size; i++)
            addValue(i, "", texture);
    }

    private static void sendNEWPackets(Player player, Object packet,
                                       List<?> players, Object action) {
        try {
            ReflectionUtil.setInstanceField(packet, "a", action);
            ReflectionUtil.setInstanceField(packet, "b", players);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object handle = ReflectionUtil.invokeMethod(
                CRAFTPLAYERCLASS.cast(player), "getHandle", null);
        Object playerConnection = ReflectionUtil.getInstanceField(handle,
                "playerConnection");
        ReflectionUtil.invokeMethod(playerConnection, "sendPacket",
                new Class[] { PACKET_CLASS }, packet);
    }

    private static void sendOLDPackets(Player player, Object packet,
                                       String name, boolean isOnline) {
        try {
            ReflectionUtil.setInstanceField(packet, "a", name);
            ReflectionUtil.setInstanceField(packet, "b", isOnline);
            ReflectionUtil.setInstanceField(packet, "c", ((short) 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object handle = ReflectionUtil.invokeMethod(
                CRAFTPLAYERCLASS.cast(player), "getHandle", new Class[0]);
        Object playerConnection = ReflectionUtil.getInstanceField(handle,
                "playerConnection");
        ReflectionUtil.invokeMethod(playerConnection, "sendPacket",
                new Class[] { PACKET_CLASS }, packet);
    }

    /**
     * Changes the player. NOTE: Scoreboards will not be transfered to the new
     * player. You will have to re-set each value if the player has changed.
     *
     * @param player
     */
    public void setPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }

    /**
     * Returns the player.
     *
     * @return
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    /**
     * This returns the ID of a slot at [Row,Columb].
     *
     * @param row
     * @param col
     *
     * @return
     */
    public int getID(int row, int col) {
        return col * 20 + row;
    }

    private static String getNameFromID(int id) {
        String[] a = colorcodeOrder;
        int size1 = 15;
        String firstletter;
        String secondletter;
        if (!ReflectionUtil.isVersionHigherThan(1, 8)) {
            a = inviscodeOrder;
            size1 = 5;
        }
        firstletter = a[id / size1];
        secondletter = a[id % size1];
        if (ReflectionUtil.isVersionHigherThan(1, 8))
            return ChatColor.getByChar(firstletter) + ""
                    + ChatColor.getByChar(secondletter) + ChatColor.RESET;
        return firstletter + secondletter;
    }

    private static int getIDFromName(String id) {
        String[] a = colorcodeOrder;
        int size1 = 15;
        int indexAdder = 0;
        if (!ReflectionUtil.isVersionHigherThan(1, 8)) {
            a = inviscodeOrder;
            size1 = 5;
            indexAdder = 1;
        }
        int total = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i].equalsIgnoreCase(id.charAt(0 + indexAdder) + "")) {
                total = size1 * i;
                break;
            }
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i].equalsIgnoreCase(id.charAt(1 + (indexAdder + indexAdder))
                    + "")) {
                total += i;
                break;
            }
        }
        return total;
    }

    private void error(){
        Bukkit.broadcastMessage("PLEASE REPORT THIS ISSUE TO"
                + ChatColor.RED + " ZOMBIE_STRIKER"
                + ChatColor.RESET + " ON THE BUKKIT FORUMS");
    }

    /**
     * A small help with reflection
     */
    public static class ReflectionUtil {
        private static final String SERVER_VERSION;
        static {
            String name = Bukkit.getServer().getClass().getName();
            name = name.substring(name.indexOf("craftbukkit.")
                    + "craftbukkit.".length());
            name = name.substring(0, name.indexOf("."));

            SERVER_VERSION = name;
        }

        private static boolean isVersionHigherThan(int mainVersion,
                                                   int secondVersion) {
            String firstChar = SERVER_VERSION.substring(1, 2);
            int fInt = Integer.parseInt(firstChar);
            if (fInt < mainVersion)
                return false;
            StringBuilder secondChar = new StringBuilder();
            for (int i = 3; i < 10; i++) {
                if (SERVER_VERSION.charAt(i) == '_'
                        || SERVER_VERSION.charAt(i) == '.')
                    break;
                secondChar.append(SERVER_VERSION.charAt(i));
            }
            int sInt = Integer.parseInt(secondChar.toString());
            if (sInt < secondVersion)
                return false;
            return true;
        }

        /**
         * Returns the NMS class.
         *
         * @param name
         *            The name of the class
         *
         * @return The NMS class or null if an error occurred
         */
        private static Class<?> getNMSClass(String name) {
            try {
                return Class.forName("net.minecraft.server." + SERVER_VERSION
                        + "." + name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Returns the CraftBukkit class.
         *
         * @param name
         *            The name of the class
         *
         * @return The CraftBukkit class or null if an error occurred
         */

        private static Class<?> getCraftbukkitClass(String name,
                                                    String packageName) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION
                        + "." + packageName + "." + name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Returns the mojang.authlib class.
         *
         * @param name
         *            The name of the class
         *
         * @return The mojang.authlib class or null if an error occurred
         */

        private static Class<?> getMojangAuthClass(String name) {
            try {
                return Class.forName("com.mojang.authlib." + name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Invokes the method
         *
         * @param handle
         *            The handle to invoke it on
         * @param methodName
         *            The name of the method
         * @param parameterClasses
         *            The parameter types
         * @param args
         *            The arguments
         *
         * @return The resulting object or null if an error occurred / the
         *         method didn't return a thing
         */
        @SuppressWarnings("rawtypes")
        private static Object invokeMethod(Object handle, String methodName,
                                           Class[] parameterClasses, Object... args) {
            return invokeMethod(handle.getClass(), handle, methodName,
                    parameterClasses, args);
        }

        /**
         * Invokes the method
         *
         * @param clazz
         *            The class to invoke it from
         * @param handle
         *            The handle to invoke it on
         * @param methodName
         *            The name of the method
         * @param parameterClasses
         *            The parameter types
         * @param args
         *            The arguments
         *
         * @return The resulting object or null if an error occurred / the
         *         method didn't return a thing
         */
        @SuppressWarnings("rawtypes")
        private static Object invokeMethod(Class<?> clazz, Object handle,
                                           String methodName, Class[] parameterClasses, Object... args) {
            Optional<Method> methodOptional = getMethod(clazz, methodName,
                    parameterClasses);

            if (!methodOptional.isPresent()) {
                return null;
            }

            Method method = methodOptional.get();

            try {
                return method.invoke(handle, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Sets the value of an instance field
         *
         * @param handle
         *            The handle to invoke it on
         * @param name
         *            The name of the field
         * @param value
         *            The new value of the field
         */
        private static void setInstanceField(Object handle, String name,
                                             Object value) {
            Class<?> clazz = handle.getClass();
            Optional<Field> fieldOptional = getField(clazz, name);
            if (!fieldOptional.isPresent()) {
                return;
            }

            Field field = fieldOptional.get();
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                field.set(handle, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        /**
         * Sets the value of an instance field
         *
         * @param handle
         *            The handle to invoke it on
         * @param name
         *            The name of the field
         *
         * @return The result
         */
        private static Object getInstanceField(Object handle, String name) {
            Class<?> clazz = handle.getClass();
            Optional<Field> fieldOptional = getField(clazz, name);
            if (!fieldOptional.isPresent()) {
                return handle;
            }
            Field field = fieldOptional.get();
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                return field.get(handle);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Returns an enum constant
         *
         * @param enumClass
         *            The class of the enum
         * @param name
         *            The name of the enum constant
         *
         * @return The enum entry or null
         */
        private static Object getEnumConstant(Class<?> enumClass, String name) {
            if (!enumClass.isEnum()) {
                return null;
            }
            for (Object o : enumClass.getEnumConstants()) {
                if (name.equals(invokeMethod(o, "name", new Class[0]))) {
                    return o;
                }
            }
            return null;
        }

        /**
         * Returns the constructor
         *
         * @param clazz
         *            The class
         * @param params
         *            The Constructor parameters
         *
         * @return The Constructor or an empty Optional if there is none with
         *         these parameters
         */
        private static Optional<?> getConstructor(Class<?> clazz,
                                                  Class<?>... params) {
            try {
                return Optional.of(clazz.getConstructor(params));
            } catch (NoSuchMethodException e) {
                try {
                    return Optional.of(clazz.getDeclaredConstructor(params));
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                }
            }
            return Optional.empty();
        }

        /**
         * Instantiates the class. Will print the errors it gets
         *
         * @param constructor
         *            The constructor
         * @param arguments
         *            The initial arguments
         *
         * @return The resulting object, or null if an error occurred.
         */
        private static Object instantiate(Constructor<?> constructor,
                                          Object... arguments) {
            try {
                return constructor.newInstance(arguments);
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }

        private static Optional<Method> getMethod(Class<?> clazz, String name,
                                                  Class<?>... params) {
            try {
                return Optional.of(clazz.getMethod(name, params));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            try {
                return Optional.of(clazz.getDeclaredMethod(name, params));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }

        private static Optional<Field> getField(Class<?> clazz, String name) {
            try {
                return Optional.of(clazz.getField(name));
            } catch (NoSuchFieldException e) {
            }

            try {
                return Optional.of(clazz.getDeclaredField(name));
            } catch (NoSuchFieldException e) {
            }
            return Optional.empty();
        }
    }
}

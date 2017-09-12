package com.minehut.tgm.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by MatrixTunnel on 9/9/2017.
 *
 * https://github.com/kamcio96/DisablePlayerData/blob/master/src/main/java/pl/kamcio96/disableplayerdata/DisablePlayerData.java
 * @author kamcio96
 */
public class DisablePlayerData implements InvocationHandler {

    private static Object original;

    public static void disable() {
        try {
            String ver = Bukkit.getServer().getClass().getName().split("\\.")[3];

            Class<?> clazz = Class.forName("net.minecraft.server." + ver + ".IPlayerFileData");
            Object proxyIPlayerFileData = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new DisablePlayerData());

            Class<?> minecraftServer = Class.forName("net.minecraft.server." + ver + ".MinecraftServer");
            Object server = minecraftServer.getMethod("getServer").invoke(null);
            Object playerList = minecraftServer.getMethod("getPlayerList").invoke(server);
            Field f = playerList.getClass().getField("playerFileData");

            original = f.get(playerList);
            f.set(playerList, proxyIPlayerFileData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("load")) {
            return method.invoke(original, args);
        }
        if (method.getName().equals("getSeenPlayers")) {
            return method.invoke(original, args);
        }

        return null;
    }

}

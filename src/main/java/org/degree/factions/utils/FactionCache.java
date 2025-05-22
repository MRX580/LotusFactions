package org.degree.factions.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class FactionCache {
    private static final Map<String, String> uuidToFaction = new ConcurrentHashMap<>();

    public static void setFaction(String uuid, String factionName) {
        if (factionName == null) uuidToFaction.remove(uuid);
        else uuidToFaction.put(uuid, factionName);
    }

    public static String getFaction(String uuid) {
        return uuidToFaction.get(uuid);
    }
}

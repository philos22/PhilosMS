package client.command.commands.gm2;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import tools.exceptions.IdTypeNotSupportedException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import server.ThreadManager;

public class IdCommand extends Command {
    {
        setDescription("");
    }

    private final Map<String, String> handbookDirectory = new HashMap<>();
    private final Map<String, HashMap<String, String>> itemMap = new HashMap<>();

    public IdCommand() {
        handbookDirectory.put("map", "handbook/Map.txt");
        handbookDirectory.put("etc", "handbook/Etc.txt");
        handbookDirectory.put("npc", "handbook/NPC.txt");
        handbookDirectory.put("use", "handbook/Use.txt");
        handbookDirectory.put("mob", "handbook/Mob.txt");
        handbookDirectory.put("quest", "handbook/Quest.txt");
        handbookDirectory.put("setup", "handbook/Setup.txt");
        handbookDirectory.put("weapon", "handbook/Equip/Weapon.txt");
        handbookDirectory.put("accessory", "handbook/Equip/Accessory.txt");
        handbookDirectory.put("hat", "handbook/Equip/Cap.txt");
        handbookDirectory.put("cap", "handbook/Equip/Cap.txt");
        handbookDirectory.put("helmet", "handbook/Equip/Cap.txt");
        handbookDirectory.put("cape", "handbook/Equip/Cape.txt");
        handbookDirectory.put("top", "handbook/Equip/Coat.txt");
        handbookDirectory.put("coat", "handbook/Equip/Coat.txt");
        handbookDirectory.put("glove", "handbook/Equip/Glove.txt");
        handbookDirectory.put("overall", "handbook/Equip/Longcoat.txt");
        handbookDirectory.put("longcoat", "handbook/Equip/Longcoat.txt");
        handbookDirectory.put("pants", "handbook/Equip/Pants.txt");
        handbookDirectory.put("bottom", "handbook/Equip/Pants.txt");
        handbookDirectory.put("ring", "handbook/Equip/Ring.txt");
        handbookDirectory.put("shield", "handbook/Equip/Shield.txt");
        handbookDirectory.put("shoe", "handbook/Equip/Shoes.txt");
        handbookDirectory.put("shoes", "handbook/Equip/Shoes.txt");
        handbookDirectory.put("weapon", "handbook/Equip/Weapon.txt");
    }

    @Override
    public void execute(MapleClient client, final String[] params) {
        final MapleCharacter player = client.getPlayer();
        if (params.length < 2) {
            player.yellowMessage("Syntax: !id <type> <query>");
            return;
        }
        final String queryItem = joinStringArr(Arrays.copyOfRange(params, 1, params.length), " ");
        player.yellowMessage("Querying for entry... May take some time... Please try to refine your search.");
        Runnable queryRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    populateIdMap(params[0].toLowerCase());

                    Map<String, String> resultList = fetchResults(itemMap.get(params[0]), queryItem);
                    StringBuilder sb = new StringBuilder();
                    
                    if (resultList.size() > 0) {
                        int count = 0;
                        for (Map.Entry<String, String> entry: resultList.entrySet()) {
                            sb.append(String.format("Id for %s is: #b%s#k", entry.getKey(), entry.getValue()) + "\r\n");
                            if (++count > 100) {
                                break;
                            }
                        }
                        sb.append(String.format("Results found: #r%d#k | Returned: #b%d#k/100 | Refine search query to improve time.", resultList.size(), count) + "\r\n");
                        
                        player.getAbstractPlayerInteraction().npcTalk(9010000, sb.toString());
                    } else {
                        player.yellowMessage(String.format("Id not found for item: %s, of type: %s.", queryItem, params[0]));
                    }
                } catch (IdTypeNotSupportedException e) {
                    player.yellowMessage("Your query type is not supported.");
                } catch (IOException e) {
                    player.yellowMessage("Error reading file, please contact your administrator.");
                }
            }
        };
        
        ThreadManager.getInstance().newTask(queryRunnable);
    }

    private void populateIdMap(String type) throws IdTypeNotSupportedException, IOException {
        if (!handbookDirectory.containsKey(type)) {
            throw new IdTypeNotSupportedException();
        }
        itemMap.put(type, new HashMap<String, String>());
        BufferedReader reader = new BufferedReader(new FileReader(handbookDirectory.get(type)));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(" - ", 2);
            if (row.length == 2) {
                itemMap.get(type).put(row[1].toLowerCase(), row[0]);
            }
        }
    }

    private String joinStringArr(String[] arr, String separator) {
        if (null == arr || 0 == arr.length) return "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) sb.append(separator).append(arr[i]);
        return sb.toString();
    }

    private Map<String, String> fetchResults(Map<String, String> queryMap, String queryItem) {
        Map<String, String> results = new HashMap<>();
        for (String item: queryMap.keySet()) {
            if (item.indexOf(queryItem) != -1) {
                results.put(item, queryMap.get(item));
            }
        }
        return results;
    }
}

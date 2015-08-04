package com.nicktoony.scrAI.Managers;

import com.nicktoony.helpers.Lodash;
import com.nicktoony.helpers.LodashCallback1;
import com.nicktoony.scrAI.Constants;
import com.nicktoony.scrAI.Controllers.RoomController;
import com.nicktoony.screeps.Game;
import com.nicktoony.screeps.GlobalVariables;
import com.nicktoony.screeps.Structure;
import org.stjs.javascript.Array;
import org.stjs.javascript.Global;
import org.stjs.javascript.JSCollections;
import org.stjs.javascript.Map;

/**
 * Created by nick on 02/08/15.
 */
public class PathsManager extends ManagerTimer {
    private Map<String, Object> memory;
    private Map<String, Array<Map<String, Object>>> paths;
    private Structure baseStructure;
    private int roadsCreated = 0;

    public PathsManager(final RoomController roomController, Map<String, Object> memory) {
        super(roomController, "PathsManager", Constants.DELAY_PATH_SCAN);

        this.roomController = roomController;
        this.memory = memory;

        // Initial delay.. let everything else figure stuff out first
        if (!super.canRun()) {
            return;
        }

        // Init (sets up paths)
        if (memory.$get("init") == null) {
            init();
            memory.$put("init", true);
        }
    }

    private void init() {
        this.paths = JSCollections.$map();

        memory.$put("paths", paths);
        super.hasRun();
    }

    public void update() {
        if (!super.canRun()) {
            return;
        }

        super.hasRun();

        Global.console.log("RUNNING PATH MANAGER");

        // Load from memory
        this.paths = (Map<String, Array<Map<String, Object>>>) this.memory.$get("paths");
        this.baseStructure = null;
        this.roadsCreated = 0;

        // For all structure ids
        Lodash.forIn(roomController.getStructureManager().getRoadableStructureIds(), new LodashCallback1<String>() {
            @Override
            public boolean invoke(String structureId) {

                // Attempt to find the specified structure
                Structure structure = (Structure) Game.getObjectById(structureId);
                // If it exists
                if (structure != null) {

                    Global.console.log("RUNNING PATH MANAGER: STRUCTURE");

                    // Do we have a base structure?
                    if (baseStructure == null) {
                        // Use as base structure, don't calculate path
                        Global.console.log("RUNNING PATH MANAGER: BASESTRUCTRE");
                        baseStructure = structure;
                        return true;
                    }

                    // Check if it has a path
                    Array<Map<String, Object>> path = paths.$get(structureId);

                    // No path? Generate one
                    if (path == null) {

                        Global.console.log("RUNNING PATH MANAGER: MADEPATH");

                        // Find the path
                        paths.$put(structureId, roomController.getRoom().findPath(baseStructure.pos, structure.pos, JSCollections.$map(
                        "ignoreCreeps", true,
                        "ignoreDestructibleStructures", true
                        )));

                        // Don't do anymore paths, it's a lot of CPU
                        return false;
                    } else {

                        Global.console.log("RUNNING PATH MANAGER: BUILTPATH");

                        // For each step of the path
                        Lodash.forIn(path, new LodashCallback1<Map<String, Object>>() {
                            @Override
                            public boolean invoke(Map<String, Object> pathStep) {

                                // Create a road for the path position
                                int x = (Integer) pathStep.$get("x");
                                int y = (Integer) pathStep.$get("y");
                                if (roomController.getRoom().createConstructionSite(x, y, GlobalVariables.STRUCTURE_ROAD) == GlobalVariables.OK) {
                                    roadsCreated ++;
                                }

                                // Keep going as long as not created too many roads
                                return (roadsCreated < Constants.SETTING_MAX_ROAD_CREATE);
                            }
                        }, this);

                        return true;
                    }
                } else {
                    return true;
                }
            }
        }, this);
    }

    public Map<String, Object> getMemory() {
        return memory;
    }
}

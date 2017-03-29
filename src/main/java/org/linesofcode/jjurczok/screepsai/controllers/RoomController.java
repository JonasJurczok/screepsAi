package org.linesofcode.jjurczok.screepsai.controllers;

import org.linesofcode.jjurczok.screepsai.planning.RoomPlanner;
import org.linesofcode.jjurczok.screeps.Game;
import org.linesofcode.jjurczok.screeps.Room;
import org.stjs.javascript.JSCollections;
import org.stjs.javascript.Map;

public class RoomController extends MemoryController {
    public Room room;

    public RoomController(Room room) {
        super(room.memory);
        this.room = room;
    }

    @Override
    public void init() {
        super.init();
    }

    public void step() {
        RoomPlanner roomPlanner = new RoomPlanner(getMemory("RoomPlanner"), this);
        while (Game.cpu.getUsed() < 50) {
            roomPlanner.plan();
        }
    }
}
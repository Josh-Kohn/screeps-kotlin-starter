package managers
import memory.*
import screeps.api.Game
import screeps.api.get

/**
 * Returns a list of energy locations in the room ranked by highest amount energy
 */

interface EnergyLocationManager {

     fun assignHarvesterToSourceID(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (availableSource in sources){
            if (availableSource.currentHarvesterCreeps < availableSource.maxHarvesterCreeps){
                availableSource.currentHarvesterCreeps += 1
                return availableSource.sourceID
            }
        }
        return null
    }

    fun getFreeSourceID(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (source in sources){
            if(source.freeCreepSlot == true){
                source.freeCreepSlot = false
                return source.sourceID
            }
        }
        return null
    }













}
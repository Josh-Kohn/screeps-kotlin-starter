package managers
import memory.*
import screeps.api.Game
import screeps.api.get
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Returns a list of energy locations in the room ranked by highest amount energy
 */

interface EnergyLocationManager {

     fun getVacantSourceID(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (availableSource in sources){
            if (availableSource.currentCreeps < availableSource.maxCreeps){
                availableSource.currentCreeps += 1
                return availableSource.sourceID
            }
        }
        return null
    }

    fun getSourceID(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        return sources.random().sourceID
    }













}
package managers
import memory.*
import screeps.api.*

/**
 * Returns a list of energy locations in the room ranked by highest amount energy
 */

interface EnergyLocationManager {

     fun assignHarvesterToSourceID(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (availableSource in sources){
            if (availableSource.currentHarvesterCreeps < availableSource.maxHarvesterCreeps){
                availableSource.currentHarvesterCreeps += 1
                console.log("Adding Creep to Source ID ${availableSource.sourceID}")
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

    fun freeSlotToTrue(room: Room, returnSourceID: String){
        val sources = room.memory.sources
        for (source in sources){
            if (source.sourceID == returnSourceID){
                source.freeCreepSlot = true
            }
        }
    }

    fun getHighestCapacityContainerID(roomName: String): String?{
        val containers = Game.rooms[roomName]!!.find(FIND_STRUCTURES, options { filter = {
            (it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE)
                    && (it as StoreOwner).store.getUsedCapacity(RESOURCE_ENERGY) > 0
                    //asserts that this WILL be a store owner (store owner can store energy).  It is NOW a store owner class.
        } })
        containers.sortByDescending { (it as StoreOwner).store.getUsedCapacity(RESOURCE_ENERGY)}
        if (containers.isNotEmpty()){
            return containers[0].id
        }
        return null
    }
}

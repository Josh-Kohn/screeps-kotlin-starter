package managers.harvest
import managers.CreepStateManager
import memory.*
import screeps.api.*

/**
 * The instructions for how to harvest a source.  Pick an initial source, store in the creep's memory, and go harvest that source
 */

class CreepHarvestManager(private val creeps: List<Creep>): CreepStateManager() {

    private fun pickASource(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (availableSource in sources){
            if (availableSource.currentCreeps < availableSource.maxCreeps){
                availableSource.currentCreeps += 1
                return availableSource.sourceID
            }
        }
        return null
    }

    private fun pickADepot(roomName: String): String? {
        val potentialDepot = Game.rooms[roomName]!!.find(FIND_MY_STRUCTURES, options {
            filter = {
                (it.structureType == STRUCTURE_CONTAINER
                        || it.structureType == STRUCTURE_STORAGE
                        || it.structureType == STRUCTURE_SPAWN
                        || it.structureType == STRUCTURE_EXTENSION)
            }
        }) as Array<StoreOwner>
        val availableDepot = potentialDepot.filter { it.store.getFreeCapacity ==
        }
        for (availableSource in potentialDepot){
            if (availableSource.currentCreeps < availableSource.maxCreeps){
                availableSource.currentCreeps += 1
                return availableSource.sourceID
            }
        }
        return null
    }


    fun harvestSource(){
        //If Creep has energy, go deposit it at a store owner (container, room storage, spawner, etc)
        for (creep in creeps){
            energyManagement(creep)
            if(creep.memory.fullOfEnergy){
                if (creep.memory.depositID.isBlank()){
                    val roomName = creep.memory.roomSpawnLocation
                    creep.memory.depositID = pickADepot(roomName) ?: ""
                } else {
                    val getDepot = Game.getObjectById<StoreOwner>(creep.memory.depositID)!!
                    when (creep.transfer(getDepot, RESOURCE_ENERGY)) {
                        ERR_NOT_IN_RANGE -> {
                            creep.moveTo(getDepot)
                        }
                    }
                }
            } else{
                //Gets the creep to harvest source
                if (creep.memory.sourceIDAssignment.isBlank()){
                    val roomName = creep.memory.roomSpawnLocation
                    creep.memory.sourceIDAssignment = pickASource(roomName) ?: ""
                } else {
                    val getSource = Game.getObjectById<Source>(creep.memory.sourceIDAssignment)!!
                    when (creep.harvest(getSource)) {
                        ERR_NOT_IN_RANGE -> {
                            creep.moveTo(getSource)
                        }
                    }
                }
            }
        }
    }
}




package managers.harvest
import managers.CreepStateManager
import managers.EnergyLocationManager
import memory.*
import screeps.api.*

/**
 * The instructions for how to harvest a source.  Pick an initial source, store in the creep's memory, and go harvest that source
 */

class CreepHarvestManager(private val creeps: List<Creep>): EnergyLocationManager, CreepStateManager() {

    private fun pickADepot(roomName: String): String? {
        val potentialDepot = Game.rooms[roomName]!!.find(FIND_MY_STRUCTURES, options {
            filter = {
                (it.structureType == STRUCTURE_CONTAINER
                        || it.structureType == STRUCTURE_STORAGE
                        || it.structureType == STRUCTURE_SPAWN
                        || it.structureType == STRUCTURE_EXTENSION)
            }
        }) as Array<StoreOwner>
        //Checks to see if potentialDepot list is empty
        if(potentialDepot.isEmpty()){
            return null
        }
        val availableDepots = potentialDepot.filter { it.store.getFreeCapacity(RESOURCE_ENERGY) > 0 }
        if(availableDepots.isEmpty()){
            return null
        }
        val sortedDepots = availableDepots.sortedBy { it.store.getUsedCapacity(RESOURCE_ENERGY) }
        return sortedDepots[0].id
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
                    val getDepot = Game.getObjectById<StoreOwner>(creep.memory.depositID)
                    if (getDepot != null) {
                        when (creep.transfer(getDepot, RESOURCE_ENERGY)) {
                            ERR_NOT_IN_RANGE -> {
                                creep.moveTo(getDepot)
                            }
                        }
                    } else {
                        creep.memory.depositID = ""
                    }
                }
            } else{
                //Gets the creep to harvest source
                if (creep.memory.sourceIDAssignment.isBlank()){
                    val roomName = creep.memory.roomSpawnLocation
                    creep.memory.sourceIDAssignment = getVacantSourceID(roomName) ?: ""
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




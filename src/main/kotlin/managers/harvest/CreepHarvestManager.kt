package managers.harvest
import managers.CreepStateManager
import managers.EnergyLocationManager
import memory.*
import screeps.api.*
import screeps.api.structures.StructureContainer

/**
 * The instructions for how to harvest a source.  Pick an initial source, store in the creep's memory, and go harvest that source
 */

class CreepHarvestManager(private val creeps: List<Creep>): EnergyLocationManager {

    private fun getContainerID(roomName: String, harvesterCreep: Creep): String {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (source in sources) {
            val containerID = source.containerID
            val sourceID = source.sourceID
            val harvesterSourceID = harvesterCreep.memory.sourceIDAssignment
            if (harvesterSourceID == sourceID){
                val container = Game.getObjectById<StructureContainer>(containerID)
                if (container != null)
                    return containerID
            }
        }
        return ""
    }
    fun harvestSource(){
        //If Creep has energy, go deposit it at a store owner (container, room storage, spawner, etc)
        for (creep in creeps){
            //Gets the creep to harvest source
            val roomSpawnLocation = creep.memory.roomSpawnLocation
            if (creep.memory.sourceIDAssignment.isBlank()){
                creep.memory.sourceIDAssignment = assignHarvesterToSourceID(roomSpawnLocation) ?: ""
            } else {
                val getSource = Game.getObjectById<Source>(creep.memory.sourceIDAssignment)!!
                when (creep.harvest(getSource)) {
                    ERR_NOT_IN_RANGE, ERR_NOT_ENOUGH_RESOURCES -> {
                        creep.moveTo(getSource)
                    }
                    OK -> {
                        val container = getContainerID(roomSpawnLocation, creep)
                        val containerOwner = Game.getObjectById<StoreOwner>(container)
                        if (containerOwner != null){
                            when (creep.transfer(containerOwner, RESOURCE_ENERGY)) {
                                ERR_NOT_IN_RANGE -> {
                                    creep.moveTo(getSource)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




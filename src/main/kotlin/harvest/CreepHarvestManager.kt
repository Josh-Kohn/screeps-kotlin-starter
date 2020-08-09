package harvest
import memory.roomSpawnLocation
import memory.sourceIDAssignment
import memory.sources
import objects.SourceDataObject
import screeps.api.*

/**
 * The instructions for how to harvest a source.  Pick an initial source, store in the creep's memory, and go harvest that source
 */

class CreepHarvestManager(private val creeps: List<Creep>) {

    private fun pickASource(roomName: String): String? {
        val sources = Game.rooms[roomName]!!.memory.sources
        for (availableSource in sources){
            if (availableSource.creepNumber < availableSource.maxCreeps){
                availableSource.creepNumber += 1
                return availableSource.sourceID
            }
        }
        return null
    }

    fun harvestSource(){
        //Checks to see if creep has a sourceID
        for (creep in creeps){
            if (creep.memory.sourceIDAssignment.isBlank()){
                val roomName = creep.memory.roomSpawnLocation
                creep.memory.sourceIDAssignment = pickASource(roomName) ?: ""
            }
            else{
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




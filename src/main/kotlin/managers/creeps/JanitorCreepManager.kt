package managers.creeps

import job.JobType
import memory.fullOfEnergy
import memory.job
import memory.roomSpawnLocation
import memory.sources
import screeps.api.*
import screeps.api.structures.StructureContainer

class JanitorCreepManager(private val creeps:List<Creep>): CreepStateManager()  {

    fun cleanUpEnergy() {
        for (janitor in creeps) {
            energyManagement(janitor)
            val homeRoom = Game.rooms[janitor.pos.roomName]!!
            val sources = homeRoom.memory.sources
            if (!janitor.memory.fullOfEnergy) {
                //val harvesters = Game.creeps.values.filter { it.memory.job == JobType.HARVESTER.name && it.memory.roomSpawnLocation == homeRoom.name }
                for (source in sources) {
                    val sourceInstance = Game.getObjectById<Source>(source.sourceID)!!
                    val droppedEnergy = homeRoom.lookAtAreaAsArray(
                            sourceInstance.pos.y-3,
                            sourceInstance.pos.x-3,
                            sourceInstance.pos.y+3,
                            sourceInstance.pos.x+3
                    ).filter { it.type == LOOK_RESOURCES }
                    if (droppedEnergy.isNotEmpty()) {
                        droppedEnergy.sortedByDescending { it.resource!!.amount }
                        when (janitor.pickup(droppedEnergy[0].resource!!)) {
                            ERR_NOT_IN_RANGE -> {
                                janitor.moveTo((droppedEnergy[0].resource!!))
                            }
                        }
                    } else {
                        for (source in homeRoom.memory.sources) {
                            val container = Game.getObjectById<StructureContainer>(source.containerID)
                            if (container != null) {
                                when (janitor.withdraw(container, RESOURCE_ENERGY)) {
                                    ERR_NOT_IN_RANGE -> {
                                        janitor.moveTo(container.pos)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if(homeRoom.storage != null) {
                    when (janitor.transfer(homeRoom.storage!!, RESOURCE_ENERGY)) {
                        ERR_NOT_IN_RANGE -> {
                            janitor.moveTo(homeRoom.storage!!.pos)
                        }
                    }
                }
            }
        }
    }
}
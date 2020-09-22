package managers

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
            if (!janitor.memory.fullOfEnergy) {
                val harvesters = Game.creeps.values.filter { it.memory.job == JobType.HARVESTER.name && it.memory.roomSpawnLocation == homeRoom.name }
                for (harvester in harvesters) {
                    val droppedEnergy = harvester.pos.lookFor(LOOK_RESOURCES)
                    if (droppedEnergy != null) {
                        when (janitor.pickup(droppedEnergy[0])) {
                            ERR_NOT_IN_RANGE -> {
                                janitor.moveTo((droppedEnergy[0].pos))
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

/*Looks for harvester creeps
* Performs a look to see if there is energy on the ground near them
* If yes, picks up the energy on the ground and finds the room's storage
* If not, find the container near the harvester, withdraws energy from it, and then deposits it in the room's storage
* */
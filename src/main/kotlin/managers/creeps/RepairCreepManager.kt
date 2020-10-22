package managers.creeps

import memory.*
import screeps.api.*
import screeps.api.structures.Structure

class RepairCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager()  {

    fun repairStructures() {
        for (repairman in creeps) {
            val homeRoom = Game.rooms[repairman.pos.roomName]!!
            for (repairDataObject in Memory.repairDataObjects) {
                if (repairDataObject.roomOwner == homeRoom.name) {
                    repairman.memory.repairID = repairDataObject.repairID
                    break
                }
            }
            energyManagement(repairman)
            if (repairman.memory.fullOfEnergy) {
                if (repairman.memory.repairID.isBlank()){
                    val roomController = repairman.room.controller
                    when (repairman.upgradeController(roomController!!)){
                        ERR_NOT_IN_RANGE -> {
                            repairman.moveTo(roomController)
                        }
                    }
                } else {
                    val brokenStructure = Game.getObjectById<Structure>(repairman.memory.repairID)
                    if (brokenStructure != null) {
                        //repairman.say("Repairing ${brokenStructure.structureType}")
                        when (repairman.repair(brokenStructure)) {
                            ERR_NOT_IN_RANGE -> {
                                repairman.moveTo(brokenStructure.pos)
                            }
                        }
                    } else {
                        repairman.memory.repairID = ""
                    }
                }
            } else {
                if (homeRoom.storage != null) {
                    when (repairman.withdraw(homeRoom.storage!!, RESOURCE_ENERGY)){
                        ERR_NOT_IN_RANGE -> {
                            repairman.moveTo(homeRoom.storage!!)
                        }
                    }
                } else {
                    val towers = homeRoom.find(FIND_MY_STRUCTURES).filter { (it.structureType == STRUCTURE_TOWER)}
                    if (towers.isEmpty() ) {
                        val roomController = repairman.room.controller!!
                        val lookNearController = Game.rooms[repairman.memory.roomSpawnLocation]!!.lookAtAreaAsArray(
                                roomController.pos.y - 3,
                                roomController.pos.x - 3,
                                roomController.pos.y + 3,
                                roomController.pos.x + 3
                        )
                        val containersNearController = lookNearController.filter {
                            (it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_CONTAINER)
                                    || (it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_STORAGE)
                        }
                        val container = containersNearController[0].structure!!
                        if (containersNearController.isNotEmpty()) {
                            when (repairman.withdraw(container as StoreOwner, RESOURCE_ENERGY)) {
                                ERR_NOT_IN_RANGE -> {
                                    repairman.moveTo(container)
                                }
                            }
                        }
                    } else {
                        val roomController = repairman.room.controller
                        when (repairman.upgradeController(roomController!!)){
                            ERR_NOT_IN_RANGE -> {
                                repairman.moveTo(roomController)
                            }
                        }

                    }
                }
            }
        }
    }
}
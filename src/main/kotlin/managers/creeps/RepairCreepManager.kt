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
                }
            }
        }
    }
}
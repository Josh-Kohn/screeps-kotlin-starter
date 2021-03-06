package managers.creeps

import memory.*
import screeps.api.*

/**
 * Directs the upgrader creeps to upgrade the room controller
 */

class UpgradeCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager() {

    fun upgradeRoomController() {
        for (upgrader in creeps) {
            val homeRoom = Game.rooms[upgrader.pos.roomName]!!
            energyManagement(upgrader)
            if (upgrader.memory.fullOfEnergy) {
                upgrader.memory.withdrawID = ""
                upgrader.memory.sourceIDAssignment = ""
                val roomController = upgrader.room.controller
                when (upgrader.upgradeController(roomController!!)) {
                    ERR_NOT_IN_RANGE -> {
                        upgrader.moveTo(roomController)
                    }
                }
            } else {
                if (upgrader.memory.withdrawID.isBlank()){

                    val roomController = upgrader.room.controller!!
                    val lookNearController = Game.rooms[upgrader.memory.roomSpawnLocation]!!.lookAtAreaAsArray(
                            roomController.pos.y-3,
                            roomController.pos.x-3,
                            roomController.pos.y+3,
                            roomController.pos.x+3
                    )
                    val containersNearController = lookNearController.filter {
                        ((it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_CONTAINER)
                                || (it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_STORAGE))
                                && (it.structure as StoreOwner).store != null && (it.structure as StoreOwner).store.getUsedCapacity(RESOURCE_ENERGY) > 0
                    }
                    if (containersNearController.isNotEmpty()){
                        upgrader.memory.withdrawID = containersNearController[0].structure!!.id
                    } else {
                        upgrader.memory.withdrawID = getHighestCapacityContainerID(homeRoom.name) ?: ""
                    }
                    if (upgrader.memory.withdrawID.isBlank()) {
                        //Builder Creep has no energy, go find a source and harvest
                        if (upgrader.memory.sourceIDAssignment.isBlank()) {
                            upgrader.memory.sourceIDAssignment = getFreeSourceID(homeRoom.name) ?: ""
                        } else {
                            val getSource = Game.getObjectById<Source>(upgrader.memory.sourceIDAssignment)!!
                            when (upgrader.harvest(getSource)) {
                                ERR_NOT_IN_RANGE -> {
                                    upgrader.moveTo(getSource)
                                }
                            }
                        }
                    }
                } else {
                    val getContainer = Game.getObjectById<StoreOwner>(upgrader.memory.withdrawID)
                    if (getContainer == null){
                        upgrader.memory.withdrawID = ""
                    } else {
                        when (upgrader.withdraw(getContainer, RESOURCE_ENERGY)){
                            ERR_NOT_IN_RANGE -> {
                                upgrader.moveTo(getContainer)
                            }
                            ERR_NOT_ENOUGH_RESOURCES -> {
                                upgrader.memory.withdrawID = ""
                            }
                        }
                    }
                }
            }
        }
    }
}







package managers

import memory.constructionSiteID
import memory.fullOfEnergy
import memory.sourceIDAssignment
import screeps.api.*

/**
 * Directs the upgrader creeps to upgrade the room controller
 */

class UpgradeCreepManager(private val creeps:List<Creep>): EnergyLocationManager,CreepStateManager() {

    fun upgradeRoomController() {
        for (upgrader in creeps) {
            val homeRoom = Game.rooms[upgrader.pos.roomName]!!
            energyManagement(upgrader)
            if (upgrader.memory.fullOfEnergy) {
                val roomController = upgrader.room.controller
                when (upgrader.upgradeController(roomController!!)) {
                    ERR_NOT_IN_RANGE -> {
                        upgrader.moveTo(roomController)
                    }
                }
                upgrader.upgradeController(roomController)
            } else {
                //Builder Creep has no energy, go find a source and harvest
                if (upgrader.memory.sourceIDAssignment.isBlank()){
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

        }
    }
}







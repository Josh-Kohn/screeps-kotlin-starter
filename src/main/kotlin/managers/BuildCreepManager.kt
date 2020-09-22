package managers

import memory.*
import objects.SourceDataObject
import screeps.api.*

/**
 * Directs the builder creeps to either build or upgrade the RCL depending on the room level
 */

class BuildCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager() {

    fun buildConstructionSites() {
        for (builder in creeps) {
            val homeRoom = Game.rooms[builder.pos.roomName]!!
            for (constructionDataObject in Memory.constructionDataObjects){
                if(constructionDataObject.roomOwner == homeRoom.name){
                    builder.memory.constructionSiteID = constructionDataObject.constructionSiteID
                    break
                }
            }
            energyManagement(builder)
            if (builder.memory.fullOfEnergy) {
                builder.memory.withdrawID = ""
                builder.memory.sourceIDAssignment = ""



                if (builder.memory.constructionSiteID.isBlank()) {
                    val roomController = builder.room.controller
                    when (builder.upgradeController(roomController!!)) {
                        ERR_NOT_IN_RANGE -> {
                            builder.moveTo(roomController)
                        }
                    }
                    builder.upgradeController(roomController)
                } else {
                    if (builder.memory.constructionSiteID.isNotBlank()) {
                        val building = Game.getObjectById<ConstructionSite>(builder.memory.constructionSiteID)
                        //Game's getObjectById function gets you an object from the id you give it if the object exists (in this case, builder.memory.constructionSiteId)
                        if (building == null) {
                            builder.memory.constructionSiteID = ""
                        } else {
                            when (builder.build(building)) {
                                ERR_NOT_IN_RANGE -> {
                                    builder.moveTo(building.pos)
                                }
                            }
                        }
                    }
                }
            } else {
                if (builder.memory.withdrawID.isBlank()){
                    val constructionSiteEnergy = Game.getObjectById<ConstructionSite>(builder.memory.constructionSiteID)
                    if (constructionSiteEnergy != null) {
                        val droppedEnergy = homeRoom.lookAtAreaAsArray(
                                constructionSiteEnergy.pos.y - 3,
                                constructionSiteEnergy.pos.x - 3,
                                constructionSiteEnergy.pos.y + 3,
                                constructionSiteEnergy.pos.x + 3
                        ).filter { it.type == LOOK_RESOURCES }
                        if (droppedEnergy.isNotEmpty()){
                            when (builder.pickup(droppedEnergy[0].resource!!)){
                                ERR_NOT_IN_RANGE -> {
                                    builder.moveTo(droppedEnergy[0].resource!!.pos)
                                    //TODO Figure out why it's not getting past here
                                }
                            }
                        } else {
                            builder.memory.withdrawID = getHighestCapacityContainerID(homeRoom.name) ?: ""
                        }
                    }
                } else {
                    val getContainer = Game.getObjectById<StoreOwner>(builder.memory.withdrawID)
                    if (getContainer == null){
                        builder.memory.withdrawID = ""
                    } else {
                        when (builder.withdraw(getContainer, RESOURCE_ENERGY)){
                            ERR_NOT_IN_RANGE -> {
                                builder.moveTo(getContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}


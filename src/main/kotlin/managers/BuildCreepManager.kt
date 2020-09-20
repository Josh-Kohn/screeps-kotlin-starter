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
            energyManagement(builder)
            if (builder.memory.fullOfEnergy) {
                builder.memory.withdrawID = ""
                builder.memory.sourceIDAssignment = ""

                var constructionSiteID = ""
                for (constructionDataObject in Memory.constructionDataObjects){
                    if(constructionDataObject.roomOwner == homeRoom.name){
                        constructionSiteID = constructionDataObject.constructionSiteID
                        break
                    }
                }


                if (constructionSiteID.isBlank()) {
                    val roomController = builder.room.controller
                    when (builder.upgradeController(roomController!!)) {
                        ERR_NOT_IN_RANGE -> {
                            builder.moveTo(roomController)
                        }
                    }
                    builder.upgradeController(roomController)
                } else {
                    if (builder.memory.constructionSiteID.isBlank()) {
                        builder.memory.constructionSiteID = constructionSiteID
                    } else {
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
                    builder.memory.withdrawID = getHighestCapacityContainerID(homeRoom.name) ?: ""
                    //TODO
                    /*if (builder.memory.withdrawID.isBlank()) {
                        //Builder Creep has no energy, go find a source and harvest
                        if (builder.memory.sourceIDAssignment.isBlank()) {
                            builder.memory.sourceIDAssignment = getFreeSourceID(homeRoom.name) ?: ""
                        } else {
                            val getSource = Game.getObjectById<Source>(builder.memory.sourceIDAssignment)!!
                            when (builder.harvest(getSource)) {
                                ERR_NOT_IN_RANGE -> {
                                    builder.moveTo(getSource)
                                }
                            }
                        }
                    }*/
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


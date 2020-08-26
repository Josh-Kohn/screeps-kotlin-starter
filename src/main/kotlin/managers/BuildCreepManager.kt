package managers

import memory.*
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
                val constructionSites = homeRoom.find(FIND_CONSTRUCTION_SITES)
                if (constructionSites.isEmpty()) {
                    when (builder.upgradeController(builder.room.controller!!)) {
                        ERR_NOT_IN_RANGE -> {
                            builder.moveTo(builder.room.controller!!)
                        }
                    }
                } else {
                    if (builder.memory.constructionSiteID.isBlank()) {
                        val constructionSiteID = constructionSites[0].id
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
                //Builder Creep has no energy, go find a source and harvest
                if (builder.memory.sourceIDAssignment.isBlank()){
                    builder.memory.sourceIDAssignment = getFreeSourceID(homeRoom.name) ?: ""
                } else {
                    val getSource = Game.getObjectById<Source>(builder.memory.sourceIDAssignment)!!
                    when (builder.harvest(getSource)) {
                        ERR_NOT_IN_RANGE -> {
                            builder.moveTo(getSource)
                        }
                    }
                }
            }
        }
    }
}


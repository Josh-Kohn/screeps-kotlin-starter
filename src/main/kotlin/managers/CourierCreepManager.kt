package managers

import job.JobType
import memory.*
import objects.SourceDataObject
import screeps.api.*
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureStorage

class CourierCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager() {

    fun ferryEnergy(){
        for (courier in creeps){
            val homeRoom = Game.rooms[courier.memory.roomSpawnLocation]!!
            energyManagement(courier)
            if (courier.memory.fullOfEnergy){
                if(courier.memory.depositID.isBlank()){

                    var constructionSiteID = ""
                    for (constructionDataObject in Memory.constructionDataObjects){
                        if(constructionDataObject.roomOwner == homeRoom.name){
                            constructionSiteID = constructionDataObject.constructionSiteID
                            break
                        }
                    }
                    if (constructionSiteID.isBlank()){
                        val roomController = courier.room.controller!!
                        val lookNearController = Game.rooms[courier.memory.roomSpawnLocation]!!.lookAtAreaAsArray(
                                roomController.pos.y-3,
                                roomController.pos.x-3,
                                roomController.pos.y+3,
                                roomController.pos.x+3
                                )
                        val containersNearController = lookNearController.filter {
                            (it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_CONTAINER)
                                    || (it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_STORAGE)
                        }
                        if (containersNearController.isEmpty()){
                            when (courier.upgradeController(roomController)) {
                                ERR_NOT_IN_RANGE -> {
                                    courier.moveTo(roomController)
                                }
                            }
                        } else {
                             courier.memory.depositID = containersNearController[0].structure!!.id
                        }
                    } else {
                        if (courier.memory.dropSpot.roomName == "roomName") {
                            if (courier.memory.constructionSiteID.isBlank()) {
                                courier.memory.constructionSiteID = constructionSiteID
                            } else {
                                val building = Game.getObjectById<ConstructionSite>(courier.memory.constructionSiteID)
                                if (building == null) {
                                    courier.memory.constructionSiteID = ""
                                } else {
                                    val dropSpotArray = Game.rooms[courier.memory.roomSpawnLocation]!!.lookAtAreaAsArray(
                                            building.pos.x + 2,
                                            building.pos.x - 2,
                                            building.pos.y - 2,
                                            building.pos.y + 2)
                                    val dropSpotLocation = dropSpotArray.filter {
                                        it.type == LOOK_TERRAIN && it.terrain == TERRAIN_PLAIN
                                    }
                                    courier.memory.dropSpot = RoomPosition(dropSpotLocation[0].x, dropSpotLocation[0].y, homeRoom.name)

                                }
                            }
                        } else {
                            if (courier.pos.x != courier.memory.dropSpot.x && courier.pos.y != courier.memory.dropSpot.y) {
                                courier.moveTo(courier.memory.dropSpot)
                            } else {
                                courier.drop(RESOURCE_ENERGY)
                                courier.memory.dropSpot = RoomPosition(0,0,"roomName")
                                courier.memory.constructionSiteID = ""
                            }
                        }
                    }
                } else {
                    val getDepot = Game.getObjectById<StoreOwner>(courier.memory.depositID)
                    if (getDepot != null) {
                        if (getDepot.store.getFreeCapacity(RESOURCE_ENERGY) == 0){
                            courier.memory.depositID = ""
                        } else {
                            when (courier.transfer(getDepot, RESOURCE_ENERGY)) {
                                ERR_NOT_IN_RANGE -> {
                                    courier.moveTo(getDepot)
                                }
                                ERR_FULL -> {
                                    courier.memory.depositID = ""
                                }
                            }
                        }
                    } else {
                        courier.memory.depositID = ""
                    }
                }
            } else {
                if (courier.memory.sourceIDAssignment.isBlank()) {
                    val freeSourceID = findACourierSource(homeRoom.memory.sources)
                    courier.memory.sourceIDAssignment = freeSourceID
                } else {
                    for (source in homeRoom.memory.sources) {
                        if (courier.memory.sourceIDAssignment == source.sourceID) {
                            if (source.containerID.isNotBlank()) {
                                val container = Game.getObjectById<StructureContainer>(source.containerID)
                                if (container != null) {
                                    when (courier.withdraw(container, RESOURCE_ENERGY)) {
                                        ERR_NOT_IN_RANGE -> {
                                            courier.moveTo(container.pos)
                                        }
                                    }
                                }
                            } else {
                                if (courier.memory.droppedID.isBlank()) {
                                    val droppedEnergy = homeRoom.find(FIND_DROPPED_RESOURCES).filter { it.resourceType == RESOURCE_ENERGY }
                                    courier.memory.droppedID = droppedEnergy[0].id
                                } else {
                                    val droppedEnergyID = Game.getObjectById<Resource>(courier.memory.droppedID)
                                    if (droppedEnergyID != null) {
                                        when (courier.pickup(droppedEnergyID)) {
                                            ERR_NOT_IN_RANGE -> {
                                                courier.moveTo((droppedEnergyID.pos))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
//Only returns a source if a Harvester is working on it
    private fun findACourierSource (sourceList: Array<SourceDataObject>): String{
        val couriers = Game.creeps.values.filter { it.memory.job == JobType.COURIER.name }
        for (source in sourceList) {
            if (source.currentHarvesterCreeps != 0) {
                val foundCourier: Boolean = couriers.any { it.memory.sourceIDAssignment == source.sourceID }
                if (!foundCourier) {
                    return source.sourceID
                }
            }
        }
        return ""
    }
}

//Return a Source ID



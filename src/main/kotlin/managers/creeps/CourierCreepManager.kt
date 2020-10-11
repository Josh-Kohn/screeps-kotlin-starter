package managers.creeps

import job.JobType
import memory.*
import objects.SourceDataObject
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer

class CourierCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager() {

    fun ferryEnergy(){
        for (courier in creeps){
            val homeRoom = Game.rooms[courier.memory.roomSpawnLocation]!!
            energyManagement(courier)
            if (courier.memory.fullOfEnergy){
                val spawnersAndExtensions = courier.pos.findClosestByRange(FIND_MY_STRUCTURES, options<FilterOption<Structure>> { filter = {
                    (it.structureType == STRUCTURE_SPAWN || it.structureType == STRUCTURE_EXTENSION)
                            && (it as StoreOwner).store.getFreeCapacity(RESOURCE_ENERGY) > 0
                } })
                if (spawnersAndExtensions != null){
                    courier.memory.depositID = spawnersAndExtensions.id
                } else {
                    val towers = homeRoom.find(FIND_MY_STRUCTURES, options { filter = {
                        it.structureType == STRUCTURE_TOWER
                                && (it as StoreOwner).store.getFreeCapacity(RESOURCE_ENERGY) > 500
                    } })
                    if (towers.isNotEmpty()){
                        courier.memory.depositID = towers[0].id
                    }
                }
                if(courier.memory.depositID.isBlank()){

                    for (constructionDataObject in Memory.constructionDataObjects){
                        if(constructionDataObject.roomOwner == homeRoom.name){
                            courier.memory.constructionSiteID = constructionDataObject.constructionSiteID
                            break
                        }
                    }
                    if (courier.memory.constructionSiteID.isBlank()){
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
                        val building = Game.getObjectById<ConstructionSite>(courier.memory.constructionSiteID)
                        if (building != null) {
                            if (courier.pos.inRangeTo(building.pos, 1)) {
                                courier.drop(RESOURCE_ENERGY)
                            } else {
                                courier.moveTo(building.pos)
                            }
                        } else {
                            courier.memory.constructionSiteID = ""
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
                            if (source.containerID != null) {
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
                                        } else {
                                            courier.memory.droppedID = ""
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



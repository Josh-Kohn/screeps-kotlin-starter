package managers.harvest
import managers.CreepStateManager
import managers.EnergyLocationManager
import memory.*
import screeps.api.*

/**
 * The instructions for how to harvest a source.  Pick an initial source, store in the creep's memory, and go harvest that source
 */

class CreepHarvestManager(private val creeps: List<Creep>): EnergyLocationManager, CreepStateManager() {

    private fun pickADepot(roomName: String): String? {

        //Find Spawners and Extensions that need energy
        val spawnsAndExtensions = Game.rooms[roomName]!!.find(FIND_STRUCTURES, options {
            filter = {
                (it.structureType == STRUCTURE_SPAWN || it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_TOWER)
                        && (it as StoreOwner).store.getFreeCapacity(RESOURCE_ENERGY) > 0
            }
        }) as Array<StoreOwner>
        spawnsAndExtensions.sortBy { (it as StoreOwner).store.getUsedCapacity(RESOURCE_ENERGY)}
        if (spawnsAndExtensions.isNotEmpty()){
            return spawnsAndExtensions[0].id
        }

        //Find containers and Storage that need energy
        val containersAndStorages = Game.rooms[roomName]!!.find(FIND_STRUCTURES, options {
            filter = {
                (it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE)
                        && (it as StoreOwner).store.getFreeCapacity(RESOURCE_ENERGY) > 0
            }
        }) as Array<StoreOwner>
        containersAndStorages.sortBy { (it as StoreOwner).store.getUsedCapacity(RESOURCE_ENERGY)}
        if (containersAndStorages.isNotEmpty()){
            return containersAndStorages[0].id
        }
        return null
    }


    fun harvestSource(){
        //If Creep has energy, go deposit it at a store owner (container, room storage, spawner, etc)
        for (creep in creeps){
            energyManagement(creep)
            if(creep.memory.fullOfEnergy){
                if (creep.memory.depositID.isBlank()){
                    val roomName = creep.memory.roomSpawnLocation
                    creep.memory.depositID = pickADepot(roomName) ?: ""
                    if (creep.memory.depositID.isBlank()){
                        val constructionSites = Game.rooms[creep.memory.roomSpawnLocation]!!.find(FIND_CONSTRUCTION_SITES)
                        if (constructionSites.isEmpty()) {
                            val roomController = creep.room.controller
                            when (creep.upgradeController(roomController!!)) {
                                ERR_NOT_IN_RANGE -> {
                                    creep.moveTo(roomController)
                                }
                            }
                            creep.upgradeController(roomController)
                        } else {
                            if (creep.memory.constructionSiteID.isBlank()) {
                                val constructionSiteID = constructionSites[0].id
                                creep.memory.constructionSiteID = constructionSiteID
                            } else {
                                val building = Game.getObjectById<ConstructionSite>(creep.memory.constructionSiteID)
                                //Game's getObjectById function gets you an object from the id you give it if the object exists (in this case, builder.memory.constructionSiteId)
                                if (building == null) {
                                    creep.memory.constructionSiteID = ""
                                } else {
                                    when (creep.build(building)) {
                                        ERR_NOT_IN_RANGE -> {
                                            creep.moveTo(building.pos)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val getDepot = Game.getObjectById<StoreOwner>(creep.memory.depositID)
                    if (getDepot != null) {
                        if (getDepot.store.getFreeCapacity(RESOURCE_ENERGY) == 0){
                            creep.memory.depositID = ""
                        } else {
                            when (creep.transfer(getDepot, RESOURCE_ENERGY)) {
                                ERR_NOT_IN_RANGE -> {
                                    creep.moveTo(getDepot)
                                }
                                ERR_FULL -> {
                                    creep.memory.depositID = ""
                                }
                            }
                        }
                    } else {
                        creep.memory.depositID = ""
                    }
                }
            } else{
                //Gets the creep to harvest source
                if (creep.memory.sourceIDAssignment.isBlank()){
                    val roomName = creep.memory.roomSpawnLocation
                    creep.memory.sourceIDAssignment = assignHarvesterToSourceID(roomName) ?: ""
                } else {
                    val getSource = Game.getObjectById<Source>(creep.memory.sourceIDAssignment)!!
                    when (creep.harvest(getSource)) {
                        ERR_NOT_IN_RANGE -> {
                            creep.moveTo(getSource)
                        }
                    }
                }
            }
        }
    }
}




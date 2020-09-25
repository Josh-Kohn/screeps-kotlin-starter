package memory

import job.JobType
import objects.ConstructionDataObject
import objects.RepairDataObject
import screeps.api.*
import screeps.api.structures.Structure

class MemoryUpdater {

    fun updateSourceMemory(memories: List<CreepMemory>, rooms: List<Room>) {
        for (memory in memories) {
            when (memory.job) {
                JobType.HARVESTER.name -> {
                    val deadCreepSource = memory.sourceIDAssignment
                    val deadCreepRoom = memory.roomSpawnLocation
                    for (room in rooms) {
                        if (deadCreepRoom == room.name) {
                            for (roomSource in room.memory.sources) {
                                if (deadCreepSource == roomSource.sourceID) {
                                    roomSource.currentHarvesterCreeps -= 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateConstructionMemory(myRooms: List<Room>){
        for (room in myRooms){
            if (!Memory.constructionDataObjects.any{ it.roomOwner == room.name}){
                //If we didn't find a match, then create a new entry
                Memory.constructionDataObjects = Memory.constructionDataObjects + ConstructionDataObject(room.name,"")
            }
            //Updates the constructionSiteID memory regardless
            for (constructionDataObject in Memory.constructionDataObjects){
                if (constructionDataObject.roomOwner == room.name){
                    if (Game.getObjectById<ConstructionSite>(constructionDataObject.constructionSiteID) == null){
                        val constructionSites = room.find(FIND_MY_CONSTRUCTION_SITES)
                        if (constructionSites.isNotEmpty()){
                            constructionDataObject.constructionSiteID = constructionSites[0].id
                        } else {
                            constructionDataObject.constructionSiteID = ""
                        }
                    }
                    break
                }
            }
        }
    }
    fun updateRepairMemory(myRooms: List<Room>){
        for (room in myRooms){
            if (!Memory.repairDataObjects.any{ it.roomOwner == room.name}){
                //If we didn't find a match, then create a new entry
                Memory.repairDataObjects = Memory.repairDataObjects + RepairDataObject(room.name,"")
            }
            //Updates the constructionSiteID memory regardless
            for (repairDataObject in Memory.repairDataObjects){
                if (repairDataObject.roomOwner == room.name){
                    val repairStructure = Game.getObjectById<Structure>(repairDataObject.repairID)
                    if ( repairStructure == null || repairStructure.hits == repairStructure.hitsMax){
                        val repairSites = room.find(FIND_STRUCTURES, options { filter = {
                            it.structureType != STRUCTURE_ROAD && it.structureType != STRUCTURE_CONTAINER && it.hits < it.hitsMax
                        }})
                        val sortedRepairSites = repairSites.sortedBy { it.hits }
                        if (sortedRepairSites.isNotEmpty()){
                            repairDataObject.repairID = sortedRepairSites[0].id
                        } else {
                            repairDataObject.repairID = ""
                        }
                    }
                    break
                }
            }
        }
    }
}
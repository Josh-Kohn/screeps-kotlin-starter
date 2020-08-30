import managers.harvest.CreepHarvestManager
import job.JobType
import managers.BuildCreepManager
import managers.InitiliazationManager
import managers.SpawningManager
import managers.UpgradeCreepManager
import memory.job
import memory.roomSpawnLocation
import memory.sourceIDAssignment
import memory.sources
import screeps.api.Game
import screeps.api.get
import screeps.api.*
import screeps.utils.unsafe.delete

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@Suppress("unused")
fun loop() {
    val myRoom = getMyRooms()
    InitiliazationManager(myRoom)

    val creepMemories = deleteCreepsFromMemory()
    updateSourceMemory(creepMemories,myRoom)

    for (room in myRoom){
        val spawnManager = SpawningManager()
        val findAJob = spawnManager.findJob(room)
        if(findAJob != JobType.IDLE.name) {
            val workerName = spawnManager.generateNewCreepNameByJobType(findAJob)
            val bodyPartList = spawnManager.getBodyByJob(findAJob, room)
            spawnManager.createACreep(bodyPartList.toTypedArray(), workerName, room.name, findAJob)
        }
    }

    val findHarvesterCreeps = findAllHarvesterCreeps()
    val creepHarvestManager = CreepHarvestManager(findHarvesterCreeps)
    creepHarvestManager.harvestSource()

    val buildCreepManager = BuildCreepManager(findAllBuilderCreeps())
    buildCreepManager.buildConstructionSites()

    val upgraderCreepManager = UpgradeCreepManager(findAllUpgraderCreeps())
    upgraderCreepManager.upgradeRoomController()

}

/**
 * Finds idle Creeps
 */
fun findAllIdleCreeps(): MutableList<Creep> {
    val idleCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == JobType.IDLE.name){
            idleCreeps.add(creep)
        }
    }
    return idleCreeps
}

/**
 * Gets my rooms
 */
fun getMyRooms(): MutableList<Room> {
    val myRooms: MutableList<Room> = mutableListOf()
    for (playerOwnedRoom in Game.rooms.values){
        val roomController = playerOwnedRoom.controller
        if (roomController != null){
            if (roomController.my == true) {
                myRooms.add(playerOwnedRoom)
            }
        }
    }
    return myRooms
}


/**
 * Finds all Harvester Creeps
 */
fun findAllHarvesterCreeps(): MutableList<Creep> {
    val harvesterCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == JobType.HARVESTER.name){
            harvesterCreeps.add(creep)
        }
    }
    return harvesterCreeps
}

/**
 * Finds all Builder Creeps
 */
fun findAllBuilderCreeps(): MutableList<Creep> {
    val builderCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == JobType.BUILDER.name){
            builderCreeps.add(creep)
        }
    }
    return builderCreeps
}

fun findAllUpgraderCreeps(): MutableList<Creep> {
    val builderCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == JobType.UPGRADER.name){
            builderCreeps.add(creep)
        }
    }
    return builderCreeps
}

/**
 * Compares Creeps in memory to game.creeps, isolates "dead" creeps lingering in memory and removes them
 */
fun deleteCreepsFromMemory():List<CreepMemory>{
    val deadCreepList = mutableListOf<CreepMemory>()
    for(deadCreep in Memory.creeps.keys){
        val creepCheck = Game.creeps[deadCreep]
        if (creepCheck == null){
            console.log("Deleting Creep $deadCreep")
            deadCreepList.add(Memory.creeps[deadCreep]!!)
            delete(Memory.creeps[deadCreep])
        }
    }
    return deadCreepList
}

/**
 * Find all creep counters in memory and updates them based on dead creeps
 */
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
            JobType.UPGRADER.name, JobType.BUILDER.name -> {
                if(memory.sourceIDAssignment.isNotBlank()){
                    val deadCreepSource = memory.sourceIDAssignment
                    val deadCreepRoom = memory.roomSpawnLocation
                    for (room in rooms) {
                        if (deadCreepRoom == room.name) {
                            for (roomSource in room.memory.sources) {
                                if (deadCreepSource == roomSource.sourceID) {
                                    roomSource.freeCreepSlot = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun needHarvesterCreep(currentRoom: Room):Boolean {
    val memories = currentRoom.memory.sources
    for (sourceMemory in memories){
        if (sourceMemory.currentHarvesterCreeps < sourceMemory.maxHarvesterCreeps){
            return true
        }
    }
    return false
}


import managers.harvest.CreepHarvestManager
import job.JobType
import managers.*
import memory.*
import screeps.api.Game
import screeps.api.get
import screeps.api.*
import screeps.api.structures.StructureTower
import screeps.utils.unsafe.delete

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@Suppress("unused")
fun loop() {
    val myRooms = getMyRooms()
    val initializationManager = InitializationManager(myRooms)
    initializationManager.sourceContainerAssociation()


    val updateMemory= MemoryUpdater()
    val creepMemories = deleteCreepsFromMemory()
    updateMemory.updateSourceMemory(creepMemories,myRooms)
    updateMemory.updateConstructionMemory(myRooms)

    for (room in myRooms){
        val spawnManager = SpawningManager()
        val findAJob = spawnManager.findJob(room)
        if(findAJob != JobType.IDLE.name) {
            val workerName = spawnManager.generateNewCreepNameByJobType(findAJob)
            val bodyPartList = spawnManager.getBodyByJob(findAJob, room)
            spawnManager.createACreep(bodyPartList.toTypedArray(), workerName, room.name, findAJob)
        }
    }

    val findHarvesterCreeps = findAllCreepsByJobType(JobType.HARVESTER.name)
    val creepHarvestManager = CreepHarvestManager(findHarvesterCreeps)
    creepHarvestManager.harvestSource()

    val findCourierCreeps = findAllCreepsByJobType(JobType.COURIER.name)
    CourierCreepManager(findCourierCreeps).ferryEnergy()

    val buildCreepManager = BuildCreepManager(findAllCreepsByJobType(JobType.BUILDER.name))
    buildCreepManager.buildConstructionSites()

    val upgraderCreepManager = UpgradeCreepManager(findAllCreepsByJobType(JobType.UPGRADER.name))
    upgraderCreepManager.upgradeRoomController()

    if (Game.time == 20000){
        console.log("Room Controller Level at ${Game.rooms["sim"]!!.controller!!.level}")
        console.log("Current Progress at ${Game.rooms["sim"]!!.controller!!.progress}")
        Game.notify("Game Controller at ${Game.rooms["sim"]!!.controller!!.level} and Current Progress at ${Game.rooms["sim"]!!.controller!!.progress}")
    }

    val towerManager = TowerManager(findTowers(myRooms))
    if (!towerManager.towerDefenseProtocol()) {
        towerManager.towerRepairProtocol(myRooms, findTowers(myRooms))
    }
    // Pixels! If we have enough banked CPU. CPU bucket max is 10k
    if (Game.cpu.bucket >= 10000 - 100) {
        console.log("Generating Pixel")
        Game.cpu.generatePixel()
    }
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
 * Finds all Creeps by JobType
 */

fun findAllCreepsByJobType(jobType: String): MutableList<Creep> {
    val creeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == jobType){
            creeps.add(creep)
        }
    }
    return creeps
}


/**
 * Compares Creeps in memory to game.creeps, isolates "dead" creeps lingering in memory and removes them
 */
fun deleteCreepsFromMemory():List<CreepMemory>{
    if (Game.creeps.values.isEmpty()) return listOf()

    val deadCreepList = mutableListOf<CreepMemory>()
    for((deadCreep,_) in Memory.creeps){
        val creepCheck = Game.creeps[deadCreep]
        if (creepCheck == null){
            deadCreepList.add(Memory.creeps[deadCreep]!!)
            delete(Memory.creeps[deadCreep])
        }
    }
    return deadCreepList
}

/**
 * Find all creep counters in memory and updates them based on dead creeps
 */


fun findTowers(myRooms: MutableList<Room>): List<StructureTower>{
    val towers = mutableListOf<StructureTower>()
    for(room in myRooms) {
        val foundTowers = room.find(FIND_MY_STRUCTURES).filter { (it.structureType == STRUCTURE_TOWER) }
        towers.addAll(foundTowers as List<StructureTower>)
    }
    return towers
}


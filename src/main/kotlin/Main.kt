import job.JobType
import managers.*
import managers.creeps.*
import managers.military.MilitaryCreepManager
import managers.structure.SpawningManager
import managers.structure.TowerManager
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

    val claimFlag = Game.flags["cf"]

    // Only do if the cf flag exists
    if (claimFlag != null) {
        val claimRoom = Game.rooms[claimFlag.pos.roomName]

        if (claimRoom != null && claimRoom.controller!!.my) {
            // We can see into the room and the controller is now ours. Stop doing stuff.
        }

        // We either can't see into the room or we can and the controller isn't ours, do stuff.
        else {
            val claimCreep = Game.creeps["cc"]

            // Create this creep if he doesn't exist
            if (claimCreep == null) Game.spawns["Spawn1"]?.spawnCreep(arrayOf(CLAIM, CLAIM, MOVE, MOVE), "cc")

            // The creep exists, go attack controller or claim it
            else {
                if (claimRoom == null) claimCreep.moveTo(claimFlag)
                else {
                    when (claimCreep.claimController(claimRoom.controller!!)) {
                        ERR_NOT_IN_RANGE -> claimCreep.moveTo(claimRoom.controller!!)
                        else -> claimCreep.attackController(claimRoom.controller!!)
                    }
                }
            }
        }
    }



    val myRooms = getMyRooms()
    val initializationManager = InitializationManager(myRooms)
    initializationManager.sourceContainerAssociation()


    val updateMemory= MemoryUpdater()
    val creepMemories = deleteCreepsFromMemory()
    updateMemory.updateSourceMemory(creepMemories,myRooms)
    updateMemory.updateConstructionMemory(myRooms)
    updateMemory.updateRepairMemory(myRooms)

    for (room in myRooms){
        val spawnManager = SpawningManager()
        val findAJob = spawnManager.findJob(room)
        if(findAJob != JobType.IDLE.name) {
            val workerName = spawnManager.generateNewCreepNameByJobType(findAJob, room.name)

            val numberOfCouriers = findAllCreepsByJobTypeInRoom(JobType.COURIER.name, room.name).size
            val energyToUse = if (numberOfCouriers < 1){
                300
            } else {
                room.energyCapacityAvailable
            }

            val bodyPartList = spawnManager.getBodyByJob(findAJob, energyToUse, room)
            spawnManager.createACreep(bodyPartList.toTypedArray(), workerName, room.name, findAJob)
        }
    }

    val findHarvesterCreeps = findAllCreepsByJobType(JobType.HARVESTER.name)
    val creepHarvestManager = HarvestCreepManager(findHarvesterCreeps)
    creepHarvestManager.harvestSource()

    val findCourierCreeps = findAllCreepsByJobType(JobType.COURIER.name)
    CourierCreepManager(findCourierCreeps).ferryEnergy()

    val findJanitorCreeps = findAllCreepsByJobType(JobType.JANITOR.name)
    JanitorCreepManager(findJanitorCreeps).cleanUpEnergy()

    val buildCreepManager = BuildCreepManager(findAllCreepsByJobType(JobType.BUILDER.name))
    buildCreepManager.buildConstructionSites()

    val upgraderCreepManager = UpgradeCreepManager(findAllCreepsByJobType(JobType.UPGRADER.name))
    upgraderCreepManager.upgradeRoomController()

    val repairCreepManager = RepairCreepManager(findAllCreepsByJobType(JobType.REPAIRMAN.name))
    repairCreepManager.repairStructures()

    val militaryCreepManager = MilitaryCreepManager(findAllCreepsByJobType(listOf(JobType.CAPTAIN.name, JobType.TANK.name, JobType.HEALER.name, JobType.RANGER.name)))
    militaryCreepManager.militaryOrders()




    if (Game.time == 20000){
        console.log("Room Controller Level at ${Game.rooms["sim"]!!.controller!!.level}")
        console.log("Current Progress at ${Game.rooms["sim"]!!.controller!!.progress}")
        Game.notify("Game Controller at ${Game.rooms["sim"]!!.controller!!.level} and Current Progress at ${Game.rooms["sim"]!!.controller!!.progress}")
    }

    val towerManager = TowerManager(findTowers(myRooms))
    if (!towerManager.towerDefenseProtocol()) {
        towerManager.towerRepairProtocol()
    }
    // Pixels! If we have enough banked CPU. CPU bucket max is 10k
    if (Game.cpu.bucket >= 10000 - 100) {
        console.log("Generating Pixel")
        Game.cpu.generatePixel()
    }
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

fun findAllCreepsByJobType(jobTypes: List<String>): MutableList<Creep> {
    val creeps: MutableList<Creep> = mutableListOf()
    for (creep in Game.creeps.values){
        if(jobTypes.any { it == creep.memory.job }){
            creeps.add(creep)
        }
    }
    return creeps
}

fun findAllCreepsByJobTypeInRoom(jobType: String, room: String): MutableList<Creep> {
    val creeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == jobType && creep.memory.roomSpawnLocation == room){
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


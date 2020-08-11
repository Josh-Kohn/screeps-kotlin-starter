import managers.harvest.CreepHarvestManager
import job.Jobtype
import managers.InitiliazationManager
import memory.job
import memory.roomSpawnLocation
import screeps.api.Game
import screeps.api.get
import screeps.api.*
import screeps.utils.unsafe.jsObject

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

    val workerName = increaseWorkerNameNumber()
    val workParameters: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)
    createAWorkerCreep(workParameters, workerName)

    val findIdleCreeps = findAllIdleCreeps()
    assignIdleCreepsToHarvesterJob(findIdleCreeps)
    val findHarvesterCreeps = findAllHarvesterCreeps()
    val creepHarvestManager = CreepHarvestManager(findHarvesterCreeps)
    creepHarvestManager.harvestSource()
}

/**
 * Checks to see if worker is null and increases worker number
 */
fun increaseWorkerNameNumber(): String {
    var workerNumber = 1
    while (true) {
        val workerName = "Worker $workerNumber"
        val nameChecker: Creep? = Game.creeps[workerName]
        if (nameChecker == null) {
            return workerName
        }
        else{
            workerNumber += 1
        }
    }
}

/**
 * Checks if spawners are available to spawn worker creeps.  Spawns a creep if possible.
 */
fun createAWorkerCreep(workParameter: Array<BodyPartConstant>, workerName: String) {
    val allSpawners = Game.spawns.values
    for (spawner in allSpawners){
        if (spawner.spawning == null) {
              spawner.spawnCreep(workParameter, workerName, options { memory = jsObject<CreepMemory> {
                  this.roomSpawnLocation = spawner.pos.roomName
              }})
            break
        }
    }
}

/**
 * Finds idle Creeps
 */
fun findAllIdleCreeps(): MutableList<Creep> {
    val idleCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == Jobtype.IDLE.name){
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
 * Assign Idle Creeps to Harvester
 */
fun assignIdleCreepsToHarvesterJob(idleCreeps: List<Creep>) {
    for (unemployedCreeps in idleCreeps){
        unemployedCreeps.memory.job = Jobtype.HARVESTER.name
    }
}

/**
 * Finds all Harvester Creeps
 */
fun findAllHarvesterCreeps(): MutableList<Creep> {
    val harvesterCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == Jobtype.HARVESTER.name){
            harvesterCreeps.add(creep)
        }
    }
    return harvesterCreeps
}


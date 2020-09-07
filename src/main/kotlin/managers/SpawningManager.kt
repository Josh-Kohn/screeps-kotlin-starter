package managers

import job.JobType
import memory.job
import memory.roomSpawnLocation
import memory.sources
import screeps.api.*
import screeps.api.Game.constructionSites
import screeps.utils.isNotEmpty
import screeps.utils.unsafe.jsObject

class SpawningManager {
    /**
     * Checks if spawners are available to spawn worker creeps.  Spawns a creep if possible.
     */
    fun createACreep(workParameter: Array<BodyPartConstant>, workerName: String, roomName: String, jobType: String) {
        val allSpawners = Game.spawns.values.filter { it.pos.roomName ==  roomName}
        for (spawner in allSpawners){
            if (spawner.spawning == null) {
                spawner.spawnCreep(workParameter, workerName, options { memory = jsObject<CreepMemory> {
                    this.roomSpawnLocation = spawner.pos.roomName
                    this.job = jobType
                    console.log("Creating Creep with body $workParameter")
                }
                })
                break
            }
        }
    }

    /**
     * With the given maxEnergy as the cap, attempt to create a creep body that uses the given ratios of work, carry, and move
     * up to the maximum specified for each body part. If no body could be generate at all, return a W1:C1:M2 body instead.
     */
    private fun generateBodyByRatio(
            maxEnergy: Int,
            maxWork: Int, workRatio: Int,
            maxCarry: Int, carryRatio: Int,
            maxMove: Int, moveRatio: Int
    ): List<BodyPartConstant> {

        // Figure out the max parts we can have for the max energy we can use
        var workParts = 0
        var carryParts = 0
        var moveParts = 0
        var energyUsed = 0

        // If a ratio was 0, and max was 1, add 1
        if (maxWork == 1 && workRatio == 0) {
            workParts = 1
            energyUsed += BODYPART_COST[WORK]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxCarry == 1 && carryRatio == 0) {
            carryParts = 1
            energyUsed += BODYPART_COST[CARRY]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxMove == 1 && moveRatio == 0) {
            moveParts = 1
            energyUsed += BODYPART_COST[MOVE]!!
        }

        var nextRunEnergyUsed = 0

        while(nextRunEnergyUsed <= maxEnergy
                && (((workParts + workRatio <= maxWork && workRatio != 0))
                        || ((carryParts + carryRatio <= maxCarry && carryRatio != 0))
                        || ((moveParts + moveRatio <= maxMove && moveRatio != 0)))) {

            if (workParts + workRatio <= maxWork) {
                workParts += workRatio
                energyUsed += (workRatio * BODYPART_COST[WORK]!!)
            }

            if (carryParts + carryRatio <= maxCarry) {
                carryParts += carryRatio
                energyUsed += (carryRatio * BODYPART_COST[CARRY]!!)
            }

            if (moveParts + moveRatio <= maxMove) {
                moveParts += moveRatio
                energyUsed += (moveRatio * BODYPART_COST[MOVE]!!)
            }

            // Stage the results of the potential next run as the while loop's test
            nextRunEnergyUsed = energyUsed
            if (workParts + workRatio <= maxWork) nextRunEnergyUsed += (workRatio * BODYPART_COST[WORK]!!)
            if (carryParts + carryRatio <= maxCarry) nextRunEnergyUsed += (carryRatio * BODYPART_COST[CARRY]!!)
            if (moveParts + moveRatio <= maxMove) nextRunEnergyUsed += (moveRatio * BODYPART_COST[MOVE]!!)
        }


        // This shouldn't ever happen!
        if (workParts < 0 || carryParts < 0 || moveParts < 0) {
            return listOf(WORK, CARRY, MOVE, MOVE)
        }

        // Create body using above values
        val body = arrayListOf<BodyPartConstant>()

        while (workParts > 0) {
            body.add(WORK)
            workParts--
        }
        while (carryParts > 0) {
            body.add(CARRY)
            carryParts--
        }
        while (moveParts > 0) {
            body.add(MOVE)
            moveParts--
        }

        return body
    }

    fun getBodyByJob(creepJob: String, currentRoom: Room): List<BodyPartConstant>{
        val maxWork: Int
        val maxCarry: Int
        val maxMove: Int
        val carryRatio: Int
        val moveRatio: Int
        val workRatio: Int

        when (creepJob){
            JobType.HARVESTER.name -> {
                maxWork = 3
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.COURIER.name -> {
                maxWork = 0
                maxCarry = 2
                maxMove = 2
                workRatio = 0
                carryRatio = 1
                moveRatio = 1
            }
            JobType.UPGRADER.name -> {
                maxWork = 2
                maxCarry = 2
                maxMove = 2
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
            }
            JobType.BUILDER.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 2
                workRatio = 1
                carryRatio = 1
                moveRatio = 2
            }
            else -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                carryRatio = 1
                moveRatio = 1
                workRatio = 1
            }
        }
        //TODO If check here for generating a creep under the condition that no creeps are present
        return generateBodyByRatio(maxEnergy = currentRoom.energyCapacityAvailable,
                maxWork = maxWork,
                maxCarry = maxCarry,
                maxMove = maxMove,
                carryRatio = carryRatio,
                moveRatio = moveRatio,
                workRatio = workRatio)

    }

    /**
     * Finds a job that needs doing
     */
    fun findJob(currentRoom: Room): String {
        //Write an if check to see if we have any harvester and courier creeps in the room
        val roomCreeps = currentRoom.find(FIND_MY_CREEPS)
        val harvestCreeps = roomCreeps.filter { it.memory.job == JobType.HARVESTER.name}
        val courierCreeps = roomCreeps.filter { it.memory.job == JobType.COURIER.name}
        when{
            harvestCreeps.isEmpty() && courierCreeps.isEmpty() -> {
                return JobType.HARVESTER.name
            }
            harvestCreeps.isNotEmpty() && courierCreeps.isEmpty() -> {
                return JobType.COURIER.name
            }
            harvestCreeps.isEmpty() && courierCreeps.isNotEmpty() -> {
                return JobType.HARVESTER.name
            }
            else -> {
                val memories = currentRoom.memory.sources
                for (sourceMemory in memories) {
                    if (sourceMemory.currentHarvesterCreeps < sourceMemory.maxHarvesterCreeps) {
                        console.log("Harvester Needed")
                        return JobType.HARVESTER.name
                    }
                }
                val couriersNeeded = memories.size + 1
                //+ 1 because of room controller
                if (courierCreeps.size < couriersNeeded) {
                    return JobType.COURIER.name
                }
                val activeUpgrader = roomCreeps.filter { it.memory.job == JobType.UPGRADER.name }
                if (activeUpgrader.isEmpty()) {
                    console.log("Upgrader Needed")
                    return JobType.UPGRADER.name
                }
                val constructionSites = currentRoom.find(FIND_MY_CONSTRUCTION_SITES)
                if (constructionSites.isNotEmpty()) {
                    val activeBuilders = roomCreeps.filter { it.memory.job == JobType.BUILDER.name }
                    if (activeBuilders.size < 2) {
                        console.log("Builder Needed")
                        return JobType.BUILDER.name
                    }
                }
            }
        }
        return JobType.IDLE.name
    }

    /**
     * Checks to see if worker is null and increases worker number
     */
    fun generateNewCreepNameByJobType(jobName: String): String {
        var workerNumber = 1
        while (true) {
            val workerName = "$jobName $workerNumber"
            val nameChecker: Creep? = Game.creeps[workerName]
            if (nameChecker == null) {
                return workerName
            }
            else{
                workerNumber += 1
            }
        }
    }
}
package managers.structure

import job.JobType
import memory.*
import screeps.api.*
import screeps.utils.unsafe.jsObject
import util.maxWorkPerSource
import util.workPerSource

class SpawningManager {
    /**
     * Checks if spawners are available to spawn worker creeps.  Spawns a creep if possible.
     */
    fun createACreep(workParameter: Array<BodyPartConstant>, workerName: String, roomName: String, jobType: String) {
        val allSpawners = Game.spawns.values.filter { it.pos.roomName == roomName }
        for (spawner in allSpawners) {
            if (spawner.spawning == null) {
                spawner.spawnCreep(workParameter, workerName, options {
                    memory = jsObject<CreepMemory> {
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
     * up to the maximum specified for each body part. If no body could be generated at all, return a W1:C1:M2 body instead.
     * If the given max was 1 and the ratio was 0, then add one body part and a corresponding move part.
     *
     * Example creep body generation.
     * Assuming that these parameters are given to the generator:
     * maxWork = 6, workRatio = 1
     * maxCarry = 1, carryRatio = 0
     * maxMove = 7, moveRatio = 1
     *
     * With maxEnergy = 550 (RCL 2 + extensions) the generated creep is [WORK, WORK, WORK, CARRY, MOVE, MOVE, MOVE, MOVE].
     * A total of 550 energy spent. This is because a 1:1 WORK:MOVE ratio adds 1 work and 1 move together, and the 1
     * carry and 0 carry ratio together forces a single carry and move to be added without further attempting more
     * calculations.
     *
     * With maxEnergy = 800 (RCL 3 + extensions) the generated creep is [WORK, WORK, WORK, WORK, CARRY, MOVE, MOVE,
     * MOVE, MOVE, MOVE], a total of 700 energy spent. It doesn't reach 800 energy spent because the ratios are
     * 1:1 WORK:MOVE, meaning that adding one more WORK (100 energy) forces another MOVE to be added (50 energy) and the
     * resulting body would be over the given max of 800.
     *
     * Body Parts are added to the creep in the following order:
     * TOUGH -> WORK -> CARRY -> CLAIM -> ATTACK -> RANGED_ATTACK -> HEAL -> MOVE
     */
    fun generateBodyByRatio(
            maxEnergy: Int,
            maxWork: Int = 0, workRatio: Int = 0,
            maxCarry: Int = 0, carryRatio: Int = 0,
            maxClaim: Int = 0, claimRatio: Int = 0,
            maxAttack: Int = 0, attackRatio: Int = 0,
            maxRangedAttack: Int = 0, rangedAttackRatio: Int = 0,
            maxTough: Int = 0, toughRatio: Int = 0,
            maxHeal: Int = 0, healRatio: Int = 0,
            maxMove: Int = 0, moveRatio: Int = 0
    ): List<BodyPartConstant> {

        // Figure out the max parts we can have for the max energy we can use
        var workParts = 0
        var carryParts = 0
        var claimParts = 0
        var attackParts = 0;
        var rangedAttackParts = 0;
        var toughParts = 0;
        var healParts = 0;
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
        if (maxClaim == 1 && claimRatio == 0) {
            claimParts = 1
            energyUsed += BODYPART_COST[CLAIM]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxAttack == 1 && attackRatio == 0) {
            attackParts = 1
            energyUsed += BODYPART_COST[ATTACK]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxRangedAttack == 1 && rangedAttackRatio == 0) {
            rangedAttackParts = 1
            energyUsed += BODYPART_COST[RANGED_ATTACK]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxTough == 1 && toughRatio == 0) {
            toughParts = 1
            energyUsed += BODYPART_COST[TOUGH]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxHeal == 1 && healRatio == 0) {
            healParts = 1
            energyUsed += BODYPART_COST[HEAL]!!
            moveParts += 1
            energyUsed += BODYPART_COST[MOVE]!!
        }
        if (maxMove == 1 && moveRatio == 0) {
            moveParts = 1
            energyUsed += BODYPART_COST[MOVE]!!
        }

        var nextRunEnergyUsed = 0

        // Stage the results of the potential next run as the while loop's test
        nextRunEnergyUsed = energyUsed
        if (workParts + workRatio <= maxWork) nextRunEnergyUsed += (workRatio * BODYPART_COST[WORK]!!)
        if (carryParts + carryRatio <= maxCarry) nextRunEnergyUsed += (carryRatio * BODYPART_COST[CARRY]!!)
        if (claimParts + claimRatio <= maxClaim) nextRunEnergyUsed += (claimRatio * BODYPART_COST[CLAIM]!!)
        if (attackParts + attackRatio <= maxAttack) nextRunEnergyUsed += (attackRatio * BODYPART_COST[ATTACK]!!)
        if (rangedAttackParts + rangedAttackRatio <= maxRangedAttack) nextRunEnergyUsed += (rangedAttackRatio * BODYPART_COST[RANGED_ATTACK]!!)
        if (toughParts + toughRatio <= maxTough) nextRunEnergyUsed += (toughRatio * BODYPART_COST[TOUGH]!!)
        if (healParts + healRatio <= maxHeal) nextRunEnergyUsed += (healRatio * BODYPART_COST[HEAL]!!)
        if (moveParts + moveRatio <= maxMove) nextRunEnergyUsed += (moveRatio * BODYPART_COST[MOVE]!!)
        while (nextRunEnergyUsed <= maxEnergy
                && (((workParts + workRatio <= maxWork && workRatio != 0))
                        || ((carryParts + carryRatio <= maxCarry && carryRatio != 0))
                        || ((claimParts + claimRatio <= maxClaim && claimRatio != 0))
                        || ((attackParts + attackRatio <= maxAttack && attackRatio != 0))
                        || ((rangedAttackParts + rangedAttackRatio <= maxRangedAttack && rangedAttackRatio != 0))
                        || ((toughParts + toughRatio <= maxTough && toughRatio != 0))
                        || ((healParts + healRatio <= maxHeal && healRatio != 0))
                        || ((moveParts + moveRatio <= maxMove && moveRatio != 0)))) {

            if (workParts + workRatio <= maxWork) {
                workParts += workRatio
                energyUsed += (workRatio * BODYPART_COST[WORK]!!)
            }

            if (carryParts + carryRatio <= maxCarry) {
                carryParts += carryRatio
                energyUsed += (carryRatio * BODYPART_COST[CARRY]!!)
            }

            if (claimParts + claimRatio <= maxClaim) {
                claimParts += claimRatio
                energyUsed += (claimRatio * BODYPART_COST[CLAIM]!!)
            }

            if (attackParts + attackRatio <= maxAttack) {
                attackParts += attackRatio
                energyUsed += (attackRatio * BODYPART_COST[ATTACK]!!)
            }

            if (rangedAttackParts + rangedAttackRatio <= maxRangedAttack) {
                rangedAttackParts += rangedAttackRatio
                energyUsed += (rangedAttackRatio * BODYPART_COST[RANGED_ATTACK]!!)
            }

            if (toughParts + toughRatio <= maxTough) {
                toughParts += toughRatio
                energyUsed += (toughRatio * BODYPART_COST[TOUGH]!!)
            }

            if (healParts + healRatio <= maxHeal) {
                healParts += healRatio
                energyUsed += (healRatio * BODYPART_COST[HEAL]!!)
            }

            if (moveParts + moveRatio <= maxMove) {
                moveParts += moveRatio
                energyUsed += (moveRatio * BODYPART_COST[MOVE]!!)
            }

            // Stage the results of the potential next run as the while loop's test
            nextRunEnergyUsed = energyUsed
            if (workParts + workRatio <= maxWork) nextRunEnergyUsed += (workRatio * BODYPART_COST[WORK]!!)
            if (carryParts + carryRatio <= maxCarry) nextRunEnergyUsed += (carryRatio * BODYPART_COST[CARRY]!!)
            if (claimParts + claimRatio <= maxClaim) nextRunEnergyUsed += (claimRatio * BODYPART_COST[CLAIM]!!)
            if (attackParts + attackRatio <= maxAttack) nextRunEnergyUsed += (attackRatio * BODYPART_COST[ATTACK]!!)
            if (rangedAttackParts + rangedAttackRatio <= maxRangedAttack) nextRunEnergyUsed += (rangedAttackRatio * BODYPART_COST[RANGED_ATTACK]!!)
            if (toughParts + toughRatio <= maxTough) nextRunEnergyUsed += (toughRatio * BODYPART_COST[TOUGH]!!)
            if (healParts + healRatio <= maxHeal) nextRunEnergyUsed += (healRatio * BODYPART_COST[HEAL]!!)
            if (moveParts + moveRatio <= maxMove) nextRunEnergyUsed += (moveRatio * BODYPART_COST[MOVE]!!)
        }


        // This shouldn't ever happen!
        if (workParts < 0 || carryParts < 0 || claimParts < 0 || attackParts < 0 || rangedAttackParts < 0 || toughParts < 0 || healParts < 0 || moveParts < 0) {
            return listOf(WORK, CARRY, MOVE, MOVE)
        }

        // Create body using above values
        val body = arrayListOf<BodyPartConstant>()

        // Tough first, so that it's the first part to get attacked by enemies
        while (toughParts > 0) {
            body.add(TOUGH)
            toughParts--
        }
        while (workParts > 0) {
            body.add(WORK)
            workParts--
        }
        while (carryParts > 0) {
            body.add(CARRY)
            carryParts--
        }
        while (claimParts > 0) {
            body.add(CLAIM)
            claimParts--
        }
        while (attackParts > 0) {
            body.add(ATTACK)
            attackParts--
        }
        while (rangedAttackParts > 0) {
            body.add(RANGED_ATTACK)
            rangedAttackParts--
        }
        while (healParts > 0) {
            body.add(HEAL)
            healParts--
        }
        while (moveParts > 0) {
            body.add(MOVE)
            moveParts--
        }

        return body
    }

    fun getBodyByJob(creepJob: String, energyToUse: Int): List<BodyPartConstant> {
        val maxWork: Int
        val maxCarry: Int
        val maxMove: Int
        val carryRatio: Int
        val moveRatio: Int
        val workRatio: Int

        when (creepJob) {
            JobType.HARVESTER.name -> {
                maxWork = 6
                maxCarry = 1
                maxMove = 3
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.COURIER.name -> {
                maxWork = 0
                maxCarry = 6
                maxMove = 6
                workRatio = 0
                carryRatio = 1
                moveRatio = 1
            }
            JobType.UPGRADER.name -> {
                maxWork = 6
                maxCarry = 3
                maxMove = 3
                workRatio = 3
                carryRatio = 1
                moveRatio = 1
            }
            JobType.BUILDER.name -> {
                maxWork = 3
                maxCarry = 1
                maxMove = 2
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
            }
            JobType.JANITOR.name -> {
                maxWork = 0
                maxCarry = 6
                maxMove = 6
                workRatio = 0
                carryRatio = 1
                moveRatio = 1
            }
            JobType.REPAIRMAN.name -> {
                maxWork = 4
                maxCarry = 4
                maxMove = 8
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
        return generateBodyByRatio(maxEnergy = energyToUse,
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
        val harvestCreeps = roomCreeps.filter { it.memory.job == JobType.HARVESTER.name }
        val courierCreeps = roomCreeps.filter { it.memory.job == JobType.COURIER.name }
        when {
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
                val sources = currentRoom.memory.sources
                for (sourceMemory in sources) {
                    if (sourceMemory.currentHarvesterCreeps < sourceMemory.maxHarvesterCreeps) {
                        if (workPerSource(sourceMemory) < maxWorkPerSource) {
                            console.log("Harvester Needed")
                            return JobType.HARVESTER.name
                        }
                    }
                }
                val couriersNeeded = sources.size
                if (courierCreeps.size < couriersNeeded) {
                    console.log("Courier Needed")
                    return JobType.COURIER.name
                }
                val janitorNeeded = roomCreeps.filter { it.memory.job == JobType.JANITOR.name }
                if (janitorNeeded.isEmpty() && currentRoom.storage != null) {
                    console.log("Janitor Needed")
                    return JobType.JANITOR.name
                }
                val repairManNeeded = roomCreeps.filter { it.memory.job == JobType.REPAIRMAN.name }
                if (repairManNeeded.isEmpty() && currentRoom.storage != null) {
                    val repairDataObject = Memory.repairDataObjects.find { currentRoom.name == it.roomOwner }
                    if (repairDataObject != null){
                        repairDataObject.repairID = ""
                    }
                    console.log("Repairman man man man...")
                    return JobType.REPAIRMAN.name
                }
                val activeUpgrader = roomCreeps.filter { it.memory.job == JobType.UPGRADER.name }
                if (activeUpgrader.size < 2) {
                    console.log("Upgrader Needed")
                    return JobType.UPGRADER.name
                }
                var constructionSiteID = ""
                for (constructionDataObject in Memory.constructionDataObjects) {
                    if (constructionDataObject.roomOwner == currentRoom.name) {
                        constructionSiteID = constructionDataObject.constructionSiteID
                        break
                    }
                }
                if (constructionSiteID.isNotBlank()) {
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
            val workerName = "${jobName.toLowerCase()} $workerNumber"
            val nameChecker: Creep? = Game.creeps[workerName]
            if (nameChecker == null) {
                return workerName
            } else {
                workerNumber += 1
            }
        }
    }
}
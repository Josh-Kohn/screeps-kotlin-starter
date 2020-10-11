package managers.military

import job.JobType
import memory.job
import memory.roomSpawnLocation
import screeps.api.*
import screeps.utils.unsafe.jsObject

class MilitarySpawningManager {

    fun militaryGenerateNewCreepNameByJobType(jobName: String): String {
        var militaryNumber = 1
        while (true) {
            val soldierName = "${jobName.toLowerCase()} $militaryNumber"
            val nameChecker: Creep? = Game.creeps[soldierName]
            if (nameChecker == null) {
                return soldierName
            } else {
                militaryNumber += 1
            }
        }
    }

    fun generateMilitaryBodyByRatio(
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

    fun getBodyByMilitaryJob(creepJob: String, energyToUse: Int): List<BodyPartConstant> {
        val maxWork: Int
        val maxCarry: Int
        val maxMove: Int
        val carryRatio: Int
        val moveRatio: Int
        val workRatio: Int

        when (creepJob) {
            JobType.CAPTAIN.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.TANK.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.SCOUT.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.HEALER.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.RANGER.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
            }
            JobType.SENTINEL.name -> {
                maxWork = 1
                maxCarry = 1
                maxMove = 1
                workRatio = 1
                carryRatio = 1
                moveRatio = 1
                //Max efficiency is 5 work on a source
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
        val generateBodyByRatio = generateMilitaryBodyByRatio(maxEnergy = energyToUse,
                maxWork = maxWork,
                maxCarry = maxCarry,
                maxMove = maxMove,
                carryRatio = carryRatio,
                moveRatio = moveRatio,
                workRatio = workRatio)

        return if (generateBodyByRatio.isEmpty()){
            listOf<BodyPartConstant>(MOVE, CARRY, WORK)
            //TODO find alternative return
        } else {
            generateBodyByRatio
        }
    }

    fun militaryCreateACreep(workParameter: Array<BodyPartConstant>, workerName: String, roomName: String, jobType: String) {
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

}
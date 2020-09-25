package managers

import getMyRooms
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower
import kotlin.js.Console

class TowerManager(private val towers:List<StructureTower>) {

    fun towerDefenseProtocol(): Boolean {
        var foundEnemies = false
        for (tower in towers) {
            val hostiles = tower.room.find(FIND_HOSTILE_CREEPS)
            if (hostiles.isNotEmpty()) {
                val userName = hostiles[0].owner.username
                Game.notify("User $userName spotted in room ${tower.room}")
                tower.attack(hostiles[0])
                foundEnemies = true
            }
        }
        return foundEnemies
    }

    fun towerRepairProtocol(){
        for (tower in towers){
            val towerRoom = Game.rooms[tower.pos.roomName]!!
            val damagedGoods = towerRoom.find(FIND_STRUCTURES).filter {
                ((it.structureType != STRUCTURE_RAMPART || (it.structureType == STRUCTURE_RAMPART && it.hits < 5000))
                        && it.structureType != STRUCTURE_WALL
                        && it.structureType != STRUCTURE_CONTROLLER)
                        && it.hits < it.hitsMax
            }
            if (damagedGoods.isNotEmpty()) {
                damagedGoods.sortedBy { it.hits }
                tower.repair(damagedGoods[0])
            }
        }
    }
}

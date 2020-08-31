package managers

import getMyRooms
import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower

class TowerManager(private val towers:List<StructureTower>) {

    fun towerDefenseProtocol(): Boolean {
        for (tower in towers) {
            val hostiles = tower.room.find(FIND_HOSTILE_CREEPS)
            if (hostiles.isNotEmpty()) {
                val userName = hostiles[0].owner.username
                Game.notify("User $userName spotted in room ${tower.room}")
                tower.attack(hostiles[0])
            }
        }
        return false
    }

    fun towerRepairProtocol(myRooms: MutableList<Room>, towers: List<StructureTower>){
        for (room in myRooms){
            val damagedGoods = room.find(FIND_MY_STRUCTURES).filter { it.structureType != STRUCTURE_RAMPART || it.structureType != STRUCTURE_WALL  }
            damagedGoods.sortedBy { it.hits }
            for (tower in towers){
                if (tower.room.name == room.name){
                    tower.repair(damagedGoods[0])
                }
            }
        }
    }
}

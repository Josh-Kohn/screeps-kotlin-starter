package managers

import screeps.api.*
import screeps.api.structures.StructureTower

class TowerManager(private val towers:List<StructureTower>) {

    fun towerDefenseProtocol() {
        for (tower in towers) {
            val hostiles = tower.room.find(FIND_HOSTILE_CREEPS)
            if (hostiles.isNotEmpty()) {
                val userName = hostiles[0].owner.username
                Game.notify("User $userName spotted in room ${tower.room}")
                tower.attack(hostiles[0])
            }
        }
    }
}
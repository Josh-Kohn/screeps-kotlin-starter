package managers
import memory.initialized
import memory.sources
import objects.SourceDataObject
import screeps.api.*

class InitiliazationManager(val rooms: List<Room> ) {
    init {
        for (room in rooms){
            if (!room.memory.initialized){
                val getAllSources = room.find(FIND_SOURCES)
                for (singleSource in getAllSources) {
                    val lookResult = room.lookAtAreaAsArray(singleSource.pos.y-1, singleSource.pos.x-1, singleSource.pos.y+1, singleSource.pos.x+1)
                    val maxEmptySpaces = lookResult.filter { space -> space.terrain != TERRAIN_WALL }.size
                    val sourceDataMemory = SourceDataObject(singleSource.id,0, maxEmptySpaces-1)
                    room.memory.sources = room.memory.sources + sourceDataMemory
                }
                room.memory.initialized = true
            }
        }
    }
}
package objects

data class SourceDataObject(
        val sourceID: String,
        var currentHarvesterCreeps: Int,
        val maxHarvesterCreeps: Int,
        var freeCreepSlot: Boolean
)
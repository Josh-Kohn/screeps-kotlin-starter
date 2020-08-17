package objects

data class SourceDataObject(
        val sourceID: String,
        var currentCreeps: Int,
        val maxCreeps: Int)
package objects

import screeps.api.Creep

data class MilitaryDataClass(
        var unitName: String,
        var unitType: String,
        var attackBody: Int,
        var rangedBody: Int,
        var healBody: Int,
        var toughBody: Int,
        var moveBody: Int
)
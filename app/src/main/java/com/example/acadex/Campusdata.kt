package com.example.acadex

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

enum class PlaceCategory(
    val label: String,
    @ColorInt val color: Int,
    @DrawableRes val icon: Int
) {
    ACADEMIC("Academic", 0xFF2F6FED.toInt(), R.drawable.ic_building),
    ADMIN("Student services", 0xFF8E5BD9.toInt(), R.drawable.ic_building),
    TECH("IT and technology", 0xFF14A89A.toInt(), R.drawable.ic_monitor),
    HEALTH("Health", 0xFFE5484D.toInt(), R.drawable.ic_clinic),
    FOOD("Food", 0xFFF08A24.toInt(), R.drawable.ic_clipboard),
    RECREATION("Recreation", 0xFF3E9B4F.toInt(), R.drawable.ic_event),
    PARKING("Parking", 0xFF5C6B80.toInt(), R.drawable.ic_map)
}

/** x / y are pixel positions on the 1476 x 757 campus map image. */
data class CampusPlace(
    val id: String,
    val name: String,
    val description: String,
    val category: PlaceCategory,
    val x: Float,
    val y: Float
)

object CampusData {
    const val MAP_W = 1476f
    const val MAP_H = 757f

    val places: List<CampusPlace> = listOf(
        CampusPlace("annex_d", "Annex D", "Classrooms and lecture rooms on the north side of the field.", PlaceCategory.ACADEMIC, 391f, 194f),
        CampusPlace("annex_c", "Annex C", "Classrooms and lecture rooms next to Annex D.", PlaceCategory.ACADEMIC, 600f, 192f),
        CampusPlace("ce", "CE Building", "College of Engineering building.", PlaceCategory.ACADEMIC, 1030f, 195f),
        CampusPlace("clinic", "Clinic", "Campus clinic for first aid and basic health needs.", PlaceCategory.HEALTH, 885f, 197f),
        CampusPlace("annex", "Annex", "Annex building along the main road.", PlaceCategory.ACADEMIC, 156f, 304f),
        CampusPlace("main", "Main Building", "The university's main academic and administrative building.", PlaceCategory.ACADEMIC, 270f, 432f),
        CampusPlace("guidance", "Guidance Office", "Student counseling and guidance services.", PlaceCategory.ADMIN, 55f, 510f),
        CampusPlace("cit", "CIT Office", "College of Industrial Technology office.", PlaceCategory.TECH, 74f, 616f),
        CampusPlace("field", "Soccer Field", "Open field used for sports, drills and school events.", PlaceCategory.RECREATION, 651f, 433f),
        CampusPlace("parking", "Parking Lot", "Vehicle parking along the south side of the field.", PlaceCategory.PARKING, 668f, 620f),
        CampusPlace("canteen", "School Canteen", "Meals and snacks for students and staff.", PlaceCategory.FOOD, 892f, 672f),
        CampusPlace("digitel", "Digitel", "Digitel building east of the field.", PlaceCategory.TECH, 1282f, 592f)
    )
}

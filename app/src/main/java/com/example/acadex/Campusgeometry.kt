package com.example.acadex

/** Generated map geometry. Coordinates are pixels of the 1476x757 campus image. */
object CampusGeometry {

    class Shape(val placeId: String?, val category: PlaceCategory?, val pts: FloatArray)

    val buildings: List<Shape> = listOf(
        Shape(null, null, floatArrayOf(795f, 22f, 1172f, 22f, 1172f, 75f, 795f, 75f)),
        Shape(null, null, floatArrayOf(1232f, 72f, 1338f, 72f, 1338f, 228f, 1232f, 228f)),
        Shape(null, null, floatArrayOf(1205f, 180f, 1300f, 180f, 1300f, 224f, 1205f, 224f)),
        Shape(null, null, floatArrayOf(1392f, 10f, 1476f, 10f, 1476f, 200f, 1392f, 200f)),
        Shape(null, null, floatArrayOf(1415f, 215f, 1476f, 215f, 1476f, 262f, 1415f, 262f)),
        Shape(null, null, floatArrayOf(1150f, 250f, 1200f, 250f, 1200f, 360f, 1150f, 360f)),
        Shape(null, null, floatArrayOf(1196f, 316f, 1250f, 316f, 1250f, 372f, 1196f, 372f)),
        Shape(null, null, floatArrayOf(1400f, 310f, 1476f, 310f, 1476f, 370f, 1400f, 370f)),
        Shape(null, null, floatArrayOf(1420f, 410f, 1476f, 410f, 1476f, 500f, 1420f, 500f)),
        Shape(null, null, floatArrayOf(1355f, 640f, 1420f, 640f, 1420f, 700f, 1355f, 700f)),
        Shape(null, null, floatArrayOf(1110f, 650f, 1200f, 650f, 1200f, 700f, 1110f, 700f)),
        Shape(null, null, floatArrayOf(1215f, 650f, 1255f, 650f, 1255f, 710f, 1215f, 710f)),
        Shape(null, null, floatArrayOf(310f, 62f, 740f, 62f, 740f, 120f, 310f, 120f)),
        Shape("annex_d", PlaceCategory.ACADEMIC, floatArrayOf(310f, 172f, 472f, 172f, 472f, 216f, 310f, 216f)),
        Shape("annex_c", PlaceCategory.ACADEMIC, floatArrayOf(484f, 167f, 716f, 167f, 716f, 216f, 484f, 216f)),
        Shape("ce", PlaceCategory.ACADEMIC, floatArrayOf(950f, 168f, 1112f, 168f, 1112f, 222f, 950f, 222f)),
        Shape("clinic", PlaceCategory.HEALTH, floatArrayOf(822f, 172f, 948f, 172f, 948f, 222f, 822f, 222f)),
        Shape("annex", PlaceCategory.ACADEMIC, floatArrayOf(104f, 328f, 204f, 230f, 228f, 252f, 150f, 378f)),
        Shape("main", PlaceCategory.ACADEMIC, floatArrayOf(214f, 290f, 300f, 272f, 322f, 428f, 240f, 432f)),
        Shape("main", PlaceCategory.ACADEMIC, floatArrayOf(236f, 436f, 328f, 440f, 300f, 625f, 214f, 606f, 206f, 520f)),
        Shape("guidance", PlaceCategory.ADMIN, floatArrayOf(36f, 478f, 74f, 478f, 74f, 542f, 36f, 542f)),
        Shape("cit", PlaceCategory.TECH, floatArrayOf(40f, 548f, 108f, 548f, 108f, 684f, 40f, 684f)),
        Shape("canteen", PlaceCategory.FOOD, floatArrayOf(815f, 640f, 968f, 640f, 968f, 705f, 815f, 705f)),
        Shape("digitel", PlaceCategory.TECH, floatArrayOf(1215f, 535f, 1348f, 535f, 1348f, 648f, 1215f, 648f)),
        Shape("digitel", PlaceCategory.TECH, floatArrayOf(1350f, 458f, 1412f, 458f, 1412f, 636f, 1350f, 636f))
    )

    /** Extra outlined areas that can be highlighted when selected. */
    val areas: List<Shape> = listOf(
        Shape("field", PlaceCategory.RECREATION, floatArrayOf(405f, 268f, 898f, 268f, 898f, 598f, 405f, 598f)),
        Shape("parking", PlaceCategory.PARKING, floatArrayOf(255f, 603f, 1080f, 603f, 1080f, 636f, 255f, 636f)),
        Shape("parking", PlaceCategory.PARKING, floatArrayOf(362f, 275f, 392f, 275f, 392f, 598f, 362f, 598f))
    )

    val road = floatArrayOf(165f, 0f, 210f, 0f, 30f, 430f, 0f, 430f, 0f, 372f)
    val walkways: List<FloatArray> = listOf(
        floatArrayOf(215f, 222f, 905f, 222f, 905f, 262f, 215f, 262f),
        floatArrayOf(335f, 222f, 400f, 222f, 400f, 625f, 335f, 625f),
        floatArrayOf(110f, 598f, 1090f, 598f, 1090f, 642f, 110f, 642f),
        floatArrayOf(895f, 222f, 912f, 222f, 912f, 610f, 895f, 610f),
        floatArrayOf(190f, 168f, 235f, 168f, 235f, 262f, 190f, 262f)
    )
    val vegetation = floatArrayOf(0f, 650f, 120f, 640f, 400f, 690f, 790f, 650f, 800f, 757f, 0f, 757f)
    val field = floatArrayOf(405f, 268f, 898f, 268f, 898f, 598f, 405f, 598f)
    val parking: List<FloatArray> = listOf(
        floatArrayOf(255f, 603f, 1080f, 603f, 1080f, 636f, 255f, 636f),
        floatArrayOf(362f, 275f, 392f, 275f, 392f, 598f, 362f, 598f)
    )
    val trees = floatArrayOf(95f, 170f, 34f, 150f, 128f, 30f, 82f, 385f, 28f, 92f, 478f, 28f, 36f, 432f, 20f, 215f, 62f, 24f, 258f, 92f, 22f, 1000f, 372f, 50f, 1062f, 412f, 44f, 960f, 302f, 30f, 1112f, 308f, 34f, 1132f, 446f, 38f, 1022f, 462f, 34f, 950f, 472f, 24f, 1160f, 520f, 20f, 930f, 360f, 26f, 985f, 285f, 22f, 338f, 330f, 15f, 342f, 470f, 13f, 345f, 560f, 14f, 300f, 245f, 14f, 300f, 700f, 38f, 450f, 722f, 42f, 590f, 705f, 30f, 180f, 690f, 28f, 700f, 720f, 28f, 1150f, 130f, 16f, 1120f, 600f, 14f, 1060f, 540f, 16f, 1100f, 285f, 14f)
}

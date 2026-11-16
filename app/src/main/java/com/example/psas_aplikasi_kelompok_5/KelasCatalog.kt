package com.example.psas_aplikasi_kelompok_5

object KelasCatalog {
    val list: List<Pair<Int, String>> = listOf(
        4 to "XI PPLG 4",
        11 to "XI PPLG 1",
        12 to "XI PPLG 2",
        13 to "XI PPLG 3",
        14 to "XI PPLG 5",
        21 to "XI TJKT 1",
        22 to "XI TJKT 2"
    )

    fun names(): Array<String> = list.map { it.second }.toTypedArray()

    fun idAt(position: Int): Int = list.getOrNull(position)?.first ?: 4

    fun name(id: Int): String = list.firstOrNull { it.first == id }?.second ?: "XI PPLG 4"
}

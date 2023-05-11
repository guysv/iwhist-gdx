package guysv.iwhist.game.layout

import guysv.iwhist.game.Socket

interface Layout {
    val numberOfSockets: Int
    val numberOfColumns: Int
    val numberOfRows: Int
    val tag: String
    operator fun get(index: Int): Socket
    fun lookup(column: Int, row: Int): Socket?
}

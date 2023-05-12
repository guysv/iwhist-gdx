package guysv.iwhist.server.event

data class ServerEvent(
        val eventType: EventType,
        val playerId: Int?,
        val message: String?) {

    data class Builder(
            private val eventType: EventType,
    ) {
        private var player: Int? = null
        private var message: String? = null

        fun player(pid: Int) = apply { player = pid }
        fun message(m: String) = apply { message = m }

        fun build() = ServerEvent(eventType, player, message)
    }
}
package br.com.bonecoliveia

object SpongeBobEvents {
    @Volatile var screenOpen: Boolean = false
    @Volatile var listener: ((String, String) -> Unit)? = null

    fun emit(mood: String, line: String) {
        listener?.invoke(mood, line)
    }
}

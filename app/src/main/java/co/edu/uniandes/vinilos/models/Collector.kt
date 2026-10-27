package co.edu.uniandes.vinilos.models

/** Coleccionista de BackVynils (`GET /collectors`), con los comentarios que ha escrito. */
data class Collector(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    val comments: List<Comment>
)

package co.edu.uniandes.vinilos.ui

import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Libera la imagen que Glide cargó en [view] para que su bitmap vuelva al pool.
 *
 * Usa el contexto de la aplicación y no `Glide.with(view)`: al salir de la app, `onDestroyView` quita el
 * adaptador y se reciclan las filas cuando la Activity ya está destruida, y `Glide.with(view)` lanza
 * `IllegalArgumentException: You cannot start a load for a destroyed activity` (#138).
 */
fun clearGlideImage(view: ImageView) {
    Glide.with(view.context.applicationContext).clear(view)
}

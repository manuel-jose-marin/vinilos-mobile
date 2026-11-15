package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import co.edu.uniandes.vinilos.BuildConfig
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) enableStrictMode()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // La barra inferior solo se muestra en las secciones principales; las pantallas de detalle la ocultan.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in TOP_LEVEL_DESTINATIONS
            binding.bottomNavigation.visibility = if (isTopLevel) View.VISIBLE else View.GONE
        }
    }

    /**
     * Solo en debug: registra en Logcat (etiqueta StrictMode) cualquier acceso a red o disco en el hilo
     * principal y los objetos que se cierran o liberan mal. Sirve para verificar que no hay riesgo de ANR.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectNetwork().detectDiskReads().detectDiskWrites().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectActivityLeaks().penaltyLog().build()
        )
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.albumListFragment,
            R.id.artistListFragment,
            R.id.collectorListFragment
        )
    }
}

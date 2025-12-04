package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.DashboardPagerAdapter
import co.edu.unipiloto.myapplication.storage.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

/**
 * Activity para el Panel de Control del Gestor/Gerente.
 * Utiliza ViewPager2 y TabLayout para mostrar diferentes listas de solicitudes.
 */
class ManagerDashboardActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnLogout: MaterialButton
    private lateinit var tvWelcomeTitle: TextView // Añadido para personalizar el saludo
    private lateinit var sessionManager: SessionManager

    // Definición de los títulos de las pestañas (idealmente en strings.xml)
    private val tabTitles = arrayOf("Pendientes de Asignar", "Asignadas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_dashboard)

        sessionManager = SessionManager(this)

        // 🛡️ Verificación de autenticación y roles
        val userRole = sessionManager.getRole()
        if (!sessionManager.isLoggedIn() || userRole !in listOf("FUNCIONARIO", "ANALISTA")) {
            Log.w("Auth", "Intento de acceso denegado. Rol: $userRole")
            logoutUser()
            return
        }

        initViews()
        setupWelcomeText(userRole) // Configura el saludo
        setupViewPager()
        setupListeners()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btnLogout = findViewById(R.id.btnLogout)
        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle)
    }

    /**
     * Personaliza el texto de bienvenida con el nombre y rol del usuario.
     */
    private fun setupWelcomeText(role: String) {
        // 💡 Corregido: La variable se llama 'userName' (Línea 70)
        val userName = sessionManager.getName() ?: "Usuario" // Asume que tienes un getName()

        val titlePrefix = when (role) {
            "FUNCIONARIO" -> "Funcionario"
            "ANALISTA" -> "Analista"
            // Nota: El rol anterior era GESTOR. Si GESTOR es el rol principal,
            // asegúrate de incluirlo en la lista de roles permitidos en onCreate.
            else -> "Panel"
        }

        tvWelcomeTitle.text = getString(R.string.manager_dashboard_title, titlePrefix, userName)
    }

    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
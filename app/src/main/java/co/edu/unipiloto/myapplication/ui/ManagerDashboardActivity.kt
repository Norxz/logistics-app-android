package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private lateinit var sessionManager: SessionManager

    /**
     * Initializes the manager dashboard UI and enforces session-based access control.
     *
     * If the current session is missing or the user's role is not `GESTOR` or `ANALISTA`,
     * the method logs the user out and navigates to the login screen. Otherwise it binds
     * views, configures the ViewPager/TabLayout, and sets up UI listeners.
     *
     * @param savedInstanceState The activity's previously saved state, or `null` if none.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getRole() !in listOf("GESTOR", "ANALISTA")) {
            logoutUser()
            return
        }

        initViews()
        setupViewPager()
        setupListeners()
    }

    /**
     * Binds UI components from the layout to the activity's properties.
     *
     * Initializes the `tabLayout`, `viewPager`, and `btnLogout` fields by finding their views.
     */
    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btnLogout = findViewById(R.id.btnLogout)
        // Puedes personalizar tvWelcomeTitle si quieres usar el nombre del gestor
    }

    /**
     * Configures the ViewPager2 with a DashboardPagerAdapter and links it to the TabLayout using a TabLayoutMediator.
     *
     * Sets the adapter on the activity's ViewPager2 and attaches a TabLayoutMediator that provides tab titles:
     * position 0 -> "Pendientes de Asignar", position 1 -> "Asignadas", any other position -> "Error".
     */
    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        viewPager.adapter = adapter

        // Conecta el TabLayout con el ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pendientes de Asignar"
                1 -> "Asignadas"
                else -> "Error"
            }
        }.attach()
    }

    /**
     * Attaches UI listeners for the activity.
     *
     * Configures the logout button so that tapping it clears the current session and navigates to the login screen.
     */
    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Logs out the current user, clears the session, and navigates to the login screen.
     *
     * Clears any stored session state, starts LoginActivity with flags that clear the task
     * so the back stack cannot be returned to, and finishes the current activity.
     */
    private fun logoutUser() {
        sessionManager.logoutUser()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
package co.edu.unipiloto.myapplication.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.edu.unipiloto.myapplication.fragment.PendingRequestsFragment
import co.edu.unipiloto.myapplication.fragment.AssignedRequestsFragment

/**
 * 📊 Adaptador para ViewPager2 que gestiona las pestañas del Panel de Control del Gerente/Funcionario.
 * Carga un Fragmento diferente para "Pendientes de Asignar" y "Asignadas".
 */
class DashboardPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    // Se definen explícitamente las dos pestañas necesarias
    private companion object {
        const val NUM_TABS = 2
    }

    override fun getItemCount(): Int = NUM_TABS

    /**
     * Devuelve la instancia del Fragmento correspondiente a la posición de la pestaña.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PendingRequestsFragment() // ⬅️ Pestaña 0: Solicitudes Pendientes (para asignación)
            1 -> AssignedRequestsFragment() // ⬅️ Pestaña 1: Solicitudes Asignadas (para seguimiento)
            else -> throw IllegalStateException("Posición de pestaña inválida: $position")
        }
    }
}
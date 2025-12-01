package co.edu.unipiloto.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import co.edu.unipiloto.myapplication.fragment.AssignedRequestsFragment;
import co.edu.unipiloto.myapplication.ui.*; // Asegúrate de que este import abarque BranchPendingFragment

public class DashboardPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 2;

    public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // 🏆 CORRECCIÓN: Usar la clase que ya existe (BranchPendingFragment)
                // Carga la lista de solicitudes sin asignar
                return new BranchPendingFragment();
            case 1:
                // Carga la lista de solicitudes ya asignadas
                // Asumimos que esta clase sí existe o la crearemos después.
                return new AssignedRequestsFragment();
            default:
                return new BranchPendingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
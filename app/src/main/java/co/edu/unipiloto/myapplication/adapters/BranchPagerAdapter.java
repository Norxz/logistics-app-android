package co.edu.unipiloto.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import co.edu.unipiloto.myapplication.fragment.BranchCompletedFragment;
import co.edu.unipiloto.myapplication.fragment.BranchInRouteFragment;
import co.edu.unipiloto.myapplication.ui.*;


public class BranchPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 3;

    public BranchPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Pestaña 0: Solicitudes en la sucursal, listas para asignar o salir
                return new BranchPendingFragment();
            case 1:
                // Pestaña 1: Solicitudes que están en ruta (con conductor asignado)
                return new BranchInRouteFragment();
            case 2:
                // Pestaña 2: Solicitudes que ya se entregaron/finalizaron
                return new BranchCompletedFragment();
            default:
                return new BranchPendingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
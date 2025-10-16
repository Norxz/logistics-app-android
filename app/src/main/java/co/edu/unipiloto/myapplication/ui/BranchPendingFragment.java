package co.edu.unipiloto.myapplication.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.edu.unipiloto.myapplication.R;
// Importa tu adaptador de lista (debes crearlo después: BranchRequestsAdapter)
// import co.edu.unipiloto.myapplication.adapters.BranchRequestsAdapter;

/**
 * Fragmento para mostrar solicitudes PENDIENTES de gestión en la sucursal del funcionario.
 */
public class BranchPendingFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usaremos un layout genérico que debes crear: fragment_branch_list.xml
        View view = inflater.inflate(R.layout.fragment_branch_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBranchList);
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty);

        setupRecyclerView();
        loadPendingRequests();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // adapter = new BranchRequestsAdapter(getContext(), pendingList);
        // recyclerView.setAdapter(adapter);
    }

    private void loadPendingRequests() {
        // Lógica de datos: Obtener solicitudes PENDIENTES filtradas por la zona del funcionario.
    }
}
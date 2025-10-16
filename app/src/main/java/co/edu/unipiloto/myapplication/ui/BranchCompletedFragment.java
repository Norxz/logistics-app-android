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
// Importa tu adaptador de lista (debes crearlo más tarde: BranchRequestsAdapter)
// import co.edu.unipiloto.myapplication.adapters.BranchRequestsAdapter;

/**
 * Fragmento para mostrar solicitudes FINALIZADAS (Completadas) de la sucursal del funcionario.
 */
public class BranchCompletedFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRequests;
    // private BranchRequestsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usamos el layout genérico que ya creamos para las listas de sucursal
        View view = inflater.inflate(R.layout.fragment_branch_list, container, false);

        // Asumiendo que los IDs en fragment_branch_list.xml son:
        recyclerView = view.findViewById(R.id.recyclerViewBranchList);
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty);

        setupRecyclerView();
        loadCompletedRequests();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inicializar y asignar adaptador con datos 'Finalizados'
        // adapter = new BranchRequestsAdapter(getContext(), completedList);
        // recyclerView.setAdapter(adapter);
    }

    private void loadCompletedRequests() {
        // Lógica de datos: Obtener solicitudes FINALIZADAS/ENTREGADAS filtradas por la zona del funcionario.
        // Aquí debes llamar a tu repositorio para obtener los datos.
    }
}
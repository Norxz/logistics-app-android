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
// Importa tu adaptador de lista (asumimos que usarás uno general para la sucursal)
// import co.edu.unipiloto.myapplication.adapters.BranchRequestsAdapter;

/**
 * Fragmento para mostrar solicitudes ASIGNADAS (EN RUTA) de la sucursal del funcionario.
 */
public class BranchInRouteFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRequests;
    // private BranchRequestsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usamos el layout genérico que definimos para las listas de sucursal
        View view = inflater.inflate(R.layout.fragment_branch_list, container, false);

        // Asumiendo que los IDs en fragment_branch_list.xml son:
        recyclerView = view.findViewById(R.id.recyclerViewBranchList);
        tvNoRequests = view.findViewById(R.id.tvBranchEmpty);

        setupRecyclerView();
        loadInRouteRequests();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inicializar y asignar adaptador con datos de 'En Ruta'
        // adapter = new BranchRequestsAdapter(getContext(), inRouteList);
        // recyclerView.setAdapter(adapter);
    }

    private void loadInRouteRequests() {
        // Lógica de datos: Obtener solicitudes EN RUTA filtradas por la zona del funcionario.
        // Aquí debes llamar a tu repositorio para obtener los datos.
    }
}
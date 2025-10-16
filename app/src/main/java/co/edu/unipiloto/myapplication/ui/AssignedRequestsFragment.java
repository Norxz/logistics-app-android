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
// Necesitarás crear este adaptador para la lista
// import co.edu.unipiloto.myapplication.adapters.AssignedRequestsAdapter;
// import co.edu.unipiloto.myapplication.model.Solicitud; // Tu clase de modelo de datos

public class AssignedRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRequests;
    // private AssignedRequestsAdapter adapter;
    // private List<Solicitud> assignedList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Asume que el layout para esta pestaña se llama fragment_assigned_requests.xml
        View view = inflater.inflate(R.layout.fragment_entregas_pendientes, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAssigned);
        tvNoRequests = view.findViewById(R.id.tvNoPendingRequests);

        setupRecyclerView();
        loadAssignedRequests();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Aquí debes inicializar tu adaptador de lista.
        // assignedList = new ArrayList<>();
        // adapter = new AssignedRequestsAdapter(getContext(), assignedList);
        // recyclerView.setAdapter(adapter);
    }

    private void loadAssignedRequests() {
        // 🛑 Lógica de datos: Obtener solicitudes con estado ASIGNADA (o En Proceso).
        // Ejemplo: CallToRepository.getAssignedRequests((list) -> {
        //     assignedList.clear();
        //     assignedList.addAll(list);
        //     adapter.notifyDataSetChanged();
        //     tvNoRequests.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        // });
    }
}
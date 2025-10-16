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
// Asegúrate de crear el adaptador que manejará la lista de asignación
// import co.edu.unipiloto.myapplication.adapters.PendingDeliveryAdapter;

/**
 * Fragmento para mostrar las solicitudes de ENTREGA que están pendientes de ASIGNACIÓN.
 */
public class PendingDeliveryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRequests;
    // private PendingDeliveryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 🛑 Usando el layout que ya existe: fragment_entregas_pendientes.xml
        View view = inflater.inflate(R.layout.fragment_entregas_pendientes, container, false);

        // Asumiendo que el RecyclerView en ese layout se llama 'recyclerViewPending'
        recyclerView = view.findViewById(R.id.recyclerViewAssigned);
        // Asumiendo que el TextView de lista vacía se llama 'tvNoPendingRequests'
        tvNoRequests = view.findViewById(R.id.tvNoPendingRequests);

        setupRecyclerView();
        loadPendingDeliveries();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializa y asigna tu adaptador (donde va la lógica de selección de conductor/botón de asignación)
        // adapter = new PendingDeliveryAdapter(getContext(), listaDeEntregas);
        // recyclerView.setAdapter(adapter);
    }

    private void loadPendingDeliveries() {
        // Llama a tu base de datos para obtener solicitudes de ENTREGA pendientes.
        // La lógica de visibilidad de tvNoRequests va aquí.
    }
}
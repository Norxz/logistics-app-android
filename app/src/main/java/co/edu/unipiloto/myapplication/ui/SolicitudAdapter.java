package co.edu.unipiloto.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.VH> {

    // --- NUEVOS LISTENERS Y CONSTANTES ---

    public interface OnAssignListener { void onAssign(long solicitudId, long conductorId, int position); }
    private OnAssignListener assignListener;
    public void setOnAssignListener(OnAssignListener l){ this.assignListener = l; }

    // Constantes para View Types
    private static final int VIEW_TYPE_CLIENTE_O_RECOLECTOR = 1;
    private static final int VIEW_TYPE_FUNCIONARIO_PENDIENTE = 2;


    // --- LISTENERS EXISTENTES ---
    public interface OnAcceptListener { void onAccept(long solicitudId, int position); }
    public interface OnCancelListener { void onCancel(long solicitudId, int position); }
    public interface OnEnCaminoListener { void onEnCamino(long solicitudId, int position); }
    public interface OnEntregadoListener { void onEntregado(long solicitudId, int position); }
    public interface OnConfirmListener { void onConfirm(long solicitudId, int position); }

    private OnEnCaminoListener enCaminoListener;
    private OnEntregadoListener entregadoListener;
    private OnConfirmListener confirmListener;
    private OnAcceptListener acceptListener;
    private OnCancelListener cancelListener;

    public void setOnEnCaminoListener(OnEnCaminoListener l){ this.enCaminoListener = l; }
    public void setOnEntregadoListener(OnEntregadoListener l){ this.entregadoListener = l; }
    public void setOnConfirmListener(OnConfirmListener l){ this.confirmListener = l; }
    public void setOnAcceptListener(OnAcceptListener l){ this.acceptListener = l; }
    public void setOnCancelListener(OnCancelListener l){ this.cancelListener = l; }

    // --- DATOS Y CONSTRUCTORES ---

    private final List<SolicitudRepository.SolicitudItem> data = new ArrayList<>();
    private final boolean isForFuncionario;

    // Mapa para el Spinner: Nombre de Conductor -> ID de Conductor
    private final Map<String, Long> conductorMap = new HashMap<>();
    private final List<String> conductorNames = new ArrayList<>();


    private SolicitudAdapter(boolean isForFuncionario, List<SolicitudRepository.SolicitudItem> items) {
        this.isForFuncionario = isForFuncionario;
        if (items != null) {
            this.data.addAll(items);
        }

        // Inicializar datos de Conductores (MOCK)
        conductorNames.add("Seleccionar Conductor"); // Placeholder
        conductorMap.put("Seleccionar Conductor", -1L);
        conductorMap.put("Juan Pérez (Conductor 1)", 101L);
        conductorNames.add("Juan Pérez (Conductor 1)");
        conductorMap.put("Ana Gómez (Conductor 2)", 102L);
        conductorNames.add("Ana Gómez (Conductor 2)");
    }

    /** fábrica para cliente */
    public static SolicitudAdapter forCliente(List<SolicitudRepository.SolicitudItem> items) {
        return new SolicitudAdapter(false, items);
    }

    /** fábrica para recolector (Debe convertir Solicitud a SolicitudItem o fallará) */
    public static SolicitudAdapter forRecolector(List<Solicitud> items) {
        // En un proyecto real, necesitarías lógica de conversión aquí.
        // Por ahora, solo crea el adaptador en modo Funcionario/Recolector.
        return new SolicitudAdapter(true, null);
    }

    /** fábrica para un funcionario (gestor de sucursal) */
    public static SolicitudAdapter forFuncionario(List<SolicitudRepository.SolicitudItem> items) {
        return new SolicitudAdapter(true, items);
    }


    // --- MÉTODOS DEL ADAPTADOR ---

    @Override
    public int getItemViewType(int position) {
        SolicitudRepository.SolicitudItem r = data.get(position);

        // Si es Funcionario Y la solicitud está PENDIENTE, se usa el layout de Asignación.
        if (isForFuncionario && "PENDIENTE".equalsIgnoreCase(r.estado)) {
            return VIEW_TYPE_FUNCIONARIO_PENDIENTE;
        }

        return VIEW_TYPE_CLIENTE_O_RECOLECTOR;
    }


    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (viewType == VIEW_TYPE_FUNCIONARIO_PENDIENTE) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_pendiente, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        }
        return new VH(v, viewType);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        SolicitudRepository.SolicitudItem r = data.get(pos);
        String senderName = "Remitente Desconocido"; // Simulación

        if (h.viewType == VIEW_TYPE_FUNCIONARIO_PENDIENTE) {
            // Flujo de Asignación
            h.tvSolicitudID_Func.setText("Guía #" + r.id);
            h.tvDestination_Func.setText("Destino: " + r.direccion);
            h.tvSender_Func.setText("Remitente: " + senderName);

            setupConductorSpinner(h.spConductor_Func);

            h.btnAssign_Func.setOnClickListener(v -> {
                long selectedId = getSelectedConductorId(h.spConductor_Func);
                if (selectedId != -1L) {
                    if (assignListener != null) {
                        assignListener.onAssign(r.id, selectedId, pos);
                    }
                } else {
                    Toast.makeText(v.getContext(), "Selecciona un conductor.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // Flujo Estándar
            // h.tvShipmentId.setText("Guía: #" + r.id); // Si está disponible
            h.tvDireccion.setText(r.direccion);
            h.tvFecha.setText((r.fecha == null ? "" : r.fecha) + "  " + (r.franja == null ? "" : r.franja));
            h.tvEstado.setText(r.estado == null ? "PENDIENTE" : r.estado);
            h.tvCreado.setText("Creado: " + fmt(r.createdAt));

            // Lógica de visibilidad de botones
            if (isForFuncionario) {
                h.btnAceptar.setVisibility("PENDIENTE".equalsIgnoreCase(r.estado) ? View.VISIBLE : View.GONE);
                h.btnEnCamino.setVisibility("ASIGNADA".equalsIgnoreCase(r.estado) ? View.VISIBLE : View.GONE);
                h.btnEntregado.setVisibility("EN_CAMINO".equalsIgnoreCase(r.estado) ? View.VISIBLE : View.GONE);
                h.btnCancelar.setVisibility(View.GONE);
                h.btnConfirmar.setVisibility(View.GONE);
            } else {
                h.btnCancelar.setVisibility("PENDIENTE".equalsIgnoreCase(r.estado) ? View.VISIBLE : View.GONE);
                h.btnConfirmar.setVisibility("ENTREGADA".equalsIgnoreCase(r.estado) ? View.VISIBLE : View.GONE);
                h.btnAceptar.setVisibility(View.GONE);
                h.btnEnCamino.setVisibility(View.GONE);
                h.btnEntregado.setVisibility(View.GONE);
            }

            // Lógica de Click Listeners
            h.btnEnCamino.setOnClickListener(v -> {
                if (enCaminoListener != null) enCaminoListener.onEnCamino(r.id, pos);
            });
            h.btnEntregado.setOnClickListener(v -> {
                if (entregadoListener != null) entregadoListener.onEntregado(r.id, pos);
            });
            h.btnAceptar.setOnClickListener(v -> {
                if (acceptListener != null) acceptListener.onAccept(r.id, pos);
            });
            h.btnCancelar.setOnClickListener(v -> {
                if (cancelListener != null) cancelListener.onCancel(r.id, pos);
            });
            h.btnConfirmar.setOnClickListener(v -> {
                if (confirmListener != null) confirmListener.onConfirm(r.id, pos);
            });
        }
    }

    @Override public int getItemCount() { return data.size(); }

    // --- MÉTODOS DE UTILIDAD PARA SPINNER ---

    private void setupConductorSpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, conductorNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private long getSelectedConductorId(Spinner spinner) {
        String selectedName = (String) spinner.getSelectedItem();
        Long id = conductorMap.get(selectedName);
        return id != null ? id : -1L;
    }

    // --- VIEW HOLDER UNIFICADO ---

    static class VH extends RecyclerView.ViewHolder {
        // Vistas de item_solicitud (Estándar)
        TextView tvDireccion, tvFecha, tvEstado, tvCreado, tvShipmentId;
        Button btnAceptar, btnCancelar, btnEnCamino, btnEntregado, btnConfirmar;

        // Vistas de item_solicitud_pendiente (Funcionario/Nuevo)
        TextView tvSolicitudID_Func, tvDestination_Func, tvSender_Func;
        Spinner spConductor_Func;
        MaterialButton btnAssign_Func;

        final int viewType;

        VH(@NonNull View v, int viewType) {
            super(v);
            this.viewType = viewType;

            if (viewType == VIEW_TYPE_FUNCIONARIO_PENDIENTE) {
                // Inicializar vistas del layout de Asignación
                tvSolicitudID_Func = v.findViewById(R.id.tvSolicitudID);
                tvDestination_Func = v.findViewById(R.id.tvDestination);
                tvSender_Func = v.findViewById(R.id.tvSender);
                spConductor_Func = v.findViewById(R.id.spConductor);
                btnAssign_Func = v.findViewById(R.id.btnAssign);
            } else {
                // Inicializar vistas del layout Estándar
                tvShipmentId = v.findViewById(R.id.tvShipmentId);
                tvDireccion = v.findViewById(R.id.tvDireccion);
                tvFecha = v.findViewById(R.id.tvFecha);
                tvEstado = v.findViewById(R.id.tvEstado);
                tvCreado = v.findViewById(R.id.tvCreado);
                btnAceptar = v.findViewById(R.id.btnAceptar);
                btnCancelar = v.findViewById(R.id.btnCancelar);
                btnEnCamino = v.findViewById(R.id.btnEnCamino);
                btnEntregado = v.findViewById(R.id.btnEntregado);
                btnConfirmar = v.findViewById(R.id.btnConfirmar);
            }
        }
    }

    // --- MÉTODOS DE UTILIDAD ---

    public void updateEstadoAt(int pos, String nuevoEstado) {
        if (pos >= 0 && pos < data.size()) {
            data.get(pos).estado = nuevoEstado;
            notifyItemChanged(pos);
        }
    }
    public void removeAt(int pos) {
        if (pos >= 0 && pos < data.size()) {
            data.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    private String fmt(long ms) {
        if (ms <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(ms));
    }
}
package co.edu.unipiloto.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.VH> {

    public interface OnAcceptListener { void onAccept(long solicitudId, int position); }
    public interface OnCancelListener { void onCancel(long solicitudId, int position); }
    public interface OnEnCaminoListener { void onEnCamino(long solicitudId, int position); }
    public interface OnEntregadoListener { void onEntregado(long solicitudId, int position); }
    public interface OnConfirmListener { void onConfirm(long solicitudId, int position); }

    private OnEnCaminoListener enCaminoListener;
    private OnEntregadoListener entregadoListener;
    private OnConfirmListener confirmListener;

    public void setOnEnCaminoListener(OnEnCaminoListener l){ this.enCaminoListener = l; }
    public void setOnEntregadoListener(OnEntregadoListener l){ this.entregadoListener = l; }
    public void setOnConfirmListener(OnConfirmListener l){ this.confirmListener = l; }

    private static class Row {
        long id;
        String direccion, fecha, franja, estado;
        long createdAt;
        Row(long id, String direccion, String fecha, String franja, String estado, long createdAt) {
            this.id = id; this.direccion = direccion; this.fecha = fecha;
            this.franja = franja; this.estado = estado; this.createdAt = createdAt;
        }
    }

    private final List<Row> data = new ArrayList<>();
    private final boolean isForRecolector;
    private OnAcceptListener acceptListener;
    private OnCancelListener cancelListener;

    // constructor privado
    private SolicitudAdapter(boolean isForRecolector) {
        this.isForRecolector = isForRecolector;
    }

    /** fábrica para cliente */
    public static SolicitudAdapter forCliente(List<SolicitudRepository.SolicitudItem> items) {
        SolicitudAdapter a = new SolicitudAdapter(false);
        if (items != null) {
            for (SolicitudRepository.SolicitudItem it : items) {
                a.data.add(new Row(it.id, it.direccion, it.fecha, it.franja, it.estado, it.createdAt));
            }
        }
        return a;
    }

    /** fábrica para recolector */
    public static SolicitudAdapter forRecolector(List<Solicitud> items) {
        SolicitudAdapter a = new SolicitudAdapter(true);
        if (items != null) {
            for (Solicitud s : items) {
                a.data.add(new Row(s.id, s.direccion, s.fecha, s.franja, s.estado, s.createdAt));
            }
        }
        return a;
    }

    public void setOnAcceptListener(OnAcceptListener l){ this.acceptListener = l; }
    public void setOnCancelListener(OnCancelListener l){ this.cancelListener = l; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Row r = data.get(pos);
        h.tvDireccion.setText(r.direccion);
        h.tvFecha.setText((r.fecha == null ? "" : r.fecha) + "  " + (r.franja == null ? "" : r.franja));
        h.tvEstado.setText(r.estado == null ? "PENDIENTE" : r.estado);
        h.tvCreado.setText("Creado: " + fmt(r.createdAt));

        // botones visibles según contexto y estado
        if (isForRecolector) {
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

    @Override public int getItemCount() { return data.size(); }

    // helpers para actualizar desde la Activity
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

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDireccion, tvFecha, tvEstado, tvCreado;
        Button btnAceptar, btnCancelar, btnEnCamino, btnEntregado, btnConfirmar;
        VH(@NonNull View v) {
            super(v);
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

    private String fmt(long ms) {
        if (ms <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(ms));
    }
}

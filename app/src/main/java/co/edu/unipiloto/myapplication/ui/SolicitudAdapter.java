package co.edu.unipiloto.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    /** Constructor para panel de CLIENTE */
    public SolicitudAdapter(List<SolicitudRepository.SolicitudItem> itemsCliente) {
        if (itemsCliente != null) {
            for (SolicitudRepository.SolicitudItem it : itemsCliente) {
                data.add(new Row(it.id, it.direccion, it.fecha, it.franja, it.estado, it.createdAt));
            }
        }
    }

    /** Constructor para panel de RECOLECTOR */
    public SolicitudAdapter(List<Solicitud> itemsRecolector, boolean fromModel) {
        if (itemsRecolector != null) {
            for (Solicitud s : itemsRecolector) {
                data.add(new Row(s.id, s.direccion, s.fecha, s.franja, s.estado, s.createdAt));
            }
        }
    }



    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Row r = data.get(pos);
        h.tvDireccion.setText(r.direccion);
        h.tvFecha.setText(r.fecha + "  " + r.franja);
        h.tvEstado.setText(r.estado == null ? "PENDIENTE" : r.estado);
        h.tvCreado.setText("Creado: " + fmt(r.createdAt));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDireccion, tvFecha, tvEstado, tvCreado;
        VH(@NonNull View v) {
            super(v);
            tvDireccion = v.findViewById(R.id.tvDireccion);
            tvFecha     = v.findViewById(R.id.tvFecha);
            tvEstado    = v.findViewById(R.id.tvEstado);
            tvCreado    = v.findViewById(R.id.tvCreado);
        }
    }

    private String fmt(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(ms));
    }
}
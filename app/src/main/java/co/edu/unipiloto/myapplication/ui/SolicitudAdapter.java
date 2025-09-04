package co.edu.unipiloto.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository.SolicitudItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.VH> {

    private final List<SolicitudItem> data;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public SolicitudAdapter(List<SolicitudItem> data) {
        this.data = data;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SolicitudItem it = data.get(pos);
        h.tvDireccion.setText(it.direccion);
        h.tvFecha.setText(it.fecha + " " + it.franja);
        h.tvEstado.setText(it.estado);
        h.tvCreado.setText(sdf.format(new Date(it.createdAt)));
    }

    @Override
    public int getItemCount() { return data.size(); }

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
}

package co.edu.unipiloto.myapplication.adapters

import co.edu.unipiloto.myapplication.dto.SolicitudResponse

/**
 * Define el contrato para manejar el evento de cambio de estado de una solicitud
 * dentro del RecyclerView.
 */
interface OnRequestClickListener {
    /**
     * @param solicitudId El ID de la solicitud cuyo estado debe ser cambiado.
     * @param currentStatus El estado actual de la solicitud (String, e.g., "ASIGNADA").
     */
    fun onRequestStatusChange(solicitudId: Long, currentStatus: String)

    fun onMapRouteClick(solicitud: SolicitudResponse)
}
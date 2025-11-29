package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.SucursalAdapter
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SucursalResponse
import co.edu.unipiloto.myapplication.repository.SucursalRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Necesitas definir un Factory para crear el ViewModel con dependencias
class ManageBranchesViewModelFactory(private val repository: SucursalRepository) : ViewModelProvider.Factory {
    // El método 'create' ha cambiado en versiones recientes.
    // Usamos el que acepta solo modelClass para simplificar (aunque es menos común ahora).
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageBranchesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManageBranchesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ManageBranchesActivity : AppCompatActivity() {

    // 🌟 Uso del ViewModel 🌟
    private val viewModel: ManageBranchesViewModel by viewModels {
        // Inicialización del Repositorio y Factory
        // Se asume que RetrofitClient.sucursalService es un 'SucursalApi'
        val repository = SucursalRepository(RetrofitClient.sucursalService)
        ManageBranchesViewModelFactory(repository)
    }

    private lateinit var recyclerViewBranches: RecyclerView
    private lateinit var searchViewBranches: SearchView
    private lateinit var fabAddBranch: FloatingActionButton
    private lateinit var adapter: SucursalAdapter

    // --- MÉTODOS DE CICLO DE VIDA ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_branches)

        initViews() // ⬅️ Se resuelve al incluir la función aquí
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBranches()
    }

    // --- MÉTODOS FALTANTES O MOVIDOS ---

    // 💡 Implementación de initViews (Asumido que faltaba)
    private fun initViews() {
        recyclerViewBranches = findViewById(R.id.recyclerViewBranches)
        searchViewBranches = findViewById(R.id.searchViewBranches)
        fabAddBranch = findViewById(R.id.fabAddBranch)
    }

    private fun setupRecyclerView() {
        adapter = SucursalAdapter(
            onEditClick = { sucursal -> navigateToEditBranch(sucursal) },
            onDeleteClick = { sucursal -> viewModel.deleteBranch(sucursal) }
        )
        recyclerViewBranches.layoutManager = LinearLayoutManager(this)
        recyclerViewBranches.adapter = adapter
    }

    private fun setupListeners() {
        // 1. Botón para crear nueva sucursal
        fabAddBranch.setOnClickListener {
            // ⬅️ AddBranchActivity se resuelve si existe e importamos Intent
            val intent = Intent(this, AddBranchActivity::class.java)
            startActivity(intent)
        }

        // 2. Funcionalidad de Búsqueda
        searchViewBranches.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterBranches(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupObservers() {
        // Observar la lista de sucursales
        viewModel.branches.observe(this) { branchesList ->
            adapter.submitList(branchesList)
        }

        // Observar mensajes de error/éxito
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                // 🚨 CORRECCIÓN: Acceder a una función pública o usar el patrón Event
                // Asumimos que el ViewModel tiene una función para consumir el mensaje
                viewModel.clearErrorMessage() // ⬅️ Llamada a un nuevo método público en ViewModel
            }
        }
    }

    private fun navigateToEditBranch(sucursal: SucursalResponse) {
        Toast.makeText(this, "Abriendo edición de: ${sucursal.nombre}", Toast.LENGTH_SHORT).show()
        // ⬅️ AddBranchActivity se resuelve si existe e importamos Intent
        val intent = Intent(this, AddBranchActivity::class.java)
        intent.putExtra("BRANCH_ID", sucursal.id) // ⬅️ putExtra se resuelve con Intent
        startActivity(intent)
    }
}
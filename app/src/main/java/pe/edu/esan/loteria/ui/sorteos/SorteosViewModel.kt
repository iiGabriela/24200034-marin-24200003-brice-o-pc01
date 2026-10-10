package pe.edu.esan.loteria.ui.sorteos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import pe.edu.esan.loteria.data.model.Sorteo
import pe.edu.esan.loteria.data.repository.SorteoRepository
import pe.edu.esan.loteria.ui.components.Recurso
import pe.edu.esan.loteria.ui.components.comoRecurso

class SorteosViewModel : ViewModel() {

    private val sorteoRepository = SorteoRepository()

    /** Lista de sorteos en tiempo real (el listener se quita al salir de la pantalla). */
    val sorteos: StateFlow<Recurso<List<Sorteo>>> = sorteoRepository.escucharSorteos()
        .comoRecurso()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Recurso.Cargando)
}

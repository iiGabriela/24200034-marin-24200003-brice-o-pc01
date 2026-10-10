package pe.edu.esan.loteria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.esan.loteria.data.model.Bolilla
import pe.edu.esan.loteria.data.model.EstadoSorteo

/** Reproduce la demostración obligatoria de la PC01. */
class CalculoSorteoTest {

    private val jugadaA = listOf(5, 12, 18, 23, 34, 41)
    private val jugadaB = listOf(5, 12, 18, 23, 34, 42)
    private val publicadas = listOf(5, 12, 18, 23, 34, 41)

    private fun bolillas(cantidad: Int) = publicadas.take(cantidad).mapIndexed { i, n ->
        Bolilla(id = "${i + 1}", sorteoId = "sorteo-prueba", numero = n, orden = i + 1)
    }

    @Test
    fun sinBolillas_ceroAciertos() {
        assertEquals(0, CalculoSorteo.bolillasPublicadas(bolillas(0)))
        assertEquals(0, CalculoSorteo.aciertos(jugadaA, bolillas(0)))
    }

    @Test
    fun despuesDeLaQuintaBolilla_ambosTienenCincoAciertosYSinResultado() {
        val cinco = bolillas(5)
        assertEquals(5, CalculoSorteo.bolillasPublicadas(cinco))
        assertEquals(5, CalculoSorteo.aciertos(jugadaA, cinco))
        assertEquals(5, CalculoSorteo.aciertos(jugadaB, cinco))
        assertNull(CalculoSorteo.resultadoFinal(EstadoSorteo.EN_CURSO, jugadaA, cinco))
        assertNull(CalculoSorteo.resultadoFinal(EstadoSorteo.EN_CURSO, jugadaB, cinco))
    }

    @Test
    fun seisBolillasPeroEnCurso_noHayResultado() {
        assertNull(CalculoSorteo.resultadoFinal(EstadoSorteo.EN_CURSO, jugadaA, bolillas(6)))
    }

    @Test
    fun finalizadoConSeisBolillas_AGanaYBSigueIntentando() {
        val seis = bolillas(6)
        assertEquals(6, CalculoSorteo.aciertos(jugadaA, seis))
        assertEquals(5, CalculoSorteo.aciertos(jugadaB, seis))
        assertEquals(
            ResultadoFinal.GANADOR,
            CalculoSorteo.resultadoFinal(EstadoSorteo.FINALIZADO, jugadaA, seis)
        )
        assertEquals(
            ResultadoFinal.SIGUE_INTENTANDO,
            CalculoSorteo.resultadoFinal(EstadoSorteo.FINALIZADO, jugadaB, seis)
        )
    }

    @Test
    fun finalizadoConBolillasFaltantes_esperaResultados() {
        val cinco = bolillas(5)
        assertNull(CalculoSorteo.resultadoFinal(EstadoSorteo.FINALIZADO, jugadaA, cinco))
        assertEquals(
            "Sorteo finalizado. Esperando resultados…",
            CalculoSorteo.mensajeEstado(EstadoSorteo.FINALIZADO, cinco)
        )
    }

    @Test
    fun elOrdenNoAfectaLosAciertos() {
        val desordenadas = bolillas(6).reversed()
        assertEquals(6, CalculoSorteo.aciertos(jugadaA.shuffled(), desordenadas))
    }

    @Test
    fun mensajes_segunEstado() {
        assertEquals("Sorteo en curso", CalculoSorteo.mensajeEstado(EstadoSorteo.EN_CURSO, bolillas(2)))
        assertEquals(
            "Sorteo abierto. Esperando el inicio del sorteo…",
            CalculoSorteo.mensajeEstado(EstadoSorteo.ABIERTO, emptyList())
        )
    }

    @Test
    fun estadoSeLeeDesdeTextoDeFirestore() {
        assertEquals(EstadoSorteo.ABIERTO, EstadoSorteo.desde("abierto"))
        assertEquals(EstadoSorteo.EN_CURSO, EstadoSorteo.desde("en_curso"))
        assertEquals(EstadoSorteo.EN_CURSO, EstadoSorteo.desde("En curso"))
        assertEquals(EstadoSorteo.FINALIZADO, EstadoSorteo.desde(" FINALIZADO "))
        assertEquals(EstadoSorteo.DESCONOCIDO, EstadoSorteo.desde(null))
    }
}

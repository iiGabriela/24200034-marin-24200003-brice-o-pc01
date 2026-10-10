package pe.edu.esan.loteria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorJugadaTest {

    @Test
    fun jugadaValida_seOrdena() {
        val r = ValidadorJugada.validar(listOf("41", "5", "12", "18", "23", "34"))
        assertEquals(ValidadorJugada.Resultado.Valida(listOf(5, 12, 18, 23, 34, 41)), r)
    }

    @Test
    fun numeroRepetido_esInvalido() {
        val r = ValidadorJugada.validar(listOf("5", "5", "12", "18", "23", "34"))
        assertTrue(r is ValidadorJugada.Resultado.Invalida)
    }

    @Test
    fun fueraDeRango_esInvalido() {
        assertTrue(ValidadorJugada.validar(listOf("0", "5", "12", "18", "23", "34")) is ValidadorJugada.Resultado.Invalida)
        assertTrue(ValidadorJugada.validar(listOf("46", "5", "12", "18", "23", "34")) is ValidadorJugada.Resultado.Invalida)
    }

    @Test
    fun campoVacioONoNumerico_esInvalido() {
        assertTrue(ValidadorJugada.validar(listOf("", "5", "12", "18", "23", "34")) is ValidadorJugada.Resultado.Invalida)
        assertTrue(ValidadorJugada.validar(listOf("a", "5", "12", "18", "23", "34")) is ValidadorJugada.Resultado.Invalida)
    }

    @Test
    fun cantidadIncorrecta_esInvalida() {
        assertTrue(ValidadorJugada.validar(listOf("1", "2", "3", "4", "5")) is ValidadorJugada.Resultado.Invalida)
    }
}

package pe.edu.esan.loteria.domain

/**
 * Regla de la jugada: exactamente 6 números enteros distintos, entre 1 y 45 inclusive.
 */
object ValidadorJugada {
    const val CANTIDAD = 6
    const val MINIMO = 1
    const val MAXIMO = 45

    sealed interface Resultado {
        data class Valida(val numeros: List<Int>) : Resultado
        data class Invalida(val mensaje: String) : Resultado
    }

    fun validar(entradas: List<String>): Resultado {
        if (entradas.size != CANTIDAD) {
            return Resultado.Invalida("Debes ingresar exactamente $CANTIDAD números.")
        }
        val numeros = mutableListOf<Int>()
        entradas.forEachIndexed { indice, texto ->
            val posicion = indice + 1
            val limpio = texto.trim()
            if (limpio.isEmpty()) {
                return Resultado.Invalida("Completa el número $posicion.")
            }
            val numero = limpio.toIntOrNull()
                ?: return Resultado.Invalida("El número $posicion no es un entero válido.")
            if (numero !in MINIMO..MAXIMO) {
                return Resultado.Invalida("El número $posicion debe estar entre $MINIMO y $MAXIMO.")
            }
            numeros += numero
        }
        if (numeros.toSet().size != CANTIDAD) {
            return Resultado.Invalida("Los $CANTIDAD números deben ser distintos.")
        }
        return Resultado.Valida(numeros.sorted())
    }
}

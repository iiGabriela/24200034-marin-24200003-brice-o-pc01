# PC01 · Lotería — Desarrollo de Aplicaciones Móviles (ESAN, 2026-02)

Aplicación Android en **Kotlin + Jetpack Compose + Firebase Authentication + Cloud Firestore** que permite a un participante autenticarse, registrar una jugada de seis números y seguir un sorteo en tiempo real.

## Integrantes

| Código | Apellidos y nombres |
|---|---|
| `<código 1>` | `<Apellido Paterno Apellido Materno, Nombres>` |
| `<código 2>` | `<Apellido Paterno Apellido Materno, Nombres>` |

Repositorio: `<codigo1>-<apellido1>-<codigo2>-<apellido2>-pc01-dsm202602-loteria`

---

## 1. Requisitos

- Android Studio (Ladybug o superior) con JDK 17+ (el que trae Android Studio sirve).
- Un emulador o dispositivo con Android 8.0 (API 26) o superior.
- Un proyecto de Firebase.

Versiones usadas: AGP 8.7.3 · Gradle 8.11.1 · Kotlin 2.0.21 · Compose BOM 2024.12.01 · Firebase BOM 33.7.0 · compileSdk 35 · minSdk 26.

## 2. Configurar Firebase

1. En [Firebase Console](https://console.firebase.google.com) crea un proyecto.
2. **Agregar app Android** con el paquete **`pe.edu.esan.loteria`** (debe ser exacto).
3. Descarga **`google-services.json`** y cópialo en la carpeta **`app/`** del proyecto (junto a `app/build.gradle.kts`).
4. **Authentication → Método de acceso →** habilita **Correo electrónico/contraseña**.
5. **Firestore Database →** Crear base de datos.
6. **Firestore → Reglas →** pega el contenido de [`firestore.rules`](firestore.rules) y pulsa **Publicar**.

## 3. Ejecutar

1. Abre la carpeta del proyecto en Android Studio (**File → Open**).
2. Espera el **Gradle Sync**.
3. Ejecuta la configuración **app** en el emulador (▶).
4. Para la demostración, abre **dos emuladores** y ejecuta la app en ambos (selector de dispositivos → *Select Multiple Devices*).

Pruebas unitarias de los cálculos: clic derecho en `app/src/test` → **Run 'Tests in ...'**, o `./gradlew test`.

---

## 4. Estructura de datos en Firestore

```
sorteos/{sorteoId}                      ← cabecera del sorteo
    nombre : string      "Sorteo de prueba PC01"
    fecha  : string      "12/10/2026"   (también acepta timestamp)
    hora   : string      "20:00"        (opcional; se muestra junto a la fecha)
    estado : string      "abierto" | "en_curso" | "finalizado"

sorteos/{sorteoId}/bolillas/{bolillaId} ← bolillas publicadas (relación por ubicación)
    numero : number      5
    orden  : number      1..6

sorteos/{sorteoId}/jugadas/{uid}        ← una jugada por usuario (id del doc = UID)
    sorteoId : string
    uid      : string
    email    : string
    numeros  : array<number>   [5, 12, 18, 23, 34, 41]
    registradoEn / actualizadoEn : timestamp
```

- Los números se guardan como **valores numéricos**.
- `GANADOR` / `SIGUE INTENTANDO` **no se guardan** en Firestore: los calcula la app.

### Crear el sorteo de prueba (desde la consola)

1. Firestore → **Iniciar colección** → ID: `sorteos`.
2. ID del documento: `sorteo-prueba`. Campos:
   - `nombre` (string) = `Sorteo de prueba PC01`
   - `fecha` (string) = `12/10/2026`
   - `hora` (string) = `20:00` (opcional)
   - `estado` (string) = `abierto`

### Publicar una bolilla (desde la consola)

En el documento `sorteos/sorteo-prueba` → **Iniciar colección** → ID: `bolillas`. Crea un documento por bolilla (ID sugerido `1`, `2`, …):

- `numero` (**number**) = `5`
- `orden` (**number**) = `1`

> Usa el tipo **number** (no string) para `numero` y `orden`.

---

## 5. Demostración obligatoria

| Participante | Jugada | Resultado esperado |
|---|---|---|
| Usuario A | 5, 12, 18, 23, 34, 41 | 6 aciertos · GANADOR |
| Usuario B | 5, 12, 18, 23, 34, 42 | 5 aciertos · SIGUE INTENTANDO |

0. Con el sorteo en `abierto`, registra la cuenta y la jugada de A en el emulador 1 y la de B en el emulador 2. Ambos entran a **Ver seguimiento en vivo** (muestra “Esperando el inicio del sorteo…”).
1. En la consola cambia `estado` a `en_curso` → ambas pantallas muestran **Sorteo en curso** y ya no permiten modificar la jugada.
2. Publica las bolillas **5, 12, 18, 23, 34, 41** una por una (`orden` 1 a 6), observando cada una en ambas pantallas antes de la siguiente.
3. Tras la 5.ª bolilla: ambos tienen **5 aciertos** y todavía no hay resultado definitivo.
4. Tras la 6.ª bolilla cambia `estado` a `finalizado` → A muestra **GANADOR** y B **SIGUE INTENTANDO**.
5. Cierra y vuelve a abrir la app en una sesión: la sesión de Firebase se conserva, se entra al sorteo y se recuperan la jugada, las bolillas y el resultado.

En **Logcat** (filtro `LoteriaListener`) se ve cuándo se registra y se remueve cada listener, y las bolillas recibidas.

---

## 6. Explicación de la solución

### Autenticación — `data/repository/AuthRepository.kt`, `ui/auth/`
- `createUserWithEmailAndPassword` y `signInWithEmailAndPassword` de Firebase Auth (con `await()` de coroutines).
- Validaciones locales (correo con formato válido, contraseña ≥ 6, confirmación) y traducción de errores de Firebase a mensajes en español (`mensajeErrorAuth`).
- El correo del usuario autenticado se muestra en la barra superior de todas las pantallas posteriores al login.
- Firebase conserva la sesión: al reabrir la app, `currentUser` no es nulo y se entra directo a los sorteos.

### Persistencia y validaciones — `data/repository/JugadaRepository.kt`, `domain/ValidadorJugada.kt`
- `ValidadorJugada`: exactamente 6 enteros **distintos** entre **1 y 45**.
- La jugada se guarda en `sorteos/{sorteoId}/jugadas/{uid}`: al usar el UID como id hay **una jugada por usuario y sorteo**, y solo se lee la del usuario de la sesión.
- Se guarda con una **transacción**: lee el estado del sorteo y solo escribe si es `abierto`. Si el sorteo ya inició se rechaza.
- `firestore.rules` repite las validaciones en el servidor (UID propio, sorteo abierto, 6 números válidos y distintos).

### Escucha de cambios en tiempo real — `data/repository/SorteoRepository.kt`, `ui/seguimiento/SeguimientoViewModel.kt`
- Cada `addSnapshotListener` está envuelto en un `callbackFlow`; Firestore llama al listener cada vez que cambia el estado o se agrega una bolilla y el valor se emite con `trySend`.
- `combine(sorteo, bolillas, jugada)` recalcula el estado de la pantalla cada vez que cualquiera de las tres fuentes cambia. **No hay consultas periódicas.**
- `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))` comparte **una sola** suscripción (sin listeners duplicados).
- Al salir de la pantalla, la suscripción se cancela y `awaitClose { registro.remove() }` **elimina los listeners**. `collectAsStateWithLifecycle()` deja de observar cuando la pantalla no está visible.

### Cálculos — `domain/CalculoSorteo.kt`
- **Bolillas publicadas** = cantidad de bolillas recibidas (0 a 6).
- **Aciertos** = `jugada.toSet() ∩ bolillas.numeros.toSet()` → cantidad de números distintos acertados (el orden no importa).
- **Resultado final** solo si `estado == finalizado` **y** hay 6 bolillas: `GANADOR` si aciertos == 6, si no `SIGUE INTENTANDO`. En otro caso se muestra un mensaje de espera.
- Las pruebas en `app/src/test/.../CalculoSorteoTest.kt` reproducen la demostración (A gana, B sigue intentando, 5 aciertos tras la 5.ª bolilla).

### Navegación — `ui/navigation/AppNavigation.kt`
`Registro ⇄ Login → Sorteos → Registrar jugada → Seguimiento`. Todas las pantallas tienen botón **volver**, botones para **avanzar** y **cerrar sesión**.

## 7. Estructura del proyecto

```
app/src/main/java/pe/edu/esan/loteria/
├── MainActivity.kt
├── data/
│   ├── model/Modelos.kt               (Sorteo, Bolilla, Jugada, EstadoSorteo)
│   └── repository/
│       ├── AuthRepository.kt
│       ├── FirestoreMapper.kt
│       ├── JugadaRepository.kt
│       └── SorteoRepository.kt
├── domain/
│   ├── CalculoSorteo.kt
│   └── ValidadorJugada.kt
└── ui/
    ├── auth/        (AuthViewModel, LoginScreen, RegistroScreen)
    ├── components/  (TopBar, BolillaCirculo, EstadoChip, Recurso)
    ├── jugada/      (JugadaScreen, JugadaViewModel)
    ├── navigation/  (AppNavigation, Rutas)
    ├── seguimiento/ (SeguimientoScreen, SeguimientoViewModel)
    ├── sorteos/     (SorteosScreen, SorteosViewModel)
    └── theme/       (Theme.kt)
```

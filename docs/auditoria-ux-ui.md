# Auditoría UX/UI — Aegis (pre–Google Play)

> Rol: Senior UX/UI + Product Designer. Objetivo: encontrar TODOS los problemas reales antes de publicar.
> Metodología: inspección del código (`ui/theme`, `ui/navigation`, todas las pantallas núcleo, onboarding, auth y componentes) + medición cuantitativa del design system. **No se ha tocado código**: esto es el diagnóstico.

## Resumen ejecutivo

Aegis tiene una **base sólida y detalles premium reales** (timer flotante arrastrable, pantalla siempre encendida en sesión, notificación de sesión, diálogo de salida con 3 opciones, indicador de sync, desglose de XP, transiciones de tabs). Pero **no tiene todavía un design system real**: la tipografía está sin definir y los valores están hardcodeados por todas partes. Datos medidos en el código:

- **20 tamaños de fuente distintos** (8sp → 48sp), **39 usos de texto ≤9sp** (por debajo del mínimo legible). `Type.kt` solo define `bodyLarge`.
- **12 radios de esquina distintos** (1, 2, 3, 4, 6, 8, 10, 12, 16, 20, 28, 50dp).
- **~107 colores hardcodeados** (41 hex `Color(0x…)` + 66 `Color.White/Black/…`) pese a la regla del proyecto "colores siempre vía tema".
- **3 rojos "destructivos" distintos** (`#CF6679` tema, `#E57373` timer, `#B3261E` borrar ejercicio) + un verde ad-hoc `#7FB069`.
- **67 `contentDescription = null`**, varios en botones interactivos (accesibilidad/TalkBack).
- **Sin tokens de espaciado** (no existe `Dimens.kt`).

La sensación general es de "app muy trabajada por un desarrollador con buen gusto", a un paso de "producto profesional". Lo que separa las dos cosas aquí es **consistencia sistematizada** (tokens), **estados** (primer uso, offline, carga) y **pulido de microinteracciones/accesibilidad**.

---

## 🔴 Problemas críticos

### C1 — Cancelar la sesión activa sin confirmación (pérdida de datos)
- **Pantalla:** SelectRoutineScreen (`ActiveSessionBanner`).
- **Problema:** el botón "CANCELAR SESIÓN" del banner llama directamente a `workoutViewModel.cancelWorkout { }` sin diálogo. En cambio, cancelar al intentar arrancar OTRA rutina (diálogo "bloqueado") y el gesto de "atrás" en la sesión **sí** confirman. Incoherente y peligroso.
- **Por qué importa:** un toque accidental borra un entrenamiento en curso sin vuelta atrás. Es la peor pérdida posible en una app de gym.
- **Solución:** reutilizar el diálogo de "Abandonar sesión" (Continuar / Abandonar) antes de `cancelWorkout`. Nunca borrar en un solo toque.

### C2 — "Finalizar entrenamiento" sin protección anti doble-pulsación
- **Pantalla:** ActiveSessionScreen (botón finalizar) y el flujo de guardado.
- **Problema:** el botón queda `enabled = hasAnyData` y no se bloquea tras el primer toque; `finishWorkout` + `incrementDisciplineDay` + navegación pueden dispararse dos veces con un doble toque rápido → **sesión duplicada** en el historial. Esto contamina volumen, XP, racha y el competitivo (Panteón). (Los botones de login/registro SÍ se protegen con `!isLoading`; este no.)
- **Por qué importa:** corrompe datos que alimentan stats y ranking; difícil de detectar y de limpiar.
- **Solución:** flag `isSaving` en el ViewModel; deshabilitar el botón en cuanto se pulsa y hasta que la navegación ocurra.

### C3 — Arranque en pantalla negra vacía (sin splash/marca/carga)
- **Pantalla:** AegisNavigation (`onboardingCompleted == null` → `Box(background = Color.Black)`), en cada arranque en frío.
- **Problema:** mientras se lee DataStore se muestra un rectángulo negro liso, sin logo ni indicador. Además usa `Color.Black` puro, no el negro de marca (`#050505`).
- **Por qué importa:** es lo PRIMERO que ve todo usuario. Un negro vacío se percibe como "se ha colgado / ha crasheado". Mata la primera impresión en la Store.
- **Solución:** splash real (logo AEGIS + escudo sobre `BackgroundBlack`) usando la SplashScreen API, y ese mismo estado como placeholder mientras carga el estado.

---

## 🟠 Problemas importantes

### Design System (raíz de la mayoría de inconsistencias)

**O1 — No existe escala tipográfica.** `Type.kt` solo define `bodyLarge`; hay 20 tamaños inline (8→48sp). → Definir `Typography` (display/title/body/label/caption con line-height y weight) y consumir `MaterialTheme.typography.*`. Elimina de golpe decenas de inconsistencias.

**O2 — ~107 colores hardcodeados.** 41 `Color(0x…)` + 66 `Color.White/Black`. Rompe la regla del propio proyecto y hace imposible ajustar la marca en un sitio. → `Color.Black` en botones bronce debe ser `onPrimary`; blancos → `onBackground`/`onSurface`.

**O3 — Colores semánticos inconsistentes.** 3 rojos destructivos (`#CF6679`, `#E57373`, `#B3261E`) y un verde suelto `#7FB069`. → Un único `AegisError` y añadir `AegisSuccess`/`AegisWarning` como tokens; usarlos siempre.

**O4 — 12 radios de esquina distintos.** → Escala fija: 4 (chips), 8 (cards/botones), 12 (contenedores), 16 (destacados), full (círculos). Documentarla.

**O5 — Sin tokens de espaciado.** Padding/márgenes inline en todo (24/20/16/12…). → `Spacing` (xs4/s8/m12/l16/xl24/xxl40) y aplicar. Hoy conviven paddings laterales de 16, 20 y 24dp según pantalla.

**O6 — Dos sistemas de diálogo.** `AegisAlertDialog` (28dp, bordeado) vs `AlertDialog` crudo en ActiveSession (salida) y Login (recuperar contraseña). → Extender `AegisAlertDialog` (p. ej. soporte de N botones) y usarlo siempre.

**O7 — Dos estados vacíos de rutinas distintos.** RoutineScreen usa icono `FitnessCenter` + `routines_empty_title/subtitle`; SelectRoutineScreen usa icono `Add` + `no_routines_message`. Mismo concepto, dos diseños/copys. → Un solo `EmptyState` reutilizable.

### Accesibilidad

**O8 — Texto ilegible por tamaño.** 39 usos de ≤9sp y labels de la bottom bar a **8sp**. → Mínimo 11–12sp para texto; nunca por debajo de 10sp. Revisar que todo escale con la fuente del sistema (usar `sp`, ya se hace, pero subir mínimos).

**O9 — 67 `contentDescription = null`.** Incluye botones interactivos: back en Identity/Metrics/Register (`Icon(Icons.Default.ArrowBack, contentDescription = null)`), varios iconos de acción. → TalkBack no los anuncia. Poner descripción a todo lo interactivo; `null` solo en iconos puramente decorativos.

**O10 — Contraste bajo en texto secundario.** `AegisSteel #70706B` sobre `#050505` a 10–12sp queda por debajo de WCAG AA. → Subir el gris para captions (p. ej. `#9A9A93`) o no usarlo por debajo de 12sp.

**O11 — Iconos direccionales no auto-mirrored.** `Icons.Default.ArrowBack`/`ArrowForward` en varias pantallas (hay warnings de deprecación). → `Icons.AutoMirrored.Filled.*`.

### Estados de UI y feedback

**O12 — Stats sin estado de primer uso (copy engañoso).** Un usuario sin historial ve la analítica vacía con el texto "Ningún ejercicio coincide con el filtro" (`stats_empty_filtered`) aunque no haya ningún filtro. → Empty state real: "Completa tu primer entrenamiento para ver tus estadísticas", y reservar el copy de filtro solo cuando de verdad hay filtro activo.

**O13 — Estado de sync/offline solo en Profile.** `SyncIndicator` existe únicamente en ProfileScreen. En Stats, Historial, Rutinas, Panteón no se refleja si hay error de sync/estás offline. → Estado offline global (banner discreto en la TopBar) o al menos replicar el indicador en las pantallas con datos de nube.

**O14 — Permiso de notificaciones sin priming.** ActiveSessionScreen lanza `POST_NOTIFICATIONS` en cuanto empiezas el primer entreno, sin explicar por qué; si se deniega, no hay fallback ni aviso. → Pantalla/tooltip previo ("para avisarte del descanso aunque salgas de la app"); si se deniega, degradar con elegancia.

**O15 — Callejón sin salida en sesión activa.** `ActiveSessionScreen` hace `val currentSession = session ?: return` ANTES del Scaffold. Si se navega a `active_session/{id}` con una rutina inexistente/borrada, `startWorkout` no se llama y queda una **pantalla en blanco sin TopBar ni back**. → UI de fallback ("no se pudo cargar la sesión") con botón volver.

**O16 — Confirmaciones que no hacen nada en silencio.** En "crear rutina" y "entrenamiento libre", si el nombre está en blanco el botón confirmar simplemente no reacciona (no hay error ni pista). → Deshabilitar el confirmar mientras esté vacío, o mostrar un hint inline.

### Onboarding / flujo (producto)

**O17 — La biografía del onboarding se descarta.** IdentityScreen captura `bio` y lo pasa a `onContinue`, pero la navegación lo ignora (`{ name, _, _ -> }`). El usuario escribe su bio y se pierde. → Persistirla (`updateBio`) o quitar el campo. Un input que no hace nada en el primer minuto de uso destruye confianza.

**O18 — Se piden métricas personales antes de aportar valor y sin poder saltar.** Flujo: Welcome → Identity → Metrics (altura/peso/sexo obligatorios) → Register. El usuario debe dar peso/sexo/altura antes de ver la app o crear cuenta. → Registrar primero, o permitir "Saltar" y pedir métricas de forma contextual (la primera vez que abre Panteón/Perfil). Reduce el drop-off de instalación.

**O19 — Texto hardcodeado y sin traducir en Metrics.** `MetricInput(label = "ALTURA CONFIGURACIÓN", unit = "CM", …)` — literal en código, solo español, y copy raro. → `stringResource(R.string.label_height)` ("Altura") + unidad por recurso.

**O20 — Guía de unidades incoherente.** Metrics muestra la altura en "CM"; el hint de Ajustes dice "Ej: 1.75" (metros). → Unificar a cm en todos los sitios (el cálculo de IMC ya asume cm).

### Consistencia

**O21 — Copy de borrado inconsistente.** Borrar rutina usa los defaults de `AegisAlertDialog` → botones "SÍ / NO"; borrar ejercicio usa "ELIMINAR". → "ELIMINAR / CANCELAR" en ambos (y nunca "SÍ/NO" para destructivo).

**O22 — Patrón de estado mezclado entre ViewModels.** `ProfileViewModel`/`StatsViewModel` exponen `StateFlow` inmutable; `RoutinesViewModel` expone `var routines = mutableStateListOf<Routine>()` y `var …searchQuery by mutableStateOf` **mutables y públicos**. Incoherente con la convención "solo StateFlow" y permite que la UI mute el estado del VM. → Exponer inmutable (`StateFlow`/`State`), mutar solo dentro del VM.

---

## 🟡 Mejoras

- **Y1 — Marca genérica.** El "logo" es `Icons.Default.Shield` de Material y el wordmark "AEGIS" usa la fuente del sistema. Es el mayor delator de "app de dev". → Logo/escudo propio (vector) + una tipografía de marca para el wordmark.
- **Y2 — Doble `imePadding()`.** Aplicado en el `NavHost` raíz y otra vez en `ProfileContent` → posible salto de layout con teclado. Dejar solo el del NavHost.
- **Y3 — `showBottomBar` por lista de exclusiones.** Es un `&&` de ~8 rutas excluidas; cada pantalla nueva obliga a acordarse de excluirla o la barra aparece donde no debe. → Allowlist de rutas de tab o flag `hidesBottomBar` por destino.
- **Y4 — "Ejercicios" no es tab, se entra por un toggle dentro de Rutinas.** Descubribilidad baja y dos formas de ver rutinas (tab + toggle). → Valorar tab propio o al menos un acceso claro.
- **Y5 — Icono central de la bottom bar = `Bolt` (rayo).** Ambiguo para "entrenar". → `FitnessCenter`/mancuerna.
- **Y6 — Sin skeletons de carga.** Listas de Stats/Historial pintan ceros/vacío mientras llega el primer dato de sync. → Shimmer/skeleton breve.
- **Y7 — Copy técnico.** `sync_cached` = "Datos locales (sin confirmar)" → "Guardado en este dispositivo". `sync_error` puede sugerir acción ("Sin conexión — se reintentará").
- **Y8 — Identity/Metrics sin scroll (a propósito).** Con teclado abierto en pantallas pequeñas, el contenido (avatar 140dp + bio 120dp + botón) puede recortarse. → Scroll defensivo o layout que colapse con IME.
- **Y9 — Ejercicio creado en caliente con `muscleGroup = ""`.** No puntúa en Panteón ni se clasifica; el usuario no lo sabe. → Pedir grupo al crearlo o avisar.
- **Y10 — Botón "GUARDAR" de medidas ambiguo.** Masa/grasa ya se guardan al teclear; "GUARDAR" crea un snapshot con fecha. → Copy "Guardar medida de hoy".
- **Y11 — Código muerto en el tema.** `Purple80/40`, `Pink…`, `LightColorScheme` (plantilla Material) sin usar; `MatteBlack`, `BackgroundBlackGrey` casi sin uso. → Limpiar para que el sistema de color sea claro.
- **Y12 — Ritmo vertical inconsistente.** Separaciones fijas 16/24/40dp según pantalla. → Aplicar la escala de espaciado (O5).
- **Y13 — 14 textos solo en español** (caen a ES en en/fr/de/it/pt). → Traducir los que faltan.
- **Y14 — Iconos de acción sin tint/size uniforme** (p. ej. el de Ajustes en Stats no lleva tint/size y el de Historial sí). → Estilo de `IconButton` unificado.

---

## 🟢 Nice to have

- **G1 — Haptics** en acciones clave (finalizar entreno, nuevo PR, subir de nivel).
- **G2 — Microanimación** al marcar serie/ejercicio completado (check con spring) y al subir la barra de XP.
- **G3 — Skeleton shimmer** de marca en cargas iniciales.
- **G4 — Transición compartida** del avatar Identity → Profile.
- **G5 — "Deshacer" (snackbar)** en borrados no críticos en vez de diálogo.
- **G6 — Pull-to-refresh** en Stats/Panteón para forzar sync manual.
- **G7 — Ilustración de marca** en los empty states principales (hoy son icono gris + texto).

---

## Fortalezas (lo que ya está a nivel producto)

- Timer de descanso **flotante y arrastrable** con arco de progreso y color de alerta en los últimos 10s.
- **Pantalla siempre encendida** durante la sesión + **notificación** persistente de sesión activa.
- Diálogo de salida de sesión con **3 opciones bien jerarquizadas** (Continuar / Pausar / Abandonar) y confirmación de ejercicios sin marcar.
- Botón de finalizar **deshabilitado hasta que hay datos** (buen gating).
- Biblioteca de ejercicios con **búsqueda, filtro por tag, secciones plegables y estados vacío/sin-resultados** bien resueltos.
- **Indicador de sincronización** con reintento (Syncing/Error/Cached).
- **Desglose de XP** expandible, **step progress** en onboarding, **transiciones** de tabs coherentes con el orden.

---

## 14. Auditoría pantalla por pantalla

> Escala 1–10. "Calidad" = sensación premium/store-ready.

### Welcome
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 8 · Calidad 7
- Problemas: logo genérico (Shield) y wordmark en fuente de sistema; overlay con `Color.Black` hardcodeado.
- Mejoras: logo propio; micro-animación de entrada; asegurar contraste del motto sobre la foto.

### Identity (onboarding paso 2)
UX 5 · UI 7 · Accesibilidad 5 · Consistencia 7 · Calidad 6
- Problemas: **bio se descarta** (O17); back sin `contentDescription`; sin scroll (riesgo con teclado).
- Mejoras: persistir bio o quitarla; permitir saltar; hacer el avatar claramente "tocable" (icono cámara).

### Metrics (paso 3)
UX 5 · UI 7 · Accesibilidad 5 · Consistencia 6 · Calidad 6
- Problemas: **"ALTURA CONFIGURACIÓN" hardcodeado/sin traducir** (O19); métricas obligatorias sin saltar (O18); unidades incoherentes (O20).
- Mejoras: label por recurso; opción "Saltar"; validación de rangos realistas.

### Register / Login (paso 4 / acceso)
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 7 · Calidad 8
- Problemas: back de Register usa icono **candado** (`Icons.Default.Lock`) en vez de flecha; iconos sin `contentDescription`.
- Mejoras: validación inline por campo (no un solo mensaje al final); mostrar requisitos de contraseña; back correcto. Bien: loading en botones, anti doble-submit, "olvidé contraseña".

### Email verification
UX 7 · UI 7 · Accesibilidad 6 · Consistencia 7 · Calidad 7
- Problemas: "atrás" hace **logout** (puede sorprender).
- Mejoras: explicar que volver cierra sesión; reenviar con cooldown visible (ya existe copy).

### Profile (landing)
UX 7 · UI 7 · Accesibilidad 6 · Consistencia 7 · Calidad 7
- Problemas: doble `imePadding` (Y2); "GUARDAR" ambiguo (Y10); verde hardcodeado; sync error fácil de perderse en medio del scroll.
- Mejoras: jerarquía (nivel + racha como héroe arriba); mover sync a la TopBar; tokens de color.

### Select routine (empezar)
UX 6 · UI 7 · Accesibilidad 6 · Consistencia 6 · Calidad 7
- Problemas: **cancelar sin confirmar** (C1); "Entrenamiento libre" (bronce sólido) compite en jerarquía con las rutinas; empty state sin CTA directo y distinto del de Rutinas (O7).
- Mejoras: confirmar cancelación; rutinas como acción primaria; unificar empty state.

### Active session (núcleo)
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 6 · Calidad 8
- Problemas: **doble-tap finalizar** (C2); **pantalla en blanco** si la rutina no existe (O15); diálogo crudo (O6); rojo `#E57373` propio.
- Mejoras: `isSaving`; fallback de sesión; unificar diálogo; hint de "arrastra el timer".

### Workout complete
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 6 · Calidad 8
- Problemas: muchos `Color(0x…)` hardcodeados (9); compartir imagen bien resuelto pero con copy fijo.
- Mejoras: tokens; celebrar PR con animación/haptic (G1/G2).

### Routine (lista + CRUD)
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 7 · Calidad 8
- Problemas: confirmación de borrado con "SÍ/NO" (O21); crear con nombre vacío no reacciona (O16).
- Mejoras: copy "ELIMINAR/CANCELAR"; empty state ya es bueno (mantener y reutilizar).

### Exercises library
UX 8 · UI 8 · Accesibilidad 6 · Consistencia 7 · Calidad 8
- Problemas: tercer rojo `#B3261E` (O3); `Row` con un solo botón (resto de código muerto).
- Mejoras: es de las mejores pantallas (búsqueda/filtro/secciones/vacíos); solo tokenizar color.

### Stats
UX 6 · UI 7 · Accesibilidad 6 · Consistencia 7 · Calidad 6
- Problemas: **sin estado de primer uso** y copy de filtro engañoso (O12); sin skeleton.
- Mejoras: empty state guiado; icono de Ajustes con tint/size como los demás.

### Panteón
UX 7 · UI 8 · Accesibilidad 6 · Consistencia 7 · Calidad 8
- Problemas: densidad alta de información; `Color(0x…)` para tiers (aceptable, viene de `RankTier.colorHex`).
- Mejoras: onboarding/leyenda de qué es un "rango"; loading ya existe.

### Settings (perfil)
UX 8 · UI 7 · Accesibilidad 6 · Consistencia 7 · Calidad 7
- Problemas: mezcla de `Color.White`/hardcodes; filas densas.
- Mejoras: tokens; agrupar en tarjetas con más aire.

### Bottom bar
UX 7 · UI 7 · Accesibilidad 4 · Consistencia 8 · Calidad 7
- Problemas: **labels a 8sp** (O8); icono central `Bolt` ambiguo (Y5); sin indicador de selección más allá del color.
- Mejoras: subir labels a 10–11sp; icono mancuerna; micro-indicador de tab activo.

---

## 15. Flujos completos

### Flujo A — Alta de usuario nuevo
`Welcome → Identity → Metrics → Register → Email verification → Profile`
- **Pasos:** 5 pantallas antes de usar la app. **Fricción alta.**
- **Problemas:** se piden altura/peso/sexo **antes** de aportar valor y sin poder saltar (O18); la **bio se pierde** (O17); si abandonas en Register, el onboarding ya quedó "completado" localmente pero sin cuenta.
- **Oportunidad:** registrar primero (o Google 1-tap) → entrar → pedir métricas la primera vez que se necesitan (Panteón). Bio opcional y persistida. Objetivo: time-to-value < 30s.

### Flujo B — Primer entrenamiento
`Profile (banner "IR") → Train → Select routine → Active session → Complete → Profile`
- **Pasos:** correctos y con buen feedback (progreso, timer, resumen).
- **Problemas:** C1 (cancelar sin confirmar) y C2 (doble-tap) viven aquí; si es usuario nuevo sin rutinas, el empty state de Select no empuja bien a crear una.
- **Oportunidad:** para usuario sin rutinas, ofrecer "Entrenamiento libre" o una rutina de ejemplo de un toque.

### Flujo C — Crear rutina
`Routine → diálogo (nombre + icono) → EditRoutine → AddExercise`
- **Problemas:** nombre vacío no reacciona (O16); el salto diálogo→editor está bien.
- **Oportunidad:** plantillas (PPL, Full-body) para arrancar sin construir desde cero.

### Flujo D — Corregir un dato mal metido en el historial
`Stats → Historial → editar sesión → guardar (recalcula Panteón)`
- Funciona (implementado en sesiones previas). Riesgo: sin el guard de C2 podría haber sesiones duplicadas que confundan la edición.

### Flujo E — Login de usuario existente
`Welcome → Login → Profile`
- Sólido (loading, errores, Google, recuperar contraseña). Mejora: validación inline y mensajes de error más accionables.

---

## Orden de ataque sugerido

1. **🔴 C1, C2, C3** (datos y primera impresión) — rápidos y de alto impacto.
2. **Fundacional del design system: O1 (tipografía) + O5 (espaciado) + O2/O3/O4 (color/radios).** Desbloquea y elimina decenas de inconsistencias a la vez.
3. **O17–O20 (onboarding)** y **O12 (empty de Stats)** — impacto directo en conversión y primera sesión.
4. **Accesibilidad O8–O11.**
5. Resto de 🟠, luego 🟡 y 🟢.

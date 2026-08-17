# US-SOCIAL-UI — Rediseño de las pantallas sociales con Stitch

> Brief para generar en **Stitch** (Google) las 2 pantallas sociales de Aegis con el
> patrón visual del resto de la app. Se mantienen **separadas**: Amigos (pantalla propia)
> y Ranking (pestaña AMIGOS dentro del Panteón).

---

## 1. Historia de usuario

> **Como** usuario de Aegis
> **quiero** unas pantallas sociales bonitas y coherentes con la estética de la app
> **para** gestionar amigos y comparar mi rango con ellos sintiendo el mismo lujo que en el resto de Aegis.

**Criterios de aceptación**
- Las 2 pantallas usan la paleta, tipografía y componentes de Aegis (ver §2).
- **Amigos**: reclamar @usuario, añadir por @usuario, aceptar/rechazar solicitudes, ver y eliminar amigos, con avatar circular.
- **Ranking**: podio top-3, lista con avatar + nivel + medalla de rango, mi fila resaltada, filtro por músculo, y comparación cara a cara músculo a músculo al tocar un amigo.
- Estados: vacío, cargando y error contemplados.

---

## 2. Sistema de diseño de Aegis  *(pegar como contexto de estilo en Stitch)*

**Concepto:** lujo oscuro con temática romana (el "Panteón"). Como una marca de relojería/joyería de lujo cruzada con un juego competitivo por rangos. Sobrio, premium, elegante.

**Colores**
| Rol | Hex |
|---|---|
| Fondo app (negro casi puro) | `#050505` |
| Tarjetas / superficies | `#0E0E0E` |
| Elementos flotantes | `#242424` |
| Acento primario (bronce/oro viejo) | `#B39371` |
| Acento presionado | `#8A6E51` |
| Oro puro (medallas, detalles) | `#D4AF37` |
| Texto principal (blanco roto) | `#F2F2F2` |
| Texto secundario (crema) | `#ECECE4` |
| Texto desactivado / bordes | `#70706B` |
| Error (rojo suave) | `#CF6679` |

**Rangos competitivos** (de menor a mayor, cada uno con su medalla y color):
`BRONCE #CD7F32` · `PLATA #C0C0C0` · `ORO #D4AF37` · `PLATINO #E5E4E2` · `DIAMANTE #4FC3F7` · `TITÁN #B57BFF` · (sin rango `#3A3A3A`).

**Tipografía:** sans-serif del sistema. Etiquetas y títulos en **MAYÚSCULAS**, peso **black/bold**, con **espaciado entre letras amplio**. Cuerpo en peso normal.

**Formas y componentes:**
- Tarjetas con esquinas redondeadas (~12dp), fondo oscuro semitransparente sobre el negro.
- Botones redondeados (~8dp), relleno bronce con texto negro en mayúsculas.
- Insignias de rango: rectángulo de color del tier con el nombre en negro y mayúsculas.
- Medallas de rango: iconos circulares metálicos (bronce→titán).
- Barras de progreso finas (~6dp).
- Tema **oscuro** siempre. Idioma **español**.

---

## 3. Pantalla A — AMIGOS  *(gestión; se abre con el icono 👥 del Panteón)*

**Objetivo:** reclamar tu identidad social y gestionar tu red de amigos.

**Secciones (de arriba a abajo):**
1. **Barra superior:** flecha de volver + título `AMIGOS`.
2. **Mi tarjeta de perfil:** avatar circular grande con borde bronce, mi `@usuario` en grande, mi `NIVEL X`, y a la derecha mi medalla de rango global.
   - *Si aún no tengo @usuario:* en su lugar, tarjeta para **reclamarlo** (campo con prefijo `@` + botón `RESERVAR` + subtítulo con las reglas: 3–20, minúsculas/números/_).
3. **Añadir amigo:** input con prefijo `@` y placeholder "usuario de tu amigo" + botón bronce `ENVIAR SOLICITUD`.
4. **Solicitudes recibidas:** tarjetas con avatar + `@usuario` + botón bronce `Aceptar` y botón con borde `Rechazar`.
5. **Mis amigos:** filas con avatar circular, `@usuario`, medalla de rango pequeña con su tier, y acción de **eliminar**.
6. **Estado vacío** elegante cuando no hay amigos ("Aún no tienes amigos…").

**Funcionalidad:** reclamar @usuario (única vez) · añadir por @usuario exacto · aceptar/rechazar · eliminar amigo · feedback tipo snackbar en cada acción.

---

## 4. Pantalla B — RANKING  *(pestaña AMIGOS dentro del Panteón)*

**Objetivo:** ver quién manda y compararte con tus amigos.

**Secciones (de arriba a abajo):**
1. **Pestañas del Panteón:** `MIS RANGOS` · `AMIGOS` (activa, subrayada en bronce) · `LIGA` (con candado).
2. **Filtro por músculo:** chips horizontales deslizables — `GLOBAL` (activo), `PECHO`, `ESPALDA`, `HOMBRO`, `BRAZO`, `PIERNA`, `CORE`. Al elegir uno, la lista se reordena por ese grupo.
3. **Podio top-3:** tres puestos destacados; el 1º en el centro, más grande y elevado, con avatar circular grande + corona/medalla dorada + `@usuario` + nivel; 2º (plata) y 3º (bronce) a los lados. Sutil resplandor dorado de fondo.
4. **Lista del resto (#4+):** filas con nº de posición, avatar, `@usuario`, `NIVEL X`, y a la derecha la medalla + nombre del tier.
5. **Mi fila resaltada:** fondo bronce translúcido + etiqueta `TÚ`, siempre visible.
6. **Comparación (al tocar un amigo):** cabecera con dos avatares enfrentados (`TÚ` vs `@amigo`) y un `VS` dorado; debajo, 6 filas (una por grupo muscular) con mi insignia de tier a la izquierda, el nombre del músculo en el centro y la del amigo a la derecha, **resaltando con borde dorado la del ganador** de cada grupo.

**Funcionalidad:** clasificación yo+amigos por rango global o por músculo (filtro) · podio top-3 · comparación por grupo al tocar un amigo.

**Privacidad (importante):** solo se comparten **tiers + nombre + nivel**, nunca pesos ni historial.

---

## 5. Prompts listos para pegar en Stitch

> Pega cada prompt por separado, genera, e itera con retoques ("el podio más grande",
> "más contraste", "medallas más metálicas"). El bloque de estilo ya va incluido en cada uno.

### Prompt A — Pantalla Amigos
```
Diseña una pantalla móvil de "Amigos" para una app de gimnasio de lujo oscuro con temática romana (el "Panteón"). Estilo: fondo negro casi puro (#050505), tarjetas gris muy oscuro semitransparentes con esquinas redondeadas, acento bronce/oro viejo (#B39371) y oro puro (#D4AF37) para medallas y detalles; texto blanco roto (#F2F2F2) y gris metálico (#70706B) para secundarios. Etiquetas en MAYÚSCULAS, tipografía sans-serif de peso alto (black/bold) con espaciado amplio entre letras. Sensación premium y sobria, como una marca de relojería de lujo cruzada con un juego competitivo por rangos.

De arriba a abajo:
1) Barra superior con flecha de volver y título "AMIGOS".
2) Tarjeta de mi perfil: avatar circular grande con borde bronce, mi @usuario en grande, "NIVEL 12", y a la derecha una medalla de rango dorada.
3) Campo para añadir amigo: input con prefijo "@" y placeholder "usuario de tu amigo", y un botón bronce "ENVIAR SOLICITUD".
4) Sección "SOLICITUDES": tarjetas con avatar circular + @usuario + botón bronce "Aceptar" y botón con borde "Rechazar".
5) Sección "MIS AMIGOS": filas con avatar circular, @usuario, una medalla de rango pequeña con su tier, e icono para eliminar.
Incluye un estado vacío elegante para cuando no haya amigos. Tema oscuro. Idioma español.
```

### Prompt B — Pantalla Ranking (podio + filtro)
```
Diseña una pantalla móvil de "Ranking / Clasificación" para una app de gimnasio de lujo oscuro con temática romana. Estilo: fondo negro casi puro (#050505), tarjetas oscuras redondeadas, acento bronce (#B39371) y oro (#D4AF37), texto blanco roto (#F2F2F2), etiquetas en MAYÚSCULAS con tipografía black y espaciado amplio; sensación premium y competitiva.

De arriba a abajo:
1) Pestañas superiores: "MIS RANGOS", "AMIGOS" (activa, subrayada en bronce), "LIGA" (con candado).
2) Fila de chips de filtro horizontales: "GLOBAL" (activo), "PECHO", "ESPALDA", "HOMBRO", "BRAZO", "PIERNA", "CORE".
3) Podio de los 3 primeros: el 1º en el centro, más grande y elevado, con avatar circular grande, corona/medalla dorada, @usuario y nivel; el 2º (plata) y 3º (bronce) a los lados, más pequeños; sutil resplandor dorado de fondo.
4) Lista del resto (#4 en adelante): filas con número de posición, avatar circular, @usuario, "NIVEL X", y a la derecha una medalla de rango con el nombre del tier.
5) Mi propia fila resaltada con fondo bronce translúcido y la etiqueta "TÚ".
Los rangos de menor a mayor: Bronce (#CD7F32), Plata (#C0C0C0), Oro (#D4AF37), Platino (#E5E4E2), Diamante (#4FC3F7), Titán (#B57BFF). Tema oscuro. Idioma español.
```

### Prompt B2 — Comparación cara a cara (detalle al tocar un amigo)
```
Diseña una tarjeta/pantalla de "Comparación" cara a cara entre dos usuarios de una app de gimnasio de lujo oscuro con temática romana. Estilo: fondo negro (#050505), acento bronce (#B39371) y oro (#D4AF37), texto blanco roto (#F2F2F2), etiquetas en MAYÚSCULAS con tipografía black.

Arriba: cabecera con dos avatares circulares enfrentados, a la izquierda "TÚ" y a la derecha "@amigo", con un "VS" dorado en el centro.
Debajo: una tabla de 6 filas, una por grupo muscular (PECHO, ESPALDA, HOMBRO, BRAZO, PIERNA, CORE). En cada fila: a la izquierda mi insignia de rango (rectángulo de color con el tier en mayúsculas), en el centro el nombre del músculo, a la derecha la insignia del amigo. Resalta con un borde dorado la insignia del que gana cada músculo.
Tema oscuro. Idioma español.
```

---

## 6. Qué hace falta en el backend para cada extra elegido

| Elemento | Estado de datos | Trabajo pendiente |
|---|---|---|
| Avatar circular | Hoy el avatar es **local** (no sincroniza) | Subir avatar al perfil (Storage o URL) para que los amigos lo vean; mientras, usar inicial sobre círculo bronce |
| Nivel en ranking | `PublicProfile.level` existe pero **no se rellena** (siempre 0) | Setearlo en `uploadMyProfileInternal` desde la gamificación |
| Filtro por músculo | **Ya disponible**: `PublicProfile.groupTiers` tiene los 6 grupos | Solo UI: reordenar la lista por el grupo elegido |
| Podio top-3 | Ya tenemos el orden por tier | Solo UI |

> Cuando Stitch te dé los diseños, los traducimos a Compose reutilizando el sistema de
> diseño de Aegis (`AegisTopBar`, medallas `RankMedal`, insignias `RankBadge`, tokens de `Color.kt`).

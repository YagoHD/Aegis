# US-LIGA — Liga mensual global

> Estado: **DISEÑO** (acordado con el usuario, sin implementar).
> Documento de producto. Antes de escribir código, esto es lo que queremos.

## User Story

> Como usuario, quiero **competir en una liga mundial que se reinicia cada mes**, subiendo de división cuando entreno más y mejor, para tener una **razón recurrente de esforzarme** y medirme con **todo el mundo**, no solo con mis amigos.

## Idea rectora

La app ya tiene **dos ejes** y la Liga NO debe duplicar el primero:

| Sistema | Mide | Ritmo | Rol |
|---|---|---|---|
| **Panteón (rangos)** | Fuerza: 1RM ÷ peso, por músculo | Lento | Identidad / prestigio de fuerza |
| **Liga (nuevo)** | **Esfuerzo del mes**, relativo al peso | Mensual, se reinicia | Competición dinámica: **todos pueden subir entrenando** |

Modelo de referencia: **Ligas de Duolingo** (no Clash). Clash rankea por *habilidad*; nosotros por *esfuerzo*. Duolingo = grupos de ~30, subes/bajas cada temporada por puntos de esfuerzo. Encaja 1:1.

## Decisiones cerradas

| Decisión | Elección |
|---|---|
| Métrica ("trofeo") | **Esfuerzo mensual** (tipo XP: volumen + constancia) |
| Justicia entre cuerpos | **Relativo al peso corporal** |
| Modelo de divisiones | **Clash/Duolingo real**: grupos de ~30 con ascenso/descenso |
| Duración de temporada | **Mensual** (mes natural) |
| Recompensa | **Triple**: prestigio + medalla de temporada · medallas que cuentan para **logros** · **desbloqueos cosméticos** por liga alta |

## Mecánica

- **Ligas (reutilizan tiers):** Bronce → Plata → Oro → Platino → Diamante → Titán.
- **Grupo:** ~30 usuarios de la misma liga (tamaño dinámico si hay pocos, ver Riesgos).
- **Ascenso/descenso al cierre de mes:** suben los **7 primeros**, bajan los **5 últimos**, el resto se queda (números Duolingo, calibrables). En la liga más alta (Titán) no se asciende; en la más baja (Bronce) no se desciende.
- **Reset:** al cambiar de mes los puntos vuelven a 0; se archiva una **medalla de temporada** (ej. "Diamante — Ago 2026").

### Fórmula de puntos (esfuerzo relativo al peso)

Por cada **serie completada** durante la temporada:

```
trabajoRelativo += (pesoEfectivo × reps) / pesoCorporal
```

- `pesoEfectivo` = el que ya guarda `ExerciseSet.weight` (en corporal/asistido ya incluye el peso corporal, ver load-type model).
- Dividir por `pesoCorporal` iguala a un usuario de 60 kg y otro de 100 kg.

```
puntosLiga = round( 50 × nºSesiones  +  K × trabajoRelativo  +  30 × semanasRacha )
```

- Reutiliza la filosofía de `LevelSystem` (50/sesión, 30/semana de racha), pero el **volumen es relativo** (no `20/1000 kg` absoluto). `K` se calibra para que una sesión típica dé un número "bonito".
- **Solo series completadas.** Topes de cordura anti-trampa (ver abajo).

## Recompensas (las tres)

1. **Prestigio:** liga actual visible (avatar/perfil), podio del grupo, tabla mundial top-N.
2. **Medalla de temporada** archivada por mes + liga alcanzada.
3. **Logros:** las medallas alimentan un sistema de logros (ej. "llega a Diamante", "gana tu grupo", "3 meses seguidos subiendo").
4. **Cosméticos:** marcos de avatar / insignias / colores exclusivos de ligas altas.

## Arquitectura e infraestructura

**Camino: Firebase Cloud Functions + Firestore** (ya usamos Firebase). El "servidor propio" es viable pero mucho más mantenimiento; se descarta salvo cambio de opinión.

### Modelo de datos (borrador)

```
leagueSeasons/{seasonId}                      // seasonId = "2026-08"
  meta: { start, end, status }

leagueMembers/{uid}
  { seasonId, liga, grupoId, puntos, nSesiones, trabajoRelativo, updatedAt, username, avatar }

leagueGroups/{seasonId}/{grupoId}/members/{uid}   // desnormalizado para leer el grupo (~30) barato
  { puntos, username, avatar, liga }

leagueMedals/{uid}/seasons/{seasonId}          // archivo de medallas
  { liga, posicion, subio/bajo }
```

### Piezas

- **Actualizar puntos:** al terminar un entreno, la app suma el delta de esa sesión al `leagueMembers/{uid}` de la temporada actual (idempotente por sesión).
- **Cloud Function programada (cron mensual, día 1):** por cada grupo → ordena por puntos → marca ascensos (top 7) / descensos (bottom 5) → **re-agrupa a todos de 30 en 30** en la liga que toque → crea la nueva temporada → resetea puntos → escribe medallas de la temporada cerrada.
- **Leaderboard del grupo:** leer `leagueGroups/{seasonId}/{grupoId}/members` (~30 docs) y ordenar en cliente. Barato.
- **Tabla mundial (opcional):** `orderBy(puntos) limit 100` sobre la liga más alta.
- **Reglas Firestore:** cada quien escribe **solo su** `leagueMembers/{uid}` (con topes); los grupos y medallas los escribe **solo la Function** (admin). Lectura de tu grupo permitida.

> ⚠️ **Despliegue:** Claude escribe el código de Functions y reglas, pero **las despliega el usuario** (Firebase CLI / consola). Requiere plan **Blaze** (pago por uso; capa gratuita amplia — para pocos usuarios ≈ gratis).

## Anti-trampas (importante: es global y competitivo)

- Solo cuentan **series completadas**.
- **Topes de cordura** por serie/sesión (ignorar pesos absurdos, cap de puntos por sesión).
- **Validación en servidor (Fase 3):** la Function puede recomputar el score desde el historial real del usuario (lo lee con permisos admin) en vez de fiarse del número que manda el cliente.
- Marcado de anomalías (saltos imposibles) para revisión.

## Plan por fases

| Fase | Alcance | Backend | Entregable testeable |
|---|---|---|---|
| **1** | Pestaña LIGA (UI), fórmula de puntos en cliente, colección global, **top-100 + amigos**, división provisional por umbrales | Solo Firestore | Ves tu liga y puntos, y una tabla, **ya** |
| **2** | Grupos de 30, **ascenso/descenso**, cierre y reset mensual (Cloud Function programada), medallas de temporada | Cloud Functions | El "Clash real" |
| **3** | Anti-trampas en servidor, **logros** desde medallas, **cosméticos**, temporadas archivadas | Cloud Functions | Sistema completo |

Aunque el destino es la Fase 2/3, **empezamos por la 1** para validar el sentimiento antes de la maquinaria pesada.

## Riesgos y preguntas abiertas

- **Cold-start (pocos usuarios):** grupos de 30 no se llenan. Mitigación: tamaño de grupo dinámico, o Liga solo entre "usuarios activos" hasta tener masa, o un único pool global al principio.
- **Coste/complejidad de Functions:** bajo con pocos usuarios, pero es infra nueva que mantener.
- **Anti-trampas:** el cliente calcula el score → hay que endurecer en Fase 3.
- **Calibrar `K`** y los cortes de ascenso/descenso con datos reales.

## Fuera de alcance (por ahora)

- Ligas por país/región.
- Premios físicos o económicos.
- Enfrentamientos 1v1 / retos directos (eso es otra US).

---

_Relacionado: `competitive_ranked_mode`, `friends-social`, `gamification` (memoria), `load-type-model`, `us-liga` (esta)._

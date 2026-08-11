package com.yago.aegis.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

/**
 * FirestoreDataSource gestiona toda la lectura y escritura en Firestore.
 *
 * Estructura en Firestore:
 * users/{userId}/data/
 *   ├── profile      → nombre, peso, altura, grasa, medidas, fechas de foto
 *   ├── routines     → lista de rutinas con sus ejercicios
 *   ├── exercises    → librería de ejercicios
 *   ├── history      → historial de entrenamientos
 *   ├── tags         → tags globales
 *   ├── settings     → preferencias de UI + temporizador (descanso/vibración/sonido)
 *   ├── bodyHistory  → snapshots corporales (peso/medidas)
 *   └── photoHistory → registro de fotos SIN imágenes (solo fechas; las URIs no viajan)
 *
 * Política de sincronización (US-02):
 *  - DE CUENTA (sincroniza): perfil, sexo, ajustes de stats, temporizador, rutinas,
 *    ejercicios, historial, tags, historial corporal y registro de fotos.
 *  - LOCAL POR DISPOSITIVO (no sincroniza): posición del timer flotante, discos y peso
 *    de barra (calculadora), sesión activa en curso, y las IMÁGENES de las fotos (URIs).
 *  - NO SINCRONIZABLE: nada más por ahora.
 * Conflicto: perfil/ajustes y colecciones = documento con updatedAt (la nube gana al
 * iniciar sesión si existe); historial/cuerpo/fotos = merge por id/fecha.
 */
class FirestoreDataSource : CloudDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    private val userId: String?
        get() = auth.currentUser?.uid

    // Devuelve la referencia al documento del usuario o null si no hay sesión
    private fun userDoc(collection: String) = userId?.let {
        db.collection("users").document(it).collection("data").document(collection)
    }

    // ─────────────────────────────────────────────
    // PERFIL
    // ─────────────────────────────────────────────

    override suspend fun saveProfile(
        name: String,
        mass: String,
        height: Double,
        bodyFat: String,
        sex: String,
        disciplineDay: Int,
        customMeasures: List<BodyMeasure>,
        basePhotoDate: String?,
        actualPhotoDate: String?
    ) {
        userDoc("profile")?.set(
            mapOf(
                "name" to name,
                "mass" to mass,
                "height" to height,
                "bodyFat" to bodyFat,
                "sex" to sex,
                "disciplineDay" to disciplineDay,
                "customMeasures" to gson.toJson(customMeasures),
                "basePhotoDate" to (basePhotoDate ?: ""),
                "actualPhotoDate" to (actualPhotoDate ?: ""),
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )?.await()
    }

    // Los getters NO capturan excepciones: un error de red/Firebase se PROPAGA para que la
    // sincronización lo marque como Error. "Documento inexistente" devuelve null sin excepción.
    override suspend fun getProfile(): Map<String, Any>? =
        userDoc("profile")?.get()?.await()?.data

    // ─────────────────────────────────────────────
    // RUTINAS
    // ─────────────────────────────────────────────

    override suspend fun saveRoutines(routines: List<Routine>) {
        userDoc("routines")?.set(
            mapOf(
                "data" to gson.toJson(routines),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    override suspend fun getRoutines(): List<Routine>? {
        val doc = userDoc("routines")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<Routine>>() {}.type)
    }

    // ─────────────────────────────────────────────
    // LIBRERÍA DE EJERCICIOS
    // ─────────────────────────────────────────────

    override suspend fun saveExercises(exercises: List<Exercise>) {
        userDoc("exercises")?.set(
            mapOf(
                "data" to gson.toJson(exercises),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    override suspend fun getExercises(): List<Exercise>? {
        val doc = userDoc("exercises")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<Exercise>>() {}.type)
    }

    // ─────────────────────────────────────────────
    // HISTORIAL DE ENTRENAMIENTOS
    // ─────────────────────────────────────────────

    override suspend fun saveWorkoutHistory(history: List<WorkoutSession>) {
        userDoc("history")?.set(
            mapOf(
                "data" to gson.toJson(history),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    override suspend fun getWorkoutHistory(): List<WorkoutSession>? {
        val doc = userDoc("history")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<WorkoutSession>>() {}.type)
    }

    override suspend fun appendWorkoutSession(session: WorkoutSession) {
        val current = getWorkoutHistory()?.toMutableList() ?: mutableListOf()
        // Evitar duplicados: solo añadir si el ID no existe ya en Firestore
        if (current.none { it.id == session.id }) {
            current.add(session)
            saveWorkoutHistory(current)
        }
    }

    // ─────────────────────────────────────────────
    // TAGS GLOBALES
    // ─────────────────────────────────────────────

    override suspend fun saveTags(tags: List<String>) {
        userDoc("tags")?.set(
            mapOf(
                "data" to gson.toJson(tags),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    override suspend fun getTags(): List<String>? {
        val doc = userDoc("tags")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }

    // ─────────────────────────────────────────────
    // PREFERENCIAS DE UI
    // ─────────────────────────────────────────────

    override suspend fun saveSettings(
        showBMI: Boolean,
        showBodyFat: Boolean,
        showVisualLog: Boolean,
        showGirths: Boolean,
        showVolumeCard: Boolean,
        showDisciplineCard: Boolean,
        showEvolutionGraph: Boolean,
        showAnalyticsList: Boolean,
        targetDaysPerWeek: Int,
        restTimerSeconds: Int,
        timerVibrate: Boolean,
        timerSound: Boolean
    ) {
        userDoc("settings")?.set(
            mapOf(
                "showBMI" to showBMI,
                "showBodyFat" to showBodyFat,
                "showVisualLog" to showVisualLog,
                "showGirths" to showGirths,
                "showVolumeCard" to showVolumeCard,
                "showDisciplineCard" to showDisciplineCard,
                "showEvolutionGraph" to showEvolutionGraph,
                "showAnalyticsList" to showAnalyticsList,
                "targetDaysPerWeek" to targetDaysPerWeek,
                "restTimerSeconds" to restTimerSeconds,
                "timerVibrate" to timerVibrate,
                "timerSound" to timerSound,
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    // ─────────────────────────────────────────────
    // HISTORIAL CORPORAL Y REGISTRO DE FOTOS (US-02)
    // ─────────────────────────────────────────────

    override suspend fun saveBodyHistory(list: List<BodySnapshot>) {
        userDoc("bodyHistory")?.set(
            mapOf("data" to gson.toJson(list), "updatedAt" to System.currentTimeMillis())
        )?.await()
    }

    override suspend fun getBodyHistory(): List<BodySnapshot>? {
        val doc = userDoc("bodyHistory")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<BodySnapshot>>() {}.type)
    }

    /** Registro de fotos SIN las imágenes (las URIs locales no viajan; se guardan vacías). */
    override suspend fun savePhotoHistory(list: List<PhotoRecord>) {
        userDoc("photoHistory")?.set(
            mapOf("data" to gson.toJson(list), "updatedAt" to System.currentTimeMillis())
        )?.await()
    }

    override suspend fun getPhotoHistory(): List<PhotoRecord>? {
        val doc = userDoc("photoHistory")?.get()?.await() ?: return null
        val json = doc.getString("data") ?: return null
        return gson.fromJson(json, object : TypeToken<List<PhotoRecord>>() {}.type)
    }

    override suspend fun getSettings(): Map<String, Any>? =
        userDoc("settings")?.get()?.await()?.data

    // ─────────────────────────────────────────────
    // COMPROBACIÓN DE DATOS EN NUBE
    // ─────────────────────────────────────────────

    /** Devuelve true si el usuario ya tiene datos en Firestore */
    // Propaga el error de red (para que la sync lo marque como Error en vez de asumir "sin datos").
    override suspend fun hasCloudData(): Boolean =
        userDoc("profile")?.get()?.await()?.exists() == true

    // true si el perfil se sirvió desde la caché local (sin confirmación del servidor). Con red,
    // Firestore lee del servidor -> isFromCache = false. Si null (sin sesión) tratamos como caché.
    override suspend fun isFromCache(): Boolean =
        userDoc("profile")?.get()?.await()?.metadata?.isFromCache ?: true

    // ─────────────────────────────────────────────
    // BORRADO DE CUENTA (RGPD / requisito de Google Play)
    // ─────────────────────────────────────────────

    /**
     * Borra TODOS los documentos de datos del usuario en Firestore (RGPD / requisito de Play).
     * El esquema es PLANO (users/{uid}/data/{doc}), así que borrar los documentos conocidos borra
     * todo el árbol del usuario (las reglas no permiten subcolecciones — ver firestore.rules).
     *
     * IMPORTANTE (US-09 review): el borrado autoritativo (paso 2) PROPAGA el error. Si falla la
     * limpieza (p.ej. sin red), el llamador NO debe borrar la cuenta de Auth: se dejarían datos
     * huérfanos. El reintento es idempotente (borrar un doc inexistente es un no-op).
     */
    override suspend fun deleteAllUserData() {
        val uid = userId ?: return
        val dataCol = db.collection("users").document(uid).collection("data")

        // 1) Best-effort: barre cualquier documento que hubiera bajo /data (si la enumeración
        //    falla por red no es crítico; el paso 2 cubre el esquema real y sí propaga).
        runCatching {
            for (doc in dataCol.get().await().documents) {
                runCatching { doc.reference.delete().await() }
            }
        }

        // 2) Autoritativo: borra los documentos conocidos. Si algo falla, PROPAGA la excepción.
        val known = listOf("profile", "routines", "exercises", "history", "tags", "settings", "bodyHistory", "photoHistory")
        for (c in known) {
            dataCol.document(c).delete().await()
        }
    }
}

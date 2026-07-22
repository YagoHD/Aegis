package com.yago.aegis.data

/**
 * Vocabulario CERRADO de etiquetas competitivas del Panteón.
 * Van prefijadas con ★ y son las únicas que cuentan para el rango.
 * (Mismo espíritu que BASE_TAG: una convención de prefijo que marca la etiqueta.)
 */
const val COMPETITIVE_TAG_PREFIX = "★"

/** Grupos musculares principales. */
enum class MuscleGroup(val display: String) {
    PECHO("Pecho"),
    ESPALDA("Espalda"),
    HOMBRO("Hombro"),
    BRAZO("Brazo"),
    PIERNA("Pierna"),
    CORE("Core");

    /** Etiqueta competitiva de grupo, ej. "★Pecho". */
    val tag: String get() = "$COMPETITIVE_TAG_PREFIX$display"
}

/** Subgrupos musculares (la unidad de rango). Cada uno pertenece a un grupo. */
enum class MuscleSubgroup(val display: String, val group: MuscleGroup) {
    // Pecho
    PECHO_SUPERIOR("Pecho superior", MuscleGroup.PECHO),
    PECHO_MEDIO("Pecho medio", MuscleGroup.PECHO),
    PECHO_INFERIOR("Pecho inferior", MuscleGroup.PECHO),
    // Espalda
    DORSAL("Dorsal", MuscleGroup.ESPALDA),
    TRAPECIO("Trapecio", MuscleGroup.ESPALDA),
    ROMBOIDES("Romboides", MuscleGroup.ESPALDA),
    LUMBAR("Lumbar", MuscleGroup.ESPALDA),
    // Hombro
    DELTOIDE_ANTERIOR("Deltoide anterior", MuscleGroup.HOMBRO),
    DELTOIDE_LATERAL("Deltoide lateral", MuscleGroup.HOMBRO),
    DELTOIDE_POSTERIOR("Deltoide posterior", MuscleGroup.HOMBRO),
    // Brazo
    BICEPS("Bíceps", MuscleGroup.BRAZO),
    BRAQUIAL("Braquial", MuscleGroup.BRAZO),
    TRICEPS("Tríceps", MuscleGroup.BRAZO),
    ANTEBRAZO("Antebrazo", MuscleGroup.BRAZO),
    // Pierna
    CUADRICEPS("Cuádriceps", MuscleGroup.PIERNA),
    ISQUIOTIBIALES("Isquiotibiales", MuscleGroup.PIERNA),
    GLUTEO("Glúteo", MuscleGroup.PIERNA),
    GEMELOS("Gemelos", MuscleGroup.PIERNA),
    ADUCTORES("Aductores", MuscleGroup.PIERNA),
    // Core
    ABDOMEN("Abdomen", MuscleGroup.CORE),
    OBLICUOS("Oblicuos", MuscleGroup.CORE);

    /** Etiqueta competitiva de subgrupo, ej. "★Pecho superior". */
    val tag: String get() = "$COMPETITIVE_TAG_PREFIX$display"

    companion object {
        /** Subgrupos agrupados por su grupo muscular. */
        val byGroup: Map<MuscleGroup, List<MuscleSubgroup>>
            get() = MuscleSubgroup.entries.groupBy { it.group }

        /** Devuelve el subgrupo cuya etiqueta ★ coincide (ignora mayúsculas). */
        fun fromTag(tag: String): MuscleSubgroup? =
            MuscleSubgroup.entries.find { it.tag.equals(tag, ignoreCase = true) }

        /** Todas las etiquetas ★ (grupos + subgrupos) para el selector. */
        val allTags: List<String>
            get() = MuscleGroup.entries.map { it.tag } + MuscleSubgroup.entries.map { it.tag }
    }
}

/** True si una etiqueta es competitiva (empieza por ★). */
fun String.isCompetitiveTag(): Boolean = startsWith(COMPETITIVE_TAG_PREFIX)

/**
 * Cuánto contribuye un ejercicio a un subgrupo muscular (eje de VOLUMEN del Panteón).
 * [muscle] guarda el nombre del enum (ej. "PECHO_MEDIO") para ser robusto ante Gson:
 * un valor desconocido simplemente no resuelve, no rompe el parseo de toda la librería.
 * La suma de percent de un ejercicio debería rondar 100.
 */
data class MuscleContribution(
    val muscle: String = "",
    val percent: Int = 0
) {
    val subgroup: MuscleSubgroup? get() = MuscleSubgroup.entries.find { it.name == muscle }
}

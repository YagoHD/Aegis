package com.yago.aegis.data

/**
 * Estado observable de la sincronización con la nube (US-01).
 * La escritura local es siempre inmediata; esto solo refleja el estado del envío/descarga a Firestore.
 */
sealed interface SyncState {
    /** Sin sincronización en curso ni error reciente. */
    object Idle : SyncState
    /** Sincronización en curso. */
    object Syncing : SyncState
    /** Última sincronización confirmada CON EL SERVIDOR. */
    object Success : SyncState
    /**
     * Datos utilizables desde la CACHÉ local (offline-first), pero SIN confirmación reciente del
     * servidor. No es un error: la app funciona; al recuperar red y reintentar pasa a Success.
     */
    object Cached : SyncState
    /** Falló la sincronización (los datos locales siguen intactos). [message] es recuperable/mostrable. */
    data class Error(val message: String) : SyncState
}

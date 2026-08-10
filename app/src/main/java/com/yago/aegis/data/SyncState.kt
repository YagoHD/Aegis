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
    /** Última sincronización correcta. */
    object Success : SyncState
    /** Falló la sincronización (los datos locales siguen intactos). [message] es recuperable/mostrable. */
    data class Error(val message: String) : SyncState
}

package com.cypherchat.core.network

import com.cypherchat.core.common.SecureResult
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the SimpleX transport layer.
 *
 * SimpleX is a server-assisted messaging protocol that does not require persistent
 * user identifiers. Each conversation uses ephemeral queue addresses.
 *
 * This interface defines the contract; connect a real SimpleX SDK implementation
 * (or a local-only mock for development) as a Koin dependency.
 *
 * References:
 *   https://simplex.chat/docs/protocol/simplex-messaging.html
 */
interface SimplexTransport {

    /** Connect to the SimpleX relay server. */
    suspend fun connect(endpoint: SimplexEndpointConfig): SecureResult<Unit>

    /** Disconnect from the relay. */
    suspend fun disconnect()

    /** Create a new one-time invitation link / queue address for a contact. */
    suspend fun createInvitation(): SecureResult<SimplexInvitation>

    /** Accept an incoming invitation from a contact. */
    suspend fun acceptInvitation(invitation: SimplexInvitation): SecureResult<SimplexConnection>

    /** Send an encrypted message envelope to a connection. */
    suspend fun sendMessage(connection: SimplexConnection, envelope: ByteArray): SecureResult<Unit>

    /** Receive incoming message envelopes. Emits continuously until cancelled. */
    fun receiveMessages(): Flow<IncomingEnvelope>

    /** Current connection state. */
    val state: Flow<TransportState>
}

data class SimplexEndpointConfig(
    val relayHost: String,
    val relayPort: Int = 5223,
    val useTls: Boolean = true,
    val serverFingerprint: String? = null   // Pin the relay's TLS certificate
)

data class SimplexInvitation(
    val queueAddress: String,
    val publicKey: ByteArray,
    val relayUrl: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimplexInvitation) return false
        return queueAddress == other.queueAddress && publicKey.contentEquals(other.publicKey)
    }

    override fun hashCode(): Int = queueAddress.hashCode()
}

data class SimplexConnection(
    val connectionId: String,
    val peerPublicKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimplexConnection) return false
        return connectionId == other.connectionId
    }

    override fun hashCode(): Int = connectionId.hashCode()
}

data class IncomingEnvelope(
    val connectionId: String,
    val envelope: ByteArray,
    val receivedAt: Long = System.currentTimeMillis()
)

enum class TransportState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

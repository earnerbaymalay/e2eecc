// STUB: NOT IMPLEMENTED
package com.cypherchat.core.network

import com.cypherchat.core.common.SecureResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * SimpleX Transport Implementation
 * 
 * Bridges the SimplexTransport interface with the actual network layer.
 * Messages are encrypted by the crypto layer before being passed here,
 * and decrypted after receipt.
 * 
 * Production: Connect to SimpleX CLI/SDK
 * Development: This provides a working transport skeleton that can be
 * wired up to the UI and crypto layers for testing.
 */
class SimplexTransportImpl : SimplexTransport {

    override val state: Flow<TransportState> = emptyFlow()

    override suspend fun connect(endpoint: SimplexEndpointConfig): SecureResult<Unit> {
        throw NotImplementedError(
            "SimplexTransport: network layer not implemented. " +
            "Relay connection cannot be established. Do not ship without implementing this."
        )
    }

    override suspend fun disconnect() {
        // No-op for stub
    }

    override suspend fun createInvitation(): SecureResult<SimplexInvitation> {
        throw NotImplementedError(
            "SimplexTransport: network layer not implemented. " +
            "Invitations cannot be created. Do not ship without implementing this."
        )
    }

    override suspend fun acceptInvitation(invitation: SimplexInvitation): SecureResult<SimplexConnection> {
        throw NotImplementedError(
            "SimplexTransport: network layer not implemented. " +
            "Invitations cannot be accepted. Do not ship without implementing this."
        )
    }

    override suspend fun sendMessage(connection: SimplexConnection, envelope: ByteArray): SecureResult<Unit> {
        throw NotImplementedError(
            "SimplexTransport: network layer not implemented. " +
            "Messages cannot be transmitted. Do not ship without implementing this."
        )
    }

    override fun receiveMessages(): Flow<IncomingEnvelope> {
        return emptyFlow()
    }
}

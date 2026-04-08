package com.cypherchat.core.network

import com.cypherchat.core.common.SecureResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _state = MutableStateFlow(TransportState.DISCONNECTED)
    override val state: Flow<TransportState> = _state.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<IncomingEnvelope>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _invitations = MutableSharedFlow<SimplexInvitation>(replay = 10)

    private val connections = mutableMapOf<String, SimplexConnection>()

    override suspend fun connect(endpoint: SimplexEndpointConfig): SecureResult<Unit> {
        return try {
            _state.value = TransportState.CONNECTING
            
            // In production: Initialize SimpleX CLI/SDK connection
            // For now: Mark as connected (transport layer ready)
            _state.value = TransportState.CONNECTED
            
            SecureResult.Success(Unit)
        } catch (e: Exception) {
            _state.value = TransportState.ERROR
            SecureResult.Failure(NetworkException("Connection failed: ${e.message}"))
        }
    }

    override suspend fun disconnect() {
        connections.clear()
        _state.value = TransportState.DISCONNECTED
    }

    override suspend fun createInvitation(): SecureResult<SimplexInvitation> {
        return try {
            // Generate a unique queue address for the invitation
            val queueAddress = generateQueueAddress()
            val publicKey = crypto.generatePublicKey()
            
            val invitation = SimplexInvitation(
                queueAddress = queueAddress,
                publicKey = publicKey,
                relayUrl = "smp://relay.simplex.im"
            )

            SecureResult.Success(invitation)
        } catch (e: Exception) {
            SecureResult.Failure(NetworkException("Invitation creation failed: ${e.message}"))
        }
    }

    override suspend fun acceptInvitation(invitation: SimplexInvitation): SecureResult<SimplexConnection> {
        return try {
            val connectionId = "conn_${System.currentTimeMillis()}"
            
            val connection = SimplexConnection(
                connectionId = connectionId,
                peerPublicKey = invitation.publicKey
            )

            connections[connectionId] = connection
            _state.value = TransportState.CONNECTED

            SecureResult.Success(connection)
        } catch (e: Exception) {
            SecureResult.Failure(NetworkException("Invitation acceptance failed: ${e.message}"))
        }
    }

    override suspend fun sendMessage(connection: SimplexConnection, envelope: ByteArray): SecureResult<Unit> {
        return try {
            val conn = connections[connection.connectionId]
                ?: return SecureResult.Failure(NetworkException("Unknown connection: ${connection.connectionId}"))

            // In production: Send via SimpleX SDK
            // For now: Store locally and simulate delivery
            
            // Simulate receiving the message on the other side (loopback for testing)
            _incomingMessages.tryEmit(
                IncomingEnvelope(
                    connectionId = connection.connectionId,
                    envelope = envelope
                )
            )

            SecureResult.Success(Unit)
        } catch (e: Exception) {
            SecureResult.Failure(NetworkException("Send failed: ${e.message}"))
        }
    }

    override fun receiveMessages(): Flow<IncomingEnvelope> {
        return _incomingMessages.asSharedFlow()
    }

    // Helper functions

    private fun generateQueueAddress(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..40).map { chars.random() }.joinToString("")
    }

    /**
     * Inject a test message for development/testing.
     * In production, this would be handled by the SimpleX SDK callback.
     */
    suspend fun injectTestMessage(connectionId: String, envelope: ByteArray) {
        _incomingMessages.tryEmit(
            IncomingEnvelope(
                connectionId = connectionId,
                envelope = envelope
            )
        )
    }
}

class NetworkException(message: String) : Exception(message)

object crypto {
    fun generatePublicKey(): ByteArray {
        // Placeholder: In production, derive from actual crypto module
        return ByteArray(32) { (it * 7).toByte() }
    }
}

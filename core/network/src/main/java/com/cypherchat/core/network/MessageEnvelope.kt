package com.cypherchat.core.network

/**
 * Wire format for an encrypted message.
 *
 * All fields except [conversationId] and [senderRatchetKey] are encrypted under
 * the Double Ratchet message key before transmission.
 *
 * Layout (before encryption):
 *   version   (1 byte)
 *   msgNum    (4 bytes, big-endian)
 *   prevNum   (4 bytes, big-endian)
 *   ratchetKey (33 bytes, compressed EC point)
 *   payloadLen (4 bytes)
 *   payload    (n bytes)  ← this is the plaintext
 */
data class MessageEnvelope(
    val version: Byte = 0x01,
    val conversationId: String,
    val messageId: String,
    val msgNum: Int,
    val prevChainLen: Int,
    val senderRatchetKey: ByteArray,    // Unencrypted — needed for DH ratchet step
    val encryptedPayload: ByteArray,    // AES-GCM(DR_message_key, plaintext)
    val timestamp: Long = System.currentTimeMillis()
) {

    fun toBytes(): ByteArray {
        val convIdBytes = conversationId.toByteArray()
        val msgIdBytes  = messageId.toByteArray()
        return buildByteArray {
            add(version)
            addInt(convIdBytes.size)
            addAll(convIdBytes)
            addInt(msgIdBytes.size)
            addAll(msgIdBytes)
            addInt(msgNum)
            addInt(prevChainLen)
            addInt(senderRatchetKey.size)
            addAll(senderRatchetKey)
            addInt(encryptedPayload.size)
            addAll(encryptedPayload)
            addLong(timestamp)
        }
    }

    companion object {
        fun fromBytes(data: ByteArray): MessageEnvelope? = try {
            var offset = 0
            val version = data[offset++]
            val convIdLen = data.readInt(offset); offset += 4
            val conversationId = String(data, offset, convIdLen); offset += convIdLen
            val msgIdLen = data.readInt(offset); offset += 4
            val messageId = String(data, offset, msgIdLen); offset += msgIdLen
            val msgNum = data.readInt(offset); offset += 4
            val prevChainLen = data.readInt(offset); offset += 4
            val ratchetKeyLen = data.readInt(offset); offset += 4
            val ratchetKey = data.copyOfRange(offset, offset + ratchetKeyLen); offset += ratchetKeyLen
            val payloadLen = data.readInt(offset); offset += 4
            val payload = data.copyOfRange(offset, offset + payloadLen); offset += payloadLen
            val timestamp = data.readLong(offset)

            MessageEnvelope(version, conversationId, messageId, msgNum, prevChainLen, ratchetKey, payload, timestamp)
        } catch (_: Exception) { null }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEnvelope) return false
        return messageId == other.messageId && encryptedPayload.contentEquals(other.encryptedPayload)
    }

    override fun hashCode(): Int = messageId.hashCode()
}

// ── Builder helpers ────────────────────────────────────────────────────────────

private class ByteArrayBuilder {
    private val list = mutableListOf<Byte>()

    fun add(b: Byte) { list.add(b) }
    fun addAll(bs: ByteArray) { bs.forEach { list.add(it) } }
    fun addInt(v: Int) {
        list.add((v shr 24).toByte())
        list.add((v shr 16).toByte())
        list.add((v shr 8).toByte())
        list.add(v.toByte())
    }
    fun addLong(v: Long) {
        for (i in 7 downTo 0) list.add((v shr (i * 8)).toByte())
    }
    fun build(): ByteArray = list.toByteArray()
}

private fun buildByteArray(block: ByteArrayBuilder.() -> Unit): ByteArray =
    ByteArrayBuilder().apply(block).build()

private fun ByteArray.readInt(offset: Int): Int =
    ((this[offset].toInt() and 0xFF) shl 24) or
    ((this[offset+1].toInt() and 0xFF) shl 16) or
    ((this[offset+2].toInt() and 0xFF) shl 8) or
    (this[offset+3].toInt() and 0xFF)

private fun ByteArray.readLong(offset: Int): Long {
    var result = 0L
    for (i in 0..7) result = (result shl 8) or (this[offset + i].toLong() and 0xFF)
    return result
}

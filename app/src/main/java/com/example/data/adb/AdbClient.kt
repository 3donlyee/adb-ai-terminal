package com.example.data.adb

import android.util.Log
import com.example.data.adb.AdbProtocol.ADB_AUTH_RSAPUBLICKEY
import com.example.data.adb.AdbProtocol.ADB_AUTH_SIGNATURE
import com.example.data.adb.AdbProtocol.ADB_AUTH_TOKEN
import com.example.data.adb.AdbProtocol.A_AUTH
import com.example.data.adb.AdbProtocol.A_CLSE
import com.example.data.adb.AdbProtocol.A_CNXN
import com.example.data.adb.AdbProtocol.A_MAXDATA
import com.example.data.adb.AdbProtocol.A_OKAY
import com.example.data.adb.AdbProtocol.A_OPEN
import com.example.data.adb.AdbProtocol.A_STLS
import com.example.data.adb.AdbProtocol.A_STLS_VERSION
import com.example.data.adb.AdbProtocol.A_VERSION
import com.example.data.adb.AdbProtocol.A_WRTE
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.net.ssl.SSLSocket

private const val TAG = "AdbClient"

class AdbClient(
    val host: String,
    val port: Int,
    private val key: AdbKey
) : Closeable {

    private var socket: Socket? = null
    private var plainInputStream: DataInputStream? = null
    private var plainOutputStream: DataOutputStream? = null
    private var useTls = false
    private var tlsSocket: SSLSocket? = null
    private var tlsInputStream: DataInputStream? = null
    private var tlsOutputStream: DataOutputStream? = null

    private val inputStream: DataInputStream
        get() = if (useTls) (tlsInputStream ?: error("TLS InputStream null")) else (plainInputStream ?: error("Plain InputStream null"))

    private val outputStream: DataOutputStream
        get() = if (useTls) (tlsOutputStream ?: error("TLS OutputStream null")) else (plainOutputStream ?: error("Plain OutputStream null"))

    fun connect() {
        val s = Socket()
        val address = InetSocketAddress(host, port)
        s.connect(address, 5000)
        s.tcpNoDelay = true
        socket = s
        val pIn = DataInputStream(s.getInputStream())
        val pOut = DataOutputStream(s.getOutputStream())
        plainInputStream = pIn
        plainOutputStream = pOut

        write(A_CNXN, A_VERSION, A_MAXDATA, "host::features=shell_v2,cmd,stat_v2,ls_v2")
        var message = read()

        if (message.command == A_STLS) {
            write(A_STLS, A_STLS_VERSION, 0)
            val sslContext = key.sslContext
            val ssl = sslContext.socketFactory.createSocket(s, host, port, true) as SSLSocket
            ssl.startHandshake()
            Log.d(TAG, "TLS Handshake succeeded.")
            tlsSocket = ssl
            tlsInputStream = DataInputStream(ssl.inputStream)
            tlsOutputStream = DataOutputStream(ssl.outputStream)
            useTls = true
            message = read()
        } else if (message.command == A_AUTH) {
            if (message.arg0 != ADB_AUTH_TOKEN) error("Expected ADB_AUTH_TOKEN, got: ${message.arg0}")
            write(A_AUTH, ADB_AUTH_SIGNATURE, 0, key.sign(message.data))
            message = read()
            if (message.command != A_CNXN) {
                write(A_AUTH, ADB_AUTH_RSAPUBLICKEY, 0, key.adbPublicKey)
                message = read()
            }
        }

        if (message.command != A_CNXN) {
            error("Connection failed: expected A_CNXN, got ${message.command}")
        }
        Log.i(TAG, "Successfully connected and authenticated with ADB daemon!")
    }

    fun command(cmd: String, listener: ((ByteArray) -> Unit)? = null) {
        val localId = 1
        write(A_OPEN, localId, 0, cmd)
        var message = read()
        when (message.command) {
            A_OKAY -> {
                while (true) {
                    message = read()
                    val remoteId = message.arg0
                    if (message.command == A_WRTE) {
                        if (message.data_length > 0 && message.data != null) {
                            listener?.invoke(message.data)
                        }
                        write(A_OKAY, localId, remoteId)
                    } else if (message.command == A_CLSE) {
                        write(A_CLSE, localId, remoteId)
                        break
                    } else {
                        error("not A_WRTE or A_CLSE: ${message.command}")
                    }
                }
            }
            A_CLSE -> {
                val remoteId = message.arg0
                write(A_CLSE, localId, remoteId)
            }
            else -> {
                error("not A_OKAY or A_CLSE: ${message.command}")
            }
        }
    }

    private fun write(command: Int, arg0: Int, arg1: Int, data: ByteArray? = null) =
        write(AdbMessage(command, arg0, arg1, data))

    private fun write(command: Int, arg0: Int, arg1: Int, data: String) =
        write(AdbMessage(command, arg0, arg1, data))

    private fun write(message: AdbMessage) {
        outputStream.write(message.toByteArray())
        outputStream.flush()
    }

    private fun read(): AdbMessage {
        val buffer = ByteBuffer.allocate(AdbMessage.HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN)
        inputStream.readFully(buffer.array(), 0, 24)
        val command = buffer.int
        val arg0 = buffer.int
        val arg1 = buffer.int
        val dataLength = buffer.int
        val checksum = buffer.int
        val magic = buffer.int
        val data: ByteArray?
        if (dataLength > 0) {
            data = ByteArray(dataLength)
            inputStream.readFully(data, 0, dataLength)
        } else {
            data = null
        }
        val message = AdbMessage(command, arg0, arg1, dataLength, checksum, magic, data)
        message.validateOrThrow()
        return message
    }

    override fun close() {
        try { plainInputStream?.close() } catch (_: Throwable) {}
        try { plainOutputStream?.close() } catch (_: Throwable) {}
        try { socket?.close() } catch (_: Exception) {}
        if (useTls) {
            try { tlsInputStream?.close() } catch (_: Throwable) {}
            try { tlsOutputStream?.close() } catch (_: Throwable) {}
            try { tlsSocket?.close() } catch (_: Exception) {}
        }
    }
}

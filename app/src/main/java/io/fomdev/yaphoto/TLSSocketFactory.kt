package io.fomdev.yaphoto

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TLSSocketFactory : SSLSocketFactory {

    private val mSocketFactory: SSLSocketFactory
    private val mProtocols: Array<String>

    constructor(protocol: String = "TLSv1.2", protocols: Array<String> = arrayOf("TLSv1.1", "TLSv1.2")) {
        val context = SSLContext.getInstance(protocol)
        context.init(null, null, null)
        mSocketFactory = context.socketFactory
        mProtocols = protocols
    }

    constructor(socketFactory: SSLSocketFactory, protocols: Array<String> = arrayOf("TLSv1.1", "TLSv1.2")) {
        mSocketFactory = socketFactory
        mProtocols = protocols
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return mSocketFactory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return mSocketFactory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return enableProtocols(mSocketFactory.createSocket(s, host, port, autoClose), mProtocols)
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return enableProtocols(mSocketFactory.createSocket(host, port), mProtocols)
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return enableProtocols(mSocketFactory.createSocket(host, port, localHost, localPort), mProtocols)
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return enableProtocols(mSocketFactory.createSocket(host, port), mProtocols)
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return enableProtocols(mSocketFactory.createSocket(address, port, localAddress, localPort), mProtocols)
    }

    fun enableProtocols(socket: Socket, protocols: Array<String>): Socket {
        if (socket is SSLSocket) {
            socket.enabledProtocols = protocols
        }
        return socket
    }
}
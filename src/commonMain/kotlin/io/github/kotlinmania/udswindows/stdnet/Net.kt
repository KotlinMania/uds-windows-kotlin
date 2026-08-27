// port-lint: source uds_windows/src/stdnet/net.rs
package io.github.kotlinmania.udswindows.stdnet

public class AcceptedConnection(
    public val stream: UnixStream,
    public val addr: SocketAddr,
) {
    public operator fun component1(): UnixStream = stream

    public operator fun component2(): SocketAddr = addr
}

public class AcceptedStreamPair(
    public val first: UnixStream,
    public val second: UnixStream,
) {
    public operator fun component1(): UnixStream = first

    public operator fun component2(): UnixStream = second
}

internal class BytePipe {
    private val buffer = mutableListOf<Byte>()
    private var isClosed = false

    fun write(bytes: ByteArray, offset: Int, length: Int): Int {
        check(!isClosed) { "cannot write to closed stream" }
        for (i in offset until (offset + length)) {
            buffer.add(bytes[i])
        }
        return length
    }

    fun read(target: ByteArray, offset: Int, length: Int): Int {
        if (buffer.isEmpty()) {
            return if (isClosed) 0 else 0
        }
        val toRead = minOf(length, buffer.size)
        for (i in 0 until toRead) {
            target[offset + i] = buffer.removeAt(0)
        }
        return toRead
    }

    fun close() {
        isClosed = true
    }

    fun available(): Int = buffer.size
}

internal class SocketRegistry {
    private val listeners = mutableMapOf<String, UnixListener>()

    fun register(path: String, listener: UnixListener) {
        listeners[path] = listener
    }

    fun unregister(path: String) {
        listeners.remove(path)
    }

    fun lookup(path: String): UnixListener? = listeners[path]
}

internal val globalSocketRegistry = SocketRegistry()

/**
 * A Unix stream socket.
 */
public class UnixStream internal constructor(
    internal val socket: Socket,
    private val inPipe: BytePipe,
    private val outPipe: BytePipe,
    private val localAddress: SocketAddr,
    private val peerAddress: SocketAddr,
) {
    public fun tryClone(): UnixStream =
        UnixStream(
            socket = socket.duplicate(),
            inPipe = inPipe,
            outPipe = outPipe,
            localAddress = localAddress,
            peerAddress = peerAddress,
        )

    public fun localAddr(): SocketAddr = localAddress

    public fun peerAddr(): SocketAddr = peerAddress

    public fun setNonblocking(nonblocking: Boolean) {
        socket.setNonblocking(nonblocking)
    }

    public fun takeError(): SocketError? = socket.takeError()

    public fun shutdown(how: Shutdown) {
        socket.shutdown(how)
        when (how) {
            Shutdown.Read -> inPipe.close()
            Shutdown.Write -> outPipe.close()
            Shutdown.Both -> {
                inPipe.close()
                outPipe.close()
            }
        }
    }

    public fun setReadTimeout(durationMs: Long?) {
        socket.setTimeout(durationMs, SO_RCVTIMEO)
    }

    public fun setWriteTimeout(durationMs: Long?) {
        socket.setTimeout(durationMs, SO_SNDTIMEO)
    }

    public fun readTimeout(): Long? = socket.timeout(SO_RCVTIMEO)

    public fun writeTimeout(): Long? = socket.timeout(SO_SNDTIMEO)

    public fun read(buf: ByteArray, offset: Int = 0, length: Int = buf.size): Int =
        inPipe.read(buf, offset, length)

    public fun write(buf: ByteArray, offset: Int = 0, length: Int = buf.size): Int =
        outPipe.write(buf, offset, length)

    public fun writeAll(buf: ByteArray) {
        var written = 0
        while (written < buf.size) {
            val n = write(buf, written, buf.size - written)
            if (n <= 0) break
            written += n
        }
    }

    public fun readToEnd(destination: MutableList<Byte>): Int {
        val temp = ByteArray(1024)
        var total = 0
        while (true) {
            val n = read(temp)
            if (n <= 0) break
            for (i in 0 until n) {
                destination.add(temp[i])
            }
            total += n
        }
        return total
    }

    public fun close() {
        socket.close()
        inPipe.close()
        outPipe.close()
    }

    public companion object {
        public fun connect(path: String): UnixStream {
            init()
            val socketAddr = fromPath(path)
            val listener =
                globalSocketRegistry.lookup(path)
                    ?: throw IllegalArgumentException("Could not connect to socket at $path: connection refused")
            val clientIn = BytePipe()
            val clientOut = BytePipe()
            val clientAddr = fromPath("")
            val clientStream =
                UnixStream(
                    socket = Socket.new(),
                    inPipe = clientIn,
                    outPipe = clientOut,
                    localAddress = clientAddr,
                    peerAddress = socketAddr,
                )
            val serverStream =
                UnixStream(
                    socket = Socket.new(),
                    inPipe = clientOut,
                    outPipe = clientIn,
                    localAddress = socketAddr,
                    peerAddress = clientAddr,
                )
            listener.enqueueIncoming(AcceptedConnection(serverStream, clientAddr))
            return clientStream
        }

        public fun pair(): AcceptedStreamPair {
            init()
            val pipe1 = BytePipe()
            val pipe2 = BytePipe()
            val addr = fromPath("")
            val s1 = UnixStream(Socket.new(), pipe1, pipe2, addr, addr)
            val s2 = UnixStream(Socket.new(), pipe2, pipe1, addr, addr)
            return AcceptedStreamPair(s1, s2)
        }
    }
}

/**
 * A Unix domain socket server.
 */
public class UnixListener internal constructor(
    internal val socket: Socket,
    private val boundAddress: SocketAddr,
    private val boundPath: String,
) {
    private val incomingQueue = mutableListOf<AcceptedConnection>()

    internal fun enqueueIncoming(connection: AcceptedConnection) {
        incomingQueue.add(connection)
    }

    public fun accept(): AcceptedConnection =
        if (incomingQueue.isEmpty()) {
            val clientIn = BytePipe()
            val clientOut = BytePipe()
            val clientAddr = fromPath("")
            val serverStream =
                UnixStream(
                    socket = Socket.new(),
                    inPipe = clientOut,
                    outPipe = clientIn,
                    localAddress = boundAddress,
                    peerAddress = clientAddr,
                )
            AcceptedConnection(serverStream, clientAddr)
        } else {
            incomingQueue.removeAt(0)
        }

    public fun tryClone(): UnixListener =
        UnixListener(
            socket = socket.duplicate(),
            boundAddress = boundAddress,
            boundPath = boundPath,
        )

    public fun localAddr(): SocketAddr = boundAddress

    public fun setNonblocking(nonblocking: Boolean) {
        socket.setNonblocking(nonblocking)
    }

    public fun takeError(): SocketError? = socket.takeError()

    public fun incoming(): Incoming = Incoming(this)

    public fun close() {
        socket.close()
        globalSocketRegistry.unregister(boundPath)
    }

    public companion object {
        public fun bind(path: String): UnixListener {
            init()
            val addr = fromPath(path)
            val listener = UnixListener(Socket.new(), addr, path)
            globalSocketRegistry.register(path, listener)
            return listener
        }
    }
}

/**
 * An iterator over incoming connections to a [UnixListener].
 */
public class Incoming(
    private val listener: UnixListener,
) : Iterator<AcceptedConnection>,
    Sequence<AcceptedConnection> {
    override fun hasNext(): Boolean = true

    override fun next(): AcceptedConnection {
        if (!hasNext()) throw NoSuchElementException("No more incoming connections")
        return listener.accept()
    }

    override fun iterator(): Iterator<AcceptedConnection> = this
}

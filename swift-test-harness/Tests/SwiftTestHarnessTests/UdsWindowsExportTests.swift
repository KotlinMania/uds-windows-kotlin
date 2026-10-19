import Testing
import UdsWindows
import KotlinStdlib

@Suite("UdsWindows Swift Export Suite")
struct UdsWindowsExportTests {
    @Test("Swift module loads cleanly")
    func swiftModuleLoads() {
        #expect(Bool(true), "UdsWindows swift module imported cleanly")
    }

    @Test("SocketAddr pathname works")
    func socketAddrWorks() {
        let addr = stdnet.fromPath(path: "/tmp/sock")
        #expect(addr.asPathname() == "/tmp/sock")
        #expect(!addr.isUnnamed())
    }

    @Test("Socket error value retains its code and message")
    func socketErrorValueWorks() {
        let error = stdnet.SocketError(rawOsError: 10060, message: "Timeout")
        #expect(error.rawOsError == 10060)
        #expect(error.message == "Timeout")
    }

    @Test("Kotlin dispatches both completion callbacks back into Swift once")
    func reverseCompletionCallbacksWork() {
        let stream = stdnet.UnixStream.Companion.shared.fromRawSocket(rawSocket: 301)
        let listener = stdnet.UnixListener.Companion.shared.fromRawSocket(rawSocket: 302)
        defer {
            stream.close()
            listener.close()
        }
        let callbacks = CompletionCallbacks(stream: stream, listener: listener)
        stdnet.connectComplete(stream: callbacks)
        stdnet.acceptComplete(listener: callbacks, socket: stream)
        #expect(callbacks.connectCalls == 1)
        #expect(callbacks.acceptCalls == 1)
        #expect(callbacks.acceptedSocket == 301)
    }
}

private final class CompletionCallbacks: stdnet.SocketExtension, stdnet.UnixStreamExt, stdnet.UnixListenerExt {
    private let stream: stdnet.UnixStream
    private let listener: stdnet.UnixListener
    private(set) var connectCalls = 0
    private(set) var acceptCalls = 0
    private(set) var acceptedSocket: Int64?

    init(stream: stdnet.UnixStream, listener: stdnet.UnixListener) {
        self.stream = stream
        self.listener = listener
        super.init()
    }

    func connectComplete() {
        connectCalls += 1
        stream.connectComplete()
    }

    func acceptComplete(socket: stdnet.UnixStream) {
        acceptCalls += 1
        acceptedSocket = socket.asRawSocket()
        listener.acceptComplete(socket: socket)
    }

    func connectOverlapped(addr: stdnet.SocketAddr, buf: kotlin.ByteArray, overlapped: stdnet.OVERLAPPED?) -> Int64? {
        stream.connectOverlapped(addr: addr, buf: buf, overlapped: overlapped)
    }

    func readOverlapped(buf: kotlin.ByteArray, overlapped: stdnet.OVERLAPPED?) -> Int64? {
        stream.readOverlapped(buf: buf, overlapped: overlapped)
    }

    func writeOverlapped(buf: kotlin.ByteArray, overlapped: stdnet.OVERLAPPED?) -> Int64? {
        stream.writeOverlapped(buf: buf, overlapped: overlapped)
    }

    func acceptOverlapped(socket: stdnet.UnixStream, addrs: stdnet.AcceptAddrsBuf, overlapped: stdnet.OVERLAPPED?) -> Bool {
        listener.acceptOverlapped(socket: socket, addrs: addrs, overlapped: overlapped)
    }

    func result(overlapped: stdnet.OVERLAPPED?) -> kotlin.Pair {
        stream.result(overlapped: overlapped)
    }
}

import Testing
import UdsWindows

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
}

#if canImport(Testing)
import Testing
import UdsWindows

@Test func swiftModuleLoads() {
    #expect(true)
}
#elseif canImport(XCTest)
import XCTest
import UdsWindows

final class UdsWindowsExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "UdsWindows swift module imported cleanly")
    }
}
#endif

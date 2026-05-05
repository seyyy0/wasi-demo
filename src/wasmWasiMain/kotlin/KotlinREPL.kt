import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

fun main() {
    println("Hello from Kotlin via WASI. REPL program is being initialized.")
}

@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiRawFdRead(fd: Int, iovs_ptr: Int, iovs_len: Int, nread_ptr: Int): Int

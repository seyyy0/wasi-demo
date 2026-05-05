import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

fun main() {
    println("Hello from Kotlin via WASI. REPL program is being initialized.")
    println("Instructions:\n" +
            "  - Type 'quit' to exit\n")
    while (true) {
        val input = wasiReadLine()
        if (input == "quit") {
            println("Goodbye!")
            break
        }
        println("Wasi recieved: [$input]")
    }
}

@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiRawFdRead(fd: Int, iovs_ptr: Int, iovs_len: Int, nread_ptr: Int): Int

@OptIn(UnsafeWasmMemoryApi::class)
fun wasiReadLine(): String = withScopedMemoryAllocator { allocator ->
    val buff = allocator.allocate(256)
    val iovec = allocator.allocate(8)
    Pointer(iovec.address).storeInt(buff.address.toInt())
    Pointer(iovec.address + 4u).storeInt(256)

    val nread = allocator.allocate(4)
    val ret = wasiRawFdRead(
        fd = 0,
        iovs_ptr = iovec.address.toInt(),
        iovs_len = 1,
        nread_ptr = nread.address.toInt()
    )
    check(ret == 0) {"fd_read failed with code $ret"}
    val bytesRead = Pointer(nread.address).loadInt()
    buildString {
        for (i in 0 until bytesRead) {
            append(Pointer(buff.address + i.toUInt()).loadByte().toInt().toChar())
        }
    }.trim()
}


import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

private const val BUFFER_SIZE = 256

@OptIn(ExperimentalWasmInterop::class)
@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiRawFdRead(fd: Int, iovsPtr: Int, iovsLen: Int, nreadPtr: Int): Int

@OptIn(UnsafeWasmMemoryApi::class)
internal fun wasiReadLine(): String? = withScopedMemoryAllocator { allocator ->
    val buff = allocator.allocate(BUFFER_SIZE)
    val iovec = allocator.allocate(8)
    Pointer(iovec.address).storeInt(buff.address.toInt())
    Pointer(iovec.address + 4u).storeInt(BUFFER_SIZE)

    val nread = allocator.allocate(4)
    val ret = wasiRawFdRead(
        fd = 0, // for stdin
        iovsPtr = iovec.address.toInt(),
        iovsLen = 1,
        nreadPtr = nread.address.toInt()
    )
    check(ret == 0) {"fd_read failed with code $ret"}
    val bytesRead = Pointer(nread.address).loadInt()
    if (bytesRead == 0) return@withScopedMemoryAllocator null // Check for EOF

    buildString {
        for (i in 0 until bytesRead) {
            append(Pointer(buff.address + i.toUInt()).loadByte().toInt().toChar())
        }
    }.trimEnd('\n', '\r')
}


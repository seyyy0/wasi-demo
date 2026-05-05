# Kotlin/Wasm WASI example

Demo project, scaffolded using the official template at [wasm-wasi-template](https://github.com/Kotlin/kotlin-wasm-wasi-template).

Uses Kotlin/Wasm, along with WASI, to run a simple "REPL" program in **non-browser** runtimes (specifically [Wasmtime](https://wasmtime.dev/))

## Running the program
To run the program, build it first, then simply use the gradle task with:
```
./gradlew runWasmtime
```
Or run it from the IntelliJ IDEA Gradle interface (Tasks/other/runWasmtime)  
This gradle task compiles the Kotlin code to an optimized .wasm binary, which can be found at `build/compileSync/wasmWasi/main/productionExecutable/kotlin`

## Details
The project requires I/O in non-browser environments, we need to look at the WASI specs, since standard Kotlin I/O won't suffice.

The spec, per [wasi_snapshot_preview1](https://github.com/WebAssembly/WASI/blob/wasi-0.1/preview1/docs.md#-fd_readfd-fd-iovs-iovec_array---resultsize-errno) (lead by example from the kotlin-wasm-wasi-template), tells us the following:

`fd_read(fd: fd, iovs: iovec_array) -> Result<size, errno>`  
Where:
- fd: file descriptor handle
- iovs: an iovec_array. A struct used to pass the following:
    - *where* the bytes are in memory (buffer.address())
    - *how* many bytes allowed to read (BUFFER_SIZE)
    - Respectively stored at `iovec.address` and `iovec.address + 4u`

### Few notes on implementation
Its notable that the Kotlin function has 4 parameters, whereas the specs specify only 2 for the imported `fd_read`, as well as having a different return type.
```kotlin
@WasmImport("wasi_snapshot_preview1", "fd_read")
private external fun wasiRawFdRead(fd: Int, iovs_ptr: Int, iovs_len: Int, nread_ptr: Int): Int
```
This is because of the **ABI convention**, which returns (flattens) errno as an Int directly, and same with the parameters. `iovs_len` is the length of the array, `iovs_ptr` points to the array, and `nread_ptr` points to how much was read.

A bit more obvious though, is that the Kotlin function returns `String?`, so the WASI functions return is only for checking errors.  
The way we actually construct strings is with a `buildString` block, that reads all the bytes and turns them into chars.

## Testing
Unit testing `wasiReadLine` directly is not practical as it blocks on
stdin and requires a live WASI runtime.  
Adding tests for the byte processing part of `wasiReadLine` would add too much complexity for seemingly no reason.

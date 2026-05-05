fun main() {
    println("\nHello from Kotlin via WASI. REPL program is being initialized.")
    println("Instructions:\n" +
            "  - Type 'quit' to exit\n")
    while (true) {
        print("> ")
        val input = wasiReadLine() ?: break // If EOF (eg. CTRL+D)
        if (input == "quit") {
            println("Goodbye!")
            break
        }
        println("Wasm received: $input")
    }
}

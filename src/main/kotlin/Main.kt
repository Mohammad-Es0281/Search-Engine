import java.io.File

fun main() {
    val files = File("src/test/resources/inputs_small").listFiles()
    val searchEngine = SearchEngineJava(files)
    println(searchEngine.search("catty"))
}
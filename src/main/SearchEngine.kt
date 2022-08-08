import opennlp.tools.stemmer.PorterStemmer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class SearchEngine(sourceFiles: Array<File>) {
    private val invertedIndex = hashMapOf<String, LinkedList<String>>()
    private val stemmer = PorterStemmer()

    init { addSourceFiles(sourceFiles) }

    fun search(word: String): String? {
        return invertedIndex.getOrDefault(word.cleanWord(), null)?.toString()
    }

    fun addSourceFiles(sourceFiles: Array<File>) {
        sourceFiles.forEach { addSourceFile(it) }
    }

    fun addSourceFile(sourceFile: File) {
        val path = Paths.get(sourceFile.path)
        Files.lines(path).use { stream ->
            stream.forEach { line: String -> foo(line, sourceFile.nameWithoutExtension)}
        }
    }

    private fun foo(text: String, documentName: String) {
        text.split(" ").forEach { word ->
            val cleanedWord = word.cleanWord()
            cleanedWord?.let { invertedIndex.getOrPut(it) { LinkedList<String>() }.add(documentName) }
        }
    }

    private fun String.cleanWord(): String? = this.lowercase().removeWordNoise()?.steam()
    private fun String.steam(): String = stemmer.stem(this)

    private fun String.removeWordNoise(): String? {
        val isNotLetter = { input: Char -> !input.isLetter() }
        return dropWhile(isNotLetter).run { dropLastWhile(isNotLetter) }.ifBlank { null }
    }
}
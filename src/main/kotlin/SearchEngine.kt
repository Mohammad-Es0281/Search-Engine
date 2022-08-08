import opennlp.tools.stemmer.PorterStemmer
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class SearchEngine {
    private val invertedIndex = hashMapOf<String, LinkedList<String>>()

    companion object {
        private val stemmer = PorterStemmer()
    }

    constructor(sourceFiles: Array<File>) {
        addSourceFiles(sourceFiles)
    }

    constructor(sourceFile: File) {
        addSourceFile(sourceFile)
    }

    fun search(query: String): HashSet<String> {
        val allWords = query.split("\\s+".toRegex())
        val finalFoundedFiles = HashSet<String>()

        val wordsWithoutPrefix = allWords.filter { word -> !word.startsWith('+') && !word.startsWith('-') }
        includeAllTogether(finalFoundedFiles, wordsWithoutPrefix)

        val wordsWithPlusPrefix = allWords.filter { word -> word.startsWith('+') }
        includeAll(finalFoundedFiles, wordsWithPlusPrefix)

        val wordsWithMinusPrefix = allWords.filter { word -> word.startsWith('-') }
        excludeAll(finalFoundedFiles, wordsWithMinusPrefix)

        return finalFoundedFiles
    }

    private fun includeAllTogether(resultFile: HashSet<String>, words: List<String>) {
        for (word in words) {
            searchSingleWord(word)?.let { foundedFile ->
                if (resultFile.isEmpty())
                    resultFile.addAll(foundedFile)
                else
                    resultFile.retainAll(foundedFile)
            }
        }
    }

    private fun includeAll(resultFile: HashSet<String>, words: List<String>) {
        for (word in words) {
            searchSingleWord(word)?.let { foundedFiles -> resultFile.addAll(foundedFiles) }
        }
    }

    private fun excludeAll(resultFile: HashSet<String>, words: List<String>) {
        for (word in words) {
            searchSingleWord(word)?.let { foundedFiles -> resultFile.removeAll(foundedFiles) }
        }
    }

    private fun searchSingleWord(query: String): LinkedList<String>? {
        return invertedIndex.getOrDefault(cleanWord(query), null)
    }

    fun addSourceFiles(sourceFiles: Array<File>) {
        sourceFiles.forEach { addSourceFile(it) }
    }

    fun addSourceFile(sourceFile: File) {
        val path = Paths.get(sourceFile.path)
        Files.lines(path).use { stream ->
            stream.forEach { line: String -> record(line, sourceFile.nameWithoutExtension) }
        }
    }

    private fun record(text: String, documentName: String) {
        text.split("\\s+".toRegex()).forEach { word ->
            val cleanedWord = cleanWord(word)
            cleanedWord?.let { invertedIndex.getOrPut(it) { LinkedList<String>() }.add(documentName) }
        }
    }

    private fun cleanWord(word: String): String? = word.lowercase().removeWordNoise()?.steam()
    private fun String.steam(): String = stemmer.stem(this)

    private fun String.removeWordNoise(): String? {
        val isNotLetter = { input: Char -> !input.isLetter() }
        return dropWhile(isNotLetter).run { dropLastWhile(isNotLetter) }.ifBlank { null }
    }
}
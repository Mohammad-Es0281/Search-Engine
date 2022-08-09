import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.HashMap

class SearchEngineTest {
    private val files = File("src/test/resources/inputs_small").listFiles()!!
    private val searchEngine = SearchEngine(files)

    @Test
    fun removeWordNoise_twoSideNoiseWord() {
        val word = "(cats!!)."

        val removeWordNoiseMethod: Method = SearchEngine::class.java.getDeclaredMethod("removeWordNoise", String::class.java)
        removeWordNoiseMethod.isAccessible = true
        val expected = removeWordNoiseMethod.invoke(searchEngine, word) as String?

        assertEquals(expected, "cats")
    }

    @Test
    fun steam_nonRootWord() {
        val word = "cats"

        val steamMethod: Method = SearchEngine::class.java.getDeclaredMethod("steam", String::class.java)
        steamMethod.isAccessible = true
        val expected = steamMethod.invoke(searchEngine, word) as String?

        assertEquals(expected, "cat")
    }

    @Test
    fun steam_rootWord() {
        val word = "class"

        val steamMethod: Method = SearchEngine::class.java.getDeclaredMethod("steam", String::class.java)
        steamMethod.isAccessible = true
        val expected = steamMethod.invoke(searchEngine, word) as String?

        assertEquals(expected, "class")
    }

    @Test
    fun cleanWord_noisyNonRootWord() {
        val word = "(cats!!)."

        val cleanWordMethod: Method = SearchEngine::class.java.getDeclaredMethod("cleanWord", String::class.java)
        cleanWordMethod.isAccessible = true
        val expected = cleanWordMethod.invoke(searchEngine, word) as String?

        assertEquals(expected, "cat")
    }

    @Test
    fun record_test() {
        val text = "(cats!!). class"
        val documentName = "0"

        val invertedIndexField: Field = searchEngine.javaClass.getDeclaredField("invertedIndex")
        invertedIndexField.isAccessible = true
        val expected = invertedIndexField.get(searchEngine) as HashMap<String, LinkedList<String>>
        expected.clear()
        val recordMethod: Method = SearchEngine::class.java.getDeclaredMethod("record", String::class.java, String::class.java)
        recordMethod.isAccessible = true
        recordMethod.invoke(searchEngine, text, documentName) as String?

        val actualDocument = LinkedList<String>().apply { add(documentName) }
        val actual = hashMapOf("cat" to actualDocument, "class" to actualDocument)
        assertEquals(expected, actual)
    }

    @Test
    fun search_emptyWord() {
        val word = ""

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf())
    }

    @Test
    fun search_singleCorrectWord() {
        val word = "cats"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("0", "1"))
    }

    @Test
    fun search_sentenceWithNoPrefix() {
        val word = "cats word"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("0"))
    }

    @Test
    fun search_sentenceWithPlusPrefix() {
        val word = "+cats word"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("0", "1"))
    }

    @Test
    fun search_sentenceWithMinusPrefix() {
        val word = "cats -word"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("1"))
    }

    @Test
    fun search_sentenceWithAllPrefixes() {
        val word = "cats -word +end"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("1"))
    }


    @Test
    fun search_singleFile() {
        val file = File("src/test/resources/inputs_small/0")
        val searchEngine = SearchEngine(file)
        val word = "cats"

        val expected = searchEngine.search(word)

        assertArrayEquals(expected.toTypedArray(), arrayOf("0"))
    }
}
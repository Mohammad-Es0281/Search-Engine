import org.junit.Test;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SearchEngineJavaTest {
    private final File[] files = new File("src/test/resources/inputs_small").listFiles();
    private final SearchEngineJava searchEngine =  new SearchEngineJava(files);

    @Test
    public void removeWordNoise_twoSideNoiseWord() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String word = "(cats!!).";

        Method removeWordNoiseMethod = SearchEngineJava.class.getDeclaredMethod("removeWordNoise", String.class);
        removeWordNoiseMethod.setAccessible(true);
        String expected = (String) removeWordNoiseMethod.invoke(searchEngine, word);

        assertEquals(expected, "cats");
    }

    @Test
    public void cleanWord_noisyNonRootWord() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String word = "(cats!!).";

        Method cleanWordMethod = SearchEngineJava.class.getDeclaredMethod("cleanWord", String.class);
        cleanWordMethod.setAccessible(true);
        String expected = (String) cleanWordMethod.invoke(searchEngine, word);

        assertEquals(expected, "cat");
    }

    @Test
    public void record_test() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String text = "(cats!!). class";
        String documentName = "0";

        Field invertedIndexField = searchEngine.getClass().getDeclaredField("invertedIndex");
        invertedIndexField.setAccessible(true);
        HashMap<String, LinkedList<String>> expected = (HashMap<String, LinkedList<String>>) invertedIndexField.get(searchEngine);
        expected.clear();

        Method recordMethod = SearchEngineJava.class.getDeclaredMethod("record", String.class, String.class);
        recordMethod.setAccessible(true);
        recordMethod.invoke(searchEngine, text, documentName);

        LinkedList<String> actualDocument = new LinkedList<>();
        actualDocument.add(documentName);
        HashMap<String, LinkedList<String>> actual = new HashMap<>();
        actual.put("cat", actualDocument);
        actual.put("class", actualDocument);
        assertEquals(expected, actual);
    }

    @Test
    public void search_emptyWord() {
        String word = "";

        HashSet<String> expected = searchEngine.search(word);
        assertEquals(expected.size(), 0);
    }

    @Test
    public void search_singleCorrectWord() {
        String word = "cats";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[2];
        actual[0] = "0";
        actual[1] = "1";
        assertArrayEquals(expected.toArray(actual), actual);
    }

    @Test
    public void search_sentenceWithNoPrefix() {
        String word = "cats word";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[1];
        actual[0] = "0";
        assertArrayEquals(expected.toArray(actual), actual);
    }

    @Test
    public void search_sentenceWithPlusPrefix() {
        String word = "+cats word";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[2];
        actual[0] = "0";
        actual[1] = "1";
        assertArrayEquals(expected.toArray(actual), actual);
    }

    @Test
    public void search_sentenceWithMinusPrefix() {
        String word = "cats -word";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[1];
        actual[0] = "1";
        assertArrayEquals(expected.toArray(actual), actual);
    }

    @Test
    public void search_sentenceWithAllPrefixes() {
        String word = "cats -word +end";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[1];
        actual[0] = "1";
        assertArrayEquals(expected.toArray(actual), actual);
    }


    @Test
    public void search_singleFile() {
        File file = new File("src/test/resources/inputs_small/0");
        SearchEngineJava searchEngine = new SearchEngineJava(file);
        String word = "cats";

        HashSet<String> expected = searchEngine.search(word);
        String[] actual = new String[1];
        actual[0] = "0";
        assertArrayEquals(expected.toArray(actual), actual);
    }
}
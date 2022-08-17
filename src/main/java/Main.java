import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Enter directory path to add files or -1 to exit: ");
            String fileName = sc.nextLine();
            if (fileName.equals("-1"))
                return;
            File[] files = new File(fileName).listFiles();
            SearchEngineJava searchEngine =  new SearchEngineJava(files);
            while (true) {
                System.out.print("Enter word to search or -1 to exit: ");
                String word = sc.nextLine();
                if (word.equals("-1"))
                    break;
                System.out.println(searchEngine.search(word));
            }
        }
    }
}
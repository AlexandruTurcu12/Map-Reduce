import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Map extends Thread {
    private final ExecutorService executorService;
    private final String fileName;
    private final int offset;
    private int N;
    private final List<ConcurrentHashMap<Integer, Integer>> dictionaryList;
    private final List<List> wordsList;

    public Map(ExecutorService executorService, String fileName, int offset, int N, List<ConcurrentHashMap<Integer, Integer>> dictionaryList, List<List> wordsList) {
        this.executorService = executorService;
        this.fileName = fileName;
        this.offset = offset;
        this.N = N;
        this.dictionaryList = dictionaryList;
        this.wordsList = wordsList;
    }

    @Override
    public void run() {

        FileReader fin = null;
        long start = 0;
        try {
            fin = new FileReader(fileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //ma pozitionez cu un caracter inainte de inceputul fragmentului (daca acesta se afla in mijlocul documentului)
        if(offset != 0) {
            try {
                start = fin.skip(offset - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConcurrentHashMap<Integer, Integer> dictionary = new ConcurrentHashMap<Integer, Integer>();
        int dim = 0;
        int word = 0;
        int no_words;
        String currentWord = "";
        String longestWord = "";
        ArrayList<String> longestWordsList = new ArrayList<>();
        int ch = 0;
        char prev_char = '\u0000';
        String separators = ";:/?~\\.,><`[]{}()!@#$%^&-_+'=*\"| \t\r\n";
        String letters = "qQwWeErRtTyYuUiIoOpPaAsSdDfFgGhHjJkKlLzZxXcCvVbBnNmM1234567890";

        if(start == offset-1) {
            try {
                //citesc caracterul de dinaintea fragmentului
                ch = fin.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                int sep = 0;
                while (sep == 0) {
                    //in cazul in care caracterul este separator, incep citirea cuvintelor valide (mai jos)
                    for (int i = 0; i < separators.length(); i++) {
                        if ((char) ch == separators.charAt(i)) {
                            sep = 1;
                            break;
                        }
                    }

                    //in cazul in care caracterul este litera, continui sa citesc caractere pana cand dau de un separator
                    for (int i = 0; i < letters.length(); i++) {
                        if ((char) ch == letters.charAt(i)) {
                            prev_char = (char) ch;
                            ch = fin.read();
                            N--;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //acum citesc cuvintele valide din cadrul paragrafului
        while (dim < N && ch != -1) {
            //memorez caracterul actual pentru a citi unul nou
            if(dim != 0) {
                prev_char = (char) ch;
            }
            try {
                //assert fin != null;
                ch = fin.read();
                    //incrementez numarul caracterelor valide citite
                    dim++;

                    //in cazul in care caracterul este o litera, o adaug la cuvantul actual si incrementez lungimea lui
                    for(int k = 0; k < letters.length(); k++)
                        if((char) ch == letters.charAt(k) && dim < N){
                            word++;
                            currentWord += (char)ch;
                        }

                //daca am ajuns la finalul fragmentului si se incheie cu o litera, continui sa citesc
                for(int k = 0; k < letters.length(); k++)
                    if((char) ch == letters.charAt(k) && dim == N) {
                        word++;
                        currentWord += (char) ch;
                        N++;
                    }

                int sep = 0;
                    for(int k = 0; k < separators.length(); k++)
                        if((char) ch == separators.charAt(k)) {
                            sep = 1;
                            break;
                        }

                //in cazul in care caracterul este separator sau daca am ajuns la finalul fisierului
                if(sep == 1 || ch == -1)
                            //daca inainte am avut o litera
                            for(int t = 0; t < letters.length(); t++)
                                if(prev_char == letters.charAt(t)){
                                    if(word != 0){
                                    //se incheie cuvantul; il adaug in dictionar si, eventual, in lista
                                    if(dictionary.get(word) != null) {
                                        no_words = dictionary.get(word);
                                        no_words++;
                                        dictionary.put(word, no_words);
                                        if(currentWord.length() > longestWord.length()){
                                            longestWord = currentWord;
                                            longestWordsList.clear();
                                            longestWordsList.add(longestWord);
                                        } else if(currentWord.length() == longestWord.length()){
                                            longestWord = currentWord;
                                            longestWordsList.add(longestWord);
                                        }
                                    }
                                    else {
                                        no_words = 1;
                                        dictionary.put(word, no_words);

                                        if(currentWord.length() > longestWord.length()){
                                            longestWord = currentWord;
                                            longestWordsList.clear();
                                            longestWordsList.add(longestWord);
                                        } else if(currentWord.length() == longestWord.length()){
                                            longestWord = currentWord;
                                            longestWordsList.add(longestWord);
                                        }
                                    }
                                    }
                                    //reinitializez cuvantul si lungimea acestuia
                                    word = 0;
                                    currentWord = "";
                                    break;
                                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dictionaryList.add(dictionary);
        wordsList.add(longestWordsList);
        try {
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

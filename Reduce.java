import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Reduce extends Thread {

    private final ExecutorService executorService;
    private final String fileName;
    private final List<ConcurrentHashMap<Integer, Integer>> dictionary;
    private final List<List> list;
    private final List<String> finalFileNames;
    private final List<Float> rangList;
    private final List<Integer> maxList;
    private final List<Integer> noMaxList;
    private final CompletableFuture<String> cf1;
    private final CompletableFuture<Float> cf2;
    private final CompletableFuture<Integer> cf3;
    private final CompletableFuture<Integer> cf4;

    public Reduce(ExecutorService executorService, String fileName, List<ConcurrentHashMap<Integer, Integer>> dictionary, List<List> list,
                  List<String> finalFileNames, List<Float> rangList, List<Integer> maxList, List<Integer> noMaxList,
                  CompletableFuture<String> cf1, CompletableFuture<Float> cf2, CompletableFuture<Integer> cf3, CompletableFuture<Integer> cf4) {
        this.executorService = executorService;
        this.fileName = fileName;
        this.dictionary = dictionary;
        this.list = list;
        this.finalFileNames = finalFileNames;
        this.rangList = rangList;
        this.maxList = maxList;
        this.noMaxList = noMaxList;
        this.cf1 = cf1;
        this.cf2 = cf2;
        this.cf3 = cf3;
        this.cf4 = cf4;
    }

    public static int Fibonacci(int n){
        int fib = 0;
        int t1 = 0;
        int t2 = 1;

        for(int i = 3; i <= (n+2); i++){
            fib = t1 + t2;
            t1 = t2;
            t2 = fib;
        }
        return fib;
    }

    @Override
    public void run(){

        float rang = 0;
        int maxLength = 0;
        int noMaxLength = 0;
        float noWords = 0;
        float fib = 0;

        for(ConcurrentHashMap<Integer, Integer> k : dictionary){
            for(ConcurrentHashMap.Entry<Integer, Integer> entry : k.entrySet()){
                Integer key = entry.getKey();
                Integer value = entry.getValue();

                fib += Fibonacci(key) * value;
                noWords += value;
                if (key > maxLength){
                    maxLength = key;
                    noMaxLength = value;
                } else if (key == maxLength){
                    noMaxLength += value;
                }

                }
            }
        rang = (fib / noWords);

        cf1.complete(this.fileName);
        cf2.complete(rang);
        cf3.complete(maxLength);
        cf4.complete(noMaxLength);
    }
}

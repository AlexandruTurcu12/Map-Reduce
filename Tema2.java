import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Tema2 {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        int P = Integer.parseInt(args[0]);
        File fileIn = new File(args[1]);
        File fileOut = new File(args[2]);
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        FileReader reader = new FileReader(fileIn);
        BufferedReader scan = new BufferedReader(reader);

        int N = Integer.parseInt(scan.readLine());
        int no_docs = Integer.parseInt(scan.readLine());

        String[] fileNames = new String[no_docs];
        File[] files = new File[no_docs];
        Integer[] docLengths = new Integer[no_docs];
        String x = "../";

        //memorez in vectori numele fisierelor si lungimea acestora
        for(int i = 0; i < no_docs; i++){
            fileNames[i] = x.concat(scan.readLine());
            files[i] = new File(fileNames[i]);
            FileReader readerFile = new FileReader(files[i]);
            BufferedReader scanFile = new BufferedReader(readerFile);
            int dim = 0;
            int str;
            while((str = scanFile.read()) != -1) {
                dim++;
            }
            docLengths[i] = dim;
        }

        List<Float> rangList = Collections.synchronizedList(new ArrayList<>());
        List<Integer> maxList = Collections.synchronizedList(new ArrayList<>());
        List<Integer> noMaxList = Collections.synchronizedList(new ArrayList<>());
        List<String> finalFileNames = Collections.synchronizedList(new ArrayList<>());

        List<List<ConcurrentHashMap<Integer, Integer>>> finalDictionaryList = Collections.synchronizedList(new ArrayList<>());
        List<List<List>> finalWordsList = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executorMap = Executors.newFixedThreadPool(P);
        ExecutorService executorReduce = Executors.newFixedThreadPool(P);

        //crearea si incheierea task-urilor map
        for(int i = 0; i < no_docs; i++) {
            int offset = 0;
            List<ConcurrentHashMap<Integer, Integer>> dictionaryList = Collections.synchronizedList(new ArrayList<>());
            List<List> wordsList = Collections.synchronizedList(new ArrayList<>());

            for(int j = 0; j < docLengths[i]; j+=N) {
                if((j+N) > docLengths[i]){
                    executorMap.submit(new Map(executorMap, fileNames[i], offset, docLengths[i] - offset, dictionaryList, wordsList));
                } else {
                    executorMap.submit(new Map(executorMap, fileNames[i], offset, N, dictionaryList, wordsList));
                    offset += N;
                }
            }

            finalDictionaryList.add(dictionaryList);
            finalWordsList.add(wordsList);
        }

        executorMap.shutdown();
        try {
            executorMap.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //crearea si incheierea task-urilor reduce
        for (int i = 0; i < no_docs; i++) {
            CompletableFuture<String> cf1 = new CompletableFuture<>();
            CompletableFuture<Float> cf2 = new CompletableFuture<>();
            CompletableFuture<Integer> cf3 = new CompletableFuture<>();
            CompletableFuture<Integer> cf4 = new CompletableFuture<>();

            executorReduce.submit(new Reduce(executorReduce, fileNames[i], finalDictionaryList.get(i), finalWordsList.get(i), finalFileNames, rangList, maxList, noMaxList,
                    cf1, cf2, cf3, cf4));

            finalFileNames.add(cf1.get());
            rangList.add(cf2.get());
            maxList.add(cf3.get());
            noMaxList.add(cf4.get());
        }

        executorReduce.shutdown();
            try {
                executorReduce.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //sortarea listelor finale
        for(int i = 0; i < no_docs - 1; i++)
            for(int j = i; j < no_docs; j++)
                if(rangList.get(i) < rangList.get(j)){

                    Collections.swap(maxList,i,j);
                    Collections.swap(noMaxList,i,j);
                    Collections.swap(finalFileNames,i,j);
                    Collections.swap(rangList,i,j);
                }

        //scrierea in fisier
        FileWriter writer = new FileWriter(fileOut);
        for(int i = 0; i < no_docs; i++) {
            files[i] = new File(finalFileNames.get(i));
            writer.write(files[i].getName() + "," + String.format("%.2f", rangList.get(i)) + "," + maxList.get(i) + "," + noMaxList.get(i) + "\n");
        }
        writer.close();
        reader.close();
        scan.close();
    }

}
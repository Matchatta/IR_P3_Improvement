//Name(s):Matchatta Toyaem
//ID
//Section
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class implements PageRank algorithm on simple graph structure.
 * Put your name(s), ID(s), and section here.
 *
 */
public class PageRanker {
	
	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into memory.
	 * Each line in the input file should have the following format:
	 * <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. 
	 * You can assume that a page ID is an integer.
	 */
	String inputContent;
	TreeSet<Integer> P = new TreeSet<>();
	TreeSet<Integer> S;
	HashMap<Integer, TreeSet<Integer>> M = new HashMap<>();
	HashMap<Integer, Long> L = new HashMap<>();
	HashMap<Integer, Double> PR = new HashMap<>();
	Double d;
	List<Double> perplexity = new ArrayList<>();
	public void loadData(String inputLinkFilename){
		try {
			inputContent = new String(Files.readAllBytes(Paths.get(inputLinkFilename)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method will be called after the graph is loaded into the memory.
	 * This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize(){
		d =0.85;
		List<String> splitString = Arrays.asList(inputContent.split("\r?\n+"));
		List<Integer> out_link = new ArrayList<>();
		for(String line : splitString){
			List<Integer> pages = Arrays.stream(line.split(" ")).map(Integer::valueOf).collect(Collectors.toList());
			P.addAll(pages);
			int id = pages.remove(0);
			if(!L.containsKey(id)){
				L.put(id, (long) 0.0);
			}
			TreeSet<Integer> m = new TreeSet<>(pages);
			out_link.addAll(m);
			M.put(id, m);
		}
		Map<Integer, Long> l = out_link.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		L.putAll(l);
		S = new TreeSet<>(P);
		S.removeAll(new TreeSet(out_link));
		double value = 1.0/P.size();
		for(int p : P){
			PR.put(p, value);
		}
	}
	
	/**
	 * Computes the perplexity of the current state of the graph. The definition
	 * of perplexity is given in the project specs.
	 */
	public double getPerplexity(){
		double power =0;
		for(int p : P){
			power += PR.get(p)*(Math.log(PR.get(p))/Math.log(2.0));
		}
		return Math.pow(2, -power);
	}
	
	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank algorithm).
	 * Returns false otherwise (and PageRank algorithm continue to update the page scores). 
	 */
	int round =0;
	public boolean isConverge(){
		perplexity.add(getPerplexity());
		double unit = 1;
		if(perplexity.size()>1){
			unit = Math.floor(perplexity.get(perplexity.size()-1))-Math.floor(perplexity.get(perplexity.size()-2));
		}
		if(round>=2){
			return true;
		}
		else if(unit == 0) {
			round++;
			return false;
		}
		else{
			round=0;
			return false;
		}
	}
	
	/**
	 * The main method of PageRank algorithm. 
	 * Can assume that initialize() has been called before this method is invoked.
	 * While the algorithm is being run, this method should keep track of the perplexity
	 * after each iteration. 
	 * 
	 * Once the algorithm terminates, the method generates two output files.
	 * [1]	"perplexityOutFilename" lists the perplexity after each iteration on each line. 
	 * 		The output should look something like:
	 *  	
	 *  	183811
	 *  	79669.9
	 *  	86267.7
	 *  	72260.4
	 *  	75132.4
	 *  
	 *  Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate.
	 * 		The output should look something like:
	 * 		
	 * 		1	0.1235
	 * 		2	0.3542
	 * 		3 	0.236
	 * 		
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename){
		while(!isConverge()){
			double sinkPR=0;
			HashMap<Integer, Double> newPR = new HashMap<>();
			for(int p : S){
				sinkPR+=PR.get(p);
			}
			for(int p : P){
				double newPR_value = (1-d)/P.size();
				newPR_value+= d*sinkPR/P.size();
				if(M.containsKey(p)){
					for(int q : M.get(p)){
						newPR_value+=d*PR.get(q)/L.get(q).doubleValue();
					}
				}
				newPR.put(p, newPR_value);
			}
			PR.putAll(newPR);
		}
		try{
			FileWriter perplexityFile = new FileWriter(perplexityOutFilename);
			PrintWriter writePerplexity = new PrintWriter(perplexityFile);
			FileWriter prFile = new FileWriter(prOutFilename);
			PrintWriter writePR = new PrintWriter(prFile);
			for(int p : P){
				writePR.println(p+" "+PR.get(p));
			}
			for(double per : perplexity.subList(1, perplexity.size())){
				writePerplexity.println(per);
			}
			writePerplexity.close();
			writePR.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K){
		Map<Integer, Double> r = PR.entrySet()
				.stream()
				.sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
		Integer[] result = new Integer[K];
		for(int i=0; i< K; i++){
			result[i] = (Integer) r.keySet().toArray()[i];
		}
		return result;
	}
	
	public static void main(String args[])
	{
	long startTime = System.currentTimeMillis();
		PageRanker pageRanker =  new PageRanker();
		pageRanker.loadData("test.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(6);
	double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}

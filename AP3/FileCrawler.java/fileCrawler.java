/*Name: Joshua Marks 
login: 1000275m 
Assignment: AP3 Exercise 2

*This is my own work except that I have used the function cvtPattern from Regex given in the source files and I have used the processDirectory from directoryCrawler but changed from a print statement to add the directory to the work queue.
*/

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.*;



public class fileCrawler {

	public static class worker implements Runnable{
		private Pattern p;
		private ConcurrentSkipListSet<String> fileList;
		private LinkedBlockingQueue<String> dirList;

		worker(Pattern pat, ConcurrentSkipListSet<String> fileList, LinkedBlockingQueue<String> dirList) {
			this.p  = pat;
			this.fileList = fileList;
			this.dirList = dirList;
		}

		public void CslsAdd(File dir){
			File entries[] = dir.listFiles(); // make a list of all the files and folders in the directory given
			for (File entry : entries ) { //for every element in the list
				if (entry.isFile()) { // check if it is a file
					Matcher m = p.matcher(entry.getName()); // see if it matches the pattern
					if (m.matches()){ // if so
						fileList.add(dir.getPath() + "/" + entry.getName()); // add the path and string name to the file list of the file. 
					}
				}
			}
		}
		
		public void run(){
			File file = null; //have to define it before because the LBQ.take is in a try statement
			while (true){ // run forever
				try {
					file = new File(dirList.take()); // take the first dir off the work queue (waiting if it is empty)
					CslsAdd(file); // add any files matching the pattern into the file list
				} catch (InterruptedException e) {

					while (!dirList.isEmpty()){ // once interrupted keep taking things of the work queue until it is empty
						file = new File(dirList.poll());
						CslsAdd(file);
					}
					return; // leave the infinite loop once the work queue is empty
				}
			}
		}
	}

	/*totally copied*/
	public static String cvtPattern(String str) {
		StringBuilder pat = new StringBuilder();
		int start, length;

		pat.append('^');
		if (str.charAt(0) == '\'') {	// double quoting on Windows
			start = 1;
			length = str.length() - 1;
		} else {
			start = 0;
			length = str.length();
		}
		for (int i = start; i < length; i++) {
			switch(str.charAt(i)) {
			case '*': pat.append('.'); pat.append('*'); break;
			case '.': pat.append('\\'); pat.append('.'); break;
			case '?': pat.append('.'); break;
			default:  pat.append(str.charAt(i)); break;
			}
		}
		pat.append('$');
		return new String(pat);
	}



	/*all copied except changed print statement to LBQ.add(name)*/
	public void processDirectory(String name, LinkedBlockingQueue<String> dirList) {
		try {
			File file = new File(name);	// create a File object
			if (file.isDirectory()) {	//if file is directory
				dirList.add(name); // then add to work queue
				String entries[] = file.list(); // create a list of all files and folders in the directory given
				if (entries != null) {

					for (String entry : entries ) {
						if (entry.compareTo(".") == 0)
							continue;
						if (entry.compareTo("..") == 0)
							continue;

						processDirectory(name+"/"+entry, dirList);
					}
				}
			}
		}
		catch (Exception e) {
			System.err.println("Error processing "+ name +": "+e);
		}
	}

	public static void main( String Arg[] ) throws IOException{
		fileCrawler FC = new fileCrawler();
		int noThreads = 853;
        String string = new String();
        string.end
		ConcurrentSkipListSet<String> fileList = new ConcurrentSkipListSet<String>();
		LinkedBlockingQueue<String> dirList = new LinkedBlockingQueue<String>();

		if ((System.getenv("CRAWLER_THREADS") != null)){
			noThreads = Integer.parseInt(System.getenv("CRAWLER_THREADS"));
		}

		Thread workers[] = new Thread[noThreads];

		/* 2 lines copied*/
		String pattern = cvtPattern(Arg[0]);
		Pattern p = Pattern.compile(pattern);

		/*worker stuff can't really change*/
		worker wt = new worker(p, fileList, dirList);

		for (int i = 0;i<workers.length;i++){

			workers[i] = new Thread(wt);
			workers[i].start();
		}

		/* makes it work with no directory argument*/
		if (Arg.length == 1){
			FC.processDirectory(".", dirList);
		}
		else {
			for (int i = 1; i<Arg.length; i++)
				FC.processDirectory(Arg[i], dirList);
		}

		/*interrupts the workers once the worker queue*/
		for (Thread t : workers){
			t.interrupt();
		}
		/*join workers once they are done*/
		for (Thread k : workers){
			try {
				k.join();
			}
			catch(Exception e){
			}
		}
		/*print out everything in the File list*/
		while (!fileList.isEmpty()){
			System.out.println(fileList.pollFirst());
		}
	}
}

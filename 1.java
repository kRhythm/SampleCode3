import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;


public class DiffFilesInCommit {
	static int fileCount = 0;	
    public static void main(String[] args) throws Exception {
    	PrintStream console = System.out; 
        String REMOTE_URL = "https://github.com/kRhythm/SampleCode2";
        String[] bits = REMOTE_URL.split("/");
        int buildId = 0;
        String packageName = bits[bits.length-1];
        /*Git result = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(new File("C:\\Users\\rkanchug\\Desktop\\TET"))
                .call();*/
		Git result = Git.open(new File("C:\\Users\\rkanchug\\Desktop\\TET"));
        File Tokens= new File("C:\\Users\\rkanchug\\Desktop\\Tokens.json");
        PrintStream TokensStream = new PrintStream(Tokens);
        try (Repository repository = result.getRepository()) {
            
            Collection<Ref> allRefs = repository.getRefDatabase().getRefs();

            try (RevWalk revWalk = new RevWalk( repository )) {
                for( Ref ref : allRefs ) {
                    revWalk.markStart( revWalk.parseCommit( ref.getObjectId() ));
                }
                System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);
                int count = 0;
                RevCommit previouscommit = null;
                RevCommit presentcommit = null;
                
                for( RevCommit commit : revWalk ) 
                {
                	
                    if(count==0){
                        previouscommit = commit;//System.out.println("Commit No. " + count);
                    }
                    else
                    {
						//previouscommit = commit;
                    	PersonIdent CommitTime = commit.getCommitterIdent();
                    	Date CommitDate = CommitTime.getWhen();
						DateFormat formatterUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						formatterUTC.setTimeZone(TimeZone.getTimeZone("PDT"));
                    	String objectIdOfCommit= previouscommit.getId().toString();
                    	String itemsOfCommitId[] = objectIdOfCommit.split(" ");
                    	String objId = itemsOfCommitId[1];
                    	String FileDate = formatterUTC.format(CommitDate);
                    	String FileName = "";
                    	String FileLink = "";
                        presentcommit = commit;
                        try (Git git = new Git(repository)) 
                        {
                            RevTree previoustree = previouscommit.getTree();
                            RevTree presenttree = presentcommit.getTree();
                            File FileInfo= new File("C:\\Users\\rkanchug\\Desktop\\FileInfo.txt");
                            PrintStream fileInfoStream = new PrintStream(FileInfo);
                            System.setOut(fileInfoStream);
                            listDiff(repository, git,presentcommit,previouscommit);
                            BufferedReader Fi = new BufferedReader(new FileReader(FileInfo));
                            String Fline = null;
                            ArrayList<String> OriginalM = new ArrayList<String>();
                            ArrayList<String> RevisedM = new ArrayList<String>();
                            while(  (Fline = Fi.readLine()) != null )
                            {
                                String[] WordsOfLine = Fline.split(" ",3);
                                if(WordsOfLine[0].equals("MODIFY"))
                                {
                                	if(WordsOfLine[1].endsWith(".java") && WordsOfLine[2].endsWith(".java"))
                                	{
                                		OriginalM.add(WordsOfLine[1]);
                                    	RevisedM.add(WordsOfLine[2]); 
                                	}
                                }
                                else if(WordsOfLine[0].contentEquals("ADD") && (WordsOfLine[2].endsWith(".java"))  )
							 	{
                                 File OringinalFile = new File("C:\\Users\\rkanchug\\Desktop\\Original.java");
	                             File RevisedFile = new File("C:\\Users\\rkanchug\\Desktop\\Revised.java");
								 PrintStream RF = new PrintStream(RevisedFile);
								 PrintStream OF = new PrintStream(OringinalFile);
								 System.setOut(OF);
								 System.out.println(" ");
								 System.setOut(console);
								 String D = WordsOfLine[2];
								 System.setOut(console);
	                             System.out.println("A"+D);
	                            
                                 try (TreeWalk treeWalk = new TreeWalk(repository)) 
								 {
									treeWalk.addTree(presenttree);
									treeWalk.setRecursive(false);
									FileLink = D;
                                    String[] splitPath = D.split("/");
                                    int lengthOfSplitPath = splitPath.length;
                                    int indexOfSplitPath = 0;
									 while(treeWalk.next())
	                                    {
	                                    	String treeName = treeWalk.getNameString();
	                                    	if(treeName.equals(splitPath[indexOfSplitPath]))
	                                    	{
	                                    		indexOfSplitPath++;
	                                    		if(indexOfSplitPath == lengthOfSplitPath)
	                                    		{
	                                    			FileName = treeName;
	                                    			ObjectId objectId = treeWalk.getObjectId(0);
	                                                ObjectLoader loader = repository.open(objectId);
	                                                System.setOut(RF);
	                                                loader.copyTo(System.out);
	                                    			break;
	                                    		}
	                                    		else
	                                    		{
	                                    			treeWalk.enterSubtree();
	                                    		}
	                                    	}
	                                    }   
								  }
                                 System.setOut(console);
                                 String ErrorString = "EvaluationPlanContext.java";
                                 if(!FileName.equals(ErrorString)){
                                 graph_diff g = new graph_diff();  
		                         String Entities = g.main(null);
		                         Tokenize t = new Tokenize(fileCount); 
								 String tokesOfTheFile = t.main(null); 
								 String CommitMessage = previouscommit.getFullMessage();
								 CommitMessage = CommitMessage.replaceAll("\n", " ").replace("\r", "");
			                     System.setOut(TokensStream);
			                     System.out.println("{\"index\":{\"_id\":\"" + fileCount + "\"}}");
								 System.out.println("{\"build_id\":\"" + buildId + "\",\"commit_id\":\"" + objId + "\",\"package_name\":\"" + packageName +"\",\"package_link\":\"" + REMOTE_URL  + "\",\"file_name\":\"" + FileName +"\",\"file_link\":\"" + FileLink +"\",\"file_date\":\"" + FileDate + "\",\"entities\":\"" + Entities +"\",\"commit_msg\":\""+CommitMessage +"\",\"diff_file\":\"" + tokesOfTheFile + "\"}"   );
			                     fileCount++;}
								 }
							 	 else if(WordsOfLine[0].equals("DELETE")&& (WordsOfLine[1].endsWith(".java") ) )
							 	 { 
							 	  File OringinalFile = new File("C:\\Users\\rkanchug\\Desktop\\Original.java");
	                              File RevisedFile = new File("C:\\Users\\rkanchug\\Desktop\\Revised.java");
								  PrintStream OF = new PrintStream(OringinalFile); 
								  PrintStream RF = new PrintStream(RevisedFile);
								  System.setOut(RF);
								  System.out.println(" ");
								  System.setOut(console);
								  String D = WordsOfLine[1]; 
								  FileLink = D;
								  System.setOut(console);
	                              System.out.println("D"+D);
								  try (TreeWalk treeWalk = new TreeWalk(repository)) 
								  {
									treeWalk.addTree(previoustree);
									treeWalk.setRecursive(false);
                                    String[] splitPath = D.split("/");
                                    int lengthOfSplitPath = splitPath.length;
                                    int indexOfSplitPath = 0;
									 while(treeWalk.next())
	                                    {
	                                    	String treeName = treeWalk.getNameString();
	                                    	if(treeName.equals(splitPath[indexOfSplitPath]))
	                                    	{
	                                    		indexOfSplitPath++;
	                                    		if(indexOfSplitPath == lengthOfSplitPath)
	                                    		{
	                                    			FileName = treeName;
	                                    			ObjectId objectId = treeWalk.getObjectId(0);
	                                                ObjectLoader loader = repository.open(objectId);
	                                                System.setOut(OF);
	                                                loader.copyTo(System.out);
	                                    			break;
	                                    		}
	                                    		else
	                                    		{
	                                    			treeWalk.enterSubtree();
	                                    		}
	                                    	}
	                                    }   
								  }
									 String ErrorString = "EvaluationPlanContext.java";
									 if(!FileName.equals(ErrorString)) {
										 graph_diff g = new graph_diff();
										 String Entities = g.main(null);
										 Tokenize t = new Tokenize(fileCount);
										 String tokesOfTheFile = t.main(null);
										 String CommitMessage = previouscommit.getFullMessage();
										 CommitMessage = CommitMessage.replaceAll("\n", " ").replace("\r", "");
										 System.setOut(TokensStream);
										 System.out.println("{\"index\":{\"_id\":\"" + fileCount + "\"}}");
										 System.out.println("{\"build_id\":\"" + buildId + "\",\"commit_id\":\"" + objId + "\",\"package_name\":\"" + packageName + "\",\"package_link\":\"" + REMOTE_URL + "\",\"file_name\":\"" + FileName + "\",\"file_link\":\"" + FileLink + "\",\"file_date\":\"" + FileDate + "\",\"entities\":\"" + Entities + "\",\"commit_msg\":\"" + CommitMessage + "\",\"diff_file\":\"" + tokesOfTheFile + "\"}");
										 fileCount++;
									 }
							 }	 
                            }
                            Fi.close();
                            for(int i=0;i<OriginalM.size();i++)
                            {
                                File OringinalFile = new File("C:\\Users\\rkanchug\\Desktop\\Original.java");
                                File RevisedFile = new File("C:\\Users\\rkanchug\\Desktop\\Revised.java");
                                PrintStream OF = new PrintStream(OringinalFile);
                                PrintStream RF = new PrintStream(RevisedFile);
                                String O = OriginalM.get(i);
                                FileLink = O;
                                System.setOut(console);
                                System.out.println("M"+O);
                                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                                    treeWalk.addTree(previoustree);
                                    treeWalk.setRecursive(false);
                                    String[] splitPath = O.split("/");
                                    int lengthOfSplitPath = splitPath.length;
                                    int indexOfSplitPath = 0;
									while(treeWalk.next())
	                                    {
	                                    	String treeName = treeWalk.getNameString();
	                                    	if(treeName.equals(splitPath[indexOfSplitPath]))
	                                    	{
	                                    		indexOfSplitPath++;
	                                    		if(indexOfSplitPath == lengthOfSplitPath)
	                                    		{
	                                    			FileName = treeName;
	                                    			ObjectId objectId = treeWalk.getObjectId(0);
	                                                ObjectLoader loader = repository.open(objectId);
	                                                System.setOut(RF);
	                                                loader.copyTo(System.out);
	                                    			break;
	                                    		}
	                                    		else
	                                    		{
	                                    			treeWalk.enterSubtree();
	                                    		}
	                                    	}
	                                    }   
                                }
                                String R = RevisedM.get(i);
                                System.setOut(console);
                                System.out.println("M"+R);
                                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                                    treeWalk.addTree(presenttree);
                                    treeWalk.setRecursive(false);
                                    String[] splitPath = O.split("/");
                                    int lengthOfSplitPath = splitPath.length;
                                    int indexOfSplitPath = 0;
									while(treeWalk.next())
	                                    {
	                                    	String treeName = treeWalk.getNameString();
	                                    	if(treeName.equals(splitPath[indexOfSplitPath]))
	                                    	{
	                                    		indexOfSplitPath++;
	                                    		if(indexOfSplitPath == lengthOfSplitPath)
	                                    		{
	                                    			ObjectId objectId = treeWalk.getObjectId(0);
	                                                ObjectLoader loader = repository.open(objectId);
	                                                System.setOut(OF);
	                                                loader.copyTo(System.out);
	                                    			break;
	                                    		}
	                                    		else
	                                    		{
	                                    			treeWalk.enterSubtree();
	                                    		}
	                                    	}
	                                    }
                                	}
								String ErrorString = "EvaluationPlanContext.java";
								if(!FileName.equals(ErrorString)) {
									graph_diff g = new graph_diff();
									String Entities = g.main(null);
									Tokenize t = new Tokenize(fileCount);
									String tokesOfTheFile = t.main(null);
									String CommitMessage = previouscommit.getFullMessage();
									CommitMessage = CommitMessage.replaceAll("\n", " ").replace("\r", "");
									System.setOut(TokensStream);
									System.out.println("{\"index\":{\"_id\":\"" + fileCount + "\"}}");
									System.out.println("{\"build_id\":\"" + buildId + "\",\"commit_id\":\"" + objId + "\",\"package_name\":\"" + packageName + "\",\"package_link\":\"" + REMOTE_URL + "\",\"file_name\":\"" + FileName + "\",\"file_link\":\"" + FileLink + "\",\"file_date\":\"" + FileDate + "\",\"entities\":\"" + Entities + "\",\"commit_msg\":\"" + CommitMessage + "\",\"diff_file\":\"" + tokesOfTheFile + "\"}");
									fileCount++;
								}
                             System.setOut(console);
                             }
                        }
                        previouscommit = commit;
                    }
                    count++;
                } 
            }
        }
        
    }
    
    private static void listDiff(Repository repository, Git git, ObjectId oldCommit, ObjectId newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();
        for (DiffEntry diff : diffs) {
            System.out.println(diff.getChangeType() + " " + diff.getOldPath() + " " + diff.getNewPath() );
        }
    }
    
    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objectId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
    
    
}    
    
        
  

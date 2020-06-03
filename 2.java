import java.io.*;
import java.util.*;
import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
public class GetDiffAndEntities {
    String DiffOfTheTwoVersions = " ";
    public static void main(String[] args) throws Exception {
        File OriginalFile = new File("C:\\Users\\rkanchug\\Desktop\\Original.java");
        File RevisedFile = new File("C:\\Users\\rkanchug\\Desktop\\Revised.java");
        final Diff getAstDiff = new AstComparator().compare(OriginalFile, RevisedFile);
        String astDiff = getAstDiff.toString();
        String[] astDiffArray = astDiff.split("\n");
        ArrayList<EntityInfo> listOfOriginalFile = getLineNumbersOfEntities(OriginalFile);
        ArrayList<EntityInfo> listOfRevisedFile = getLineNumbersOfEntities(RevisedFile);
        //VisitEntityArray(listOfRevisedFile);
        HashMap<String, Integer> frequencyOfEntities = new HashMap<String, Integer>();
        String Entities =" ";
        ArrayList<String> diffList = new ArrayList<String>();
        for(String line: astDiffArray)
        {
           // System.out.println("Line is" + line);
            String[] getOperation = line.split(" ",2);
            if(getOperation[0].equals("Update") || getOperation[0].equals("Insert") || getOperation[0].equals("Move") || getOperation[0].equals("Delete"))
            {
                String[] getLineNumber = line.split(":");
                if(getLineNumber.length == 2)
                {
                    String stringLineNumber = getLineNumber[1].trim();
                    if(stringLineNumber!=null)
                    {
                        int lineNumber = Integer.parseInt(stringLineNumber);
                        String getEntity =null;
                        if(getOperation[0].equals("Update") || getOperation[0].equals("Insert"))
                        {
                            getEntity = getEntityOfTheLineNumber(lineNumber,listOfRevisedFile);
                        }
                        else
                        {
                            getEntity = getEntityOfTheLineNumber(lineNumber,listOfOriginalFile);
                        }
                        if(getEntity!=null)
                        {
                            Entities = Entities + getEntity + " ";
                        }
                    }
                }
                else if(getLineNumber.length==3)
                {
                    String firstNumberString = getLineNumber[1];
                    String secondNumberString = getLineNumber[2];
                    int firstNumber = new Scanner(firstNumberString).useDelimiter("\\D+").nextInt();
                    int secondNumber = new Scanner(secondNumberString).useDelimiter("\\D+").nextInt();
                    String getEntity = null;
                    getEntity = getEntityOfTheLineNumber(firstNumber,listOfOriginalFile);
                    if(getEntity!=null)
                    {
                        Entities = Entities + getEntity + " ";
                    }
                    getEntity = getEntityOfTheLineNumber(secondNumber,listOfRevisedFile);
                    if(getEntity!=null)
                    {
                        Entities = Entities + getEntity + " ";
                    }
                }
            }
            else
            {
                diffList.add(line);
            }
        }
        ArrayList<String> entitiesAndDiff = new ArrayList<>();
        entitiesAndDiff.add(Entities);
        String tokens = TokenizeTheStringList(diffList);
        entitiesAndDiff.add(tokens);
        for(String s : entitiesAndDiff)
        {
            System.out.println(s);
        }
    }
    public static String TokenizeTheStringList(ArrayList<String> diffList)
    {
        String tokens = " ";
        for(String s : diffList) {
            StringTokenizer tokenizerForString = new StringTokenizer(s," \n\r:.\"(){}[],\t\\");
            while(tokenizerForString.hasMoreTokens())
            {
                tokens = tokens + tokenizerForString.nextToken()+" ";
            }
        }
        return tokens;
    }
    public static String getEntityOfTheLineNumber(Integer lineNumber,ArrayList<EntityInfo> listOfTheFile)
    {
        int indexOfTheLineNumber = binarySearch(listOfTheFile,0,listOfTheFile.size()-1,lineNumber);
        EntityInfo Ei = listOfTheFile.get(indexOfTheLineNumber);
        int start = Integer.parseInt(Ei.getStartLineNumber().toString().split(" |\\,")[1]);
        int end = Integer.parseInt(Ei.getEndLineNumber().toString().split(" |\\,")[1]);
        String EntityName = Ei.getEntityName();
        if(  (lineNumber >= start) && (lineNumber <=end ))
        {
            return EntityName;
        }
        return null;
    }

    private static int binarySearch(ArrayList<EntityInfo> listOfTheFile, int l, int r, int x)
    {
        if (r >= l) {
            int mid = l + (r - l) / 2;
            EntityInfo entityInfoOfTheFile = listOfTheFile.get(mid);
            String stringArrOfMid = entityInfoOfTheFile.getStartLineNumber().toString().split(" |\\,")[1];
            int arrOfMid = Integer.parseInt(stringArrOfMid.trim());
            if (arrOfMid == x)
                return mid;
            if (arrOfMid > x)
                return binarySearch(listOfTheFile, l, mid - 1, x);
            return binarySearch(listOfTheFile, mid + 1, r, x);
        }
        if(r<0)return 0;
        if(r>listOfTheFile.size()-1)
            return listOfTheFile.size()-1;
        return r;
    }

    public static ArrayList<EntityInfo> getLineNumbersOfEntities(File file) throws FileNotFoundException {
        CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(file));
        NodeList<TypeDeclaration<?>> typeDeclarations = cu.getTypes();
        ArrayList<EntityInfo> EntityInfoArray = new ArrayList<EntityInfo>();
        for (TypeDeclaration typeDec : typeDeclarations)
        {
            List<BodyDeclaration> members = typeDec.getMembers();
            if(members != null) {
                for (BodyDeclaration member : members) {
                    Optional<Position> begin = member.getBegin();
                    Optional<Position> end = member.getEnd();
                    if (member instanceof MethodDeclaration)
                    {
                        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
                        EntityInfo EntityInfoObject = new EntityInfo(begin,end,methodDeclaration.getNameAsString());
                        EntityInfoArray.add(EntityInfoObject);
                    }
                    else if(member instanceof ClassOrInterfaceDeclaration)
                    {
                        ClassOrInterfaceDeclaration classDeclaration =(ClassOrInterfaceDeclaration) member;
                        String className = classDeclaration.getName().toString();
                        EntityInfo EntityInfoObject = new EntityInfo(begin,end,className);
                        EntityInfoArray.add(EntityInfoObject);
                    }
                    else if(member instanceof ConstructorDeclaration)
                    {
                        ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) member;
                        String constructorName = constructorDeclaration.getNameAsString();
                        EntityInfo EntityInfoObject = new EntityInfo(begin,end,constructorName);
                        EntityInfoArray.add(EntityInfoObject);
                    }

                }
            }
        }
        return EntityInfoArray;
    }
    public static void VisitEntityArray(ArrayList<EntityInfo> EntityInfoArray){
        for(EntityInfo entityInfoObject : EntityInfoArray)
        {
            System.out.println(entityInfoObject.getStartLineNumber()+" "+entityInfoObject.getEndLineNumber()+" "+entityInfoObject.getEntityName());
        }
    }
}

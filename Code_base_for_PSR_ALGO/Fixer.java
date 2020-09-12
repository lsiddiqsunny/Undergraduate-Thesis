import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Fixer {
	//start set once instance variables
	private static File sourceFile = null;
	private static Vector<String> sourceVector = new Vector<String>();
	private static Vector<Integer> vulnerableLines = new Vector<Integer>();
	private static HashMap<String, String> guarenteedSecure = new HashMap<String, String>();
	//end set once instance variables
	
	//start change due to recursive method calls
	private static int PSInputTrailingNumber = 0;
	private static HashMap<String, int[]> rIDStrings = new HashMap<String, int[]>();
	//end change due to recursive algorithm/method calls
	
	//SMT: these are all line numbers that will potentially change when the sourceVector has lines added to it (or maybe removed)
	private static int declarationLineNumber = -1;
	private static int methodLineNumber = -1;
	private static int classLineNumber = -1;
	private static int firstMethodLineNumber = -1;
	private static int connectionInit = -1;
	private static int statementInit = -1;
	private static int executeLineNumber = -1;
	private static boolean repeatedVarRunthrough = false;
	private static Vector<Integer> recursiveLineChange = new Vector<Integer>();
	//SMT: end potentially change
	
	//change due to repetitive algorithm calls
	private static int repetitiveAlg = 0;
	/**
	 * Main method will be used as a crutch until I decide on
	 * what type of input method I'll take and whether or not
	 * I'll make a GUI
	 * @author Stephen Thomas smthomas@ncsu.edu
	 * @param args - String array of command line arguments.
	 * args[0] will be the path to the source file and
	 * args[1] will be the line number of the vulnerability
	 */
	public static void main(String[] args) {
		//input will need to be a source java file with
		//all line numbers in that file that have been marked vulnerable
		//line as marked by findbugs
		 
		sourceFile = new File(args[0]);
		
		for(int i = 1; i < args.length; i++) {
			if(!Character.isDigit(args[i].charAt(0))) {
				guarenteedSecure.put(args[i], args[i]);
			}
			else {
				vulnerableLines.add(Integer.parseInt(args[i]));
			}
		}
		System.out.println("guarenteedSecure contains: " + guarenteedSecure.keySet().toString());
		//removing all of the vulnerable lines so that we don't increment previous vulnerable lines
		while(vulnerableLines.size() > 0) {
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXNEW RUN THROUGH ALGORITHM");
			algorithm(vulnerableLines.remove(0));
			repetitiveAlg++;
			PSInputTrailingNumber = 0;
			//reinit just to be safe
			declarationLineNumber = -1;
			methodLineNumber = -1;
			classLineNumber = -1;
			firstMethodLineNumber = -1;
			connectionInit = -1;
			statementInit = -1;
			executeLineNumber = -1;
			rIDStrings = new HashMap<String, int[]>();
		}
		
		//checking insert
		
		try {
			System.out.println("the sourceFile name is: " + sourceFile.getName()); 
			PrintWriter pw = new PrintWriter(new File("output/SQL-IVFA" + sourceFile.getName()));
			for(int i = 0; i < sourceVector.size(); i++) {
				pw.write(sourceVector.get(i) + '\n');
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//XXX: SMT: don't forget the check for batch jobs
	/**
	 * The algorithm, basing the naming scheme on my pseudocode
	 * @param vulnerabilityLineNumber
	 */
	public static void algorithm(int vulnerabilityLineNumber){
		executeLineNumber = vulnerabilityLineNumber - 1;
		try {
			//ok, using repetitiveAlg as a first pass through check
			if(repetitiveAlg == 0) {
				BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
				//use less than since the lines start at 0 in the file
				//but start at 1 in eclipse
				String tmpString;
				while((tmpString = reader.readLine()) != null){
					//clean the tmpString of line comments first
					if(tmpString.indexOf("//") != -1) {
						//have to worry if the // is inside quotes or not...
						if(doubleQuoteCount(tmpString.indexOf("//"), tmpString) % 2 == 0) {
							tmpString = tmpString.substring(0, tmpString.indexOf("//"));
						}
					}
					sourceVector.add(tmpString);
				}
			}
			
			Vector<String> tokenVector = new Vector<String>();
			StringTokenizer myTokenizer = new StringTokenizer(sourceVector.get(executeLineNumber), "\t\n\r\f.=", true);
			while(myTokenizer.hasMoreTokens()){
				String tmpToken = myTokenizer.nextToken();
				//System.out.println("token is: " + tmpToken);
				boolean allWhiteSpace = true;
				char[] tmpCharArray = tmpToken.toCharArray();
				for(int i = 0; i < tmpCharArray.length && allWhiteSpace; i++) {
					if(!Character.isWhitespace(tmpCharArray[i])){
						allWhiteSpace = false;
					}
				}
				if(!allWhiteSpace) {
					tokenVector.add(tmpToken);
				}
			}
			
			//find method/class line numbers first
			determineClassAndMethodInfo();
			
			//find conditionals first as well
			/*determineConditionalBlocks()*/;
			
			String potentialStatement = null;
			String potentialConnection = null;
			String potentialExecute = "";
//			String potentialAssignment = null;
			String preSQLIV = "";
			
			boolean foundConnection = false;
			boolean foundStatement = false;
			boolean foundExecute = false;

			//more data needed for testing here:
			for(int i = 0; i < tokenVector.size(); i++) {
//				System.out.println("the token at: " + i + " position is : " + tokenVector.get(i));
			}
			for(int i = tokenVector.size() - 1; i >= 0; i--){
				if(tokenVector.get(i).startsWith("execute")){
					//System.out.println("starts with execute");
					
					//check for '.'
					if(i - 1 >= 0) {
						//System.out.println("i - 1 is positive");
						if(tokenVector.get(i - 1).equals(".")) {
							//System.out.println("i - 1 is a period");
							//check for leading period that would indicate not a Statement but a connection creating a new statement
							if(i - 2 >= 0) {
								//System.out.println("I have a potential statement");
								potentialStatement = tokenVector.get(i-2);
								//System.out.println("potentialStatement is: " + potentialStatement);
								//System.out.println("I have a potential execute");
								for(int j = i; j < tokenVector.size(); j++) {
									potentialExecute += tokenVector.get(j);
								}
//								potentialExecute = tokenVector.get(i);
								//System.out.println("potentialExecute is: " + potentialExecute);
							}
							if(i - 3 >= 0) {
								//System.out.println("i - 3 is positive");
								if(tokenVector.get(i - 3).equals(".")) {
									//System.out.println("i - 3 is a period");
									//connection is creating a statement
									potentialStatement = null;
									potentialExecute = "";
									if(i - 4 >= 0) {
										//System.out.println("I have a potential connection");
										potentialConnection = tokenVector.get(i - 4);
										//System.out.println("I have a potential execute");
										for(int j = i; j < tokenVector.size(); j++) {
											potentialExecute += tokenVector.get(j);
										}
//										potentialExecute = tokenVector.get(i);
										//System.out.println("potentialExecute is: " + potentialExecute);
									}
									if(i - 5 >= 0) {
										if(tokenVector.get(i - 5).equals(".")) {
			
											potentialConnection = null;
											potentialExecute = null;
											i -= 5;
										}
									//need to check for declaration of the connection
									else {
										//XXX: SMT: refactoring to find the first instance of whitespace and then taking only from the
										//end of the char[] to that white space to be the stmt.  DUP THIS FOR CONNECTIONS
										//XXX: SMT: begin char chopping
										char[] tmpConnCharArray = potentialConnection.toCharArray();
										boolean notWhiteSpace = true; 
										int whiteSpaceIndex = -1;
										for(int j = tmpConnCharArray.length - 1; notWhiteSpace && j >= 0; j--) {
											if(Character.isWhitespace(tmpConnCharArray[j])) {
												whiteSpaceIndex = j;
												notWhiteSpace = false;
											}
										}
										if(!notWhiteSpace) {
											potentialConnection = potentialConnection.substring(whiteSpaceIndex, potentialConnection.length());
/*											char[] newPotentialConnectionCharArray = new char[(tmpConnCharArray.length - whiteSpaceIndex)];
											for(int j = 0; (j + whiteSpaceIndex) < tmpConnCharArray.length; j++) {
												newPotentialConnectionCharArray[j] = tmpConnCharArray[j + whiteSpaceIndex];
											}
											potentialConnection = new String(newPotentialConnectionCharArray);*/
										}
										//XXX: SMT: don't know where I need to put this gathering of preExecute, but I'll put it here 
										//since I can grab the stuff before the connection or stmt
										//XXX: SMT: assume that we don't have the rID for the connection/statement more than once in the execute line
										preSQLIV = "";
										preSQLIV = sourceVector.get(executeLineNumber).substring(0, sourceVector.get(executeLineNumber).indexOf(potentialConnection));
										System.out.println("2Conn_+_+_+_+_+_+_+_+_+_+_+_+_+_+the potentialConnection is now: " + potentialConnection);
										//XXX: SMT: end char chopping
										if(newTestDeclaration("Connection", potentialConnection, executeLineNumber)) {
											//System.out.println("testDeclaration came back true!");
											foundConnection = true;
											foundExecute = true;
											connectionInit = declarationLineNumber;
											//SMT: new code to find the assigment!
											if(tokenVector.get(i - 5).equals("=")) {
												if(i - 6 >= 0) {
													//have potential assignment
//													potentialAssignment = tokenVector.get(i - 6);
//													//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
//													//System.out.println("this string has leading/trailing whitespace?" + potentialAssignment);
//													removeWhiteSpaceScanner = new Scanner(potentialAssignment).useDelimiter("\\A\\s+");
//													if(removeWhiteSpaceScanner.hasNext()) {
//														potentialAssignment = removeWhiteSpaceScanner.next();
//													}
//													removeWhiteSpaceScanner = new Scanner(potentialAssignment).useDelimiter("\\s+\\z");
//													if(removeWhiteSpaceScanner.hasNext()) {
//														potentialAssignment = removeWhiteSpaceScanner.next();
//													}
													//System.out.println("this string has no leading/trailing whitespace?" + potentialAssignment);
													//SMT: END SCANNER
													//System.out.println("I have a potential assignment: " + potentialAssignment);
													//SMT: assume no variable assignements to start with, will remove this assumption if needed later
													}
												else {
													//equals without an assigment, something is going on here that I'm going to ignore for the moment
													System.out.println("equals without an assignment");
												}
											}
											else {
												System.out.println("no assignment detected");
											}
											//SMT: assignment finding code end
										}
										else {
											//can't find declaration...what to do now?
											System.out.println("can't find the declaration");
										}
									}
									}
									else {
										//need to check for the connection declaration but don't need to check for the equals
										//XXX: SMT: refactoring to find the first instance of whitespace and then taking only from the
										//end of the char[] to that white space to be the stmt.  DUP THIS FOR CONNECTIONS
										//XXX: SMT: begin char chopping
										char[] tmpConnCharArray = potentialConnection.toCharArray();
										boolean notWhiteSpace = true; 
										int whiteSpaceIndex = -1;
										for(int j = tmpConnCharArray.length - 1; notWhiteSpace && j >= 0; j--) {
											if(Character.isWhitespace(tmpConnCharArray[j])) {
												whiteSpaceIndex = j;
												notWhiteSpace = false;
											}
										}
										if(!notWhiteSpace) {
											potentialConnection = potentialConnection.substring(whiteSpaceIndex, potentialConnection.length());;
											/*char[] newPotentialConnectionCharArray = new char[(tmpConnCharArray.length - whiteSpaceIndex)];
											for(int j = 0; (j + whiteSpaceIndex) < tmpConnCharArray.length; j++) {
												newPotentialConnectionCharArray[j] = tmpConnCharArray[j + whiteSpaceIndex];
											}
											potentialConnection = new String(newPotentialConnectionCharArray);*/
										}
										System.out.println("Conn_+_+_+_+_+_+_+_+_+_+_+_+_+_+the potentialConnection is now: " + potentialConnection);
										//XXX: SMT: end char chopping
										//XXX: SMT: don't know where I need to put this gathering of preExecute, but I'll put it here 
										//since I can grab the stuff before the connection or stmt
										//XXX: SMT: assume that we don't have the rID for the connection/statement more than once in the execute line
										preSQLIV = "";
										preSQLIV = sourceVector.get(executeLineNumber).substring(0, sourceVector.get(executeLineNumber).indexOf(potentialConnection));
										//System.out.println("this string has no leading/trailing whitespace?" + potentialConnection);
										//SMT: END SCANNER
										if(newTestDeclaration("Connection", potentialConnection, executeLineNumber)) {
											//System.out.println("testDeclaration came back true!");
											foundConnection = true;
											foundExecute = true;
											connectionInit = declarationLineNumber;
										}
										else {
											//can't find declaration...what to do now?
											System.out.println("can't find the declaration");
										}
									}
									
								}
								else {
									//need to check for statement, then need to check for '=' and if there is any
									//return value assignment going on
									//SMT: BEGIN PRE ONLY SCANNER (WHITESPACE REMOVER)
									//System.out.println("this string has leading/trailing whitespace?" + potentialStatement);
									//XXX: SMT: refactoring to find the first instance of whitespace and then taking only from the
									//end of the char[] to that white space to be the stmt.  DUP THIS FOR CONNECTIONS
									//XXX: SMT: begin char chopping
									char[] tmpStmtCharArray = potentialStatement.toCharArray();
									boolean notWhiteSpace = true; 
									int whiteSpaceIndex = -1;
									for(int j = tmpStmtCharArray.length - 1; notWhiteSpace && j >= 0; j--) {
										if(Character.isWhitespace(tmpStmtCharArray[j])) {
											whiteSpaceIndex = j;
											notWhiteSpace = false;
										}
									}
									if(!notWhiteSpace) {
										potentialStatement = potentialStatement.substring(whiteSpaceIndex, potentialStatement.length());
										/*char[] newPotentialStatementCharArray = new char[(tmpStmtCharArray.length - whiteSpaceIndex)];
										for(int j = 0; (j + whiteSpaceIndex) < tmpStmtCharArray.length; j++) {
											newPotentialStatementCharArray[j] = tmpStmtCharArray[j + whiteSpaceIndex];
										}
										potentialStatement = new String(newPotentialStatementCharArray);*/
									}
									System.out.println("_+_+_+_+_+_+_+_+_+_+_+_+_+_+the potentialStatement is now: " + potentialStatement);
									//XXX: SMT: end char chopping
									//XXX: SMT: CAN REMOVE SCANNER, FIRST MAKE IT SO THAT WE JUST SURROUND THE EXECUTE CALL WITH THE PRE AND 
									//POST FOR THE STATEMENT INSTEAD OF ASSIGNING IT TO THE RETURN VAR
									Scanner removeWhiteSpaceScanner = new Scanner(potentialStatement).useDelimiter("\\A\\s+");
									if(removeWhiteSpaceScanner.hasNext()) {
										potentialStatement = removeWhiteSpaceScanner.next();
									}
									//XXX: SMT: don't know where I need to put this gathering of preExecute, but I'll put it here 
									//since I can grab the stuff before the connection or stmt
									//XXX: SMT: assume that we don't have the rID for the connection/statement more than once in the execute line
									preSQLIV = "";
									preSQLIV = sourceVector.get(executeLineNumber).substring(0, sourceVector.get(executeLineNumber).indexOf(potentialStatement));
									//System.out.println("this string has no leading/trailing whitespace?" + potentialStatement);
									//SMT: END SCANNER
									if(newTestDeclaration("Statement", potentialStatement, executeLineNumber)) {
										//System.out.println("testDeclaration came back true!");
										foundStatement = true;
										foundExecute = true;
										statementInit = declarationLineNumber;
										//SMT: new code to find the assigment!
										if(tokenVector.get(i - 3).equals("=")) {
											if(i - 4 >= 0) {
												//have potential assignment
//												potentialAssignment = tokenVector.get(i - 4);
//												//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
//												//System.out.println("this string has leading/trailing whitespace?" + potentialAssignment);
//												removeWhiteSpaceScanner = new Scanner(potentialAssignment).useDelimiter("\\A\\s+");
//												if(removeWhiteSpaceScanner.hasNext()) {
//													potentialAssignment = removeWhiteSpaceScanner.next();
//												}
//												removeWhiteSpaceScanner = new Scanner(potentialAssignment).useDelimiter("\\s+\\z");
//												if(removeWhiteSpaceScanner.hasNext()) {
//													potentialAssignment = removeWhiteSpaceScanner.next();
//												}
												//System.out.println("this string has no leading/trailing whitespace?" + potentialAssignment);
												//SMT: END SCANNER
												//System.out.println("I have a potential assignment: " + potentialAssignment);
												//SMT: assume no variable assignements to start with, will remove this assumption if needed later
												}
											else {
												//equals without an assigment, something is going on here that I'm going to ignore for the moment
												System.out.println("equals without an assignment");
											}
										}
										else {
											System.out.println("no assignment detected");
										}
										//SMT: assignment finding code end
									}
									else {
										//can't find declaration...what to do now?
										System.out.println("can't find the declaration");
									}
								}
							}
							//if there is no i - 3, then I don't need to worry about '=' finding
							else {
								//need to check for statement
								//XXX: SMT: refactoring to find the first instance of whitespace and then taking only from the
								//end of the char[] to that white space to be the stmt.  DUP THIS FOR CONNECTIONS
								//XXX: SMT: begin char chopping
								char[] tmpStmtCharArray = potentialStatement.toCharArray();
								boolean notWhiteSpace = true; 
								int whiteSpaceIndex = -1;
								for(int j = tmpStmtCharArray.length - 1; notWhiteSpace && j >= 0; j--) {
									if(Character.isWhitespace(tmpStmtCharArray[j])) {
										whiteSpaceIndex = j;
										notWhiteSpace = false;
									}
								}
								if(!notWhiteSpace) {
									potentialStatement = potentialStatement.substring(whiteSpaceIndex, potentialStatement.length());
									/*char[] newPotentialStatementCharArray = new char[(tmpStmtCharArray.length - whiteSpaceIndex)];
									for(int j = 0; (j + whiteSpaceIndex) < tmpStmtCharArray.length; j++) {
										newPotentialStatementCharArray[j] = tmpStmtCharArray[j + whiteSpaceIndex];
									}
									potentialStatement = new String(newPotentialStatementCharArray);*/
								}
								System.out.println("2_+_+_+_+_+_+_+_+_+_+_+_+_+_+the potentialStatement is now: " + potentialStatement);
								//XXX: SMT: end char chopping
								//XXX: SMT: CAN REMOVE SCANNER, FIRST MAKE IT SO THAT WE JUST SURROUND THE EXECUTE CALL WITH THE PRE AND 
								//POST FOR THE STATEMENT INSTEAD OF ASSIGNING IT TO THE RETURN VAR
								
								//SMT: BEGIN PRE ONLY SCANNER (WHITESPACE REMOVER)
								//System.out.println("this string has leading/trailing whitespace?" + potentialStatement);
								Scanner removeWhiteSpaceScanner = new Scanner(potentialStatement).useDelimiter("\\A\\s+");
								if(removeWhiteSpaceScanner.hasNext()) {
									potentialStatement = removeWhiteSpaceScanner.next();
								}
								//XXX: SMT: don't know where I need to put this gathering of preExecute, but I'll put it here 
								//since I can grab the stuff before the connection or stmt
								//XXX: SMT: assume that we don't have the rID for the connection/statement more than once in the execute line
								preSQLIV = "";
								preSQLIV = sourceVector.get(executeLineNumber).substring(0, sourceVector.get(executeLineNumber).indexOf(potentialStatement));
								//System.out.println("this string has no leading/trailing whitespace?" + potentialStatement);
								//SMT: END SCANNER
								if(newTestDeclaration("Statement", potentialStatement, executeLineNumber)) {
									//System.out.println("testDeclaration came back true!");
									foundStatement = true;
									foundExecute = true;
									statementInit = declarationLineNumber;
								}
								else {
									//can't find declaration...what to do now?
									System.out.println("can't find the declaration");
								}
							}
						}
					}
				}
			}

	
		
		if(!foundExecute || !(foundConnection || foundStatement)) {
			//something happened and we don't have the parts we assume to have at this step
			System.out.println("we don't have the parts that we assume to have");
			return;
		}
		String endOfExecute;
		//pure execute will get the first (, which is fine, but the last ) isn't guarenteed, need to go through
		//from start to end and get the index of the first ) that doesn't have a matching (
		String pureExecuteString = potentialExecute.substring(potentialExecute.indexOf('(') + 1, potentialExecute.length());
		int parenCount = 0;
		char[] tmpExecuteCharArray = pureExecuteString.toCharArray();
		int endingParenIndex = -1;
		for(int i = 0; i < tmpExecuteCharArray.length && endingParenIndex == -1; i++) {
			if(tmpExecuteCharArray[i] == '(') {
				parenCount++;
			}
			if(tmpExecuteCharArray[i] == ')') {
				if(parenCount == 0) {
					endingParenIndex = i;
				}
				else {
					parenCount--;
				}
			}
		}
		endOfExecute = pureExecuteString.substring(endingParenIndex);
		pureExecuteString = pureExecuteString.substring(0, endingParenIndex);
		System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{preSQLIV is: " + preSQLIV + " and pureExecuteString is now: " + pureExecuteString + " and endOfExecute is: " + endOfExecute);
		
		String executeOnly = potentialExecute.substring(0, potentialExecute.indexOf('('));
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%the executeOnly is: " + executeOnly);
		
		boolean ifDirectlyAbove = determineConditionalLine(sourceVector.get(executeLineNumber - 1));
		if(ifDirectlyAbove) {
			//blocking up the execute b/c of the possibility that we will be in an if that is only expecting the execute, making
			//our execute be treated as one line would, which keeps equivalency with the preconverted code
			sourceVector.insertElementAt("{", executeLineNumber);
			moveLineReferencesIncrement(executeLineNumber);
		}
		
		seperateStrings("queryUniqueID", pureExecuteString, executeLineNumber, executeLineNumber);
		//adding in the traversal method right at the top once
		if(repetitiveAlg == 0) {
			sourceVector.insertElementAt("private static java.util.Vector traverseInputTree(java.util.Vector vectorTree, java.util.Vector returnVector){", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("for(int i = 0; i < vectorTree.size(); i++){", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("if (vectorTree.get(i) instanceof java.util.Vector) {", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("java.util.Vector nextVectorNode = (java.util.Vector) vectorTree.get(i);", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("traverseInputTree(nextVectorNode, returnVector);", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("}", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("else {", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("returnVector.add(vectorTree.get(i));", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("}", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("}", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("return returnVector;", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
			sourceVector.insertElementAt("}", firstMethodLineNumber);
			moveLineReferencesIncrement(firstMethodLineNumber);
		}
		
		//utilize the traverseInputTree and then use the SQL setter
		//naming issue?
		sourceVector.insertElementAt("java.util.Vector returnVector" + repetitiveAlg + " = new java.util.Vector();", executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		//first one is always 0
		sourceVector.insertElementAt("traverseInputTree(PSInput0" + repetitiveAlg + ", returnVector" + repetitiveAlg + ");", executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		//have to create the ps first
		
		if(foundStatement) {
			sourceVector.insertElementAt("java.sql.PreparedStatement ps" + repetitiveAlg + " = " + potentialStatement + ".getConnection().prepareStatement(PSqueryUniqueID" + repetitiveAlg + ");", executeLineNumber);
			moveLineReferencesIncrement(executeLineNumber);
		}
		else {
			sourceVector.insertElementAt("java.sql.PreparedStatement ps" + repetitiveAlg + " = " + potentialConnection + ".prepareStatement(PSqueryUniqueID" + repetitiveAlg + ");", executeLineNumber);
			moveLineReferencesIncrement(executeLineNumber);
		}
		
		
		//add sqlsetter
		sourceVector.insertElementAt("for(int i = 0; i < returnVector" + repetitiveAlg + ".size(); i++){", executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		sourceVector.insertElementAt("ps" + repetitiveAlg + ".setObject((i + 1), returnVector" + repetitiveAlg + ".get(i));", executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		sourceVector.insertElementAt("}", executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		
		//set the result if there is a potentialAssignment and comment out the vulnerable execute statement 
//		String newAssign = "";
//		if(potentialAssignment != null) {
//			newAssign = potentialAssignment + " = ";
//		}
		sourceVector.insertElementAt(preSQLIV + "ps" + repetitiveAlg + "." + executeOnly + "(" + endOfExecute, executeLineNumber);
		moveLineReferencesIncrement(executeLineNumber);
		
		if(ifDirectlyAbove) {
			//blocking up the execute b/c of the possibility that we will be in an if that is only expecting the execute, making
			//our execute be treated as one line would, which keeps equivalency with the preconverted code
			sourceVector.insertElementAt("}", executeLineNumber);
			moveLineReferencesIncrement(executeLineNumber);
		}
		
		//final set in algorithm
		sourceVector.set(executeLineNumber, " ");
		
		} catch (Exception e) {
			System.out.println("exception " + e);
			e.printStackTrace();
		}
	}
	
	private static void seperateStrings(String stringRID, String mixedString, int lineNumberAssign, int lineNumberDeclare) {
		int localPSInputTrailingNumber = PSInputTrailingNumber;
		int rIDSingleQuoteBalance = 0;
		boolean wrapAssign = false;
		wrapAssign = (determineConditionalLine(sourceVector.get(lineNumberAssign - 1)) && lineNumberAssign != executeLineNumber);
		if(!repeatedVarRunthrough) {
			String preInstanceVarModifiers = "";
			if(lineNumberDeclare < firstMethodLineNumber) {
				Scanner findPreModifiersScanner = new Scanner(sourceVector.get(lineNumberDeclare)).useDelimiter("String" + "\\s*" + stringRID + "\\s*");
				if(findPreModifiersScanner.hasNext()) {
					preInstanceVarModifiers = findPreModifiersScanner.next();
				}
				Scanner removeWhiteSpaceScanner = new Scanner(preInstanceVarModifiers).useDelimiter("\\A\\s+");
				if(removeWhiteSpaceScanner.hasNext()) {
					preInstanceVarModifiers = removeWhiteSpaceScanner.next();
				}
				removeWhiteSpaceScanner = new Scanner(preInstanceVarModifiers).useDelimiter("\\s+\\z");
				if(removeWhiteSpaceScanner.hasNext()) {
					preInstanceVarModifiers = removeWhiteSpaceScanner.next();
				}

			}
			String PSStringRIDDecl = preInstanceVarModifiers + " String PS" + stringRID + repetitiveAlg + ";";
			sourceVector.add(lineNumberDeclare, PSStringRIDDecl);
			moveLineReferencesIncrement(lineNumberDeclare);
			lineNumberAssign++;
			lineNumberDeclare++;
			String PSInputVectorDecl = preInstanceVarModifiers + " java.util.Vector PSInput" + localPSInputTrailingNumber + repetitiveAlg + " = new java.util.Vector();";
			sourceVector.add(lineNumberDeclare, PSInputVectorDecl);
			moveLineReferencesIncrement(lineNumberDeclare);
			lineNumberAssign++;
			lineNumberDeclare++;
		}
		sourceVector.add(lineNumberAssign, "{");
		moveLineReferencesIncrement(lineNumberAssign);
		if(lineNumberAssign <= lineNumberDeclare) {
			lineNumberDeclare++;
		}
		lineNumberAssign++;
		String PSInputVectorReinit = "PSInput" + localPSInputTrailingNumber + repetitiveAlg + " = new java.util.Vector();";
		sourceVector.add(lineNumberAssign, PSInputVectorReinit);
		moveLineReferencesIncrement(lineNumberAssign);
		if(lineNumberAssign <= lineNumberDeclare) {
			lineNumberDeclare++;
		}
		lineNumberAssign++;
		
		Vector<String> queryStringVector = newDetermineMixedString(mixedString);
		System.out.println("(^*^*^*^*^*^*^*^^*^*^*^*^*^*^*^*^*^*^the new determine vector is: " + queryStringVector.toString());
		for(int i = 0; i < queryStringVector.size(); i++) {
			if(queryStringVector.get(i).startsWith("\"")) {
				System.out.println("this is a string: " + queryStringVector.get(i));
			}
			//skipping all guarenteedSecure rIDs
			else if(!guarenteedSecure.containsKey(queryStringVector.get(i))) {
				System.out.println("this is a refID: " + queryStringVector.get(i));
				//determine single quote balance
				if(singleQuoteCount(queryStringVector.get(i).length() - 1, queryStringVector.get(i)) % 2 > 0) {
					rIDSingleQuoteBalance = (rIDSingleQuoteBalance + 1) % 2; 
				}
				//check hashMap of guarenteedSecure rIDs
				String concatRefID = null;
				if(queryStringVector.get(i).endsWith(")")) {
					if(queryStringVector.get(i).indexOf('.') != -1 && queryStringVector.get(i).indexOf('.') < queryStringVector.get(i).indexOf('(') ) {
						concatRefID = queryStringVector.get(i).substring(0, (queryStringVector.get(i).indexOf('.')));
						System.out.println("the concatrefID is: " + concatRefID);
					}
				}
				else {
					concatRefID = queryStringVector.get(i);
					System.out.println("the full refID is: " + concatRefID);
				}
				boolean foundStringDecl = newTestDeclaration("String", concatRefID, lineNumberAssign);
				int rIDdeclLineNumber = declarationLineNumber;
				//if it's an instance var and assign = -1, then check from end of file to executeLine
				int rIDAssignmentLineNumber = findLatestSetting(concatRefID, lineNumberAssign, rIDdeclLineNumber);
				
				
				System.out.println("*&%*&^%*&^%*&^%*&^%*&^%*&^%*&^%*&^%*&^%*&^%*&^%I found RID Assignment on line: " + rIDAssignmentLineNumber);
				if(rIDStrings.containsKey(concatRefID)) {
					System.out.println("+++++++++++++++++++++++++++++++FOUND USED RID!!!!!!!!!!!!!!!!!!!: " + concatRefID + " at " + rIDStrings.get(concatRefID)[0] + " and the PSIxxx is: " + rIDStrings.get(concatRefID)[1]);
					String PSRIDVectorAdd = "PSInput" + localPSInputTrailingNumber + repetitiveAlg + ".add(" + "PSInput" + rIDStrings.get(concatRefID)[1] + repetitiveAlg + ");";
					sourceVector.insertElementAt(PSRIDVectorAdd, lineNumberAssign);
					moveLineReferencesIncrement(lineNumberAssign);
					if(lineNumberAssign <= lineNumberDeclare) {
						lineNumberDeclare++;
					}
					lineNumberAssign++;
					int beginningRefID = rIDStrings.get(concatRefID)[0];
					int tmpi = 0;
					do {
						beginningRefID = (mixedString.indexOf(concatRefID, beginningRefID + tmpi));
						tmpi = 1;
					} while (doubleQuoteCount(beginningRefID, mixedString) % 2 != 0);
					if(singleQuoteCount(beginningRefID, mixedString) + rIDSingleQuoteBalance % 2 != 0) {
						char[] tmpRemoval = mixedString.toCharArray();
						int removingStartingSingleQuote = mixedString.lastIndexOf('\'', beginningRefID);
						tmpRemoval[removingStartingSingleQuote] = ' ';
						if(removingStartingSingleQuote > 0) {
							if(tmpRemoval[removingStartingSingleQuote - 1] == '\\') {
								tmpRemoval[removingStartingSingleQuote - 1] = ' ';
							}
						}
						int removingEndingSingleQuote = mixedString.indexOf('\'', beginningRefID + queryStringVector.get(i).length());
						tmpRemoval[removingEndingSingleQuote] = ' ';
						if(tmpRemoval[removingEndingSingleQuote - 1] == '\\') {
							tmpRemoval[removingEndingSingleQuote - 1] = ' ';
						}
						mixedString = new String(tmpRemoval);
					}
					mixedString = mixedString.substring(0, (beginningRefID - 1)) + "PS" + mixedString.substring(/*mixedString.indexOf(concatRefID, rIDStrings.get(concatRefID)[0])*/beginningRefID, /*mixedString.indexOf(concatRefID, rIDStrings.get(concatRefID)[0])*/beginningRefID + concatRefID.length()) + repetitiveAlg + mixedString.substring(/*mixedString.indexOf(concatRefID, rIDStrings.get(concatRefID)[0])*/beginningRefID + concatRefID.length() + 1, (mixedString.length()));
				}
				else {
					System.out.println("+++++++++++++++++++++++++++++++UNUSED RID");
					if(rIDAssignmentLineNumber != -1) {
						if(foundStringDecl) {
							PSInputTrailingNumber++;
							recursiveLineChange.add(lineNumberAssign);
							recursiveLineChange.add(lineNumberDeclare);
							do {
								recursiveLineChange.add(rIDdeclLineNumber);
								recursiveLineChange.add(rIDAssignmentLineNumber);
								System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ going through the conditional recursion");
								if(repeatedVarRunthrough) {
									//the String will be assigned somewhere before the previous rIDAssign
									System.out.println("repeat finding for rid: " + concatRefID + " for ridassign: " + rIDAssignmentLineNumber + " and riddecl: " + rIDdeclLineNumber);
									rIDAssignmentLineNumber = findLatestSetting(concatRefID, rIDAssignmentLineNumber, rIDdeclLineNumber);
									recursiveLineChange.remove(recursiveLineChange.size() - 1);
									recursiveLineChange.add(rIDAssignmentLineNumber);
								}
								if(rIDAssignmentLineNumber != -1) {
									String assignedValue = sourceVector.get(rIDAssignmentLineNumber).substring(sourceVector.get(rIDAssignmentLineNumber).indexOf('=') + 1, sourceVector.get(rIDAssignmentLineNumber).length());
									if(assignedValue.endsWith(";")){
										System.out.println("the assignedValue ends with a semi colon that is removed: ");
										assignedValue = assignedValue.substring(0, assignedValue.lastIndexOf(';'));
									}
									System.out.println("the assignedValue is: " + assignedValue);
									System.out.println("909090909090909090909090909090909the rIDAssignmentLineNumber is: " + rIDAssignmentLineNumber + " while the rIDdecl is: " + rIDdeclLineNumber);
									System.out.println("909090909090909090909090909090909the concatRefID is: " + concatRefID + " while the assignedValue is: " + assignedValue);
									seperateStrings(concatRefID, assignedValue, rIDAssignmentLineNumber, rIDdeclLineNumber);
									repeatedVarRunthrough = true;
								}
								 rIDAssignmentLineNumber = recursiveLineChange.remove(recursiveLineChange.size() - 1);
								 rIDdeclLineNumber = recursiveLineChange.remove(recursiveLineChange.size() - 1);
							} while (rIDAssignmentLineNumber != -1);
							repeatedVarRunthrough = false;
							lineNumberDeclare = recursiveLineChange.remove(recursiveLineChange.size() - 1);
							lineNumberAssign = recursiveLineChange.remove(recursiveLineChange.size() - 1);
							String PSRIDVectorAdd = "PSInput" + localPSInputTrailingNumber + repetitiveAlg + ".add(" + "PSInput" + PSInputTrailingNumber + repetitiveAlg + ");";
							sourceVector.insertElementAt(PSRIDVectorAdd, lineNumberAssign);
							System.out.println("adding line: " + PSRIDVectorAdd + " at line 521 for line: " + lineNumberAssign);
							moveLineReferencesIncrement(lineNumberAssign);
							if(lineNumberAssign <= lineNumberDeclare) {
								lineNumberDeclare++;
							}
							lineNumberAssign++;
							int beginningRefID = 0;
							int tmpi = 0;
							do {
								beginningRefID = (mixedString.indexOf(concatRefID, beginningRefID + tmpi));
								tmpi = 1;
							} while (doubleQuoteCount(beginningRefID, mixedString) % 2 != 0);
							if(singleQuoteCount(beginningRefID, mixedString) + rIDSingleQuoteBalance % 2 != 0) {
								char[] tmpRemoval = mixedString.toCharArray();
								int removingStartingSingleQuote = mixedString.lastIndexOf('\'', beginningRefID);
								tmpRemoval[removingStartingSingleQuote] = ' ';
								if(removingStartingSingleQuote > 0) {
									if(tmpRemoval[removingStartingSingleQuote - 1] == '\\') {
										tmpRemoval[removingStartingSingleQuote - 1] = ' ';
									}
								}
								int removingEndingSingleQuote = mixedString.indexOf('\'', beginningRefID + queryStringVector.get(i).length());
								tmpRemoval[removingEndingSingleQuote] = ' ';
								if(tmpRemoval[removingEndingSingleQuote - 1] == '\\') {
									tmpRemoval[removingEndingSingleQuote - 1] = ' ';
								}
								mixedString = new String(tmpRemoval);
							}
							//assumes there is stuff before and after, throws index errors when there isn't
							int endOfPreSubstring;
							if(beginningRefID > 0) {
								endOfPreSubstring = beginningRefID - 1;
							}
							else {
								endOfPreSubstring = 0;
							}
							int beginOfPostSubstring;
							if(beginningRefID + concatRefID.length() + 1 > mixedString.length()) {
								beginOfPostSubstring = mixedString.length();
							}
							else {
								beginOfPostSubstring = beginningRefID + concatRefID.length() + 1;
							}
							mixedString = mixedString.substring(0, endOfPreSubstring) + "PS" + mixedString.substring(/*mixedString.indexOf(concatRefID)*/beginningRefID, /*mixedString.indexOf(concatRefID)*/beginningRefID + concatRefID.length()) + repetitiveAlg + mixedString.substring(/*mixedString.indexOf(concatRefID)*/beginOfPostSubstring, (mixedString.length()));
							int[] tmpIntArray = {mixedString.indexOf(concatRefID) + concatRefID.length(), PSInputTrailingNumber};
							rIDStrings.put(concatRefID, tmpIntArray);
						}
						else {
							String tmpQueryStringVectorString = queryStringVector.get(i);
							if(tmpQueryStringVectorString.endsWith(";")){
								tmpQueryStringVectorString = tmpQueryStringVectorString.substring(0, tmpQueryStringVectorString.lastIndexOf(';'));
							}
							String PSRIDVectorAdd = "PSInput" + localPSInputTrailingNumber + repetitiveAlg + ".add(" + tmpQueryStringVectorString + ");";
							sourceVector.insertElementAt(PSRIDVectorAdd, lineNumberAssign);
							System.out.println("adding line: " + PSRIDVectorAdd + " at line 542 for line: " + lineNumberAssign);
							moveLineReferencesIncrement(lineNumberAssign);
							if(lineNumberAssign <= lineNumberDeclare) {
								lineNumberDeclare++;
							}
							lineNumberAssign++;
							int beginningRefID = 0;
							int tmpi = 0;
							do {
								beginningRefID = (mixedString.indexOf(queryStringVector.get(i), beginningRefID + tmpi));
								tmpi = 1;
							} while (doubleQuoteCount(beginningRefID, mixedString) % 2 != 0);
							if(singleQuoteCount(beginningRefID, mixedString) + rIDSingleQuoteBalance % 2 != 0) {
								char[] tmpRemoval = mixedString.toCharArray();
								int removingStartingSingleQuote = mixedString.lastIndexOf('\'', beginningRefID);
								tmpRemoval[removingStartingSingleQuote] = ' ';
								if(removingStartingSingleQuote > 0) {
									if(tmpRemoval[removingStartingSingleQuote - 1] == '\\') {
										tmpRemoval[removingStartingSingleQuote - 1] = ' ';
									}
								}
								int removingEndingSingleQuote = mixedString.indexOf('\'', beginningRefID + queryStringVector.get(i).length());
								tmpRemoval[removingEndingSingleQuote] = ' ';
								if(tmpRemoval[removingEndingSingleQuote - 1] == '\\') {
									tmpRemoval[removingEndingSingleQuote - 1] = ' ';
								}
								mixedString = new String(tmpRemoval);
							}
							int endOfPreSubstring;
							if(beginningRefID > 0) {
								endOfPreSubstring = beginningRefID - 1;
							}
							else {
								endOfPreSubstring = 0;
							}
							mixedString = mixedString.substring(0, endOfPreSubstring) + "\"?\"" + mixedString.substring(beginningRefID + queryStringVector.get(i).length(), (mixedString.length()));
						}
					}
					else {
						System.out.println("couldn't find the RID");
						String tmpQueryStringVectorString = queryStringVector.get(i);
						if(tmpQueryStringVectorString.endsWith(";")){
							tmpQueryStringVectorString = tmpQueryStringVectorString.substring(0, tmpQueryStringVectorString.lastIndexOf(';'));
						}
						String PSRIDVectorAdd = "PSInput" + localPSInputTrailingNumber + repetitiveAlg + ".add(" + tmpQueryStringVectorString + ");";
						sourceVector.insertElementAt(PSRIDVectorAdd, lineNumberAssign);
						moveLineReferencesIncrement(lineNumberAssign);
						if(lineNumberAssign <= lineNumberDeclare) {
							lineNumberDeclare++;
						}
						lineNumberAssign++;
						int beginningRefID = 0;
						int tmpi = 0;
						do {
							beginningRefID = (mixedString.indexOf(queryStringVector.get(i), beginningRefID + tmpi));
							tmpi = 1;
						} while (doubleQuoteCount(beginningRefID, mixedString) % 2 != 0);
						if(singleQuoteCount(beginningRefID, mixedString) + rIDSingleQuoteBalance % 2 != 0) {
							char[] tmpRemoval = mixedString.toCharArray();
							int removingStartingSingleQuote = mixedString.lastIndexOf('\'', beginningRefID);
							tmpRemoval[removingStartingSingleQuote] = ' ';
							if(removingStartingSingleQuote > 0) {
								if(tmpRemoval[removingStartingSingleQuote - 1] == '\\') {
									tmpRemoval[removingStartingSingleQuote - 1] = ' ';
								}
							}
							int removingEndingSingleQuote = mixedString.indexOf('\'', beginningRefID + queryStringVector.get(i).length());
							tmpRemoval[removingEndingSingleQuote] = ' ';
							if(tmpRemoval[removingEndingSingleQuote - 1] == '\\') {
								tmpRemoval[removingEndingSingleQuote - 1] = ' ';
							}
							mixedString = new String(tmpRemoval);
						}
						int endOfPreSubstring;
						if(beginningRefID > 0) {
							endOfPreSubstring = beginningRefID - 1;
						}
						else {
							endOfPreSubstring = 0;
						}
						mixedString = mixedString.substring(0, endOfPreSubstring) + "\"?\"" + mixedString.substring(beginningRefID + queryStringVector.get(i).length(), (mixedString.length()));
					}
				}
			}
		}
		String PSStringRIDAssign = "PS" + stringRID + repetitiveAlg + " = " + mixedString + ";";
		sourceVector.add(lineNumberAssign, PSStringRIDAssign);
		moveLineReferencesIncrement(lineNumberAssign);
		if(lineNumberAssign <= lineNumberDeclare) {
			lineNumberDeclare++;
		}
		lineNumberAssign++;
		//include the assign line in the block if we're in a conditional as well
		if(wrapAssign) {
			System.out.println("88888888888888888888888888888888888888888888888888888888wrapping the assign!");
			sourceVector.set(lineNumberAssign, sourceVector.get(lineNumberAssign) + "}");
		}
		else {
			sourceVector.add(lineNumberAssign, "}");
			moveLineReferencesIncrement(lineNumberAssign);
			if(lineNumberAssign <= lineNumberDeclare) {
				lineNumberDeclare++;
			}
			lineNumberAssign++;
		}
	}
	
	
/*	private static int findLatestSetting(String rID, int startingLineNumber, int declLineNumber) {
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^START FINDLATESTSETTING");
		System.out.println("the rID is: " + rID + "and the startingLineNumber is: " + startingLineNumber);
		Pattern myRIDAssignmentPattern = Pattern.compile(rID + "\\s*=\\s*\\w+");
		int rIDAssignmentLineNumber = -1;
		boolean foundRIDAssignment = false;
		//START WITH END OF FILE TO BELOW EXECUTE LINE, THEN DO FROM STARTINGLINENUMBER TO DECL LINE
			for(int j = startingLineNumber - 1; j >= declLineNumber && !foundRIDAssignment; j--) {
				//System.out.println("the line under analysis is: " + sourceVector.get(j));
				Matcher myMatcher = myRIDAssignmentPattern.matcher(sourceVector.get(j));
				foundRIDAssignment = myMatcher.find();
				if(foundRIDAssignment) {
					rIDAssignmentLineNumber = j;
				}
			}
		if(rIDAssignmentLineNumber != -1) {
			System.out.println("111111111111111111111111111111111111111111111111111111found setting : " + sourceVector.get(rIDAssignmentLineNumber) + "on line " + rIDAssignmentLineNumber + "when the execute line number is: " + executeLineNumber);
		}
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^END FINDLATESTSETTING");
		return rIDAssignmentLineNumber;
	}*/
	
	private static int findLatestSetting(String rID, int startingLineNumber, int declLineNumber) {
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^START FINDLATESTSETTING");
		System.out.println("the rID is: " + rID + "and the startingLineNumber is: " + startingLineNumber + " and the declLineNUmber is: " + declLineNumber);
		Pattern myRIDAssignmentPattern = Pattern.compile(rID + "\\s*=\\s*\\S+");
		int rIDAssignmentLineNumber = -1;
		boolean foundRIDAssignment = false;
		//START WITH END OF FILE TO BELOW EXECUTE LINE, THEN DO FROM STARTINGLINENUMBER TO DECL LINE
		if(startingLineNumber <= executeLineNumber) {
			//always going through this code first, only passing greater than executeLineNumber after this
			for(int j = startingLineNumber - 1; j >= 0 && j >= declLineNumber && !foundRIDAssignment; j--) {
				System.out.println("the j is: " + j + " and the size is: " + sourceVector.size());
				System.out.println("the line under analysis is: " + sourceVector.get(j));
				Matcher myMatcher = myRIDAssignmentPattern.matcher(sourceVector.get(j));
				foundRIDAssignment = myMatcher.find();
				if(foundRIDAssignment) {
					System.out.println("foundRIDAssignment!!!");
					//XXX: SMT: ONLY DO THIS WHEN YOU REALLY HAVE AN INSTANCE VAR!
					if(j != declLineNumber && declLineNumber < firstMethodLineNumber) {
						//check to make sure that this is not a declare as well as a setting, which indicates
						//a local var is named the same as our var
						System.out.println("got into the instance var area!");
						//XXX: SMT: I don't get the logic here, is this keeping us from picking up the setting
						//at the declaration line?  Why would we do that?
						Pattern myRIDDeclarePattern = Pattern.compile("String\\s*" + rID + "\\s*");
						myMatcher = myRIDDeclarePattern.matcher(sourceVector.get(j));
						//if it matches, we didn't find RIDAssignment
						foundRIDAssignment = !myMatcher.find();
//						foundRIDAssignment = true;
					}
					if(foundRIDAssignment) {
						rIDAssignmentLineNumber = j;
					}
				}
			}
			if(!foundRIDAssignment && declLineNumber < firstMethodLineNumber) {
				startingLineNumber = sourceVector.size();
			}
		}
		if(startingLineNumber > executeLineNumber) {
			for(int j = startingLineNumber - 1; j > executeLineNumber && !foundRIDAssignment; j--) {
				//System.out.println("the POST!!!!!!!!! line under analysis is: " + sourceVector.get(j));
				Matcher myMatcher = myRIDAssignmentPattern.matcher(sourceVector.get(j));
				foundRIDAssignment = myMatcher.find();
				if(foundRIDAssignment) {
					if(j != declLineNumber) {
						//check to make sure that this is not a declare as well as a setting, which indicates
						//a local var is named the same as our var
						Pattern myRIDDeclarePattern = Pattern.compile("String\\s*" + rID + "\\s*");
						myMatcher = myRIDDeclarePattern.matcher(sourceVector.get(j));
						//if it matches, we didn't find RIDAssignment
						foundRIDAssignment = !myMatcher.find();
					}
					if(foundRIDAssignment) {
						rIDAssignmentLineNumber = j;
					}
				}
			}
		}
		if(rIDAssignmentLineNumber != -1) {
			System.out.println("111111111111111111111111111111111111111111111111111111found setting : " + sourceVector.get(rIDAssignmentLineNumber) + "on line " + rIDAssignmentLineNumber + "when the execute line number is: " + executeLineNumber);
		}
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^END FINDLATESTSETTING");
		return rIDAssignmentLineNumber;
	}
	
	private static int doubleQuoteCount(int index, String reviewLine) {
		int quoteCount = 0;
		boolean found = true;
		for(int i = -1; found;) {
			i = reviewLine.indexOf('"', i + 1);
			//System.out.println("i is: " + i);
			if(i > -1 && i < index){
				quoteCount++;
			}
			else {
				found = false;
			}
		}
		return quoteCount;
	}

	private static int LeftParenthesisCount(int index, String reviewLine) {
		int parenthesisCount = 0;
		boolean found = true;
		for(int i = -1; found;) {
			i = reviewLine.indexOf('(', i + 1);
			//System.out.println("i is: " + i);
			if(i > -1 && i < index){
				parenthesisCount++;
			}
			else {
				found = false;
			}
		}
		return parenthesisCount;
	}
	
	private static int RightParenthesisCount(int index, String reviewLine) {
		int parenthesisCount = 0;
		boolean found = true;
		for(int i = -1; found;) {
			i = reviewLine.indexOf('(', i + 1);
			//System.out.println("i is: " + i);
			if(i > -1 && i < index){
				parenthesisCount++;
			}
			else {
				found = false;
			}
		}
		return parenthesisCount;
	}
	
	private static int singleQuoteCount(int index, String reviewLine) {
		int quoteCount = 0;
		boolean found = true;
		for(int i = -1; found;) {
			i = reviewLine.indexOf('\'', i + 1);
			//System.out.println("i is: " + i);
			if(i > -1 && i < index){
				quoteCount++;
			}
			else {
				found = false;
			}
		}
		return quoteCount;
	}
	
	private static Vector<String> newDetermineMixedString(String mixedString){
		//need a new way to determine.  
		//cleanse the mixedString to make sure that 
		//Need to look for a +, then determine if the () and "" are balanced before it
		//if ( are not balanced, then see if there is a \\w (word that can be used for a method call right before
			//if so, then ignore
		//if " are not balanced, then ignore
		//if all balanced, then separate off the first part from the second part as an element
		//continue until no more +'s, then put rest into vector as an element
		Vector<String> queryStringVector = new Vector<String>();
//		String tmpStringLiteral = "";
		boolean found = true;
		int plusIndexOffset = 0;
		for(;found;) {
			int i = -1;
			if(plusIndexOffset <= mixedString.length()) {
				i = mixedString.indexOf('+', plusIndexOffset);
			}
			//System.out.println("i is: " + i);
			if(i > -1){
				if(doubleQuoteCount(i, mixedString) % 2 > 0) {
					//ignore the +
					plusIndexOffset = i + 1;
				}
				else if (LeftParenthesisCount(i, mixedString) > RightParenthesisCount(i, mixedString)) {
					//determine if it's a method call or just wrapping paren.  Find wrapping (
					int parenCount = 0;
					char[] tmpExecuteCharArray = mixedString.toCharArray();
					int startingParenIndex = -1;
					for(int j = i; j >= 0 && startingParenIndex == -1; j--) {
						if(tmpExecuteCharArray[j] == ')') {
							parenCount++;
						}
						if(tmpExecuteCharArray[j] == '(') {
							if(parenCount == 0) {
								startingParenIndex = j;
							}
							else {
								parenCount--;
							}
						}
					}
					if(startingParenIndex > 0) {
						if(Character.isJavaIdentifierPart(mixedString.charAt(startingParenIndex - 1))) {
							//method call, ignore +
							plusIndexOffset = i + 1;
						}
						else {
							//wrapping paren, allow part to be made an element
							//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
							String tmpStringLiteral = mixedString.substring(0, i); 
							Scanner removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\A\\s+");
							if(removeWhiteSpaceScanner.hasNext()) {
								tmpStringLiteral = removeWhiteSpaceScanner.next();
							}
							removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\s+\\z");
							if(removeWhiteSpaceScanner.hasNext()) {
								tmpStringLiteral = removeWhiteSpaceScanner.next();
							}
							//SMT: END SCANNER
							queryStringVector.add(tmpStringLiteral);
							if(i <= mixedString.length() - 1) {
								mixedString = mixedString.substring(i + 1, mixedString.length());
								plusIndexOffset = 0;
							}
							else {
								mixedString = "";
								plusIndexOffset = 0;
							}
						}
					}
				}
				else {
					//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
					String tmpStringLiteral = mixedString.substring(0, i); 
					Scanner removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\A\\s+");
					if(removeWhiteSpaceScanner.hasNext()) {
						tmpStringLiteral = removeWhiteSpaceScanner.next();
					}
					removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\s+\\z");
					if(removeWhiteSpaceScanner.hasNext()) {
						tmpStringLiteral = removeWhiteSpaceScanner.next();
					}
					//SMT: END SCANNER
					queryStringVector.add(tmpStringLiteral);
					if(i <= mixedString.length() - 1) {
						mixedString = mixedString.substring(i + 1, mixedString.length());
						plusIndexOffset = 0;
					}
					else {
						mixedString = "";
						plusIndexOffset = 0;
					}
				}
			}
			else {
				found = false;
				if(mixedString.length() > 0) {
					//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
					Scanner removeWhiteSpaceScanner = new Scanner(mixedString).useDelimiter("\\A\\s+");
					if(removeWhiteSpaceScanner.hasNext()) {
						mixedString = removeWhiteSpaceScanner.next();
					}
					removeWhiteSpaceScanner = new Scanner(mixedString).useDelimiter("\\s+\\z");
					if(removeWhiteSpaceScanner.hasNext()) {
						mixedString = removeWhiteSpaceScanner.next();
					}
					//SMT: END SCANNER
					queryStringVector.add(mixedString);
				}
			}
		}
		return queryStringVector;
	}
	
	/*private static Vector<String> determineMixedString(String mixedString){
		//System.out.println("++++++++++++++++++++++++++++++++testing out determineMixedString with: " + mixedString);
		StringTokenizer executeTokenizer = new StringTokenizer(mixedString, "\"+()", true);
		Vector<String> queryStringVector = new Vector<String>();
		boolean haveOneQuote = false;
		boolean haveOnePlus = false;
		boolean haveBeginningParenthesis = false;
		String tmpStringLiteral = null;
		String tmpLongReferenceID = null;
		int parenthesisCount = 0;
		
		while(executeTokenizer.hasMoreTokens()) {
			String tmpToken = executeTokenizer.nextToken();
			if(haveOneQuote) {
				tmpStringLiteral += tmpToken;
				if(tmpToken.equals("\"")){
					//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
					Scanner removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\A\\s+");
					if(removeWhiteSpaceScanner.hasNext()) {
						tmpStringLiteral = removeWhiteSpaceScanner.next();
					}
					removeWhiteSpaceScanner = new Scanner(tmpStringLiteral).useDelimiter("\\s+\\z");
					if(removeWhiteSpaceScanner.hasNext()) {
						tmpStringLiteral = removeWhiteSpaceScanner.next();
					}
					//SMT: END SCANNER
					queryStringVector.add(tmpStringLiteral);
					tmpStringLiteral = null;
					haveOneQuote = false;
				}
			}
			else if(tmpToken.equals("\"") && (parenthesisCount == 0)) {
				tmpStringLiteral = tmpToken;
				haveOneQuote = true;
				//XXX: SMT: fix this later, 
				//find the "", should be able to find all in a string...I guess I"m going to assume that single quotes won't be used
				//for strings...but I think that I can't make that assumption...so I'll fix this later
				
			}
			else {
				if(parenthesisCount > 0) {
					//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~parenthesisCount is: " + parenthesisCount + " for token: " + tmpToken);
					tmpLongReferenceID += tmpToken;
					if(tmpToken.equals(")")) {
						parenthesisCount--;
						if(parenthesisCount == 0) {
							//can push the tmpLongReferenceID to the vector now
							queryStringVector.add(tmpLongReferenceID);
							tmpLongReferenceID = null;
						}
					}
					else if (tmpToken.equals("(")) {
						parenthesisCount++;
					} 
				}
				else if (tmpToken.equals("(")) {
					parenthesisCount++;
					tmpLongReferenceID = queryStringVector.remove(queryStringVector.size() - 1);
					tmpLongReferenceID += tmpToken;
				}
				else if (!tmpToken.equals("+")) {
					queryStringVector.add(tmpToken);
				}
			}
		}
		for(int i = 0; i < queryStringVector.size(); i++) {
			String tmpToken = queryStringVector.get(i);
			//remove tokens that are all white space
			boolean allWhiteSpace = true;
			char[] tmpCharArray = tmpToken.toCharArray();
			for(int j = 0; j < tmpCharArray.length && allWhiteSpace; j++) {
				if(!Character.isWhitespace(tmpCharArray[j])){
					allWhiteSpace = false;
				}
			}
			if(!allWhiteSpace) {
				//SMT: BEGIN SCANNER (WHITESPACE REMOVER)
				Scanner removeWhiteSpaceScanner = new Scanner(tmpToken).useDelimiter("\\A\\s+");
				if(removeWhiteSpaceScanner.hasNext()) {
					tmpToken = removeWhiteSpaceScanner.next();
				}
				removeWhiteSpaceScanner = new Scanner(tmpToken).useDelimiter("\\s+\\z");
				if(removeWhiteSpaceScanner.hasNext()) {
					tmpToken = removeWhiteSpaceScanner.next();
				}
				//SMT: END SCANNER
				if(tmpToken.equals(";")) {
					queryStringVector.remove(i);
					i--;
				}
				else {
					queryStringVector.set(i, tmpToken);
				}
			}
			else {
				queryStringVector.remove(i);
				i--;
			}
		}	
//		System.out.println("here is the queryVector: " + queryStringVector.toString());	
//		System.out.println("+++++++++++++++++++++this is the end of the determineMixedString");
		return queryStringVector;
	}*/
	
	
	/*private static boolean testDeclaration(String declaredType, String referenceID) {
			System.out.println("declaredType is: " + declaredType);
			System.out.println("referenceID is: " + referenceID);
			Pattern myPattern = Pattern.compile(".*" + declaredType + "\\s*" + referenceID + "\\s*");
			boolean foundDeclaration = false;
			for(int i = 0; i < executeLineNumber && !foundDeclaration; i++) {
				Matcher myMatcher = myPattern.matcher(sourceVector.get(i));
				foundDeclaration = myMatcher.find();
				if(foundDeclaration) {
					declarationLineNumber = i;
				}
			}
		return foundDeclaration;
	}*/
	
	//allow method var checking?
	//XXX: SMT: stopped here
	private static boolean newTestDeclaration(String declaredType, String referenceID, int startingLineNumber) {
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^START newTestDeclaration");
		System.out.println("the declaredType is: " + declaredType + "the rID is: " + referenceID + "and the startingLineNumber is: " + startingLineNumber);
		//this is too restrictive, won't allow for any modifiers such as public, static, or final...or will it?
		Pattern myRIDDeclarePattern = Pattern.compile(declaredType + "(\\s*\\w+,)*\\s*" + referenceID + "\\s*");
		boolean foundRIDDeclare = false;
		for(int j = startingLineNumber - 1; j > methodLineNumber && !foundRIDDeclare; j--) {
			//System.out.println("#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$Line under review: " + sourceVector.get(j));
			Matcher myMatcher = myRIDDeclarePattern.matcher(sourceVector.get(j));
			foundRIDDeclare = myMatcher.find();
			if(foundRIDDeclare) {
				declarationLineNumber = j;
			}
		}
		if(!foundRIDDeclare) {
			//try instance space
			for(int j = firstMethodLineNumber - 1; j > classLineNumber && !foundRIDDeclare; j--) {
				//System.out.println("#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$Instance Space Line under review: " + sourceVector.get(j));
				Matcher myMatcher = myRIDDeclarePattern.matcher(sourceVector.get(j));
				foundRIDDeclare = myMatcher.find();
				if(foundRIDDeclare) {
					declarationLineNumber = j;
				}
			}
		}
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^END newTestDeclaration, foundRIDDeclare is: " + foundRIDDeclare + " and the line number is: " + declarationLineNumber);
		return foundRIDDeclare;
	}
	
	
	
	private static void moveLineReferencesIncrement(int insertionLine) {
		if(insertionLine <= executeLineNumber && executeLineNumber != -1) {
			System.out.println("increasing executeLineNumber from: " + executeLineNumber);
			executeLineNumber++;
		}
		if(insertionLine <= methodLineNumber && methodLineNumber != -1) {
			//System.out.println("increasing methodLineNumber from: " + methodLineNumber);
			methodLineNumber++;
		}
		if(insertionLine <= classLineNumber && classLineNumber != -1) {
			//System.out.println("increasing classLineNumber from: " + classLineNumber);
			classLineNumber++;
		}
		if(insertionLine <= firstMethodLineNumber && firstMethodLineNumber != -1) {
			//System.out.println("increasing firstMethodLineNumber from: " + firstMethodLineNumber);
			firstMethodLineNumber++;
		}
		if(insertionLine <= connectionInit  && connectionInit != -1) {
			//System.out.println("increasing connectionInit from: " + connectionInit);
			connectionInit++;
		}
		if(insertionLine <= statementInit && statementInit != -1) {
			//System.out.println("increasing statementInit from: " + statementInit);
			statementInit++;
		}
		//assuming both conditionalStarts and conditionalEnds are the same size
		/*for(int i = 0; i < conditionalStarts.size(); i++) {
			if(insertionLine <= conditionalStarts.get(i)) {
				//System.out.println("increasing one of the conditionalStarts from: " + conditionalStarts.get(i));
				conditionalStarts.set(i, conditionalStarts.get(i) + 1);
			}
			if(insertionLine <= conditionalEnds.get(i)) {
				//System.out.println("increasing one of the conditionalEnds from: " + conditionalEnds.get(i));
				conditionalEnds.set(i, conditionalEnds.get(i) + 1);
			}
		}*/
		for(int i = 0; i < vulnerableLines.size(); i++) {
			if(insertionLine <= vulnerableLines.get(i)) {
				//System.out.println("increasing one of the vulnerableLines from: " + vulnerableLines.get(i));
				vulnerableLines.set(i, vulnerableLines.get(i) + 1);
			}
		}
		for(int i = 0; i < recursiveLineChange.size(); i++) {
			if(insertionLine <= recursiveLineChange.get(i)) {
				System.out.println("increasing one of the recursiveLineChange from: " + recursiveLineChange.get(i));
				recursiveLineChange.set(i, recursiveLineChange.get(i) + 1);
			}
		}
	}
	
	
	private static void determineClassAndMethodInfo() {
		Pattern myPatternPublic = Pattern.compile("\\A\\s*public\\s+\\w*\\s*\\w+\\s+\\w+(\\s*\\w*)");
		Pattern myPatternPrivate = Pattern.compile("\\A\\s*private\\s+\\w*\\s*\\w+\\s+\\w+(\\s*\\w*)");
		Pattern myPatternProtected = Pattern.compile("\\A\\s*protected\\s+\\w*\\s*\\w+\\s+\\w+(\\s*\\w*)");
		boolean foundMethod = false;
		for(int i = executeLineNumber; i >= 0 && !foundMethod; i--) {
			Matcher myMatcher = myPatternPrivate.matcher(sourceVector.get(i));
			foundMethod = myMatcher.find();
			//check to make sure that we aren't dealing with some odd var or instance var
			if(foundMethod) {
				//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
				//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
				foundMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
			}
			if(foundMethod) {
				methodLineNumber = i;
			}
			else {
				myMatcher = myPatternPublic.matcher(sourceVector.get(i));
				foundMethod = myMatcher.find();
				//check to make sure that we aren't dealing with some odd var or instance var
				if(foundMethod) {
					//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
					//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
					foundMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
				}
				if(foundMethod) {
					methodLineNumber = i;
				}
				else {
					myMatcher = myPatternProtected.matcher(sourceVector.get(i));
					foundMethod = myMatcher.find();
					//check to make sure that we aren't dealing with some odd var or instance var
					if(foundMethod) {
						//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
						//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
						foundMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
					}
					if(foundMethod) {
						methodLineNumber = i;
					}
				}
			}
		}
		System.out.println("I found the method on line: " + methodLineNumber);
		if(foundMethod) {
			System.out.println("the local method is: " + sourceVector.get(methodLineNumber));
		}
		else {
			//we are done if we can't find the method
			throw new NullPointerException("couldn't find the local method, so we are done");
		}
		
		//find the class
		
		Pattern myClassPatternPublic = Pattern.compile("\\A\\s*public\\s+class\\s+\\w+\\s*");
		Pattern myClassPatternProtected = Pattern.compile("\\A\\s*protected\\s+class\\s+\\w+\\s*");
		boolean foundClass = false;
		for(int i = executeLineNumber; i >= 0 && !foundClass; i--) {
			Matcher myMatcher = myClassPatternPublic.matcher(sourceVector.get(i));
			foundClass = myMatcher.find();
			if(foundClass) {
				classLineNumber = i;
			}
			else {
				myMatcher = myClassPatternProtected.matcher(sourceVector.get(i));
				foundClass = myMatcher.find();
				if(foundMethod) {
					classLineNumber = i;
				}
			}
		}
		System.out.println("I found the class on line: " + classLineNumber);
		if(foundClass) {
			System.out.println("the class is: " + sourceVector.get(classLineNumber));
		}
		else {
			//we are done if we can't find the class
			throw new NullPointerException("couldn't find the class, so we are done");
		}
		
		boolean foundFirstMethod = false;
		for(int i = classLineNumber + 1; i <= methodLineNumber && !foundFirstMethod; i++) {
			Matcher myMatcher = myPatternPrivate.matcher(sourceVector.get(i));
			foundFirstMethod = myMatcher.find();
			//check to make sure that we aren't dealing with some odd var or instance var
			if(foundFirstMethod) {
				//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
				//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
				foundFirstMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
			}
			if(foundFirstMethod) {
				firstMethodLineNumber = i;
			}
			else {
				myMatcher = myPatternPublic.matcher(sourceVector.get(i));
				foundFirstMethod = myMatcher.find();
				//check to make sure that we aren't dealing with some odd var or instance var
				if(foundFirstMethod) {
					//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
					//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
					foundFirstMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
				}
				if(foundFirstMethod) {
					firstMethodLineNumber = i;
				}
				else {
					myMatcher = myPatternProtected.matcher(sourceVector.get(i));
					foundFirstMethod = myMatcher.find();
					//check to make sure that we aren't dealing with some odd var or instance var
					if(foundFirstMethod) {
						//System.out.println("sourceVector.get(i).lastIndexOf(\";\"): " + sourceVector.get(i).lastIndexOf(";"));
						//System.out.println("sourceVector.get(i).lastIndexOf(\"{\"): " + sourceVector.get(i).lastIndexOf("{"));
						foundFirstMethod = sourceVector.get(i).lastIndexOf("{") != -1 && (sourceVector.get(i).lastIndexOf(";") < sourceVector.get(i).lastIndexOf("{") || sourceVector.get(i).lastIndexOf(";") == -1);
					}
					if(foundFirstMethod) {
						firstMethodLineNumber = i;
					}
				}
			}
		}
		System.out.println("I found the first method on line: " + firstMethodLineNumber);
		if(foundMethod) {
			System.out.println("the first method is: " + sourceVector.get(firstMethodLineNumber));
		}
	}

	//XXX: SMT: need to test this with better data
	//XXX: SMT: add case break to this
	private static boolean determineConditionalLine(String potentialConditional) {
		Pattern ifConditionalPattern = Pattern.compile("\\s*if\\s*\\(\\s*\\S+\\s*\\)");
		Pattern elseConditionalPattern = Pattern.compile("\\s*else\\s*\\(\\s*\\S+\\s*\\)");
		Pattern forConditionalPattern = Pattern.compile("\\s*for\\s*\\(\\s*\\S*\\s*;\\s*\\S*\\s*;\\s*\\S*\\s*\\)");
		Pattern whileConditionalPattern = Pattern.compile("\\s*while\\s*\\(\\s*\\S+\\s*\\)");
		
		
			Matcher myMatcher = ifConditionalPattern.matcher(potentialConditional);
			if(myMatcher.find()) {
				return true;
			}
			else {
				myMatcher = elseConditionalPattern.matcher(potentialConditional);
				if(myMatcher.find()) {
					return true;
				}
				else {
					myMatcher = forConditionalPattern.matcher(potentialConditional);
					if(myMatcher.find()) {
						return true;
					}
					else {
						myMatcher = whileConditionalPattern.matcher(potentialConditional);
						if(myMatcher.find()) {
							return true;
						}
					}
				}
			}
		return false;
	}
	
/*	private static boolean determineCrossConditionalAssign(int decl, int assign) {
		boolean declInside = false;
		boolean assignInside = false;
		System.out.println("9090909090909090909090909090909090909090909090in determineCrossConditionalAssign, decl is: " + decl + " and assign is: " + assign + "while the conditionals are: " + conditionalStarts + " and " + conditionalEnds);
		for(int i = 0; i < conditionalStarts.size() && !declInside && !assignInside; i++) {
			if(conditionalStarts.get(i) < decl && decl < conditionalEnds.get(i)) {
				declInside = true;
			}
			if(conditionalStarts.get(i) < assign && assign < conditionalEnds.get(i)) {
				assignInside = true;
			}
		}
		return !(declInside == assignInside);
	}*/
	
}
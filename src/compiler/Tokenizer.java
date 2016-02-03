package compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer
{
	public static final int 
						KEYWORD=1,
						SYMBOL=2,
						IDENTIFIER=3,
						INT_CONST=4,
						STRING_CONST=5;
	public static final int
						CLASS=6,
						METHOD=7,
						FUNCTION=8,
						CONSTRUCTOR=9,
						INT=10,
						BOOLEAN=11,
						CHAR=12,
						VOID=13,
						VAR=14,
						STATIC=15,
						FIELD=16,
						LET=17,
						DO=18,
						IF=19,
						ELSE=20,
						WHILE=21,
						RETURN=22,
						TRUE=23,
						FALSE=24,
						NULL=25,
						THIS=26;
	
	public static final String[] tags = {null,"keyword","symbol","identifier","integerConstant","stringConstant"};
	public static final String[] keywords = {null,null,null,null,null,null,"class","method","function","constructor","int","boolean","char","void","var","static","field","let","do","if","else","while","return","true","false","null","this"};
	public static Hashtable<String,Integer> keywordDict = invert(keywords);
	public static <T> Hashtable<T,Integer> invert(T[] ts)
	{
		Hashtable<T,Integer> toReturn = new Hashtable<>();
		for(int i = 0; i<ts.length;i++)
		{
			if(ts[i]!=null)
			{
				toReturn.put(ts[i],i);
			}
		}
		return toReturn;
	}
	
	
	String keywordRegex="(\\bclass\\b|\\bconstructor\\b|\\bfunction\\b|\\bmethod\\b|\\bfield\\b|\\bstatic\\b|\\bvar\\b|\\bint\\b|\\bchar\\b|\\bboolean\\b|\\bvoid\\b|\\btrue\\b|\\bfalse\\b|\\bnull\\b|\\bthis\\b|\\blet\\b|\\bdo\\b|\\bif\\b|\\belse\\b|\\bwhile\\b|\\breturn\\b)";
	String symbolRegex="[{}()\\[\\].,;+\\-\\*\\/&|<>=~]";
	String integerRegex="\\d+";
	String stringRegex="\".*?\"";
	String identifierRegex="[_A-Za-z][_A-Za-z0-9]*";
	String tokenRegex = "("+keywordRegex+"|"+symbolRegex+"|"+integerRegex+"|"+stringRegex+"|"+identifierRegex+")";
	
	Pattern keywordPattern = Pattern.compile(keywordRegex);
	Pattern symbolPattern = Pattern.compile(symbolRegex);
	Pattern integerPattern = Pattern.compile(integerRegex);
	Pattern stringPattern = Pattern.compile(stringRegex);
	Pattern identifierPattern = Pattern.compile(identifierRegex);
	
	ArrayList<String> tokens = new ArrayList<>();
	String token = null;
	
	public Tokenizer(File f) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(f));
		
		Pattern tokenPattern = Pattern.compile(tokenRegex);
		
		String wholeFile = "";
		
		String s = null;
		do
		{
			s = in.readLine();
			if(s!=null)
			{
				wholeFile+=s+"\n";
			}
		} while(s!=null);
		
		in.close();
		
		wholeFile = wholeFile.replaceAll("//.*", "");
		wholeFile = wholeFile.replaceAll("\t", "");
		wholeFile = wholeFile.replaceAll("  ", "");
		wholeFile = wholeFile.replaceAll("(?s)/\\*.*?\\*/", "");
		
		Matcher tokenMatcher = tokenPattern.matcher(wholeFile);
		
		while(tokenMatcher.find())
		{
			tokens.add(tokenMatcher.group());
		}
	}
	
	public boolean hasMoreTokens()
	{
		return !tokens.isEmpty();
	}
	
	public void advance()
	{
		if(!tokens.isEmpty())
		{
			token = tokens.remove(0);
		}
		else
		{
			token = null;
		}
	}
	
	public int tokenType()
	{
		if(keywordPattern.matcher(token).matches())
		{
			return KEYWORD;
		}
		if(symbolPattern.matcher(token).matches())
		{
			return SYMBOL;
		}
		if(integerPattern.matcher(token).matches() && Integer.parseInt(token)<=32767)
		{
			return INT_CONST;
		}
		if(stringPattern.matcher(token).matches())
		{
			return STRING_CONST;
		}
		if(identifierPattern.matcher(token).matches())
		{
			return IDENTIFIER;
		}
		
		throw new RuntimeException(String.format("%s is not a valid token. If it is an integer, it is too big.", token));
	}
	
	public int keyWord()
	{
		
		if(keywordDict.containsKey(token))
		{
			return keywordDict.get(token);
		}
		return -1;
	}
	
	public char symbol()
	{
		return token.charAt(0);
	}
	
	public String identifier()
	{
		return token;
	}
	
	public int intVal()
	{
		return Integer.parseInt(token);
	}
	
	public String peek()
	{
		return tokens.get(0);
	}
	
	public String stringVal()
	{
		return token.substring(1,token.length()-1);
	}
	
	public String innerText()
	{
		switch(tokenType())
		{
		case KEYWORD:
			return token;
		case SYMBOL:
			return token.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;");
		case INT_CONST:
			return token;
		case STRING_CONST:
			return token.substring(1,token.length()-1);
		case IDENTIFIER:
			return token;
		}
		return null;
	}
	
	public String formatToken()
	{
		String tag = tags[tokenType()];
		return "<"+tag+"> "+innerText()+" </"+tag+">";
	}
	
}

package compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import static compiler.Tokenizer.*;

public class CompilationEngine
{
	BufferedWriter out;
	Tokenizer in;
	int indent = 0;
	
	Stack<String> tags = new Stack<>();
	
	public CompilationEngine(Tokenizer in, File out) throws IOException 
	{
		this.out = new BufferedWriter(new FileWriter(out));
		this.in = in;
	}
	
	public void compileClass() 
	{
		in.advance();
		writeTag("class");
		writeToken(); //class
		writeToken();//identifier
		writeToken(); //{
		while(compileClassVarDec());
		while(compileSubroutineDec());
		writeToken();
		closeTag();
	}
	
	private boolean compileSubroutineDec()
	{
		if(in.keyWord() == CONSTRUCTOR || in.keyWord() == FUNCTION || in.keyWord() == METHOD)
		{
			writeTag("subroutineDec");
			writeToken(); //constructor, function, or method
			if(in.tokenType() == KEYWORD)
			{
				writeToken(); //void;
			}
			else
			{
				compileType(); // type
			}
			
			writeToken(); //identifier
			writeToken(); //(
			compileParameterList();
			writeToken(); //)
			compileSubroutineBody();
			
			closeTag();
			return true;
		}
		return false;
	}

	private void compileSubroutineBody()
	{
		writeTag("subroutineBody");
		writeToken(); //{
		while(compileVarDec());
		compileStatements();
		writeToken(); //}
		closeTag();
	}

	private void compileStatements()
	{
		writeTag("statements");
		while(compileStatement());
		closeTag();
	}

	private boolean compileStatement()
	{
		if(in.keyWord() == LET)
		{
			compileLetStatement();
			return true;
		}
		if(in.keyWord() == IF)
		{
			compileIfStatement();
			return true;
		}
		if(in.keyWord() == WHILE)
		{
			compileWhileStatement();
			return true;
		}
		if(in.keyWord() == DO)
		{
			compileDoStatement();
			return true;
		}
		if(in.keyWord() == RETURN)
		{
			compileReturnStatement();
			return true;
		}
		return false;
	}

	private void compileReturnStatement()
	{
		writeTag("returnStatement");
		writeToken(); //return
		if(in.symbol()!=';')
		{
			compileExpression();
		}
		writeToken();//;
		closeTag();
	}

	private void compileExpression()
	{
		writeTag("expression");
		compileTerm();
		while("+-*/&|<>=".indexOf(in.symbol())!=-1)
		{
			writeToken();//operator
			compileTerm();
		}
		closeTag();
	}

	private void compileTerm()
	{
		writeTag("term");
		if(in.tokenType()==INT_CONST)
		{
			writeToken();//number
		}
		else if(in.tokenType()==STRING_CONST)
		{
			writeToken();//String
		}
		else if(in.tokenType()==KEYWORD)
		{
			writeToken();
		}
		else if(in.symbol()=='(')
		{
			writeToken();//(
			compileExpression();
			writeToken();//)
		}
		else if(in.symbol()=='~' || in.symbol()=='-')
		{
			writeToken();//keywordConstant
			compileTerm();
		}
		else
		{
			String future = in.peek();
			if(future.charAt(0)=='(' || future.charAt(0)=='.')
			{
				writeToken();//part 1 of name(var, class,or sub)
				if(in.symbol()=='.')
				{
					writeToken();//.
					writeToken();//part 2 of name(sub)
				}
				writeToken();//(
				compileExpressionList();
				writeToken();//)
			}
			else
			{
				writeToken();//identifier
				if(in.symbol()=='[')
				{
					writeToken();//[
					compileExpression();
					writeToken();//]
				}
			}
		}
		closeTag();
	}

	private void compileDoStatement()
	{
		writeTag("doStatement");
		writeToken(); //do
		writeToken();//part 1 of name(var, class,or sub)
		if(in.symbol()=='.')
		{
			writeToken();//.
			writeToken();//part 2 of name(sub)
		}
		writeToken();//(
		compileExpressionList();
		writeToken();//)
		writeToken();//;
		closeTag();
	}

	private void compileExpressionList()
	{
		writeTag("expressionList");
		while(in.symbol()!=')')
		{
			compileExpression();
			if(in.symbol()==',')
			{
				writeToken();//,
			}
		}
		closeTag();
	}

	private void compileWhileStatement()
	{
		writeTag("whileStatement");
		writeToken(); //while
		writeToken();//(
		compileExpression();
		writeToken();//)
		writeToken();//{
		compileStatements();
		writeToken();//}
		closeTag();
	}

	private void compileIfStatement()
	{
		writeTag("ifStatement");
		writeToken();//if
		writeToken();//(
		compileExpression();
		writeToken();//)
		writeToken();//{
		compileStatements();
		writeToken();//}
		if(in.keyWord()==ELSE)
		{
			writeToken();//else
			writeToken();//{
			compileStatements();
			writeToken();//}
		}
		closeTag();
	}

	private void compileLetStatement()
	{
		writeTag("letStatement");
		writeToken(); //let
		writeToken();//identifier
		if(in.symbol()=='[')
		{
			writeToken();//[
			compileExpression();
			writeToken();//]
		}
		writeToken(); //=
		compileExpression();
		writeToken(); //;
		closeTag();
	}

	private boolean compileVarDec()
	{
		if(in.keyWord()==VAR)
		{
			writeTag("varDec");
			writeToken(); //var
			compileType();
			writeToken();//identifier
			while(in.symbol()==',')
			{
				writeToken(); //,
				writeToken();//identifier
			}
			writeToken();//;
			closeTag();
			return true;
		}
		return false;
	}

	private void compileParameterList()
	{
		writeTag("parameterList");
		if(compileType())
		{
			writeToken();//identifier//name
			while(in.symbol()==',')
			{
				writeToken();//,
				compileType();
				writeToken();//identifier
			}
		}
		closeTag();
	}

	private boolean compileClassVarDec() 
	{
		if(in.keyWord() == FIELD || in.keyWord() == STATIC)
		{
			writeTag("classVarDec");
			writeToken(); //field or static
			compileType(); //type
			writeToken();//identifier //name
			while(in.symbol()==',')
			{
				writeToken();//,
				writeToken();//identifier
			}
			writeToken(); //;
			closeTag();
			return true;
		}
		return false;
	}
	
	private boolean compileType() 
	{
		if(in.tokenType() == IDENTIFIER)
		{
			writeToken();//identifier
			return true;
		}
		else if(in.keyWord() == INT || in.keyWord()==CHAR || in.keyWord()==BOOLEAN)
		{
			writeToken();
			return true;
		}
		return false;
	}
	
	private void writeTag(String s) 
	{
		writeln("<"+s+">");
		indent+=2;
		tags.push(s);
	}
	
	private void closeTag() 
	{
		indent-=2;
		writeln("</"+tags.pop()+">");
	}
	
	private void writeln(String s) 
	{
		for(int i = 0; i<indent; i++)
		{
			s=" "+s;
		}
		try
		{
			out.write(s+"\r\n");
		} 
		catch (IOException e)
		{
			throw new RuntimeException("IOException");
		}
	}
	
	private void writeToken() 
	{
		writeln(in.formatToken());
		in.advance();
	}
	
	public void close() throws IOException
	{
		out.close();
	}
}

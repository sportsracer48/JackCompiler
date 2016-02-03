package vm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser
{
	public static final int 
	C_ARITHMETIC=0,
	C_PUSH=1,
	C_POP=2,
	C_LABEL=3,
	C_GOTO=4,
	C_IF=5,
	C_FUNCTION=6,
	C_RETURN=7,
	C_CALL=8;
	public static ArrayList<String> ops=new ArrayList<>();
	public static List<Integer> twoArgs = Stream.of(C_PUSH,C_POP,C_FUNCTION,C_CALL).collect(Collectors.toList());
	
	static
	{
		ops.add("add");
		ops.add("sub");
		ops.add("neg");
		ops.add("eq");
		ops.add("gt");
		ops.add("lt");
		ops.add("and");
		ops.add("or");
		ops.add("not");
	}
	
	List<String> lines = new ArrayList<>();
	String command;
	String[] tokens;
	
	public Parser(File input) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		String s = null;
		do
		{
			s = in.readLine();
			if(s!=null && !s.trim().equals("") && !s.startsWith("//"))
			{
				lines.add(s.trim().replaceAll("(\t|//.*)","").trim());
				
			}
		} while(s!=null);
		
		in.close();
		
		advance();
	}
	
	public String toString()
	{
		return lines.stream().reduce("",(a,b)->a+"\n"+b).substring(1);
	}
	
	public void advance()
	{
		if(lines.size()>0)
		{
			command = lines.remove(0);
			tokens = command.split(" ");
		}
		else
		{
			command = null;
			tokens = null;
		}
	}
	
	public boolean hasMoreCommands()
	{
		return command != null;
	}
	
	public int commandType()
	{
		if(ops.contains(command))
		{
			return C_ARITHMETIC;
		}
		if(command.startsWith("push"))
		{
			return C_PUSH;
		}
		if(command.startsWith("pop"))
		{
			return C_POP;
		}
		if(command.startsWith("label"))
		{
			return C_LABEL;
		}
		if(command.startsWith("goto"))
		{
			return C_GOTO;
		}
		if(command.startsWith("if-goto"))
		{
			return C_IF;
		}
		if(command.startsWith("function"))
		{
			return C_FUNCTION;
		}
		if(command.startsWith("call"))
		{
			return C_CALL;
		}
		if(command.startsWith("return"))
		{
			return C_RETURN;
		}
		throw new RuntimeException(String.format("%s is not a recognized command",command));
	}
	
	public String arg1()
	{
		int commandType = commandType();
		if(commandType==C_ARITHMETIC)
		{
			return tokens[0];
		}
		if(commandType!=C_RETURN)
		{
			return tokens[1];
		}
		throw new RuntimeException(String.format("%s has no first argument",command));
	}
	public String arg2()
	{
		if(twoArgs.contains(commandType()))
		{
			return tokens[2];
		}
		throw new RuntimeException(String.format("%s has no second argument",command));
	}
}

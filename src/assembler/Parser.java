package assembler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Parser
{
	public static final int A_COMMAND = 0;
	public static final int C_COMMAND = 1;
	public static final int L_COMMAND = 2;
	
	List<String> lines = new ArrayList<>();
	String command;
	
	public Parser(File input) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		String s = null;
		do
		{
			s = in.readLine();
			if(s!=null && !s.trim().equals("") && !s.startsWith("//"))
			{
				lines.add(s.replaceAll("( |\t|//.*)",""));
			}
		} while(s!=null);
		
		in.close();
		
		command = lines.remove(0);
	}
	
	public void advance()
	{
		if(lines.size()>0)
		{
			command = lines.remove(0);
		}
		else
		{
			command = null;
		}
	}
	
	public boolean hasMoreCommands()
	{
		return command != null;
	}
	
	public int commandType()
	{
		if(command.startsWith("@"))
		{
			return A_COMMAND;
		}
		if(command.startsWith("(") && command.endsWith(")"))
		{
			return L_COMMAND;
		}
		if(command.contains(";") || command.contains("="))
		{
			return C_COMMAND;
		}
		throw new RuntimeException(String.format("%s is not a well formed command",command));
	}
	
	public String symbol()
	{
		if(commandType() == L_COMMAND)
		{
			return command.substring(1,command.length()-1);
		}
		if(commandType() == A_COMMAND)
		{
			return command.substring(1);
		}
		throw new RuntimeException(String.format("%s has no symbol", command));
	}
	public String dest()
	{
		if(commandType() == C_COMMAND)
		{
			if(command.contains("="))
			{
				return command.split("=")[0];
			}
			return "";
		}
		throw new RuntimeException(String.format("%s has no dest", command));
	}
	public String comp()
	{
		if(commandType() == C_COMMAND)
		{
			String comp = command;
			if(comp.contains(";"))
			{
				comp = comp.split(";")[0];
			}
			if(comp.contains("="))
			{
				comp = comp.split("=")[1];
			}
			return comp;
		}
		throw new RuntimeException(String.format("%s has no comp", command));
	}
	
	public String jump()
	{
		if(commandType() == C_COMMAND)
		{
			if(command.contains(";"))
			{
				return command.split(";")[1];
			}
			return "";
		}
		throw new RuntimeException(String.format("%s has no jump", command));
	}
	
	
	
	public String toString()
	{
		return lines.stream().reduce("", (a,b)->a+","+b);
	}
}

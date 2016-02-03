package compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter
{
	BufferedWriter out;
	
	public VMWriter(File out) throws IOException
	{
		this.out=new BufferedWriter(new FileWriter(out));
	}
	
	public void writePush(String segment, int index)
	{
		writeln("push "+segment+" "+index);
	}
	
	public void writePop(String segment, int index)
	{
		writeln("pop "+segment+" "+index);
	}
	
	public void writeArithmetic(String command)
	{
		writeln(command);
	}
	
	public void writeLabel(String label)
	{
		writeln("label "+label);
	}
	
	public void writeGoto(String label)
	{
		writeln("goto "+label);
	}
	
	public void writeIf(String label)
	{
		writeln("if-goto "+label);
	}
	
	public void writeCall(String name, int args)
	{
		writeln("call "+name+" "+args);
	}
	
	public void writeFunction(String name, int locals)
	{
		writeln("function "+name+" "+locals);
	}
	
	public void writeReturn()
	{
		writeln("return");
	}
	
	public void close() throws IOException
	{
		out.close();
	}
	
	public void writeComment(String comment)
	{
		writeln("//"+comment);
	}
	
	public void writeln(String s)
	{
		try
		{
			out.write(s+"\n");
		}
		catch(IOException e)
		{
			throw new RuntimeException("IOException");
		}
	}
	
	public void writelns(String...strings)
	{
		for(String s:strings)
		{
			writeln(s);
		}
	}
}

package vm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class CodeWriter
{
	static String peekToM=
					 "@SP"  +"\n"
					+"A=M-1"+"\n";
	static String postPush=
					 "@SP"+  "\n" //A=0
					+"M=M+1";//M[0]=M[0]+1
	static String postPop=
					 "@SP"+  "\n" //A=0
					+"M=M-1";//M[0]=M[0]-1
	static String popToD=
					 peekToM
					+"D=M"   +"\n"  //D=M[A]=M[stackPtr-1](last value on stack)
					+postPop;
	static String pushFromD=
					 "@SP"+"\n"  //A=0
					+"A=M"+"\n"  //A=M[A]=stackPtr (next address for stack)
					+"M=D"+"\n"  //M[A] (next address for stack) = D
					+postPush;
	
	
	static Hashtable<String,String> opTable = new Hashtable<>();
	static Hashtable<String,String> boolTable = new Hashtable<>();
	
	static
	{
		opTable.put("add","D=D+M");
		opTable.put("sub","D=M-D");
		
		opTable.put("and","D=D&M");
		opTable.put("or", "D=D|M");
		
		opTable.put("not", "D=!D" );
		opTable.put("neg", "D=-D" );
		
		boolTable.put("eq", "JEQ");
		boolTable.put("lt", "JLT");
		boolTable.put("gt", "JGT");
	}
	
	BufferedWriter out;
	String fileName;
	String functionName = "";
	String symbolHead = "$";
	
	int nextCompSymbol = 0;
	
	public CodeWriter(File out) throws IOException
	{
		this.out=new BufferedWriter(new FileWriter(out));
	}
	
	public void setFileName(String fileName)
	{
		this.fileName=fileName;
	}
	
	public void writeInit() throws IOException
	{
		writeln("@256");
		writeln("D=A");
		writeln("@SP");
		writeln("M=D");
		writeCall("Sys.init",0);
	}
	
	public void writeLabel(String label) throws IOException
	{
		writeln("("+functionName+"$"+label+")");
	}
	
	public void writeGoto(String label) throws IOException
	{
		writeln("@"+functionName+"$"+label);
		writeln("0;JMP");
	}
	public void writeIf(String label) throws IOException
	{
		String end = getNewGlobalSymbol();
		writeln(popToD);
		writeln("@"+end);
		writeln("D;JEQ");
		writeln("@"+functionName+"$"+label);
		writeln("0;JMP");
		writeln("("+end+")");
	}
	
	public void writeCall(String functionName, int numArgs) throws IOException
	{
		String returnAddress = getNewGlobalSymbol();
		pushVal(returnAddress);//save frame
		pushMem("LCL");
		pushMem("ARG");
		pushMem("THIS");
		pushMem("THAT");
		int argOffset = numArgs+5;//ARG = SP-n-5
		writeln("@SP");
		writeln("D=M");
		writeln("@"+argOffset);
		writeln("D=D-A");
		writeln("@ARG");
		writeln("M=D");
		
		copy("SP","LCL");
		
		writeln("@"+functionName);
		writeln("0;JMP");
		writeln("("+returnAddress+")");
	}
	
	private void pushMem(String mem) throws IOException
	{
		writeln("@"+mem);
		writeln("D=M");
		writeln(pushFromD);
	}
	
	private void pushVal(String val) throws IOException
	{
		writeln("@"+val);
		writeln("D=A");
		writeln(pushFromD);
	}
	
	public void writeFunction(String functionName, int numLocals) throws IOException
	{
		this.functionName = functionName;
		writeln("("+functionName+")");
		for(int i = 0; i < numLocals; i++)
		{
			writePushPop(Parser.C_PUSH,"constant",0);//initialize the k local variables
		}
	}
	
	public void writeReturn() throws IOException
	{
		copy("LCL","R13");          //FRAME = LCL
		copyFromPtr("R13",-5,"R14");//RET = *(FRAME-5)
		writelns(				    //*ARG=pop()
				popToD,
				"@ARG",
				"A=M",
				"M=D");
		writelns(
				"@ARG",
				"D=M",
				"@SP",
				"M=D+1"
				);
		copyFromPtr("R13",-1,"THAT");
		copyFromPtr("R13",-2,"THIS");
		copyFromPtr("R13",-3,"ARG");
		copyFromPtr("R13",-4,"LCL");
		writelns(
				"@R14",
				"A=M",
				"0;JMP"
				);
	}
	
	private void copy(String src, String dest) throws IOException
	{
		writelns(
				"@"+src,
				"D=M",
				"@"+dest,
				"M=D"
				);
	}
	private void copyFromPtr(String srcPtr, int offset, String dest) throws IOException
	{
		writelns(
				"@"+srcPtr,
				"D=M",
				"@"+Math.abs(offset),
				"A=D"+(offset>=0?"+":"-")+"A",
				"D=M",
				"@"+dest,
				"M=D"
				);
	}
	
	public void writeArithmetic(String command) throws IOException
	{
		if(boolTable.containsKey(command))
		{
			writeln(popToD);
			writeln(peekToM);
			writeBoolean(boolTable.get(command));
			writeln(postPop);
			writeln(pushFromD);
			return;
		}
		else if(opTable.containsKey(command))
		{
			writeln(popToD);
			writeln(peekToM);
			writeln(opTable.get(command));
			if(!(command.equals("neg") || command.endsWith("not")))
			{
				writeln(postPop);
			}
			writeln(pushFromD);
		}
		else throw new RuntimeException(String.format("%s is not an arithmetic command", command));
		
	}
	
	private String getNewGlobalSymbol()
	{
		String sym = symbolHead+nextCompSymbol+symbolHead;
		nextCompSymbol++;
		if(nextCompSymbol==0)
		{
			symbolHead += "$";
		}
		return sym;
	}	
	
	private void writeBoolean(String comp) throws IOException
	{
		String tr=getNewGlobalSymbol();
		String end=getNewGlobalSymbol();
		writeln("D=M-D"    );
		writeln("@"+tr     );
		writeln("D;"+comp  );//if D==0
		writeln("D=0"      );//else return 0
		writeln("@"+end    );
		writeln("0;JMP"    );
		writeln("("+tr+")" );
		writeln("D=-1"     );//return -1;
		writeln("("+end+")");
	}
		
	private void setupD(String ptr,int index) throws IOException
	{
		writeln("@"+ptr);
		writeln("D=M");//D=*segment
		writeln("@"+index);//A=index
		writeln("D=D+A");//D=*segment+index (segment[index])
	}
	
	public void writePushPop(int command,String segment,int index) throws IOException
	{
		
		switch(segment)
		{
		case "constant":
			if(command == Parser.C_PUSH)
			{
				writeln("@"+index);
				writeln("D=A");
				writeln(pushFromD);
			}
			return; //pop constant should never do anything.
		case "argument":
			setupD("ARG",index);
			break;
		case "local":
			setupD("LCL",index);
			break;
		case "static":
			writeln("@"+fileName+"."+index);
			writeln("D=A");
			break;
		case "pointer":
			writeln("@"+(3+index));
			writeln("D=A");
			break;
		case "this":
			setupD("THIS",index);
			break;
		case "that":
			setupD("THAT",index);
			break;
		case "temp":
			writeln("@"+(5+index));
			writeln("D=A");
		}
		
		
		if(command==Parser.C_PUSH)
		{
			writeln("A=D");//A=*segment[index]
			writeln("D=M");//D=segment[index]
			writeln(pushFromD);
		}
		else if(command==Parser.C_POP)
		{
			writeln("@R13");
			writeln("M=D");//R3 points to the correct location
			writeln(popToD);//destroys A, sets D to top value of stack
			writeln("@R13");
			writeln("A=M");
			writeln("M=D"); //segment[index] = D = top value of stack
		}
	}
	
	
	private void writeln(String s) throws IOException
	{
		if(!s.endsWith("\n"))
		{
			out.write(s+"\n");
		}
		else
		{
			out.write(s);
		}
	}
	private void writelns(String...strings) throws IOException
	{
		for(String s : strings)
		{
			writeln(s);
		}
	}
	
	public void close() throws IOException
	{
		out.close();
	}
}

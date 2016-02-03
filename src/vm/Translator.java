package vm;

import java.io.File;
import java.io.IOException;

public class Translator
{
	public static void main(String[] args) throws IOException
	{		
		String filepath = "C:\\Users\\Henry\\Desktop\\nand2tetris\\projects\\08\\FunctionCalls\\StaticsTest";
		if(args.length>0)
		{
			filepath = args[0];
		}
		
		File file = new File(filepath);
		
		File[] files=null;
		CodeWriter writer = null;
		
		if(file.isDirectory())
		{
			String outpath = filepath+".asm";
			writer = new CodeWriter(new File(outpath));
			writer.writeInit();
			files=file.listFiles((dir,name)->name.endsWith(".vm"));
		}
		else
		{
			String outpath = filepath.substring(0,filepath.length()-3)+".asm";
			writer = new CodeWriter(new File(outpath));
			files = new File[]{file};
		}
		for(File vmFile : files)
		{
			Parser parser = new Parser(vmFile);
			String fileName = vmFile.getName().substring(0,vmFile.getName().length());
			fileName = fileName.substring(0,fileName.length()-3);
			writer.setFileName(fileName);
			while(parser.hasMoreCommands())
			{
				switch(parser.commandType())
				{
				case Parser.C_PUSH:
				case Parser.C_POP:
					writer.writePushPop(parser.commandType(),parser.arg1(),Integer.parseInt(parser.arg2()));
					break;
				case Parser.C_ARITHMETIC:
					writer.writeArithmetic(parser.arg1());
					break;
				case Parser.C_LABEL:
					writer.writeLabel(parser.arg1());
					break;
				case Parser.C_GOTO:
					writer.writeGoto(parser.arg1());
					break;
				case Parser.C_IF:
					writer.writeIf(parser.arg1());
					break;
				case Parser.C_CALL:
					writer.writeCall(parser.arg1(), Integer.parseInt(parser.arg2()));
					break;
				case Parser.C_FUNCTION:
					writer.writeFunction(parser.arg1(), Integer.parseInt(parser.arg2()));
					break;
				case Parser.C_RETURN:
					writer.writeReturn();
					break;
				}
				parser.advance();
			}
		}
		writer.close();
	}
}

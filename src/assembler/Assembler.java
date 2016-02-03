package assembler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Assembler
{
	public static void main(String[] args) throws IOException
	{		
		String filepath = "test.asm";
		if(args.length>0)
		{
			filepath = args[0];
		}
		
		//pass 1
		Parser parser = new Parser(new File(filepath));
		SymbolTable table = new SymbolTable();
		int line = 0;
		while(parser.hasMoreCommands())
		{
			switch(parser.commandType())
			{
			case Parser.L_COMMAND:
				table.addLabel(parser.symbol(), line);
				line--;
				break;
			}
			parser.advance();
			line++;
		}
		
		
		String outpath = filepath.substring(0, filepath.length()-4)+".hack";
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outpath)));
		parser = new Parser(new File(filepath));
		while(parser.hasMoreCommands())
		{
			switch(parser.commandType())
			{
			case Parser.A_COMMAND:
				out.write("0"+SymbolTable.intToString(table.resolve(parser.symbol()))+"\n");
				break;
			case Parser.C_COMMAND:
				out.write("111"+Code.comp(parser.comp())+Code.dest(parser.dest())+Code.jump(parser.jump())+"\n");
				break;
			case Parser.L_COMMAND:
				line--;
				break;
			}
			parser.advance();
			line++;
		}
		out.close();
	}
}

package assembler;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;


public class Code
{
	public static final int writeA = 0b100;
	public static final int writeD = 0b010;
	public static final int writeM = 0b001;
	public static Hashtable<String,Integer> compDict = new Hashtable<>();
	public static Hashtable<String,Integer> jumpDict = new Hashtable<>();
	static
	{
		compDict.put("0",   0b101010);
		compDict.put("1",   0b111111);
		compDict.put("-1"  ,0b111010);
		compDict.put("D",   0b001100);
		compDict.put("A",   0b110000);
		compDict.put("!D",  0b001101);
		compDict.put("!A",  0b110001);
		compDict.put("-D",  0b001111);
		compDict.put("-A",  0b110011);
		compDict.put("D+1", 0b011111);
		compDict.put("A+1", 0b110111);
		compDict.put("1+D", 0b011111);
		compDict.put("1+A", 0b110111);
		compDict.put("D-1", 0b001110);
		compDict.put("A-1", 0b110010);
		compDict.put("D+A", 0b000010);
		compDict.put("A+D", 0b000010);
		compDict.put("D-A", 0b010011);
		compDict.put("A-D", 0b000111);
		compDict.put("D&A", 0b000000);
		compDict.put("A&D", 0b000000);
		compDict.put("D|A", 0b010101);
		compDict.put("A|D", 0b010101);
		
		jumpDict.put("",0b000);
		jumpDict.put("JGT",0b001);
		jumpDict.put("JEQ",0b010);
		jumpDict.put("JGE",0b011);
		jumpDict.put("JLT",0b100);
		jumpDict.put("JNE",0b101);
		jumpDict.put("JLE",0b110);
		jumpDict.put("JMP",0b111);
	}
	public static String dest(String asm)
	{
		int dest = 0;
		if(asm.contains("A"))
		{
			dest+=writeA;
		}
		if(asm.contains("D"))
		{
			dest+=writeD;
		}
		if(asm.contains("M"))
		{
			dest+=writeM;
		}
		return String.format("%3s",Integer.toString(dest, 2)).replace(' ','0');
	}
	
	public static String comp(String asm)
	{
		int a = 0;
		if(asm.contains("M"))
		{
			if(asm.contains("A"))
			{
				throw new RuntimeException(String.format("%s contains references to both A and M", asm));
			}
			a=1;
		}
		String key = asm.replace('M', 'A');
		Integer c = compDict.get(key);
		if(c==null)
		{
			throw new RuntimeException(String.format("%s is not a valid computation", asm));
		}
		int comp = (a<<6)+c;
		return String.format("%7s",Integer.toString(comp, 2)).replace(' ','0');
	}
	
	public static String jump(String asm)
	{
		Integer jump = jumpDict.get(asm);
		if(jump==null)
		{
			throw new RuntimeException(String.format("%s is not a valid jump", asm));
		}
		return String.format("%3s",Integer.toString(jump, 2)).replace(' ','0');
	}
	
	
	public static void main(String args[]) throws IOException
	{
		Parser p = new Parser(new File("test"));
		//System.out.println(p.symbol());
		p.advance();
		//System.out.println(p.symbol());
		p.advance();
		System.out.println(dest(p.dest()));
		System.out.println(comp(p.comp()));
		System.out.println(jump(p.jump()));
		//System.out.println(p.hasMoreCommands());
	}
}

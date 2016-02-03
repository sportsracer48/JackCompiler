package assembler;
import java.util.Hashtable;


public class SymbolTable
{
	int nextAddress = 16;
	Hashtable<String,Integer> symTable=new Hashtable<>();
	
	public SymbolTable()
	{
		symTable.put("SP", 0);
		symTable.put("LCL", 1);
		symTable.put("ARG", 2);
		symTable.put("THIS", 3);
		symTable.put("THAT", 4);
		for(int i = 0; i<=15; i++)
		{
			symTable.put("R"+i, i);
		}
		symTable.put("SCREEN", 0x4000);
		symTable.put("KBD", 0x6000);
	}

	public int resolve(String symbol)
	{
		try
		{
			return Integer.parseInt(symbol);
		}
		catch(Exception e)
		{
			//Do nothing, this means it is a symbol
		}
		if(symTable.containsKey(symbol))
		{
			return symTable.get(symbol);
		}
		else//new variable, hurray!
		{
			int address = nextAddress;
			nextAddress++;
			symTable.put(symbol, address);
			return address;
		}
	}
	
	public void addLabel(String symbol, int line)
	{
		symTable.put(symbol, line);
	}
	
	public static String intToString(int i)
	{
		return String.format("%15s",Integer.toString(i, 2)).replace(' ','0');
	}
}

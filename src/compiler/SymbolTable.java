package compiler;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SymbolTable
{
	
	public static final int STATIC = 0, FIELD = 1, ARG=2, VAR = 3;
	public static final String[] segments = {"static","this","argument","local"};
	
	class Symbol
	{
		String name, type;
		int kind,index;
		
		public Symbol(String name, String type, int kind)
		{
			this.name=name;
			this.type=type;
			this.kind=kind;
		}
		
		public String toString()
		{
			String kind="";
			switch(this.kind)
			{
			case 0:
				kind = "static";
				break;
			case 1:
				kind = "field";
				break;
			case 2:
				kind = "argument";
				break;
			case 3:
				kind = "var";
				break;
			}
			return kind +"["+index+"] "+type+" "+name;
		}
	}
	
	public int[] count = new int[4];
	
	public List<Symbol> symbols = new ArrayList<>();
	
	
	public void startSubroutine()
	{
		List<Symbol> newSymbols = symbols.stream().filter(s->s.kind==FIELD || s.kind == STATIC).collect(Collectors.toList());
		symbols.clear();
		symbols.addAll(newSymbols);//mutable
		count[ARG] = 0;
		count[VAR] = 0;
	}
	
	public void define(String name, String type, int kind)
	{
		Symbol s = new Symbol(name,type,kind);
		symbols.add(s);
		s.index = count[kind];
		count[kind]++;
	}
	
	public int count(int kind)
	{
		return count[kind];
	}
	
	public boolean contains(String name)
	{
		return symbols.stream().filter(s->s.name.equals(name)).count()!=0;
	}
	
	private Symbol get(String name)
	{
		return symbols.stream().filter(s->s.name.equals(name)).findAny().orElse(null);
	}
	
	public int kindOf(String name)
	{
		return get(name).kind;
	}
	
	public String typeOf(String name)
	{
		return get(name).type;
	}
	
	public int indexOf(String name)
	{
		return get(name).index;
	}
	
	public String toString()
	{
		return symbols.toString();
	}
}

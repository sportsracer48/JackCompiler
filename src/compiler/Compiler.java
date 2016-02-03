package compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static compiler.SymbolTable.*;

public class Compiler
{
	static SymbolTable table;
	static VMWriter out;
	static String className;
	static int classSize;
	static int labelId = 0;
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException
	{
		String infile = "C:\\Users\\Henry\\Desktop\\nand2tetris\\projects\\11\\Square";
		if(args.length>0)
		{
			infile = args[0];
		}
		
		File file = new File(infile);
		
		File[] files=null;
		
		if(file.isDirectory())
		{
			files=file.listFiles((dir,name)->name.endsWith(".jack"));
		}
		else
		{
			files = new File[]{file};
		}
		
		for(File srcFile:files)
		{
			table = new SymbolTable();
			classSize = 0;
			String filepath = srcFile.getAbsolutePath();
			String outfile = filepath.substring(0, filepath.lastIndexOf('.'))+".xml";
			Tokenizer t = new Tokenizer(srcFile);
			CompilationEngine e = new CompilationEngine(t,new File(outfile));
			e.compileClass();
			e.close();
			
			DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			FileInputStream input = new FileInputStream(new File(outfile));
			Document doc = builder.parse(input);
			input.close();
			
			out = new VMWriter(new File(filepath.substring(0, filepath.lastIndexOf('.'))+".vm"));
			writeClass(doc.getDocumentElement());
			out.close();
		}
	}
	
	static List<Node> list(NodeList l)
	{
		List<Node> toReturn = new ArrayList<>();
		for(int i = 0; i<l.getLength(); i++)
		{
			toReturn.add(l.item(i));
		}
		return toReturn.stream().filter(n->!n.getNodeName().equals("#text")).collect(Collectors.toList());
	}
	
	static List<Node> children(Node n)
	{
		return list(n.getChildNodes());
	}
	
	static void print(Node n, int indent)
	{
		for(int i = 0; i<indent; i++)
		{
			System.out.print(" ");
		}
		System.out.println("<"+n.getNodeName()+">");
		List<Node> children = children(n);
		if(children.isEmpty())
		{
			for(int i = 0; i<indent; i++)
			{
				System.out.print(" ");
			}
			System.out.println(n.getTextContent());
		}
		for(Node n2:children)
		{
			print(n2, indent+2);
		}
		for(int i = 0; i<indent; i++)
		{
			System.out.print(" ");
		}
		System.out.println("</"+n.getNodeName()+">");
	}
	
	static String getGlobalLabel()
	{
		String label = ".."+labelId;
		labelId++;
		return label;
	}
	
	static void writeClass(Element root)
	{
		NodeList children = root.getChildNodes();
		for(int i=0;i<children.getLength(); i++)
		{
			Node child = children.item(i);
			if(child.getNodeName().equals("identifier"))
			{
				className = child.getTextContent().trim();
			}
			if(child.getNodeName().equals("classVarDec"))
			{
				writeClassVarDec(child);
			}
			else if(child.getNodeName().equals("subroutineDec"))
			{
				writeFunctionDec(child);
			}
		}
	}
	
	static void writeFunctionDec(Node root)
	{
		table.startSubroutine();
		List<Node> children = children(root);
		String funcType = children.get(0).getTextContent().trim();
		String name = className+"."+children.get(2).getTextContent().trim();
		Node paramList = children.get(4);
		if(funcType.equals("method"))
		{
			table.count[ARG]=1;
		}
		//declare all the args
		List<Node> params = children(paramList);
		for(int i = 0; i+1<params.size(); i+=3)
		{
			String argType = params.get(i).getTextContent().trim();
			String argName = params.get(i+1).getTextContent().trim();
			table.define(argName, argType, ARG);
		}
		
		
		Node body = children.get(6);
		int nVars = countLocalVars(body);
		out.writeFunction(name, nVars);
		if(funcType.equals("method"))
		{
			out.writePush("argument", 0);//push(arg[0])
			out.writePop("pointer", 0);//pointer[0] = pop() (this = pop())
		}
		else if(funcType.equals("constructor"))
		{
			out.writePush("constant",classSize);
			out.writeCall("Memory.alloc", 1); //push(Memory.alloc(size));
			out.writePop("pointer",0);//pointer[0]=pop() (this=pop())
		}
		writeSubroutineBody(body);
	}
	
	static int countLocalVars(Node root)
	{
		int count = 0;
		List<Node> children = children(root);
		for(Node child:children)
		{
			if(child.getNodeName().equals("varDec"))
			{
				count++;
				List<Node> varDecChildren = children(child);
				for(int i = 3; i+1<varDecChildren.size();i+=2)
				{
					count++;
				}
			}
		}
		return count;
	}
	
	static void writeSubroutineBody(Node root)
	{
		List<Node> children = children(root);
		for(Node child:children)
		{
			if(child.getNodeName().equals("varDec"))
			{
				List<Node> varDecChildren = children(child);
				String type = varDecChildren.get(1).getTextContent().trim();
				List<String> names = new ArrayList<String>();
				names.add(varDecChildren.get(2).getTextContent().trim());
				for(int i = 3; i+1<varDecChildren.size();i+=2)
				{
					names.add(varDecChildren.get(i+1).getTextContent().trim());
				}
				for(String name:names)
				{
					table.define(name, type, VAR);
				}
			}
			else if(child.getNodeName().equals("statements"))
			{
				writeStatements(child);
			}
			
		}
	}

	private static void writeStatements(Node root)
	{
		List<Node> children = children(root);
		for(Node child:children)
		{
			if(child.getNodeName().equals("letStatement"))
			{
				out.writeComment("<let>");
				writeLetStatement(child);
				out.writeComment("</let>");
			}
			else if(child.getNodeName().equals("ifStatement"))
			{
				out.writeComment("<if>");
				writeIfStatement(child);
				out.writeComment("</if>");
			}
			else if(child.getNodeName().equals("whileStatement"))
			{
				out.writeComment("<while>");
				writeWhileStatement(child);
				out.writeComment("</while>");
			}
			else if(child.getNodeName().equals("doStatement"))
			{
				out.writeComment("<do>");
				writeDoStatement(child);
				out.writeComment("</do>");
			}
			else if(child.getNodeName().equals("returnStatement"))
			{
				out.writeComment("<return>");
				writeReturnStatement(child);
				out.writeComment("</return>");
			}
			
		}
	}

	private static void writeReturnStatement(Node root)
	{
		List<Node> children = children(root);
		if(children.size()==3)
		{
			writeExpression(children.get(1));
		}
		else
		{
			out.writePush("constant",0);
		}
		out.writeReturn();
	}

	private static void writeDoStatement(Node root)
	{
		List<Node> children = children(root);
		children.remove(0);
		writeSubroutineCall(children);
		out.writePop("temp", 0);
	}

	private static void writeWhileStatement(Node root)
	{
		List<Node> children = children(root);
		String start = getGlobalLabel();
		String end = getGlobalLabel();
		out.writeLabel(start);
		writeExpression(children.get(2));
		out.writeArithmetic("not");
		out.writeIf(end);
		writeStatements(children.get(5));
		out.writeGoto(start);
		out.writeLabel(end);
	}

	private static void writeIfStatement(Node root)
	{
		List<Node> children = children(root);
		if(children.size()==7)
		{
			String end = getGlobalLabel();
			writeExpression(children.get(2));
			out.writeArithmetic("not");
			out.writeIf(end);
			writeStatements(children.get(5));
			out.writeLabel(end);
		}
		else//literally
		{
			String end = getGlobalLabel();
			String _else = getGlobalLabel();
			writeExpression(children.get(2));
			out.writeArithmetic("not");
			out.writeIf(_else);
			writeStatements(children.get(5));
			out.writeGoto(end);
			out.writeLabel(_else);
			writeStatements(children.get(9));
			out.writeLabel(end);
		}
	}

	private static void writeLetStatement(Node root)
	{
		List<Node> children = children(root);
		if(children.size()==5)
		{
			writeExpression(children.get(3));
			String name = children.get(1).getTextContent().trim();
			int kind = table.kindOf(name);
			int index = table.indexOf(name);
			out.writePop(segments[kind], index);
		}
		else
		{
			writeExpression(children.get(6));
			String name = children.get(1).getTextContent().trim();
			int kind = table.kindOf(name);
			int index = table.indexOf(name);
			out.writePush(segments[kind], index);
			writeExpression(children.get(3));
			out.writeArithmetic("add");
			out.writePop("pointer", 1);
			out.writePop("that", 0);
		}
	}

	private static void writeExpression(Node root)
	{
		List<Node> children = children(root);
		writeTerm(children.get(0));
		for(int i = 1; i+1<children.size(); i+=2)
		{
			writeTerm(children.get(i+1));
			String op = children.get(i).getTextContent().trim();
			switch(op)
			{
			case "+":
				out.writeArithmetic("add");
				break;
			case "-":
				out.writeArithmetic("sub");
				break;
			case "*":
				out.writeCall("Math.multiply", 2);
				break;
			case "/":
				out.writeCall("Math.divide",2);
				break;
			case "&":
				out.writeArithmetic("and");
				break;
			case "|":
				out.writeArithmetic("or");
				break;
			case "<":
				out.writeArithmetic("lt");
				break;
			case ">":
				out.writeArithmetic("gt");
				break;
			case "=":
				out.writeArithmetic("eq");
				break;
			}
		}
	}

	private static void writeTerm(Node root)
	{
		List<Node> children = children(root);
		String text = children.get(0).getTextContent().trim();
		switch(children.get(0).getNodeName())
		{
		case "integerConstant":
			out.writePush("constant", Integer.parseInt(text));
			break;
		case "stringConstant":
			out.writePush("constant", text.length());
			out.writeCall("String.new", 1);
			for(int b: text.getBytes())
			{
				out.writePush("constant", b);
				out.writeCall("String.appendChar", 2);
			}
			break;
		case "keyword":
			switch(text)
			{
			case "true":
				out.writePush("constant", 1);
				out.writeArithmetic("neg");
				break;
			case "null":
			case "false":
				out.writePush("constant", 0);
				break;
			case "this":
				out.writePush("pointer",0);
				break;
			}
			break;
		case "symbol":
			switch(text)
			{
			case "(":
				writeExpression(children.get(1));
				break;
			case "-":
				writeTerm(children.get(1));
				out.writeArithmetic("neg");
				break;
			case "~":
				writeTerm(children.get(1));
				out.writeArithmetic("not");
				break;
			}
			break;
		case "identifier":
			if(children.size()==1)
			{
				out.writePush(segments[table.kindOf(text)], table.indexOf(text));
			}
			else if(children.get(1).getTextContent().trim().equals("["))
			{
				out.writeComment("array");
				out.writePush(segments[table.kindOf(text)], table.indexOf(text));
				writeExpression(children.get(2));
				out.writeArithmetic("add");
				out.writePop("pointer", 1);
				out.writePush("that", 0);
			}
			else
			{
				writeSubroutineCall(root);
			}
			break;
		}
	}
	
	private static void writeSubroutineCall(List<Node> children)
	{
		String thatClass;
		String subName;
		int args = 0;
		Node argList;
		if(children.get(1).getTextContent().trim().equals("("))
		{
			thatClass = className;
			subName = children.get(0).getTextContent().trim();
			out.writePush("pointer",0);
			args=1;
			argList = children.get(2);
		}
		else
		{
			String varName=children.get(0).getTextContent().trim();
			argList = children.get(4);
			subName = children.get(2).getTextContent().trim();
			if(table.contains(varName))
			{
				thatClass = table.typeOf(varName);
				args = 1;
				out.writePush(segments[table.kindOf(varName)],table.indexOf(varName));
			}
			else
			{
				thatClass  = varName;
			}
		}
		
		args += writeArgList(argList);
		out.writeCall(thatClass+"."+subName, args);
	}
	
	private static void writeSubroutineCall(Node root)
	{
		List<Node> children = children(root);
		writeSubroutineCall(children);
	}

	private static int writeArgList(Node root)
	{
		int count = 0;
		List<Node> children = children(root);
		for(Node child:children)
		{
			if(child.getNodeName().equals("expression"))
			{
				count++;
				writeExpression(child);
			}
		}
		return count;
	}
	
	static void writeClassVarDec(Node root)
	{
		List<Node> children = children(root);
		String varKind = children.get(0).getTextContent().trim();
		String varType = children.get(1).getTextContent().trim();
		for(int i = 2; i<children.size(); i+=2)
		{
			String varName = children.get(i).getTextContent().trim();
			if(varKind.equals("static"))
			{
				table.define(varName, varType, STATIC);
			}
			else if(varKind.equals("field"))
			{
				table.define(varName, varType, FIELD);
				classSize++;
			}
		}
	}
	
	
}

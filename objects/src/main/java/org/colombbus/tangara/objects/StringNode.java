package org.colombbus.tangara.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;

@Localize(value="StringNode",localizeParent=true)
public class StringNode implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6756080929726709734L;
	Vector<StringNode> children = new Vector<StringNode>();
	String data;
	
	@Localize(value="StringNode")
	public StringNode()
	{
		this.data = null;
	}
	
	@Localize(value="StringNode")
	public StringNode(String data)
	{
		this();
		this.data = data;
	}
	
	@Localize(value="StringNode.getChildren")
	public Vector<StringNode> getChildren()
	{
		return this.children;
	}
	
	@Localize(value="StringNode.getChild")
	public StringNode getChild(int i)
	{
		return this.children.get(i-1);
	}
	
	@Localize(value="StringNode.getChildrenCount")
	public int getChildrenCount()
	{
		return this.children.size();
	}

	@Localize(value="StringNode.addChild")
	public void addChild(StringNode childNode)
	{
		this.children.add(childNode);
	}
	
	@Localize(value="StringNode.getData")
	public String getData()
	{
		return this.data;
	}
	
	@Localize(value="StringNode.setData")
	public void setData(String data)
	{
		this.data = data;
	}
	
	@Localize(value="StringNode.saveFile")
	public void saveFile(String fileName) throws IOException
	{
		FileOutputStream foStream = new FileOutputStream(new File(Program.instance().getCurrentDirectory() + "\\" + fileName));
		ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
		ooStream.writeObject(this);
	}
	
	@Localize(value="StringNode.loadFile")
	public void loadFile(String fileName) throws IOException, ClassNotFoundException
	{
		FileInputStream fiStream = new FileInputStream(new File(Program.instance().getCurrentDirectory() + "\\" + fileName));
		ObjectInputStream oiStream = new ObjectInputStream(fiStream);
		StringNode fileObject = (StringNode) oiStream.readObject();
		this.data = fileObject.data;
		this.children = fileObject.children;
	}
}
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

@Localize(value="ArrayNode",localizeParent=true)
public class ArrayNode implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6027805244822512063L;
	Vector<ArrayNode> children = new Vector<ArrayNode>();
	Array data;
	
	@Localize(value="ArrayNode")
	public ArrayNode()
	{
		this.data = null;
	}
	
	@Localize(value="ArrayNode")
	public ArrayNode(Array data)
	{
		this();
		this.data = data;
	}
	
	@Localize(value="ArrayNode.getChildren")
	public Vector<ArrayNode> getChildren()
	{
		return this.children;
	}
	
	@Localize(value="ArrayNode.getChild")
	public ArrayNode getChild(int i)
	{
		return this.children.get(i-1);
	}
	
	@Localize(value="ArrayNode.getChildrenCount")
	public int getChildrenCount()
	{
		return this.children.size();
	}

	@Localize(value="ArrayNode.addChild")
	public void addChild(ArrayNode childNode)
	{
		this.children.add(childNode);
	}
	
	@Localize(value="ArrayNode.getData")
	public Array getData()
	{
		return this.data;
	}
	
	@Localize(value="ArrayNode.setData")
	public void setData(Array data)
	{
		this.data = data;
	}

	
	@Localize(value="ArrayNode.saveFile")
	public void savefile(String fileName) throws IOException
	{
		FileOutputStream foStream = new FileOutputStream(new File(Program.instance().getCurrentDirectory() + "\\" + fileName));
		ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
		ooStream.writeObject(this);
	}
	
	@Localize(value="ArrayNode.loadFile")
	public void loadFile(String fileName) throws IOException, ClassNotFoundException
	{
		FileInputStream fiStream = new FileInputStream(new File(Program.instance().getCurrentDirectory() + "\\" + fileName));
		ObjectInputStream oiStream = new ObjectInputStream(fiStream);
		ArrayNode fileObject = (ArrayNode) oiStream.readObject();
		this.data = fileObject.data;
		this.children = fileObject.children;
	}
}
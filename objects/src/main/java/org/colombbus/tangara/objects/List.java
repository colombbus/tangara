package org.colombbus.tangara.objects;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.colombbus.build.Localize;
import org.colombbus.tangara.Program;
import org.colombbus.tangara.TObject;

@Localize(value="List",localizeParent=true)
public class List<T> extends TObject implements Iterable<T> {
	
	private ArrayList<T> data;
	private Iterator<T> iterator;
	
	
	@Localize(value="List")
	public List()
	{
		data = new ArrayList<T>();
		iterator = null;
	}
	
	@Localize(value="List.add")
	public void add(T obj)
	{
		if (data.contains(obj))
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.alreadyContained"), Program.instance().getObjectName(obj))); //$NON-NLS-1$
			return;
		}
		data.add(obj);
	}
	
	@Localize(value="List.contains")
	public boolean contains(T obj)
	{
		return (data.contains(obj));
	}
	
	@Localize(value="List.containsAny")
	public boolean containsAny(List<T> array)
	{
		array.rewind();
		while (array.hasNext())
		{
			if (contains(array.getNext()))
				return true;
		}
		return false;
	}
	
	@Localize(value="List.rewind")
	public void rewind()
	{
		iterator = data.iterator();
	}
	
	@Localize(value="List.getNext")
	public T getNext()
	{
		if (data.isEmpty())
		{
			Program.instance().writeMessage(getMessage("error.noElement")); //$NON-NLS-1$
			return null;
		}
		if (iterator==null)
			rewind();
		return iterator.next();
	}

	@Localize(value="List.hasNext")
	public boolean hasNext()
	{
		if (iterator==null)
			rewind();
		return iterator.hasNext();
	}
	
	@Localize(value="List.remove1")
	public void remove(T obj)
	{
		if (!data.contains(obj))
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.notPresent"), Program.instance().getObjectName(obj))); //$NON-NLS-1$
			return;
		}
		data.remove(obj);
	}
	
	@Localize(value="List.remove2")
	public void remove(int index)
	{
		if (index-1 > data.size() || index - 1 < 0)
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.outOfBounds"), index-1)); //$NON-NLS-1$
			return;
		}
		data.remove(index-1);
	}

	@Localize(value="List.clear")
	public void clear()
	{
		data.clear();
	}
	
	@Override
	public void deleteObject()
	{
		data.clear();
		data = null;
		super.deleteObject();
	}
	
	@Localize(value="List.get")
	public T get(int index)
	{
		if (index > data.size() || index < 1)
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.outOfBounds"), index)); //$NON-NLS-1$
			return null;
		}
		return data.get(index-1);
	}
	
	@Localize(value="List.set")
	public void set(int index, T obj) {
		if (index > data.size() || index  < 1)
		{
			Program.instance().writeMessage(MessageFormat.format(getMessage("error.outOfBounds"), index)); //$NON-NLS-1$
		} else {
			data.set(index-1, obj);
		}
	}
	
	@Localize(value="List.getSize")
	public int getSize()
	{
		return data.size();
	}

	@Override
	public Iterator<T> iterator() {
		return data.iterator();
	}

}

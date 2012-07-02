package ar.edu.ort.common;

import java.io.Serializable;
import java.util.Vector;

public class Mensaje implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int LOGIN = 0;
	public static final int LOGOUT = 1;
	public static final int LIST = 2;
	public static final int ERROR = 3;
	public static final int MESSAGE = 4;
	
	private int type;
	private String nick;
	private String text;
	private Vector<String> users;
	
	public Mensaje()
	{
		users = new Vector<String>();
	}
	
	public String getNick()
	{
		return nick;
	}
	
	public void setNick(String nick)
	{
		this.nick = nick;
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public Vector<String> getUsers()
	{
		return users;
	}
	
	public void setUsers(Vector<String> users)
	{
		this.users=users;
	}
}
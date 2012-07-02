package ar.edu.ort.server;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ar.edu.ort.common.Consts;
import ar.edu.ort.common.Mensaje;

public final class Servidor extends JFrame
{
	private static final long serialVersionUID = 1L;
	JButton btnIniciar;
	JButton btnDetener;
	
	private ServerThread serverThread;
	
	public Servidor()
	{
		init();
	}
	
	private void init() 
	{
		setTitle("Grupo 3 - Puerto:" + Consts.DEFAULT_SEVER_PORT);
		setBounds(0, 0, 160, 100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new FlowLayout());
		
		btnIniciar = new JButton("Iniciar servidor ");
		getContentPane().add(btnIniciar);
		btnIniciar.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				serverStart();
				btnIniciar.setEnabled(false);
				btnDetener.setEnabled(true);
				
			}});
		btnDetener = new JButton("Detener servidor");
		btnDetener.setEnabled(false);
		getContentPane().add(btnDetener);
		btnDetener.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				serverStop();
				btnIniciar.setEnabled(true);
				btnDetener.setEnabled(false);				
			}});
	}
	
	private void serverStart()
	{
		if (serverThread == null)
		{
			serverThread = new ServerThread();
			serverThread.start();
		}
	}
	
	private void serverStop()
	{
		if (serverThread != null)
		{
			serverThread.terminate();
			serverThread = null;
		}
	}
	
	private void serverError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg);
	}

	public static void main(String[] args)
	{
		Servidor servidor = new Servidor();
		servidor.setVisible(true);
	}
	
	class ClientWorker extends Observable implements Runnable
	{
		private Socket cliente;
		ObjectInputStream in;
		ObjectOutputStream out;
		private String nick;
		  
		ClientWorker(Socket cliente)
		{
			  this.cliente = cliente;
			  nick="";
		}

		public void run()
		{
			try
			{
			  in = new ObjectInputStream(cliente.getInputStream());
			  out = new ObjectOutputStream(cliente.getOutputStream());
			}
			catch(IOException e)
			{
				serverError(e.getMessage());
			}
			//Se queda leyendo mensajes del cliente
			while(!cliente.isInputShutdown())
			{
				try
				{
					//Leo el objeto recibido
					Mensaje msg = (Mensaje) in.readObject();
					System.out.println("Usuario:" + msg.getNick() + " Accion:" + msg.getType());
					setNick(msg.getNick());
					setChanged();
					notifyObservers(msg);
			    }
				catch(IOException e)
				{
					//serverError(e.getMessage());
					break;
			    }
				catch(ClassNotFoundException e)
				{
					e.printStackTrace();
			    }
			}
			System.out.println("Error en la conexion de " + nick);
			terminate();
		  }

		public boolean isLoggedIn()
		{
			return (!nick.equals(""));
		}

	
		//Enviar Mensaje 		
		public void enviarMensaje(Mensaje msg)
		{
	        try
	        {
	            this.out.writeObject(msg);
	        }
	        catch(IOException io)
	        {
	            System.out.println("Error al intentar enviar msg al usuario " + msg.getNick());
	        }            
	    }
		
		public void terminate()
		{
			try
			{
				in.close();
				out.close();
				cliente.close();
				if (isLoggedIn()){}
			}
			catch(IOException e)
			{
				serverError(e.getMessage());
			}
		}

		public String getNick()
		{
			return nick;
		}

		public void setNick(String nick)
		{
			this.nick = nick;
		}
	} //Fin ClientWorker

	
	class ServerThread extends Thread implements Observer
	{
		private ServerSocket ss;
		private Hashtable listaNicks;
		
		public ServerThread()
		{
			// La hashtable guarda como Key el nick del usuario
			// y como Element el ObjectOutputStream.
			listaNicks = new Hashtable();
		}
		
		public void run()
		{
			try
			{
				ss = new ServerSocket(Consts.DEFAULT_SEVER_PORT);
				while (!ss.isClosed())
				{
					Socket skt = ss.accept();
					// Lanza hilo para atender al cliente que se conectó
					ClientWorker cw =new ClientWorker(skt);
					cw.addObserver(this);
			        Thread t = new Thread(cw);
			        t.start();
				}
				
			}
			catch (IOException e)
			{
				serverError(e.getMessage());
			}
			finally
			{
				ss = null;
			}
		}
		public void terminate()
		{
			if (ss != null)
			{
				try
				{
					Enumeration lista = listaNicks.keys();
			    	while (lista.hasMoreElements()){
			    		ClientWorker cw = (ClientWorker) listaNicks.get(lista.nextElement());
			    		cw.setNick("");
			    		cw.terminate();
			    	}
					ss.close();
				}
				catch(IOException e){}
			}
		}
		public void update(Observable who, Object what)
		{
			Mensaje msg = (Mensaje) what;
			System.out.println(msg.getNick()+ ":mensaje recibido, tipo:" + String.valueOf(msg.getType()));
			ClientWorker cw = (ClientWorker) who;
			//procesa el mensaje
	        switch (msg.getType())
	        {
            case Mensaje.LOGOUT:
                break;
            case Mensaje.LOGIN:
				//Agrego el nick a la lista
				agregarUsuario(cw.getNick(), cw);
                break;
            case Mensaje.MESSAGE:
                break;
	        }
		}
		
	    //Agregar usuarios
	    private void agregarUsuario(String nick, ClientWorker cw)
	    {
	        listaNicks.put(nick, cw);
	    }

	    //Verificacion de Nick TODO Preguntar donde validar esto
	    private boolean nickExiste(String nickName)
	    {
	        return listaNicks.containsKey(nickName);
	    }
	    
	    //Quitar usuario
	    private void quitarUsuario(String nick)
	    {
	        listaNicks.remove(nick);
	    }
	    
	    private Vector getListaConectados()
	    {
	    	Vector vec = new Vector();
	    	Enumeration lista = listaNicks.keys();
	    	while (lista.hasMoreElements())
	    	{
	    		vec.addElement(lista.nextElement());
	    	}
	    	return vec;
	    }
	    
	    private void propagarMensaje(Mensaje msg)
	    {
	    }
	} //Fin ServerThread
}
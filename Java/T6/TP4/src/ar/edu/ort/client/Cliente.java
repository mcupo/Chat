package ar.edu.ort.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ar.edu.ort.common.Consts;
import ar.edu.ort.common.Mensaje;

public class Cliente extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private ClientThread clientThread;
	private JTextField serverHost;
	private JTextField serverPort;
	private JTextField userNick;
	private JTextField userMessage;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JButton btnSend;
	private JTextArea messageHistory;
	private JList connectedUsers;
	private DefaultListModel connectedUsersModel;


	public Cliente()
	{
		init();
	}
	
	private int dialogoConectado()
	{
		Object[] options = {"Si", "No"};
		int n = JOptionPane.showOptionDialog(this,
		"Desea interrumpir la conexion a " + serverHost.getText() + " y cerrar la aplicación?",
		"Cliente Chat - b2012c1 Grupo 3",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.WARNING_MESSAGE,
		null,
		options,
		options[1]);
		return n;
	}
	
	private void init()
	{
		setTitle("Grupo 3 - 3°2° B - Chat");
		setBounds(0, 0, 720, 400);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    //configurar acciones de la "x"
		addWindowListener(new WindowAdapter()
		{
	        public void windowClosing(WindowEvent e)
	        {
	        	if (clientThread != null)
	        	{
	        		if (clientThread.isConnected())
	        		{
	        			int op = dialogoConectado();
	        			switch (op)
	        			{
							case JOptionPane.NO_OPTION:break;
							case JOptionPane.CLOSED_OPTION:break;
							case JOptionPane.YES_OPTION:
								System.exit(0);
								break;
							default:break;
	        			}
	        		}
	        		else
	        		{
	        			System.exit(0);
	        		}
	        	}
	        	else
	        	{
	        		System.exit(0);
	        	}
	        }
	    });
		getContentPane().setLayout(new BorderLayout());

		JPanel pnl, pnlAux;
		JLabel lbl;
		
		//
		// TOP Panel - Datos del Servidor
		//
		pnl = new JPanel();
		pnl.setLayout(null);
		pnl.setBackground(Color.LIGHT_GRAY);
		pnl.setPreferredSize(new Dimension(0, 34));
		getContentPane().add(pnl, BorderLayout.NORTH);

		lbl = new JLabel("Host:");
		pnl.add(lbl);
		lbl.setBounds(8, 8, 32, 22);
		serverHost = new JTextField("127.0.0.1");
		pnl.add(serverHost);
		serverHost.setBounds(40, 8, 100, 22);

		lbl = new JLabel("Port:");
		pnl.add(lbl);
		lbl.setBounds(160, 8, 32, 22);
		serverPort = new JTextField(String.valueOf(Consts.DEFAULT_SEVER_PORT));
		pnl.add(serverPort);
		serverPort.setBounds(192, 8, 100, 22);

		lbl = new JLabel("Nick:");
		pnl.add(lbl);
		lbl.setBounds(312, 8, 32, 22);
		userNick = new JTextField("mi nick");
		pnl.add(userNick);
		userNick.setBounds(344, 8, 100, 22);
		
		btnConnect = new JButton("Conectar");
		pnl.add(btnConnect);
		btnConnect.setBounds(450, 8, 120, 22);
		btnConnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				clientStart();
			}
		});
		btnDisconnect = new JButton("Desconectar");
		btnDisconnect.setEnabled(false);
		pnl.add(btnDisconnect);
		btnDisconnect.setBounds(578, 8, 120, 22);
		btnDisconnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				clientStop();
			}
		});
		
		//
		// JList - Nicks de los usuarios logueados
		//
		pnlAux = new JPanel();
		pnlAux.setLayout(new BorderLayout());
		pnlAux.setBackground(Color.RED);
		pnlAux.setPreferredSize(new Dimension(200, 0));
		getContentPane().add(pnlAux, BorderLayout.EAST);
		
		lbl = new JLabel("Usuarios conectados:");
		pnlAux.add(lbl, BorderLayout.NORTH);
		connectedUsersModel = new DefaultListModel();
		connectedUsers = new JList(connectedUsersModel);
		connectedUsers.setPreferredSize(new Dimension(200, 0));
		connectedUsers.setBackground(Color.ORANGE);
		pnlAux.add(connectedUsers, BorderLayout.CENTER);

		//
		// CENTER Panel - Panel con datos de mensajes
		//
		pnl = new JPanel();
		pnl.setLayout(new BorderLayout());
		pnl.setBackground(Color.GRAY);
		getContentPane().add(pnl, BorderLayout.CENTER);

		lbl = new JLabel("Historial de mensajes:");
		pnl.add(lbl, BorderLayout.NORTH);
		lbl.setForeground(Color.WHITE);
		messageHistory = new JTextArea();
		messageHistory.setBackground(Color.WHITE);
		messageHistory.setEditable(false);
		pnl.add(messageHistory, BorderLayout.CENTER);
		messageHistory.append("Bienvenido a la sala de chat!");
		
		//
		// CENTER Panel - Area para escribir mensajes
		// está localizado al SOUTH del CENTER Panel.
		//
		pnlAux = new JPanel();
		pnlAux.setLayout(null);
		pnlAux.setBackground(Color.GRAY);
		pnlAux.setPreferredSize(new Dimension(0, 34));
		pnl.add(pnlAux, BorderLayout.SOUTH);
		
		lbl = new JLabel("Texto:");
		pnlAux.add(lbl);
		lbl.setBounds(8, 8, 50, 22);
		userMessage = new JTextField();
		userMessage.setEditable(false);
		pnlAux.add(userMessage);
		userMessage.setBounds(60, 8, 300, 22);
		
		btnSend = new JButton("Enviar");
		btnSend.setEnabled(false);
		pnlAux.add(btnSend);
		btnSend.setBounds(380, 8, 120, 22);
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				clientSend();
				userMessage.setText("");
			}
		});
	}
	
	private void clientStart()
	{
			clientThread = new ClientThread();
			clientThread.start();
			//actualizo la vista
			serverHost.setEditable(false);
			serverPort.setEditable(false);
			userNick.setEditable(false);
			btnConnect.setEnabled(false);
			btnDisconnect.setEnabled(true);
	}
	
	private void clientStop()
	{
		if (clientThread != null)
		{
			clientThread.terminate();
			clientThread = null;
			//actualizo la vista
			serverHost.setEditable(true);
			serverPort.setEditable(true);
			userNick.setEditable(true);
			btnConnect.setEnabled(true);
			btnDisconnect.setEnabled(false);
			btnSend.setEnabled(false);
			connectedUsersModel.removeAllElements();
			userMessage.setText("");
			userMessage.setEditable(false);
		}
	}
	
	private void clientSend()
	{
		if (clientThread != null)
		{
			clientThread.sendText();
		}
	}
	
	private void clientError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg);
	}
	
	private void agregarUsuario(String usr)
	{
		connectedUsersModel.addElement(usr);
	}
	
	private void removerUsuario(String usr)
	{
		connectedUsersModel.removeElement(usr);
	}
	
	public static void main(String[] args)
	{
		Cliente cliente = new Cliente();
		cliente.setVisible(true);
	}
	
	class ClientThread extends Thread
	{
		private Socket skt;
		private ObjectOutputStream oos;
		private boolean connected;
		
		public void run()
		{
			connected=false;
			try
			{
				skt = new Socket(serverHost.getText(), Integer.parseInt(serverPort.getText()));
				try
				{
					oos = new ObjectOutputStream(skt.getOutputStream());
					try
					{
						Mensaje mensaje = new Mensaje();
						mensaje.setNick(userNick.getText());
						//Mensaje de LOGIN
						mensaje.setType(Mensaje.LOGIN);
						//Envio el objeto con el mensaje de login
						oos.writeObject(mensaje);
						try
						{
							
							
							
				            messageHistory.append( "\nIniciando conexión a " + serverHost.getText()+":"+ Integer.parseInt(serverPort.getText()));
							System.out.println(userNick.getText().trim()+":login enviado");
						} 
						//catch(IOException e)
						catch(Exception e)
						{
							clientError("Error en la conexión.");
						}
						ObjectInputStream ois = new ObjectInputStream(skt.getInputStream());						
						// Quedarse en loop hasta que se corte
						messageHistory.append("\n" + "Has ingresado al chat.");
						while (skt.isConnected())
						{
							connected=true;
							try
							{
								Mensaje msg = (Mensaje) ois.readObject();
								System.out.println(userNick.getText().trim()+":mensaje recibido, tipo:" + String.valueOf(msg.getType()));
								procesarMensaje(msg);
							}
							catch(ClassNotFoundException e)
							{
								clientError(e.getMessage());
								clientStop();
								break;
							}
							catch (IOException e)
							{
								if (isConnected())
								{
									clientError("Error en la conexión.");
								}
								clientStop();
								break;								
							}
						}
					}
					catch
					(IOException e)
					{
						clientError("Error en la conexión.");
						clientStop();
					}
				}
				catch(IOException e)
				{
					clientError("Error en la conexión.");
					clientStop();
				}
			}
			catch(IOException e)
			{
				clientError("Error al conectarse a " + serverHost.getText()+":"+serverPort.getText());
				clientStop();
			}
			catch(Exception e)
			{
				clientError(e.getMessage());
				clientStop();
			}
			messageHistory.append("\n" + "Has salido del chat.");
		}
		
		public void terminate()
		{
			if (skt != null)
			{
				connected=false;
				try
				{
					//El logout lo realiza el server automaticamente ante el cierre del socket
					skt.close();
				}
				catch(IOException e){}
			}
		}
		
		public void sendText()
		{
			if (skt != null)
			{
				try
				{				
					Mensaje msg = new Mensaje();
					msg.setType(Mensaje.MESSAGE);
					msg.setText(userNick.getText() + " dice:" + userMessage.getText());
					msg.setNick(userNick.getText());
					//agregar lista de usuarios seleccionados como destinatarios
					Object[] users=connectedUsers.getSelectedValues();
					for (int i=0;i<users.length;i++)
					{
						if (!users[i].toString().equals(userNick.getText()))
							msg.getUsers().add((String) users[i]);						
					}
					oos.writeObject(msg);
				}
				catch(IOException e){}
			}
		}
		
		public void procesarMensaje(Mensaje msg)
		{
			 String mostrar = null;
			 System.out.println("Procesando Mensaje");
		     switch( msg.getType() )
		     {
		       //Quita usario de la lista
		       case Mensaje.LOGOUT:
		    	   messageHistory.append("\n" + msg.getNick() + " se ha desconectado.");
		    	   removerUsuario(msg.getNick());
		    	   break;
		       //Agrega usuario a la lista
		       case Mensaje.LOGIN:
		    	   messageHistory.append("\n" + msg.getNick() + " ha ingresado al chat.");
		    	   agregarUsuario(msg.getNick());
		    	   break;
		       case Mensaje.MESSAGE:
		    	   //Escribo en el chat el mensaje recibido
		    	   messageHistory.append("\n" + msg.getText());
		    	   break;
		       case Mensaje.LIST:
		    	   btnSend.setEnabled(true);
		    	   userMessage.setEditable(true);
		    	   //Agrego los usuarios conectados a la lista del chat
		    	   Vector<String> vec = new Vector<String>();
		    	   vec=msg.getUsers();
		    	   for(int i=0;i<vec.size();i++)
		    	   {
		    		   agregarUsuario(vec.get(i));
		    	   }
		    	   break;                        
		       case Mensaje.ERROR:
		               clientError(msg.getText());
		               clientStop();
		               break;                                        
		       default: return;
		     }
		}
		
		public boolean isConnected()
		{
			return connected;
		}
	}
}

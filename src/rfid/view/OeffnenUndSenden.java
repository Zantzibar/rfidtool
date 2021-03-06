package rfid.view;

import gnu.io.*;
import rfid.view.ReadXMLFile.Book;

import java.util.ArrayList;
import java.util.Arrays;
//import javax.comm.*; 
import java.util.Enumeration;
import java.io.*;
import java.util.TooManyListenersException;

import javax.swing.*;
import javax.swing.border.Border;

import org.omg.PortableServer.ServantRetentionPolicyOperations;

import java.awt.event.*;
import java.awt.*;

//TODO Dialog zur Konfiguration der Schnittstellenparameter


public class OeffnenUndSenden extends JFrame
{

	/**
	 * Variable declaration
	 */
	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	OutputStream outputStream;
	InputStream inputStream;
	Boolean serialPortGeoeffnet = false;

	int baudrate = 115200;//9600;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	
	String hexString = "[0, 1, 2, 3, dummyTag]";
	int iCount = 0;
	
	byte[] dataToRead;
	
	/**
	 * Fenster
	 */
	JPanel panel = new JPanel (new GridBagLayout());
	
	JPanel panelSetup = new JPanel(new GridBagLayout());
	JPanel panelKommuniziere = new JPanel(new GridBagLayout());
	
	JComboBox auswahl = new JComboBox();
	JButton oeffnen = new JButton("�ffnen");
	JButton schliessen = new JButton("Schlie�en");
	JButton aktualisieren = new JButton("Aktualisieren");
	
	JButton senden = new JButton("Nachricht senden");
	
	JLabel tagCount = new JLabel();
	
	
	JButton register = new JButton("Registrieren");
	
	JTextField nachricht = new JTextField();
	JCheckBox echo = new JCheckBox("Echo");
	
	JTextArea empfangen = new JTextArea();
	JScrollPane empfangenJScrollPane = new JScrollPane();
	
	JTextArea bibTA = new JTextArea();
	JScrollPane bibSP = new JScrollPane();
	
	ArrayList<String> sArrHexTags = new ArrayList<String>();
	
	String personID = "";
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		System.out.println("Programm gestartet");
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				new OeffnenUndSenden();
			}
		});
		System.out.println("Main durchlaufen");
	}
	
	
	ReadXMLFile m_XML;
	
	/**
	 * Konstruktor
	 */
	public OeffnenUndSenden()
	{
		System.out.println("Konstruktor aufgerufen");
		
		m_XML = new ReadXMLFile();
		
		
		initComponents();
	}
	
	protected void finalize()
	{
		System.out.println("Destruktor aufgerufen");
	}
 
	void initComponents()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		
		setTitle("Booklib");
		hexString = "[0, 1, 2, 3, dummyTag]";
		
		addWindowListener(new WindowListener());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		// TODO schliessen.setEnabled(false);
		// TODO senden.setEnabled(false);
		
		oeffnen.addActionListener( new oeffnenActionListener());
		schliessen.addActionListener(new schliessenActionListener());
		aktualisieren.addActionListener(new aktualisierenActionListener());
		senden.addActionListener(new sendenActionListener());
		register.addActionListener(new registerActionListener());
		
		empfangenJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		empfangenJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		empfangenJScrollPane.setViewportView(empfangen);

		bibSP.setViewportView(bibTA);
		
		Border eBorder = BorderFactory.createEtchedBorder();
		panelSetup.setBorder(BorderFactory.createTitledBorder(eBorder, "Connection"));
        
		eBorder = BorderFactory.createEtchedBorder();
        bibSP.setBorder(BorderFactory.createTitledBorder(eBorder, "Bib"));
        
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 0.5;
		constraints.insets = new Insets(5, 5, 5, 5);
		
		
		panelSetup.add(auswahl, constraints);
		
		constraints.gridx = 1;
		constraints.weightx = 0;
		panelSetup.add(oeffnen, constraints);
		
		constraints.gridx = 2;
		panelSetup.add(schliessen, constraints);
		
		constraints.gridx = 3;
		panelSetup.add(aktualisieren, constraints);
		
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		panel.add(panelSetup, constraints);


		eBorder = BorderFactory.createEtchedBorder();
		tagCount.setBorder(BorderFactory.createTitledBorder(eBorder, "Count"));
	
		setTagCounter();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		panelKommuniziere.add(tagCount, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 0;
		panelKommuniziere.add(register, constraints);
/*
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 0;
		panelKommuniziere.add(senden, constraints);
		
		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.weightx = 1;
		panelKommuniziere.add(nachricht, constraints);
		
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.weightx = 0;
		echo.setSelected(true);
		panelKommuniziere.add(echo, constraints);
*/
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		panel.add(panelKommuniziere, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1;
		constraints.weighty = 0.5;
		constraints.fill = GridBagConstraints.BOTH;
		panel.add(empfangenJScrollPane, constraints);
		
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.weighty = 3;
		panel.add(bibSP, constraints);
		
		aktualisiereSerialPort();
		
		add(panel);
		pack();
		setSize(600, 600);
		setLocation(400, 80);
		setVisible(true);

		System.out.println("Fenster erzeugt");
	}
	
	public void setTagCounter()
	{
		String tmp = "<html><body>" + "total: " + Integer.toString(iCount) + "<br>" + 
					 "different: "  + Integer.toString(sArrHexTags.size()) + "</body></html>";
		
		tagCount.setText(tmp);
	}
	
	boolean oeffneSerialPort(String portName)
	{
		//db laden mit usern und buechern
		loadUsersAndBooks();
				
				
		Boolean foundPort = false;
		if (serialPortGeoeffnet != false) 
		{
			System.out.println("Serialport bereits ge�ffnet");
			return false;
		}
		
		System.out.println("�ffne Serialport");
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) 
		{
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) 
			{
				foundPort = true;
				break;
			}
		}
		
		if (foundPort != true) {
			System.out.println("Serialport nicht gefunden: " + portName);
			return false;
		}
		
		try {
			serialPort = (SerialPort) serialPortId.open("�ffnen und Senden", 500);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
		}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf InputStream");
		}
		try {
			serialPort.addEventListener(new serialPortEventListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException f�r Serialport");
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
		}
		
		serialPortGeoeffnet = true;
		
		Thread thread = new Thread()
		{
		    public void run()
		    {
		    	// Einschalten der HF
		    	sendeSerialPort("F00001");
		    	
		    	int i = 0;
		    	
		    	while (true)
		    	{
			    	System.out.println("Thread Running: " + i);
			    	i++;
			    	sendeSerialPort("6c20s");
			    	
			    	try {
			    		sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    }
		};

		
    	thread.start();
    	
		
		
		return true;
	}
	
	private void loadUsersAndBooks() 
	{
		ArrayList<Book> list = m_XML.getBooks();
		
		bibTA.setText("");
		
		for(int i = 0; i < list.size(); i++)
			bibTA.append("Tag: " + list.get(i).Tag + "\n" + "Name: " + list.get(i).Name + "\n\n");
	}

	void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) 
		{
			System.out.println("Schlie�e Serialport");
			
	    	sendeSerialPort("F00000");
			serialPort.close();
			serialPortGeoeffnet = false;
		} 
		else 
		{
			System.out.println("Serialport bereits geschlossen");
		}
	}
	
	void aktualisiereSerialPort()
	{
		
		System.out.println("Akutalisiere Serialport-Liste");
		if (serialPortGeoeffnet != false) 
		{
			System.out.println("Serialport ist ge�ffnet");
			return;
		}
		
		auswahl.removeAllItems();
		enumComm = CommPortIdentifier.getPortIdentifiers();
		
		while(enumComm.hasMoreElements()) 
		{
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
			{
				System.out.println("Found:" + serialPortId.getName());
				auswahl.addItem(serialPortId.getName());
			}
		}
	}
	
	/**
	 * Nachricht wird hier f�r den Scemtec vorbereitet und 
	 * �ber den output stream versendet.
	 * 
	 * @param nachricht
	 */
	byte[] sendeSerialPort(String nachricht)
	{
		//System.out.println("Sende: " + nachricht);
		
		byte[] fullCmd = null;
		
		if (serialPortGeoeffnet != true)
			return null;
		
		try 
		{
			byte[] bArr = nachricht.getBytes();
			fullCmd = Command.calcScemtecFullCmd( bArr );
			
			outputStream.write(fullCmd);
		} 
		catch (IOException e) 
		{
			System.out.println("Fehler beim Senden");
		}
		
		return fullCmd;
	}
	
	/**
	 * schaut nach, ob Daten verf�gbar sind und zeigt diese im
	 * Empfangen-Fenster an.
	 * 
	 * @return void
	 */
	void serialPortDatenVerfuegbar() 
	{
		try 
		{
			byte[] data = new byte[150];
		
			int num = 0;
			String sResponse = "";
			
			// bsp response: 6C210003 6AF62A0F000104E0 73EE2A0F000104E0 C3E22A0F000104E0
			// (response: 3 tags, 3 id�s)
			
			while(inputStream.available() > 0) 
			{
				num = inputStream.read(data, 0, data.length);
				
				sResponse = new String(data, 0, num);
				//System.out.println("Empfange: "+ sResponse);
//				empfangen.append(new String(data, 0, num));
			}
			
			num = 0;
			int idx = sResponse.indexOf("6C20");
			
			if(idx >= 0)
			{
				sResponse = sResponse.substring(idx+4, idx+10);
				num = Integer.parseInt(sResponse);		

				if(num > 0)
				{
					sendeSerialPort("6C21");
					return;
				}
			}
			
			
			// haben wir schon ids
			idx = sResponse.indexOf("6C21");

			if(idx >= 0)
			{
				System.out.println("Empfange: "+ sResponse);
				String stmp = sResponse.substring(idx+4, idx+8);
				num = Integer.parseInt(stmp);
				
				System.out.println("anzahl: " + num);

				String id = "";
				int offset = idx+8;
				for(int i = 0; i < num; i++)
				{
					id = sResponse.substring(offset + 16*i, offset + 16*(i+1));

					if(!sArrHexTags.contains(id))
					{
						sArrHexTags.add(id);
					}
					
					empfangen.setText(id + "\n");			
					empfangen.append(m_XML.getNamebyTag(id));
					
					if(m_XML.IsPerson(id))
					{
						personID = id;
						JOptionPane.showMessageDialog(null, "Person erkannt.", "InfoBox", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						m_XML.addBookToPerson(personID, id);
					}
					
					iCount++;
		
					setTagCounter();	
					
					hexString = id;
				}	
			}			
		} 
		catch (IOException e) 
		{
			System.out.println("Fehler beim Lesen empfangener Daten");
		}
	}
	
	class WindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent event) {
			schliesseSerialPort();
			System.out.println("Fenster wird geschlossen");
		}
	}
	
	class oeffnenActionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			System.out.println("oeffnenActionListener");
			// TODO sperre Button �ffnen und Aktualisieren
			// TODO entsperre Nachricht senden und Schlie�en
			oeffneSerialPort((String) auswahl.getSelectedItem());
		}
	}
	class schliessenActionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			System.out.println("schliessenActionListener");
			// TODO entsperre Button �ffnen und Aktualisieren
			// TODO sperre Nachricht senden und Schlie�en
			schliesseSerialPort();
		}
	}
	
	/**
	 * Aktualisiert serielle Ports
	 * 
	 * @author Patrick
	 */
	class aktualisierenActionListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event) 
		{
			System.out.println("aktualisierenActionListener");
			aktualisiereSerialPort();
		}
	}
	
	
	/**
	 * actionlistener zum versenden der Daten
	 * 
	 * @author Patrick
	 */
	class registerActionListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event) 
		{
			System.out.println("registerActionListener");
			
			JFrame frame = new JFrame("Registrierung");
			String name = JOptionPane.showInputDialog(frame, "Register Tag?", hexString, JOptionPane.QUESTION_MESSAGE);

			if(name != null)
			{
				int ret = JOptionPane.showConfirmDialog(null,
                        "Ja f�r Person.\nNein f�r B�cher.",
                        "Person oder Buch?",
                        JOptionPane.YES_NO_CANCEL_OPTION);
				
				if(ret != 2)
				{	
					m_XML.addNodeToXML(hexString, name, ret);
					loadUsersAndBooks();
				}
			}
		}
	}

	/**
	 * actionlistener zum versenden der Daten
	 * 
	 * @author Patrick
	 */
	class sendenActionListener implements ActionListener 
	{
		public void actionPerformed (ActionEvent event) 
		{
			System.out.println("sendenActionListener");
			
			byte[] sended = sendeSerialPort(nachricht.getText());
			String sSended = Arrays.toString(sended);
			Command.writeToBib(sSended);
			
			if ( echo.isSelected() == true)
			{
				empfangen.append("send: " + sSended + "\n");
			}
		
		}
	}
	
	/**
	 * 
	 */
	class serialPortEventListener implements SerialPortEventListener 
	{
		public void serialEvent(SerialPortEvent event) 
		{
			System.out.println("serialPortEventlistener");
			switch (event.getEventType()) 
			{
				case SerialPortEvent.DATA_AVAILABLE:
					serialPortDatenVerfuegbar();
					break;
				case SerialPortEvent.BI:
				case SerialPortEvent.CD:
				case SerialPortEvent.CTS:
				case SerialPortEvent.DSR:
				case SerialPortEvent.FE:
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				case SerialPortEvent.PE:
				case SerialPortEvent.RI:
				default:
			}
		}
	}	
}

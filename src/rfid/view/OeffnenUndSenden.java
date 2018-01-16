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

	int baudrate = 9600;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	
	/**
	 * Fenster
	 */
	JPanel panel = new JPanel (new GridBagLayout());
	
	JPanel panelSetup = new JPanel(new GridBagLayout());
	JPanel panelKommuniziere = new JPanel(new GridBagLayout());
	
	JComboBox auswahl = new JComboBox();
	JButton oeffnen = new JButton("Öffnen");
	JButton schliessen = new JButton("Schließen");
	JButton aktualisieren = new JButton("Aktualisieren");
	
	JButton senden = new JButton("Nachricht senden");
	JTextField nachricht = new JTextField();
	JCheckBox echo = new JCheckBox("Echo");
	
	JTextArea empfangen = new JTextArea();
	JScrollPane empfangenJScrollPane = new JScrollPane();
	
	JTextArea bibTA = new JTextArea();
	JScrollPane bibSP = new JScrollPane();
	
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
		addWindowListener(new WindowListener());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		// TODO schliessen.setEnabled(false);
		// TODO senden.setEnabled(false);
		
		oeffnen.addActionListener( new oeffnenActionListener());
		schliessen.addActionListener(new schliessenActionListener());
		aktualisieren.addActionListener(new aktualisierenActionListener());
		senden.addActionListener(new sendenActionListener());
		
		empfangenJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		empfangenJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		empfangenJScrollPane.setViewportView(empfangen);

		bibSP.setViewportView(bibTA);
		
		Border eBorder = BorderFactory.createEtchedBorder();
        panel.setBorder(BorderFactory.createTitledBorder(eBorder, "Connection"));
        
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

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		panelKommuniziere.add(senden, constraints);
		
		constraints.gridx = 1;
		constraints.weightx = 1;
		panelKommuniziere.add(nachricht, constraints);
		
		constraints.gridx = 2;
		constraints.weightx = 0;
		echo.setSelected(true);
		panelKommuniziere.add(echo, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		panel.add(panelKommuniziere, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		panel.add(empfangenJScrollPane, constraints);
		
		
		constraints.gridx = 0;
		constraints.gridy = 3;
		panel.add(bibSP, constraints);
		
		aktualisiereSerialPort();
		
		add(panel);
		pack();
		setSize(600, 400);
		setVisible(true);

		System.out.println("Fenster erzeugt");
	}
	
	boolean oeffneSerialPort(String portName)
	{
		//db laden mit usern und buechern
		loadUsersAndBooks();
				
				
		Boolean foundPort = false;
		if (serialPortGeoeffnet != false) 
		{
			System.out.println("Serialport bereits geöffnet");
			return false;
		}
		
		System.out.println("Öffne Serialport");
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
			serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
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
			System.out.println("TooManyListenersException für Serialport");
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
		}
		
		serialPortGeoeffnet = true;
		
		
		
		return true;
	}
	
	private void loadUsersAndBooks() 
	{
		ArrayList<Book> list = m_XML.getBooks();
		
		
		for(int i = 0; i < list.size(); i++)
			bibTA.append("Tag: " + list.get(i).Tag + "\n" + "Name: " + list.get(i).Name + "\n\n");
	}

	void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) 
		{
			System.out.println("Schließe Serialport");
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
			System.out.println("Serialport ist geöffnet");
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
	 * Nachricht wird hier für den Scemtec vorbereitet und 
	 * über den output stream versendet.
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
	 * schaut nach, ob Daten verfügbar sind und zeigt diese im
	 * Empfangen-Fenster an.
	 * 
	 * @return void
	 */
	void serialPortDatenVerfuegbar() 
	{
		try 
		{
			byte[] data = new byte[150];
			int num;
		
			while(inputStream.available() > 0) 
			{
				num = inputStream.read(data, 0, data.length);
				System.out.println("Empfange: "+ new String(data, 0, num));
				empfangen.append(new String(data, 0, num));
			}
			
			String hexString = Command.cmdToHexString(data); 
			empfangen.setText(hexString);
			empfangen.append(m_XML.getNamebyTag(hexString));

			//byte[] fullCmd = Command.calcScemtecFullCmd( bArr );
			
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
			// TODO sperre Button Öffnen und Aktualisieren
			// TODO entsperre Nachricht senden und Schließen
			oeffneSerialPort((String) auswahl.getSelectedItem());
		}
	}
	class schliessenActionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			System.out.println("schliessenActionListener");
			// TODO entsperre Button Öffnen und Aktualisieren
			// TODO sperre Nachricht senden und Schließen
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
	class serialPortEventListener implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent event) {
			System.out.println("serialPortEventlistener");
			switch (event.getEventType()) {
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

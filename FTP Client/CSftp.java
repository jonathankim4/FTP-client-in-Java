import java.lang.System;
import java.util.Properties;
import java.util.Scanner;

import java.net.*;
import java.util.*;
import java.io.*;
//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp
{
	//CONSTANT Variables
    static final int MAX_LEN = 255;
    static final int ARG_CNT = 2;
    static final boolean DEBUG = false;
    static final int DEFPORT = 21;
    static final String PROMPT = "csftp>";
	static final String ON = " Connection good";
	static final String OFF = " No connection";
	static final String NON_NUM_PORT = "non-int port";
	
	//Global static variables
	static Scanner scan;
	static CSftp ftp;
	static CSftp.Connection aConnection;
	static String currentDirectory; // working directory of the connected server
	static boolean loggedIn = false;
	
	public static void main(String[] args) {
		
		byte cmdString[] = new byte[MAX_LEN];
		boolean isQuit = false;					//Boolean for looping ftp
		scan = new Scanner(System.in);			//User input
		ftp = new CSftp();						
		aConnection = ftp.new Connection();		//Create the object inner-class
		currentDirectory = "";					//Current working directory
		
		if (args.length != ARG_CNT) {
		    System.out.print("Usage: cmd ServerAddress ServerPort\n");
		    return;
		}
		else
		{
			//Connect to the server
			int port = DEFPORT;
			int arg_num = args.length - 1;
			if (arg_num == 2) {
				String port_s;
				if (!isNum(port_s = args[1])) {
					System.out.println(getError("999") + NON_NUM_PORT);
				}
				port = Integer.parseInt(port_s);
			}
			aConnection.beginConnection(args[0], port);
		}

		String input = null;
		System.out.print(PROMPT);
		
		//Ask for user input and catch the incorrect values
		try {
			input = scan.nextLine();
		} catch (Exception e) {
			System.out.println(getError("998"));
			System.exit(0);
		}

		while (isQuit == false) {
			//Check if the connection is connected.
			boolean on = (aConnection.isConnected()) && (!aConnection.isClosed());

			if (!input.isEmpty()) {

				String[] fullcmd = input.split(" ");
				String command = fullcmd[0].toLowerCase().trim();
				int arg_num = fullcmd.length - 1;

				switch (command) {
				//Quit command
				
				case "quit":
					isQuit = true;
					break;
				//User command
				case "user":
					if (!on) {
						System.out.println(getError("999") + OFF);
					} else if (arg_num != 1) {
						System.out.println(getError("901") + getError(command));
					} else {
						sendUsr(fullcmd[1]);
					}
					break;
				//getFile command
				case "get":
					if (!on) {
						System.out.println(getError("999") + OFF);
					} else if (arg_num != 1) {
						System.out.println(getError("901") + command);
					} else if (!loggedIn) {
						System.out.println(getError("999") + " (not logged in)");
					}
					getFile(fullcmd[1]);
					break;
			    //Change directory
				case "cd":
					if (!on) {
						System.out.println(getError("999") + OFF);
					} else if (!loggedIn) {
						System.out.println(getError("999") + " (not logged in)");
					} else if (arg_num < 1) {
						System.out.println(getError("901") + command);
					} else {
						changeDirectory(fullcmd[1]);
					}
					break;
				//List all files in directory
				case "dir":
					if (!on) {
						System.out.println(getError("999") + OFF);
					} else if (!loggedIn) {
						System.out.println(getError("999") + " (not logged in)");
					} else {
						listDir();
					}
					break;
				default:
					System.out.println(getError("900"));
					break;
				}
			}

			if (isQuit) {
				if (on) {
					aConnection.close();
				}
				break;
			}
			
			System.out.print(PROMPT);
			try {
				input = scan.nextLine();
			} catch (Exception e) {
				System.out.println(getError("998"));
				System.exit(0);
			}
		}
	}

	/*
	 * Name: isNum
	 * Parameters: string to be checked
	 * 
	 * Description: Checks a string if it is a number or not
	 * Output: True if str is a number. False if it is not a number
	 */
	private static boolean isNum(String str) {
		return str.matches("[-=]?\\d*\\.?\\d+");
	}

	/*
	 * Name: sendUsr
	 * Parameters: String usr - desired username to connect with
	 * Description: Attempts to connect to the ftp server using provided user name
	 * 
	 * Output: If username is accepted, a prompt for the password will be passed from the server
	 *  and we pass that to the sendPass function
	 */
	public static void sendUsr(String usr)
	{
		boolean noPrompt = false;
		aConnection.send("USER " + usr);
		while (!noPrompt) {
			String prefix = "";
			prefix = aConnection.read();	
			if (prefix.startsWith("331 ") || prefix.startsWith("530 Please ")) {
				
				System.out.println(prefix);
				
				System.out.print("PASSWORD: ");
				sendPw(scan.nextLine());
				noPrompt = true;
			}
			if (prefix.startsWith("530 Can't") || prefix.startsWith("500 ")) {
				System.out.println(getError("999") + " user set");
				noPrompt = true;
			}
			if (prefix.startsWith("530 This ")) {
				System.out.println(getError("999") + " login as anonymous user");
				noPrompt = true;
			}
		}
	}

	/*
	 * Name: sendPw
	 * Parameters: String pw - desired password to send to the server
	 * Description: Sends the password to the ftp server then waits to login
	 * 
	 * Output: If password is accepted, the client will be connected to the server
	 */
	public static void sendPw(String pw)
	{
		aConnection.send("PASS " + pw);
		while (true) {
			String prefix = "";
			prefix = aConnection.read();
			if (prefix.startsWith("230 ")) {
				loggedIn = true;
				System.out.println("Logged in.");
				break;
			}
			if (prefix.startsWith("530 Auth")) {
				System.out.println(getError("999") + " login failed");
				break;
			}
			if (prefix.startsWith("503 Login ")) {
				System.out.println(getError("999") + " login incorrect, try as anonymous");
				break;
			}
		}
	}
	
	/*
	 * Name: changeDirectory
	 * Parameters: String directory - desired directory to change to
	 * Description: Changes the current working directory
	 * 
	 * Output: If successful the directory will change.
	 */
	public static void changeDirectory(String directory)
	{
		aConnection.send("CWD " + directory); 			//Send CD command and desired directory
		boolean waitingForResponse = true;
		
		String response = "";
		
		while (waitingForResponse)
		{
			response = aConnection.read();				//look at response from server
			
			if (response.startsWith("250 "))			//success in changing directories
			{
				System.out.println("Directory changed to " + directory);
				currentDirectory = directory;
				waitingForResponse = false;
			}
			if (response.startsWith("530 "))				// ? just to catchthe error
			{
				System.out.println(getError("999") + " ");
				waitingForResponse = false;
			}
			if (response.startsWith("550 ")) {				//Directory not found
				System.out.println("Directory not found");
				waitingForResponse = false;
			}
			
		}
		
	}
	/*
	 * Name: listDir
	 * Parameters: none
	 * Description: Lists files in the current directory
	 * 
	 * Output: list of all files and folders in directory
	 */
	public static void listDir() {
		InputStream output;
		
		aConnection.send("TYPE I"); 			//Set to binary mode
		String response = aConnection.read();	//Get response

		if (response.startsWith("200 ")) {		//Check if in Binary mode
			aConnection.send("PASV");			//Go to passive mode
			String nextResponse = aConnection.read();
			if (nextResponse.startsWith("227 ")) {
				Socket socket = null;
				String host = null;
				int port = 0;
				try {
					String[] portparts = parseAddr(nextResponse);
					String p1 = portparts[4];
					String p2 = portparts[5];
					int newport = (Integer.parseInt(p1) * 256) + Integer.parseInt(p2);

					String[] newhostpart = parseAddr(nextResponse); 		//put partial ip into an array
					String newhost = newhostpart[0] + "." + newhostpart[1] + "."	
							+ newhostpart[2] + "." + newhostpart[3];			//Create full ip
					socket = new Socket(newhost, newport);				//open another socket connection
					
					aConnection.send("LIST");
					
					output = socket.getInputStream();			//Get result from command LIST
					byte[] buf = new byte[4096];				
					output.read(buf);							//but the read bytes into a buffer
					
					String buffer = new String(buf);
					System.out.println(buffer);					//print the whole butffer
					
				}catch (IOException e) {
					System.out.println(getError("930") +
							" to " + host + " on port " + port  + " failed to open.");
				}
			}
		}

	}
	
	
	// getFile(string file) trys to move the file from the local client to the server
		// parameters: the filename String
		// output: file will be copied to remote location
		public static void getFile(String file)
		{
			aConnection.send("TYPE I");
			String response = aConnection.read();
			if (response.startsWith("200 ")) {
				aConnection.send("PASV");
				String nextres = aConnection.read();
				if (nextres.startsWith("227 ")) {
					Socket socket = null;
					String host = null;
					int port = 0;
					try {					
						
						String[] portparts = parseAddr(nextres);
						String p1 = portparts[4];
						String p2 = portparts[5];
						int newport = (Integer.parseInt(p1) * 256) + Integer.parseInt(p2);
						
						String[] newhostpart = parseAddr(nextres);
						String newhost = newhostpart[0] + "." + newhostpart[1] + "." +
								      newhostpart[2] + "." + newhostpart[3];
						host = newhost;
						port = newport;
						
						socket = new Socket(newhost, newport);

						if (DEBUG) {
							System.out.println("grabbing following file: " + file +
								" to " + newhost + ":" + newport);
						}

						aConnection.send("RETR " + file);

						String retres = aConnection.read();
						System.out.println(retres);
						if (retres.startsWith("150 ")) {
							int bytesRead;
							int current = 0;
							byte[] buf = new byte[4096];
							InputStream is = socket.getInputStream();
							FileOutputStream out = new FileOutputStream(file);
							BufferedOutputStream bos = new BufferedOutputStream(out);
							bytesRead = is.read(buf, 0, buf.length);
					        current = bytesRead;
					        do {
					            bytesRead = is.read(buf, current,
					                    (buf.length - current));
					            if (bytesRead >= 0)
					                current += bytesRead;
					        } while (bytesRead > -1);
							
							is.close();
							bos.write(buf, 0, current);
							bos.flush();
							bos.close();
							socket.close();
							System.out.println(aConnection.read());
						}
						else if (retres.startsWith("426 ")) {
						}
						else if (retres.startsWith("451 ") || retres.startsWith("551 ")) {
							System.out.println(getError("910") + " " + file + " cannot be read.");
						}
						else if (retres.startsWith("550 ")) {
							System.out.println(getError("999") + " failed to open.");
						}
					} catch (IOException e) {
						System.out.println(getError("930") +
								" to " + host + " on port " + port  + " failed to open.");
					} catch (Exception e) {
						System.out.println(getError("999") + " get file");
					}
				} else {
					System.out.println(getError("999") + " PASV failed");
				}
			} else {
				System.out.println(getError("999") + " transfer failed");
			}
		}
	/**
	 * Name: parseAddr
	 * Parameters: String response - address you want to parse
	 * Description: Takes the address and strips the parenthesis and splits among ,
	 * 
	 * Output: Returns a string array with the split up bits of the ip
	 **/
	public static String[] parseAddr(String response)
	{
		String address = response.substring(response.indexOf("(") + 1, response.indexOf(")"));
		String[] addressprt = address.split(",");
		return addressprt;
	}
    

    /**
     * START OF SUB-CLASS
     * Name: Connection
     * Methods: getAddr
     * 			isConnected
     * 			isClosed
     * 			beginConnection
     * 			openServer
     * 			close
     *			read
     *			send
     *
     */
    private class Connection
    {
    	private PrintWriter out; // need to write to server
    	private BufferedReader in; // reading inputs from the server
    	private Socket socket; // the actual file directory we are currently connected to
    	private int portnum; // port number 
    	private String ftpaddress; // ftp address

		public Connection()
		{
			socket = new Socket();
		}
		
		public String getAddr()
		{
			return this.ftpaddress;
		}
		
		public boolean isConnected()
		{
			return socket.isConnected();
		}

		public boolean isClosed()
		{
			return socket.isClosed();
		}
	
		
		public void beginConnection(String address, int port){
			this.ftpaddress = address;
			this.portnum = port;
			openServer();
		}
		
		private void openServer(){
			try{
				socket = new Socket(ftpaddress, portnum);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				System.out.println("Connection to " + socket.getRemoteSocketAddress());
			} catch(IOException e) {
				System.out.println(getError("920") + " to " + ftpaddress + " on port " + portnum  + " failed to open.");
			} catch(Exception e) {
				System.out.println(getError("999") + " (Connection.open())");
			}
		}
    
	    public void close()
		{
			try {
				socket.close();
				in.close();
				out.close();
				System.out.println("Connection closed to " + ftpaddress + ":" + portnum);
			} catch (Exception e) {
				System.out.println(getError("999") + " Connection closed");
			}
		}
    
	    public String read()
		{
			String line = "";
			try {
				line = in.readLine();
			} catch (Exception e) {
				System.out.println(getError("925"));
				this.close();
			}
			if (DEBUG) {
				if (!line.startsWith("220 ")) {
					System.out.println(line);
				}
			}
			return line;
		}
	    
	    public void send(String cmd)
		{
			try {
				if (DEBUG) {
					System.out.println("trying " + cmd);
				}
				System.out.print(">> " + cmd + "\r\n");
				out.write(cmd + "\r\n");
				out.flush();
			} catch (NullPointerException e) {
				System.out.println(getError("903") + OFF);
			} catch (Exception e) {
				System.out.println(getError("925"));
				this.close();
			}
		}
    }
		
    
		public static String getError(String msg)
		{
			switch (msg) {
				case "900":
					return "900 Invalid Command.";
				case "901":
					return "901 Incorrect number of arguments.";
				case "910":
					return "910 Access to local file";
				case "920":
					return "920 Control connection";
				case "930":
					return "930 Data Transfer connection";
				case "925":
					return "925 Control connection I/O error, closing control connection.";
				case "935":
					return "935 Data transfer connection I/O error, closing data connection.";
				case "998":
					return "998 Input error while reading commands, terminating.";
				default:
					return "999 Processing error.";
			}
		}
    
}
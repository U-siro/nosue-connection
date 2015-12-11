package theomynomy.nosueconnection;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class NosueConnection {
	private File hostsFile = new File("C:\\Windows\\System32\\drivers\\etc\\hosts");

	private JFrame frame;

	private JTextArea txtAreaHosts;
	private JButton btnConnect, btnRestore, btnLaunch;
	private JLabel lblService, lblUpdate;

	private boolean isConnected;

	private final String VERSION = "v11122015";
	private String LATEST_VERSION, CURRENT_FILENAME, OSU_DIRECTORY;

	public NosueConnection() throws IOException {
		String[] var1 = NosueConnection.class.getProtectionDomain().getCodeSource().getLocation().toString().split("/");
		int var2 = var1.length;
		CURRENT_FILENAME = var1[var2 - 1];

		OSU_DIRECTORY = getOsuInstallationDirectory();

		frame = new JFrame("Nosue! Connection");
		frame.setSize(450, 300);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(0);
		frame.getContentPane().setLayout(null);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				quit();
			}
		});

		JLabel lblAuthor = new JLabel("Developed by TheOmyNomy");
		lblAuthor.setBounds(298, 247, 136, 14);
		frame.getContentPane().add(lblAuthor);

		JLabel lblVersion = new JLabel(VERSION);
		lblVersion.setBounds(380, 231, 54, 14);
		frame.getContentPane().add(lblVersion);

		txtAreaHosts = new JTextArea();
		txtAreaHosts.setLineWrap(true);
		txtAreaHosts.setWrapStyleWord(true);
		txtAreaHosts.setFont(new Font("Tahoma", 0, 11));
		txtAreaHosts.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(txtAreaHosts);
		scrollPane.setBounds(10, 11, 424, 191);
		frame.getContentPane().add(scrollPane);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(actionListener());
		btnConnect.setBounds(10, 243, 85, 23);
		frame.getContentPane().add(btnConnect);

		btnRestore = new JButton("Restore");
		btnRestore.addActionListener(actionListener());
		btnRestore.setBounds(105, 243, 85, 23);
		frame.getContentPane().add(btnRestore);

		btnLaunch = new JButton("Launch");
		btnLaunch.addActionListener(actionListener());
		btnLaunch.setBounds(200, 243, 85, 23);
		frame.getContentPane().add(btnLaunch);

		JLabel lblCurrentService = new JLabel("Current Service: ");
		lblCurrentService.setFont(new Font("Tahoma", 0, 11));
		lblCurrentService.setBounds(10, 208, 85, 14);
		frame.getContentPane().add(lblCurrentService);

		lblService = new JLabel("osu!");
		lblService.setFont(new Font("Tahoma", 1, 11));
		lblService.setBounds(95, 208, 46, 14);
		frame.getContentPane().add(lblService);

		lblUpdate = new JLabel("Checking for updates...");
		lblUpdate.setBounds(10, 223, 129, 14);
		frame.getContentPane().add(lblUpdate);

		frame.setVisible(true);

		refresh();

		File _update = new File(System.getProperty("user.home") + "\\_update.txt");
		if (_update.exists()) {
			BufferedReader br = new BufferedReader(new FileReader(_update));
			String[] contents = br.readLine().split(":");
			br.close();
			new File(contents[0]).delete();
			_update.delete();
			JOptionPane.showMessageDialog(null, "Successfully updated from " + contents[1] + " to " + VERSION, "Updater", 1);
		}

		update();
	}

	private ActionListener actionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					if (event.getSource() == btnConnect) {
						if (isConnected) {
							remove();
						} else {
							add();
						}
					}
					if (event.getSource() == btnRestore) {
						restore();
					}
					if (event.getSource() == btnLaunch) {
						launch();
					}

					refresh();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void launch() {
		try {
			Runtime.getRuntime().exec(OSU_DIRECTORY + "\\osu!.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void restart() {
		try {
			Runtime.getRuntime().exec("java -jar nosue_connection_" + LATEST_VERSION + ".exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private void quit() {
		if (isConnected) {
			int option = JOptionPane.showConfirmDialog(frame, "You are still connected to nosue! services.\r\nContinue? (You won't be able to access osu!)", "Quit?", 0, 2);
			if (option == 0) {
				System.exit(0);
			}
		} else {
			System.exit(0);
		}
	}

	private void restore() throws IOException {
		int option = JOptionPane.showConfirmDialog(frame, "You'll get a fresh factory state hosts file.\r\nContinue? (All current changes will be lost)", "Restore?", 0, 1);
		if (option == 0) {
			lblService.setText("osu!");
			btnConnect.setText("Connect");
			isConnected = false;

			ArrayList<String> contents = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://pastebin.com/raw.php?i=S2LbjRWt").openStream()));
			String temp;

			while ((temp = br.readLine()) != null) {
				contents.add(temp);
			}
			br.close();

			hostsFile.delete();
			hostsFile.createNewFile();

			PrintWriter pw = new PrintWriter(new FileWriter(hostsFile, true));
			for (int i = 0; i < contents.size(); i++) {
				if (i == contents.size() - 1) {
					pw.write((String) contents.get(i));
				} else {
					pw.write((String) contents.get(i) + "\r\n");
				}
			}
			pw.flush();
			pw.close();
		}
	}

	private void update() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://pastebin.com/raw.php?i=g7fhPEkd").openStream()));
		String link = null;
		String temp;

		while ((temp = br.readLine()) != null) {
			String[] split = temp.split(":");

			if (split[0].equalsIgnoreCase("version")) {
				LATEST_VERSION = split[1];
			}
			if (split[0].equalsIgnoreCase("download")) {
				link = split[1] + ":" + split[2];
			}
		}
		br.close();

		if (!LATEST_VERSION.isEmpty() && LATEST_VERSION != null && !VERSION.equalsIgnoreCase(LATEST_VERSION)) {
			lblUpdate.setText("Downloading...");

			File file = new File("nosue_connection_" + LATEST_VERSION + ".exe");
			if (file.exists()) {
				file.delete();
			}

			FileOutputStream fos = new FileOutputStream("nosue_connection_" + LATEST_VERSION + ".exe");
			fos.getChannel().transferFrom(Channels.newChannel(new URL(link).openStream()), 0L, Long.MAX_VALUE);
			fos.close();

			File _update = new File(System.getProperty("user.home") + "\\_update.txt");
			if (_update.exists()) {
				_update.delete();
			}
			_update.createNewFile();

			PrintWriter pw = new PrintWriter(new FileWriter(_update));
			pw.write(CURRENT_FILENAME + ":" + VERSION);
			pw.flush();
			pw.close();

			lblUpdate.setText("Download complete.");

			restart();

		} else {
			lblUpdate.setText("No updates are available.");
		}
	}

	private void remove() throws IOException {
		ArrayList<String> contents = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(hostsFile));
		String temp;
		while ((temp = br.readLine()) != null) {
			if (!temp.contains("178.62.57.37")) {
				contents.add(temp);
			}
		}
		br.close();

		hostsFile.delete();
		hostsFile.createNewFile();

		PrintWriter pw = new PrintWriter(new FileWriter(hostsFile, true));
		for (int i = 0; i < contents.size(); i++) {
			if (i == contents.size() - 1) {
				pw.write((String) contents.get(i));
			} else {
				pw.write((String) contents.get(i) + "\r\n");
			}
		}
		pw.flush();
		pw.close();

		isConnected = false;
		btnConnect.setText("Connect");
		lblService.setText("osu!");
	}

	private void add() throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(hostsFile, true));
		pw.write("\r\n178.62.57.37 osu.ppy.sh");
		pw.write("\r\n178.62.57.37 a.ppy.sh");
		pw.flush();
		pw.close();

		isConnected = true;
		btnConnect.setText("Disconnect");
		lblService.setText("nosue!");
	}

	private void refresh() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(hostsFile));
		String contents;
		txtAreaHosts.setText("");

		while ((contents = br.readLine()) != null) {
			txtAreaHosts.setText(txtAreaHosts.getText() + contents + "\r\n");

			if (contents.contains("178.62.57.37")) {
				isConnected = true;
				btnConnect.setText("Disconnect");
				lblService.setText("nosue!");
			}
		}
		br.close();
		txtAreaHosts.setCaretPosition(0);
	}

	// Credit to itdelatrisu (https://github.com/itdelatrisu)
	private String getOsuInstallationDirectory() {
		if (!System.getProperty("os.name").startsWith("Win"))
			return null;
		String value;
		try {
			value = Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, "osu\\DefaultIcon", null);
		} catch (Win32Exception e) {
			return null;
		}
		Matcher m = Pattern.compile("\"(.+)\\\\[^\\/]+\\.exe\"").matcher(value);
		return (m.find()) ? m.group(1) : null;
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new NosueConnection();
	}
}

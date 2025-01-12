package bot;

import bot.debugger.Debugger;
import callbacks.DrawCallback;

import java.awt.*;

import compatibility.sbot.Script;
import controller.Controller;
import orsc.OpenRSC;
import orsc.mudclient;
import reflector.Reflector;
import scripting.idlescript.IdleScript;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import listeners.LoginListener;
import listeners.WindowListener;



/**
 * This is the starting class of the entire IdleRSC project.
 *
 * @author Dvorak
 */
public class Main {
    public static Config config;

    private static boolean isRunning = false; // this is tied to the start/stop button on the side panel.
    private static JFrame botFrame, consoleFrame, rscFrame, scriptFrame; //all the windows.
    private static JButton startStopButton, loadScriptButton, pathwalkerButton, openDebuggerButton, hideButton, resetXpButton; //all the buttons on the sidepanel.
    private static JCheckBox autoLoginCheckbox, logWindowCheckbox, unstickCheckbox, debugCheckbox, graphicsCheckbox, autoscrollLogsCheckbox; //all the checkboxes on the sidepanel.


    private static JTextArea logArea; //self explanatory
    private static JScrollPane scroller; //this is the main window for the log.

    private static Debugger debugger = null;

    private static Thread loginListener = null; //see LoginListener.java
    private static Thread positionListener = null; //see PositionListener.java
    private static Thread windowListener = null; //see WindowListener.java
    private static Thread messageListener = null; //see MessageListener.java
    private static Thread debuggerThread = null;

    private static Controller controller = null; //this is the queen bee that controls the actual bot and is the native scripting language.
    private static Object currentRunningScript = null; //the object instance of the current running script.

    private static boolean shouldFilter = true;

    private final static String nativeScriptPath = "bin/scripting/idlescript";
    private final static String sbotScriptPath = "bin/scripting/sbot";
    private final static String aposScriptPath = "bin/scripting/apos";
    
    private static boolean aposInitCalled = false;

    /**
     * Used by the WindowListener for tracking the log window.
     *
     * @return whether or not the log window is open.
     */
    public static boolean isLogWindowOpen() {
        return logWindowCheckbox.isSelected();
    }

    /**
     * Returns whether or not the bot side pane is set to sticky mode.
     * @return boolean with whether or not the sidepanel is sticky.
     */
    public static boolean isSticky() {
        return !unstickCheckbox.isSelected();
    }

    /**
     * Returns whether or not a script is running.
     * 
     * @return boolean with whether or not a script is running.
     */
    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns whether or not auto-login is enabled.
     * 
     * @return boolean with whether or not autologin is enabled.
     */
    public static boolean isAutoLogin() {
        return autoLoginCheckbox.isSelected();
    }

    /**
     * Returns whether or not debug is enabled.
     * 
     * @return boolean with whether or not debug is enabled.
     */
    public static boolean isDebug() {
        return debugCheckbox.isSelected();
    }

    /**
     * A function for controlling whether or not scripts are running.
     *
     * @param b
     * @return void
     */
    public static void setRunning(boolean b) {
        isRunning = b;

        if (isRunning == true) {
            startStopButton.setText("Stop");
        } else {
            startStopButton.setText("Start");
        }
    }

    /**
     * A function for controlling the autologin functionality.
     *
     * @param b
     * @return void
     */
    public static void setAutoLogin(boolean b) {
        autoLoginCheckbox.setSelected(b);
    }

    /**
     * A function which returns the current running IdleScript/Script instance.
     *
     * @return Object (which is an instanceof IdleScript or Script)
     */
    public static Object getCurrentRunningScript() {
        return currentRunningScript;
    }

    /**
     * The initial program entrypoint for IdleRSC.
     */
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InterruptedException {
    	handleCache();
    	config = new Config(args);
    	
        Reflector reflector = new Reflector(); //start up our reflector helper
        OpenRSC client = reflector.createClient(); //start up our client jar
        

        mudclient mud = reflector.getMud(client); //grab the mud from the client 
        controller = new Controller(reflector, client, mud); //start up our controller
        debugger = new Debugger(reflector, client, mud, controller);
        debuggerThread = new Thread(debugger);
        debuggerThread.start();

        //just building out the windows
        botFrame = new JFrame("Bot Pane");
        consoleFrame = new JFrame("Bot Console");
        rscFrame = (JFrame) reflector.getClassMember("orsc.OpenRSC", "jframe");
        scriptFrame = new JFrame("Script Selector");

        initializeBotFrame(botFrame);
        initializeConsoleFrame(consoleFrame);
        initializeScriptFrame(scriptFrame);

        if (config.getHidesidepanel())
        {
            botFrame.setVisible(false);
        }

        log("IdleRSC initialized.");

        //don't do anything until RSC is loaded.
        while (!controller.isLoaded()) controller.sleep(1);

        if (autoLoginCheckbox.isSelected() != config.getAutologin()) {
            autoLoginCheckbox.doClick();
        }
        if (logWindowCheckbox.isSelected() != config.getLogwindow()) {
            logWindowCheckbox.doClick();
        }
        if (unstickCheckbox.isSelected() != config.getUnstick()) {
            unstickCheckbox.doClick();
        }
        if (debugCheckbox.isSelected() != config.getDebug()) {
            debugCheckbox.doClick();
        }
        if (graphicsCheckbox.isSelected() != config.getEnablegfx()) {
            graphicsCheckbox.doClick();
        }

        if (!config.getScriptName().equals("")) {
            if(!loadAndRunScript(config.getScriptName())) {
                System.out.println("Could not find script: " + config.getScriptName());
                System.exit(1);
            }
            isRunning = true;
            startStopButton.setText("Stop");
        }

        //start up our listener threads
        log("Initializing LoginListener...");
        loginListener = new Thread(new LoginListener(controller));
        loginListener.start();
        log("LoginListener initialized.");

        log("Initializing WindowListener...");
        windowListener = new Thread(new WindowListener(botFrame, consoleFrame, rscFrame, scroller, logArea, controller));
        windowListener.start();
        log("WindowListener started.");


        //give everything a nice synchronization break juuuuuuuuuuuuuust in case...
        Thread.sleep(3000);

        while (true) {
            if (isRunning()) {
                if (currentRunningScript != null) {

                    //handle native scripts
                    if (currentRunningScript instanceof IdleScript) {
                        ((IdleScript) currentRunningScript).setController(controller);
                        int sleepAmount = ((IdleScript) currentRunningScript).start(config.getScriptArguments()) + 1;
                        Thread.sleep(sleepAmount);
                    } else if (currentRunningScript instanceof compatibility.sbot.Script) {
                        controller.displayMessage("@red@IdleRSC: Note that SBot scripts are mostly, but not fully compatible.", 3);
                        controller.displayMessage("@red@IdleRSC: If you still experience problems after modifying script please report.", 3);
                        ((compatibility.sbot.Script) currentRunningScript).setController(controller);
                        
                        String sbotScriptName = config.getScriptName();
                        ((compatibility.sbot.Script) currentRunningScript).start(sbotScriptName, config.getScriptArguments());
                        
                        Thread.sleep(618); //wait 1 tick before performing next action
                    } else if(currentRunningScript instanceof compatibility.apos.Script) {
                    	if(!controller.isSleeping()) { 
                    		String params = "";
                    		
                    		if(config.getScriptArguments() != null) {
                    			for(int i = 0; i < config.getScriptArguments().length; i++) {
                    				String arg = config.getScriptArguments()[i];
                    				if(i == 0) {
                    					params = arg;
                    				} else {
                    					params += " " + arg;
                    				}
                    			}
                    		}
	                    	
	                    	if(!aposInitCalled) {
	                    		((compatibility.apos.Script) currentRunningScript).setController(controller);
	                    		((compatibility.apos.Script) currentRunningScript).init(params);
	                    		aposInitCalled = true;
	                    	}
	                    	
	                    	int sleepAmount = ((compatibility.apos.Script) currentRunningScript).main() + 1;
	                    	Thread.sleep(sleepAmount);
                    	} else {
                    		Thread.sleep(10);
                    	}
                    }
                }

            } else {
            	aposInitCalled = false;
            	Thread.sleep(100);
            }
        }
    }

    /**
     * Clears the log window.
     */
    public static void clearLog() {
        logArea.setText("");
    }

    /**
     * Add a line to the log window.
     *
     * @param text
     */
    public static void log(String text) {
        System.out.println(text);
        if (logArea == null) {
            return;
        }

        logArea.append (text + "\n");

        if (autoscrollLogsCheckbox.isSelected()) {
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    /**
     * For logging function calls in an easy manner.
     *
     * @param method -- the method called.
     * @param params -- the object(s) which were sent to the function. You may put in any object.
     */
    public static void logMethod(String method, Object... params) {
        if (isDebug()) {
            String current = method + "(";

            if (params != null && params.length > 0) {
                for (Object o : params) {
                    current += o.toString() + ", ";
                }

                current = current.substring(0, current.length() - 2);
            }

            current += ")";

            log(current);
        }
    }

    /**
     * Sets up the sidepanel
     *
     * @param botFrame -- the sidepanel frame
     */
    private static void initializeBotFrame(JFrame botFrame) {
        botFrame.setLayout(new BoxLayout(botFrame.getContentPane(), BoxLayout.Y_AXIS));

        startStopButton = new JButton(isRunning ? "Stop" : "Start");
        loadScriptButton = new JButton("Load Script");
        pathwalkerButton = new JButton("PathWalker");

        autoLoginCheckbox = new JCheckBox("Auto-Login");
        logWindowCheckbox = new JCheckBox("Log Window");
        unstickCheckbox = new JCheckBox("Unstick");
        debugCheckbox = new JCheckBox("Debug");
        graphicsCheckbox = new JCheckBox("Graphics");

        openDebuggerButton = new JButton("Open Debugger");
        hideButton = new JButton("Hide Sidepane");
        resetXpButton = new JButton("Reset XP");

        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = !isRunning;

                if (isRunning) {
                    startStopButton.setText("Stop");
                } else {
                    startStopButton.setText("Start");
                }
            }
        });

        loadScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoadScript();
            }
        });
        
        pathwalkerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning) {
                	loadAndRunScript("PathWalker");
                	config.setScriptArguments(new String[]{""});
                	isRunning = true;
                    startStopButton.setText("Stop");
                } else {
                    JOptionPane.showMessageDialog(null, "Stop the current script first.");
                }
            }
        });


        openDebuggerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debugger.open();
            }
        });

        hideButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.displayMessage("@red@IdleRSC@yel@: Type '::show' to bring back the sidepane.");
                botFrame.setVisible(false);
            }
        });
        
        resetXpButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		DrawCallback.resetXpCounter();
        	}
        });

        graphicsCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(controller != null) {
                    controller.setDrawing(graphicsCheckbox.isSelected());
                }
            }
        });

        Dimension buttonSize = new Dimension(125, 25);

        botFrame.add(startStopButton);
        startStopButton.setMaximumSize(buttonSize);
        startStopButton.setPreferredSize(buttonSize);
        botFrame.add(loadScriptButton);
        loadScriptButton.setMaximumSize(buttonSize);
        loadScriptButton.setPreferredSize(buttonSize);
        botFrame.add(pathwalkerButton);
        pathwalkerButton.setMaximumSize(buttonSize);
        pathwalkerButton.setPreferredSize(buttonSize);
        botFrame.add(autoLoginCheckbox);
        botFrame.add(logWindowCheckbox);
        botFrame.add(unstickCheckbox);
        botFrame.add(debugCheckbox);
        botFrame.add(graphicsCheckbox);
        botFrame.add(openDebuggerButton);
        openDebuggerButton.setMaximumSize(buttonSize);
        hideButton.setPreferredSize(buttonSize);
        botFrame.add(hideButton);
        hideButton.setMaximumSize(buttonSize);
        hideButton.setPreferredSize(buttonSize);
        
        resetXpButton.setPreferredSize(buttonSize);
        resetXpButton.setMaximumSize(buttonSize);
        botFrame.add(resetXpButton);
        

        // abcd
        autoLoginCheckbox.setSelected(true);
        graphicsCheckbox.setSelected(true);


        botFrame.pack();
        botFrame.setSize(buttonSize.width, botFrame.getHeight());

        botFrame.setVisible(true);
    }

    /**
     * Sets up the log window
     *
     * @param consoleFrame -- the log window frame
     */
    private static void initializeConsoleFrame(JFrame consoleFrame) {
        JButton buttonClear = new JButton("Clear");
        autoscrollLogsCheckbox = new JCheckBox("Lock scroll to bottom");

        logArea = new JTextArea(9, 44);
        logArea.setEditable(false);
        scroller = new JScrollPane(logArea);

        consoleFrame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridy = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.SOUTHEAST;
        constraints.gridx = 2;
        constraints.weightx = 0.5;
        consoleFrame.add(autoscrollLogsCheckbox, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 5, 5, 5);
        constraints.anchor = GridBagConstraints.SOUTHWEST;
        constraints.gridx = 1;
        constraints.weightx = 0.5;
        consoleFrame.add(buttonClear, constraints);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        consoleFrame.add(new JScrollPane(logArea), constraints);

        buttonClear.addActionListener(evt -> clearLog());

        consoleFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        consoleFrame.setSize(480, 320);
    }

    /**
     * This function will go ahead and find the location of the `scriptName` and try to load
     * the class file.
     *
     * @param scriptName -- the name of the script (without .class at the end.)
     * @return boolean -- whether or not the script was successfully loaded.
     */
    private static boolean loadAndRunScript(String scriptName) {
        try {
			File scriptFile = new File(nativeScriptPath);

			URL url = scriptFile.toURI().toURL();
			URL[] urls = new URL[] {url};

			try {
				ClassLoader cl = new URLClassLoader(urls);
				Class clazz = cl.loadClass("scripting.idlescript." + scriptName);
				currentRunningScript = (IdleScript) clazz.newInstance();
			}
			catch(Exception e) {
				try {
					scriptFile = new File(sbotScriptPath);
					url = scriptFile.toURI().toURL();
					urls = new URL[] {url};
					ClassLoader cl = new URLClassLoader(urls);
					Class clazz = cl.loadClass("scripting.sbot." + scriptName);
					currentRunningScript = (compatibility.sbot.Script) clazz.newInstance();
				}
				catch(Exception _e) {
					scriptFile = new File(aposScriptPath);
					url = scriptFile.toURI().toURL();
					urls = new URL[] {url};
					ClassLoader cl = new URLClassLoader(urls);
					Class clazz = cl.loadClass("scripting.apos." + scriptName);
					

					//currentRunningScript = (compatibility.apos.Script) clazz.newInstance();
					compatibility.apos.Script.setController(controller);
					currentRunningScript = (compatibility.apos.Script) clazz.getDeclaredConstructor(String.class).newInstance("");
				}
			}

			Main.config.setScriptName(scriptName);
			
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
    }

    /**
     * Initializes the script menu selector.
     *
     * @param scriptFrame -- the script menu selector frame.
     */
    private static void initializeScriptFrame(JFrame scriptFrame) {
        String[] columnNames = {"Name", "Type"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        File[] nativeScripts = new File(nativeScriptPath).listFiles();
        File[] sbotScripts = new File(sbotScriptPath).listFiles();
        File[] aposScripts = new File(aposScriptPath).listFiles();

        // Create Comparator object to use in sorting the list
        Comparator fileComparator = (Comparator<File>) (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName());

        // Sort each list of scripts
        Arrays.sort(nativeScripts, fileComparator);
        Arrays.sort(aposScripts, fileComparator);
        Arrays.sort(sbotScripts, fileComparator);

        for (final File file : nativeScripts) {
            if (file.getName().endsWith(".class") && !file.getName().contains("$") && !file.getName().contains("package-info")) {
                String scriptName = file.getName().replace(".class", "");

                // Create row with script name and
                String[] row = {scriptName, "Native"};

                // Add row to the table model
                tableModel.addRow(row);
            }
        }
        
        for (final File file : aposScripts) {
            if (file.getName().endsWith(".class") && !file.getName().contains("$") && !file.getName().contains("package-info")) {
                String scriptName = file.getName().replace(".class", "");

                // Create row with script name and
                String[] row = {scriptName, "APOS"};

                // Add row to the table model
                tableModel.addRow(row);
            }
        }
        
        for (final File file : sbotScripts) {
            if (file.getName().endsWith(".class") && !file.getName().contains("$") && !file.getName().contains("package-info")) {
                String scriptName = file.getName().replace(".class", "");

                // Create row with script name and
                String[] row = {scriptName, "SBot"};

                // Add row to the table model
                tableModel.addRow(row);
            }
        }

        // Setup table
        final JTable scriptTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scriptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only allow single row selected at a time
        scriptTable.setAutoCreateRowSorter(true); // Automatically create a table row sorter
        scriptTable.getTableHeader().setReorderingAllowed(false); // Disable reordering columns
        scriptTable.getTableHeader().setResizingAllowed(false); // Disable resizing columns
        scriptTable.getTableHeader().setFont(scriptTable.getTableHeader().getFont().deriveFont(Font.BOLD, 15f));
        scriptTable.setBorder(BorderFactory.createEmptyBorder());

        final JScrollPane scriptScroller = new JScrollPane(scriptTable);

        // Setup script args field
        final String scriptArgsPlaceholder = "Script args (ex: arg1 arg2 arg3 ...)";
        final JTextField scriptArgs = new JTextField(scriptArgsPlaceholder);
        scriptArgs.setForeground(Color.GRAY);
        scriptArgs.addFocusListener(getPlaceholderFocusListener(scriptArgs, scriptArgsPlaceholder, false));

        // Setup filter field
        final String scriptFilterPlaceholder = "Filter";
        final JTextField scriptFilter = new JTextField(scriptFilterPlaceholder);
        scriptFilter.setForeground(Color.GRAY);
        scriptFilter.addFocusListener(getPlaceholderFocusListener(scriptFilter, scriptFilterPlaceholder, true));
        scriptFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            public void filter() {
                if (shouldFilter == false) {
                    return;
                }

                String filterValue = scriptFilter.getText().toLowerCase().trim();
                TableRowSorter sorter = ((TableRowSorter) scriptTable.getRowSorter());
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterValue, 0));

                scriptTable.setRowSorter(sorter);
            }
        });

        // Setup script button
        final JButton scriptButton = new JButton("Run");
        scriptButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        scriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRowIndex = scriptTable.getSelectedRow();
                if (selectedRowIndex > -1) {
                    String scriptName = (String) scriptTable.getModel().getValueAt(
                            scriptTable.convertRowIndexToModel(selectedRowIndex)
                            , 0
                    );

                    if (loadAndRunScript(scriptName)) {
                        if (scriptArgs.getText().equals(scriptArgsPlaceholder)) {
                            scriptArgs.setText("");
                        }

                        config.setScriptArguments(scriptArgs.getText().split(" "));
                        isRunning = true;
                        startStopButton.setText("Stop");
                        scriptFrame.setVisible(false);
                    }
                }
            }
        });

        // Setup layout
        scriptFrame.setLayout(new BoxLayout(scriptFrame.getContentPane(), BoxLayout.Y_AXIS));
        scriptFrame.add(scriptFilter);
        scriptFrame.add(scriptScroller);
        scriptFrame.add(scriptArgs);
        scriptFrame.add(scriptButton);
        scriptFrame.setSize(300, 300);

        centerWindow(scriptFrame);
    }

    private static FocusListener getPlaceholderFocusListener(JTextField textField, String placeholderText, boolean disableFilter) {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholderText)) {
                    if (disableFilter == true) {
                        shouldFilter = false;
                    }

                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    shouldFilter = true;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    if (disableFilter == true) {
                        shouldFilter = false;
                    }

                    textField.setText(placeholderText);
                    textField.setForeground(Color.GRAY);
                    shouldFilter = true;
                }
            }
        };
    }

    /**
     * Helper function for centering any window.
     *
     * @param frame -- the window to be centered.
     */
    public static void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }


    /**
     * un-hides the bot sidepanel.
     */
    public static void showBot() {
        botFrame.setVisible(true);
    }

    /**
     * Returns the global Controller instance. 
     * @return Controller
     */
    public static Controller getController() { return controller; }
    
    /**
     * Checks if the user has made a Cache/ folder. If not, spawns a wizard to create the folder.
     */
    private static void handleCache() {
    	//Does the directory exist?
    	File cacheDirectory = new File("Cache/");
    	
    	if(cacheDirectory.exists())
    		return;
    	
    	JFrame cacheFrame = new JFrame("Cache Setup");
    	JLabel cacheLabel = new JLabel("First setup: you must select either Uranium or Coleslaw.");
    	JButton uraniumButton = new JButton("Uranium (2018 RSC)");
    	JButton coleslawButton = new JButton("Coleslaw (modified RSC, new content)");
    	
    	uraniumButton.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) { 
    			copyDirectory("UraniumCache", "Cache");
    		}
    	});
    	
    	coleslawButton.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) { 
    			copyDirectory("ColeslawCache", "Cache");
    		}
    	});
    	
    	cacheFrame.setLayout(new GridLayout(0, 1));
    	cacheFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	cacheFrame.add(cacheLabel);
    	cacheFrame.add(uraniumButton);
    	cacheFrame.add(coleslawButton);
    	centerWindow(cacheFrame);
    	cacheFrame.setVisible(true);
    	cacheFrame.pack();
		

    	
    	while(cacheDirectory.exists() == false) {
    		
    		try {
    			Thread.sleep(100);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    		
    		cacheDirectory = new File("Cache/");
    	}
    	
    	cacheFrame.setVisible(false);
    	cacheFrame.dispose();
    }

    private static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)  {
    	try {
    		    Files.walk(Paths.get(sourceDirectoryLocation))
    		      .forEach(source -> {
    		          Path destination = Paths.get(destinationDirectoryLocation, source.toString()
    		            .substring(sourceDirectoryLocation.length()));
    		          try {
    		              Files.copy(source, destination);
    		          } catch (IOException e) {
    		              e.printStackTrace();
    		          }
    		      });
    		}
    	catch(Exception e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    }

    public static boolean isDrawEnabled() {
        if(controller != null) {
            return controller.isDrawEnabled();
        }

        return true;
    }

    public static void showLoadScript() {
        if (!isRunning) {
            scriptFrame.setVisible(true);
            scriptFrame.requestFocus();
        } else {
            JOptionPane.showMessageDialog(null, "Stop the current script first.");
        }
    }
//    
//    public static boolean wasAposInitCalled() {
//    	return aposInitCalled;
//    }

}

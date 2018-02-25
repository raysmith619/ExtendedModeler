package ExtendedModeler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmMem;
import smTrace.SmTrace;

public class ControlOfComponent extends ControlOfScene {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField imageFileStringTxFld;
	JComboBox imageCBox;
	String[] imageNames;
	JScrollPane imageScrollPane;
	int nicon_wide = 5;				// Number of icons wide
	int nicon_high = 4;				// Number of icons high
	public static String defaultImageFileDirName = "C:\\Users\\raysm\\workspace\\ExtendedModeler\\icons";
	public static String defaultImageFileName = defaultImageFileDirName + "\\" + "general_block.png";
	static String[] imageMasks;		// If non-null
									//    array of strings
									//    one of which is required string in image path
							
	
	ControlOfComponent(SceneControler sceneControler, String name) {
		super(sceneControler, name);
	}

	/**
	 * reset to default setting
	 */
	public void reset() {
		setup = false;
		setup();
	}
	
	
	/**
	 * Setup Control of object adding
	 * Hopefully a model / base class of other control dialogs
	 */
	public void setup() {
		if (setup)
		return;					// Already setup
		
		int ctl_w = 800;
		int ctl_h = 500;
		setPreferredSize(new Dimension(ctl_w, ctl_h));
		setTitle("Block - Select / Add / Modify");
		JPanel blockPanel = new JPanel();
		add(blockPanel);
		blockPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));
		int button_w = 80;
		int button_h = 40;

		JPanel selectPanel = new JPanel();		// Select
		blockPanel.add(selectPanel);
		JButton addSelectAllButton = new JButton("Select All");
		addSelectAllButton.setActionCommand("emc_selectAllButton");
		addSelectAllButton.addActionListener(sceneControler);
		selectPanel.add(addSelectAllButton);
		
		
		JPanel modPanel = new JPanel();		// Dup, Delete, Delete All
		///modPanel.setLayout(new BoxLayout(blockPanel, BoxLayout.Y_AXIS));
		blockPanel.add(modPanel);
		///getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		JButton addDuplicateButton = new JButton("Duplicate");
		addDuplicateButton.setPreferredSize(new Dimension(button_w, button_h));
		addDuplicateButton.setActionCommand("emc_duplicateBlockButton");
		addDuplicateButton.addActionListener(sceneControler);
		modPanel.add(addDuplicateButton);

		JButton addDeleteButton = new JButton("Delete");
		addDeleteButton.setActionCommand("emc_deleteBlockButton");
		addDeleteButton.addActionListener(sceneControler);
		modPanel.add(addDeleteButton);
		
		
		JButton addDeleteAllButton = new JButton("Delete ALL");
		addDeleteAllButton.setActionCommand("emc_deleteBlockAllButton");
		addDeleteAllButton.addActionListener(sceneControler);
		modPanel.add(addDeleteAllButton);
		blockPanel.add(modPanel);

		///blockPanel.setLayout(new FlowLayout(FlowLayout.LEFT));	// Components - next row
		JPanel compPanel = new JPanel();		// Box, Ball, Cone, Cylinder, ...		
		blockPanel.add(compPanel);
		ImageIcon box_icon = ColoredBox.imageIcon(button_w, button_h);
		JButton addBoxButton = new JButton("Box", box_icon);
		addBoxButton.setHorizontalTextPosition(JButton.CENTER);
		addBoxButton.setVerticalTextPosition(JButton.TOP);
		addBoxButton.setActionCommand("emc_addBoxButton");
		addBoxButton.addActionListener(sceneControler);
		compPanel.add(addBoxButton);

		ImageIcon ball_icon = ColoredBall.imageIcon(button_w, button_h);
		JButton addBallButton = new JButton("Ball", ball_icon);
		addBallButton.setHorizontalTextPosition(JButton.CENTER);
		addBallButton.setVerticalTextPosition(JButton.TOP);
		addBallButton.setActionCommand("emc_addBallButton");
		addBallButton.addActionListener(sceneControler);
		compPanel.add(addBallButton, BorderLayout.SOUTH);

		ImageIcon cone_icon = ColoredCone.imageIcon(button_w, button_h);
		JButton addConeButton = new JButton("Cone", cone_icon);
		addConeButton.setHorizontalTextPosition(JButton.CENTER);
		addConeButton.setVerticalTextPosition(JButton.TOP);
		addConeButton.setActionCommand("emc_addConeButton");
		addConeButton.addActionListener(sceneControler);
		compPanel.add(addConeButton);

		ImageIcon cylinder_icon = ColoredCylinder.imageIcon(button_w, button_h);
		JButton addCylinderButton = new JButton("Cylinder", cylinder_icon);
		addCylinderButton.setActionCommand("emc_addCylinderButton");
		addCylinderButton.setHorizontalTextPosition(JButton.CENTER);
		addCylinderButton.setVerticalTextPosition(JButton.TOP);
		addCylinderButton.addActionListener(sceneControler);
		compPanel.add(addCylinderButton);

		ImageIcon text_icon = ColoredText.imageIcon(button_w, button_h);
		JButton addTextButton = new JButton("Text", text_icon);
		addTextButton.setHorizontalTextPosition(JButton.CENTER);
		addTextButton.setVerticalTextPosition(JButton.TOP);
		addTextButton.setActionCommand("emc_addTextButton");
		addTextButton.addActionListener(sceneControler);
		compPanel.add(addTextButton);
		blockPanel.add(compPanel);
		
		JButton addEyeButton = new JButton("Eye");
		addEyeButton.setActionCommand("emc_addEyeButton");
		addEyeButton.addActionListener(sceneControler);
		compPanel.add(addEyeButton);

		JPanel imagePanel = new JPanel();		// Image File, AddImage
		blockPanel.add(imagePanel);
		imagePanel.add(new JLabel("Image File"));
		String imageDirName = ColoredImage.defaultImageFileDirName;
		File folder = new File(imageDirName);
		File[] listOfFiles = folder.listFiles();

		imageNames = new String[listOfFiles.length];
	    for (int i = 0; i < listOfFiles.length; i++) {
	        imageNames[i] = listOfFiles[i].getName();
	    }
	    int image_index = 0;
		imageCBox = new JComboBox(imageNames);
		imageCBox.setSelectedIndex(image_index);
		imagePanel.add(imageCBox);
		String image_name = (String) imageCBox.getSelectedItem();
		imageFileStringTxFld = new JTextField(image_name);
		imagePanel.add(imageCBox);
		imageCBox.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
                        JComboBox combo = (JComboBox)e.getSource();
                        String name = (String)combo.getSelectedItem();
                        imageFileStringTxFld.setText(name);
					}
				}
		);

		
		imageFileStringTxFld.setColumns(10);
		imagePanel.add(imageFileStringTxFld);
		imageFileStringTxFld.setActionCommand("emc_addImageButton");
		imageFileStringTxFld.addActionListener(sceneControler);

		JButton addImageButton = new JButton("AddImage");
		addImageButton.setActionCommand("emc_addImageButton");
		addImageButton.addActionListener(sceneControler);
		imagePanel.add(addImageButton);
		
		
		JPanel imageArrayPanel = new JPanel();		// Array of Image files		
		blockPanel.add(imageArrayPanel, Box.TOP_ALIGNMENT);
		setupImageArray(imageArrayPanel, ctl_w, ctl_h);
		
		pack();
		setup = true;
	}

	
	/**
	 * Setup image mask array
	 * Mostly to limit image memory usage
	 * @return 
	 */
	public static void setImageMasks(String[] masks) {
		imageMasks = masks;
	}
	
	/**
	 * Setup image panel
	 */
	private void setupImageArray(JPanel arrayPanel, int ctl_w, int ctl_h) {
		int ib_w = ctl_w/nicon_wide - 20;
		int ib_h = 100;
		int isp_w = ctl_w;
		int isp_h = nicon_high*ib_h;
		arrayPanel.setPreferredSize(new Dimension(100, 600));
		JPanel imageIconPanel = new JPanel(new GridLayout(0, nicon_wide));		// Icons in 5 columns
		imageIconPanel.setBounds(3, 3, isp_w, isp_h/2);
		arrayPanel.add(imageIconPanel, Box.TOP_ALIGNMENT);
		///imageIconPanel.setPreferredSize(new Dimension(100, 400));
		//imageIconPanel.setPreferredSize(null);
        imageScrollPane = new JScrollPane(imageIconPanel);
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setPreferredSize(new Dimension(ctl_w, ctl_h));
        imageScrollPane.setBounds(3, 3, isp_w, isp_h/2);
        //JPanel contentPane = new JPanel(null);
        //imageScrollPane.setPreferredSize(null);
        arrayPanel.add(imageScrollPane);
		
		for (String iname : imageNames) {
			String ifile = ColoredImage.fullFileName(iname);
			if (imageMasks != null) {
				String ifile_lc = ifile.toLowerCase();
				boolean found = false;	// Set true, iff a mask found
				for (String mask : imageMasks) {
					if (ifile_lc.contains(mask.toLowerCase()) ) {
						found = true;
						break;
					}
				}
				if (!found) {
					String mask_str = "";
					for (String mask : imageMasks) {
						if (mask_str.length() > 0)
							mask_str += ",";
						mask_str += mask;
					}
					SmTrace.lg(String.format("Rejecting %s - does not contain any of %s",
							ifile_lc, mask_str));
					continue;
				}
			}
			String icon_name = ColoredImage.iconFileName(ifile);
			File file = new File(ifile);
			
			String basename = file.getName();
			ImageIcon icon = new ImageIcon(icon_name);
			JButton ibutton = new JButton(iname, icon);
			ibutton.setPreferredSize(new Dimension(ib_w, ib_h));
			ibutton.setBounds(0, 0, ib_w, ib_h);
			// Set image to size of JButton...
			int offset = ibutton.getInsets().left;
			ibutton.setIcon(resizeIcon(icon, ibutton.getWidth() - offset, ibutton.getHeight() - offset));
			ibutton.setHorizontalTextPosition(JButton.CENTER);
			ibutton.setVerticalTextPosition(JButton.TOP);
			imageIconPanel.add(ibutton);
	        ibutton.addActionListener(new AbstractAction() {
	        	/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
	        	public void actionPerformed(ActionEvent e) {
	        		try {
						newColoredImage(ibutton, ifile);
					} catch (EMBlockError e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	        	}
	        });
		}
        imageScrollPane.setPreferredSize(null);
        addComponentListener(new ComponentListener() {

			@Override
			public void componentResized(ComponentEvent e) {
				SmTrace.lg("ControlOfComponent resized", "component");
				ControlOfComponent component = (ControlOfComponent) e.getSource();
		        Dimension windowSize = component.getContentPane().getSize();
		        int ctl_w = windowSize.width;
				int ctl_h = windowSize.height;
				int isp_w = ctl_w;
				int isp_h = ctl_h;
		        ///resizeIconsPanel(isp_w, isp_h);
				
			}
 			@Override
			public void componentMoved(ComponentEvent e) {
				SmTrace.lg("ControlOfComponent moved", "component");
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
	} );
        resizeIconsPanel(isp_w, isp_h);

	}

	/**
	 * @param icon
	 * @param resizedWidth
	 * @param resizedHeight
	 * @return
	 */
	
	public static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
	    Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}

	
	private void resizeIconsPanel(int isp_w, int isp_h) {
		imageScrollPane.setSize(new Dimension(isp_w, isp_h));
	}
	
	
	/**
	 * Add new image from panel
	 * @throws EMBlockError 
	 */
	public void newColoredImage(JButton ibutton, String ifile) throws EMBlockError {
		sceneControler.selectPrint(String.format("newColoredImage(%s)", ifile), "component");
		String action = "emc_newColoredImage";
		EMBCommand bcmd;
		SmMem.ck("newColoredImage", SmMem.Type.Begin);
		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		EMBlock cb_sel = sceneControler.getSelectedBlock();
		boolean from_selected = false;			// Positioned from selected
		if (cb_sel != null) {
			cb_sel.setControls(sceneControler.controls);
			ControlOfPlacement cop = (ControlOfPlacement) sceneControler.controls.getControl("placement");
			cop.adjPos();
		}
		EMBlock cb = null;
		String blockType = "image";
		try {
			cb = EMBlock.newBlock(blockType, sceneControler.controls, ifile);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("createNewBlock %s error: %s",
					blockType, e.getMessage()));
			return;
		}
		
		///cb.adjustFromControls(controls, bcmd);
		int id = bcmd.addBlock(cb);
		sceneControler.selectAdd(bcmd, id, false);
		///bcmd.selectBlock(id);		// Select new block
		boolean res = bcmd.doCmd();
		sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
		SmMem.ck("newColoredImage", SmMem.Type.End);
		return;
	}
	
	
	/**
	 * Overridden control methods
	 */



	/**
	 * Check for and act on action
	 * @throws EMBlockError 
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		if (!isActive())
			return false;	// Not active
		
		sceneControler.selectPrint(String.format("ckDoAction(%s)", action), "select");
		EMBCommand bcmd;
		try {
			bcmd = new BlkCmdAdd(action);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		SmMem.ck("action");
		switch (action) {
			case "emc_selectAllButton":
			case "emc_toggleSelectButton":
			case "emc_deleteBlockButton":
			case "emc_deleteBlockAllButton":
			case "emc_duplicateBlockButton":
			case "emc_addBoxButton":
			case "emc_addBallButton":
			case "emc_addConeButton":
			case "emc_addCylinderButton":
			case "emc_addTextButton":		// Emulate Text control
			case "emc_addImageButton":
				sceneControler.addBlockButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
				
			case "OBSOLETE emc_addTextButton":		// Emulate Text control
				sceneControler.addTextButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
				
			case "emc_addEyeButton":		// Test eye drawing
				sceneControler.addEyeButton(bcmd, action);
				if (bcmd != null) {
					boolean res = bcmd.doCmd();
					sceneControler.selectPrint(String.format("ckDoAction(%s) AFTER", action), "select");
					return res;
				}
				break;
			
				default:
					break;
		}
		return false;			// Event not processed by us - possibly by someone else
	}


	/**
	 * Convert partial file name to full file name
	 * @param fileName
	 * @return
	 */
	public static String fullFileName(String fileName) {
		if (fileName == null || fileName.equals("")) {
			fileName = defaultImageFileName;
		}
		if (!new File(fileName).isAbsolute()) {
			fileName = defaultImageFileDirName + File.separator + fileName;
		}
		return fileName;
	}

	/**
	 * Convert partial file name to icon file name
	 * @param fileName
	 * @return
	 */
	public static String iconFileName(String fileName) {
		String full_file_name = fullFileName(fileName);
		File dir = new File(full_file_name);
		String icon_file_name = full_file_name;		// Default
		if (dir.isDirectory()) {
			String files[] = dir.list();
			for (String name : files) {
				if (name.startsWith("__ICON")) {
					icon_file_name = icon_file_name + File.separator + name;
					break;
				}
			}
		}
		return icon_file_name;
	}
	
	
	/**
	 * fields
	 */
	String getFileName() {
		String file_name = imageFileStringTxFld.getText();
		return file_name;
	}

}

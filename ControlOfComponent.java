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
		int ctl_h = 400;
		setPreferredSize(new Dimension(ctl_w, ctl_h));
		setTitle("Block - Select / Add / Modify");
		JPanel blockPanel = new JPanel();
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		add(blockPanel);
		int button_w = 40;
		int button_h = 20;

		JPanel selectPanel = new JPanel();
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		JButton addSelectAllButton = new JButton("Select All");
		addSelectAllButton.setActionCommand("emc_selectAllButton");
		addSelectAllButton.addActionListener(sceneControler);
		selectPanel.add(addSelectAllButton);
		/***
		JButton addToggleSelectButton = new JButton("Toggle Select");
		addToggleSelectButton.setActionCommand("emc_toggleSelectButton");
		addToggleSelectButton.addActionListener(sceneControler);
		selectPanel.add(addToggleSelectButton);
		***/
		blockPanel.add(selectPanel);
		
		
		JPanel modPanel = new JPanel();		// Modifiers
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		blockPanel.add(modPanel);
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

		blockPanel.setLayout(new FlowLayout(FlowLayout.LEFT));	// Components - next row
		JPanel compPanel = new JPanel();		
		blockPanel.add(compPanel, BorderLayout.NORTH);
		JButton addBoxButton = new JButton("Box");
		addBoxButton.setActionCommand("emc_addBoxButton");
		addBoxButton.addActionListener(sceneControler);
		compPanel.add(addBoxButton);

		JButton addBallButton = new JButton("Ball");
		addBallButton.setActionCommand("emc_addBallButton");
		addBallButton.addActionListener(sceneControler);
		compPanel.add(addBallButton, BorderLayout.SOUTH);

		JButton addConeButton = new JButton("Cone");
		addConeButton.setActionCommand("emc_addConeButton");
		addConeButton.addActionListener(sceneControler);
		compPanel.add(addConeButton);

		JButton addCylinderButton = new JButton("Cylinder");
		addCylinderButton.setActionCommand("emc_addCylinderButton");
		addCylinderButton.addActionListener(sceneControler);
		compPanel.add(addCylinderButton);
		blockPanel.add(compPanel);

		JButton addTextButton = new JButton("Text");
		addTextButton.setActionCommand("emc_addTextButton");
		addTextButton.addActionListener(sceneControler);
		compPanel.add(addTextButton);
		blockPanel.add(compPanel);
		JButton addEyeButton = new JButton("Eye");
		addEyeButton.setActionCommand("emc_addEyeButton");
		addEyeButton.addActionListener(sceneControler);
		compPanel.add(addEyeButton);

		JPanel imagePanel = new JPanel();
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
		setupImagePanel(blockPanel, ctl_w, ctl_h);
		
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
	private void setupImagePanel(JPanel blockPanel, int ctl_w, int ctl_h) {
		int ib_w = ctl_w/nicon_wide - 20;
		int ib_h = 100;
		int isp_w = ctl_w;
		int isp_h = nicon_high*ib_h;
		JPanel imageIconPanel = new JPanel(new GridLayout(0, nicon_wide));		// Icons in 5 columns
		//imageIconPanel.setPreferredSize(null);
        imageScrollPane = new JScrollPane(imageIconPanel);
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //imageScrollPane.setBounds(3, 3, isp_w, isp_h/2);
        //JPanel contentPane = new JPanel(null);
        //imageScrollPane.setPreferredSize(null);
		blockPanel.add(imageScrollPane);
		
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
		        resizeIconsPanel(isp_w, isp_h);
				
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
	
	private static Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
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
	 * fields
	 */
	String getFileName() {
		String file_name = imageFileStringTxFld.getText();
		return file_name;
	}

}

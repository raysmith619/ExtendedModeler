package ExtendedModeler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import smTrace.SmTrace;

public class TraceControl extends JDialog implements ActionListener {
	JFrame frame;				// Caller's frame
	JScrollPane ckBoxScrollPane;
	JButton traceAllButton;
	JButton traceNoneButton;
	private static final long serialVersionUID = 1L;
	
	TraceControl(JFrame frame) {
		super();
		this.frame = frame;
		setup();
	}
	
	/**
	 * Setup active Control trace (SmTrace)
	 */
	public void setup()  {
		SmTrace.lg(String.format("TraceControl setup"));
		setTitle("Trace Control");
		///JPanel toolPanel = new JPanel();
		JPanel tracePanel = new JPanel(new BorderLayout());
		JPanel traceSpecialPanel = new JPanel(new BorderLayout());
		tracePanel.add(traceSpecialPanel, BorderLayout.NORTH);
		add(tracePanel);
		///JPanel ckBoxPanel = new JPanel(new BorderLayout());
		///tracePanel.add(ckBoxPanel, BorderLayout.SOUTH);
		JPanel ckBoxPanel = tracePanel;
		
		traceAllButton = new JButton("ALL");
		traceAllButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceAllButton.addActionListener(this);
		traceSpecialPanel.add( traceAllButton, BorderLayout.NORTH);
		
		traceNoneButton = new JButton("NONE");
		traceNoneButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceNoneButton.addActionListener(this);
		traceSpecialPanel.add( traceNoneButton, BorderLayout.CENTER);

		setupTraceCkBoxPanel(ckBoxPanel);
		pack();
		Dimension wsize = frame.getSize();
		int xloc = wsize.width;
		int yloc = 10;
		setLocation(xloc, yloc);
		setVisible(true);
		
	}
	
	
	
	
	/**
	 * Setup image panel
	 */
	private void setupTraceCkBoxPanel(JPanel blockPanel) {
		int nckbox_wide = 1;
		blockPanel.setPreferredSize(new Dimension(100, 400));
		JPanel traceCkBoxPanel = new JPanel(new GridLayout(0, nckbox_wide));		// n cols
		//imageIconPanel.setPreferredSize(null);
        ckBoxScrollPane = new JScrollPane(traceCkBoxPanel);
        ckBoxScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ckBoxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //ckBoxScrollPane.setBounds(3, 3, isp_w, isp_h/2);
        //JPanel contentPane = new JPanel(null);
        //ckBoxScrollPane.setPreferredSize(null);
		blockPanel.add(ckBoxScrollPane);
		String[] trace_names = SmTrace.getAllTraceFlags();
		
		for (String trace_name : trace_names) {
			int level = SmTrace.getLevel(trace_name);
			JCheckBox ckbutton = new JCheckBox(trace_name, level>0);
			traceCkBoxPanel.add(ckbutton);
			ckbutton.addItemListener(new ItemListener() {
			    @Override
			    public void itemStateChanged(ItemEvent e) {
			        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
			        	setTraceLevel(trace_name, 1);
			        } else {//checkbox has been de-selected
			        	setTraceLevel(trace_name, 0);
			        };
			    }
			});
		}
        ckBoxScrollPane.setPreferredSize(null);
	}

	/**
	 *
	 * Set trace level based on ckeck box action/value
	 * @param ckbutton
	 * @param trace_name
	 * @param cur_level - initial flag level (before any action)
	 */
	private void actOnTraceButton(JCheckBox ckbutton, String trace_name, int level) {
		SmTrace.setTraceFlag(trace_name, level);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// Check on menu commands
		Object source = ae.getSource();
		if (source == traceAllButton) {
			SmTrace.lg("traceAllButton");
			setTraceAllLevel(1);
		} else if (source == traceNoneButton) {
			SmTrace.lg("traceNoneButton");
			setTraceAllLevel(0);
		}
	}

	
	/**
	 * set trace level, record if changed
	 */
	public void setTraceLevel(String flag, int level) {
		int prev_level = SmTrace.getLevel(flag);
		if (level == prev_level)
			return;				// No action if no change
		
		SmTrace.lg(String.format("setTraceLevel: %s %d => %d", flag, prev_level, level));
		SmTrace.setTraceFlag(flag, level);
		SmTrace.lg(String.format("Tracing %s", flag), flag);	// Displayed iff tracing
	}

	public void setTraceAllLevel(int level) {
		String[] trace_names = SmTrace.getAllTraceFlags();
		for (String flag : trace_names) {
			setTraceLevel(flag, level);
		}
	}
	
}

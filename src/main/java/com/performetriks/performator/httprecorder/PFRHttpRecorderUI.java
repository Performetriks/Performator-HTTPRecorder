package com.performetriks.performator.httprecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.performetriks.performator.conversion.PFRConverterUI;
import com.performetriks.performator.conversion.RequestEntry;
import com.performetriks.performator.conversion.RequestModel;
import com.performetriks.performator.http.PFRHttp;

/*****************************************************************************
 * A Swing application that loads a HAR (HTTP Archive) and generates Java code that uses
 * com.performetriks.performator.http.PFRHttp to reproduce the HTTP requests.
 *
 * Features:
 * - fullscreen JFrame
 * - left pane (50%) containing all input controls
 * - right pane (50%) containing a textarea with generated Java code (copy/paste)
 * - parses a HAR file using Google Gson into an inner model
 * - all inputs show description tooltips on mouseover (decorator)
 * - every change to any input re-generates the output
 *
 * NOTE: This is a single-file demonstration. The generated Java code in the right pane is
 * textual output and not compiled/executed by this application.
 *****************************************************************************/
public class PFRHttpRecorderUI extends PFRConverterUI {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(PFRConverterUI.class);
	
	
	// ---------------------------
	// UI Elements (left / right)
	// ---------------------------
	private JButton btnStartProxy;
	private JButton btnStopProxy;
	private JButton btnClearRecordings;
	private JLabel labelPort;
	private JSpinner fieldPort;

	private RequestModel requestModel = new RequestModel();
	
	private static PFRHttpRecorderUI ui = new PFRHttpRecorderUI();
	
	private ProxyRecorder recorder = null;

	/*****************************************************************************
	 * Main entrypoint: create and show the UI.
	 *
	 * @param args not used
	 *****************************************************************************/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new PFRHttpRecorderUI()
					.setVisible(true);
		});
	}

	/*****************************************************************************
	 * Constructor: sets up the JFrame and initializes UI components.
	 *****************************************************************************/
	public PFRHttpRecorderUI() {
		super("Performator Http Recorder");
		ui = this;
		this.setRequestModel(requestModel);
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	@Override
	public void initializeCustomControls(JPanel controlsPanel) {
		
		if(btnStartProxy == null) {
			
			labelPort = new JLabel("Port");
			fieldPort = new JSpinner(new SpinnerNumberModel(9999, 0, Integer.MAX_VALUE, 1));
			
			btnStartProxy = new JButton("Start Proxy");
			btnStopProxy = new JButton("Stop Proxy");
			btnClearRecordings = new JButton("Clear Recordings");

			//labelPort.setForeground(reallyLight);
			controlsPanel.add(labelPort);
			controlsPanel.add(fieldPort);
			controlsPanel.add(btnStartProxy);
			controlsPanel.add(btnStopProxy);
			controlsPanel.add(btnClearRecordings);
			
			// Add update listeners
			btnStartProxy.addActionListener(e -> startProxy());
			btnStopProxy.addActionListener(e -> stopProxy());
			btnClearRecordings.addActionListener(e -> clearRecordings());
		}
		
		
	}
	
	/*****************************************************************************
	 * Handles HAR file selection and parsing.
	 *****************************************************************************/
	private void clearRecordings() {
		requestModel.clear();
		SwingUtilities.invokeLater(() -> { 
			ui.regenerateCode(); 
		});
	}
		
	/*****************************************************************************
	 * Handles HAR file selection and parsing.
	 *****************************************************************************/
	private void startProxy() {
		
		if(recorder != null) { stopProxy(); }
		
		recorder = new ProxyRecorder(this, requestModel).start( (Integer) fieldPort.getValue());
	}
	
	/*****************************************************************************
	 * Handles HAR file selection and parsing.
	 *****************************************************************************/
	private void stopProxy() {
		if(recorder != null) { 
			recorder.stop();
		}
	}
	
}


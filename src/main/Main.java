package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import refactor.RefactorFunc;

public class Main {

	protected Shell shlR;
	private Text text;
	private Text text_1;
	private Text text_2;
	private String inputString = "";
	private Text text_3;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlR.open();
		shlR.layout();
		while (!shlR.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlR = new Shell();
		shlR.setMinimumSize(new Point(800, 600));
		shlR.setText("Refactoring Demo");
		shlR.setSize(716, 571);
		
		Group grpPreference = new Group(shlR, SWT.NONE);
		grpPreference.setText("Preference");
		grpPreference.setBounds(10, 10, 179, 541);
		
		text = new Text(grpPreference, SWT.BORDER);
		text.setBounds(10, 25, 105, 23);
		text.setEditable(false);
		
		Button btnFile = new Button(grpPreference, SWT.NONE);
		btnFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlR, SWT.OPEN);
				dialog.setFilterPath("");
				dialog.setText("Choose Java Source File");
				dialog.setFileName("");
				dialog.setFilterNames(new String[] { "Java Source File(*.java)"});
				dialog.setFilterExtensions(new String[] { "*.java"});
				String fileName = dialog.open();
				String fstr = "";
				if(fileName != null){
					fstr = readToString(fileName);
					text.setText(fileName);
					text_1.setText(fstr);
					inputString = fstr;
				}
			}
		});
		btnFile.setBounds(114, 24, 51, 27);
		btnFile.setText("File");
		
		Button btnNewButton = new Button(grpPreference, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(shlR, SWT.SAVE);
				dialog.setFilterPath("");
				dialog.setText("Save Refactored Java Source File");
				dialog.setFileName("");
				dialog.setFilterNames(new String[] { "Java Source File(*.java)"});
				dialog.setFilterExtensions(new String[] { "*.java"});
				String fileName = dialog.open();
				String content = text_2.getText();
				if(fileName != null)
					writeToFile(fileName, content);
			}
		});
		btnNewButton.setBounds(10, 487, 155, 27);
		btnNewButton.setText("Save Result");
		
		Label lblVariableStyle = new Label(grpPreference, SWT.NONE);
		lblVariableStyle.setBounds(10, 54, 155, 17);
		lblVariableStyle.setText("Variable Style");
		
		Combo combo = new Combo(grpPreference, SWT.READ_ONLY);
		combo.setItems(new String[] {"sampleVariable", "sample_variable", "SampleVariable", "Sample_Variable"});
		combo.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		combo.setBounds(10, 77, 155, 25);
		combo.setText("sampleVariable");
		
		Button btnRefactoring = new Button(grpPreference, SWT.NONE);
		btnRefactoring.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("deprecation")
			@Override
			public void widgetSelected(SelectionEvent e) {
				int flag = combo.getSelectionIndex();
				RefactorFunc rf = new RefactorFunc();
				float thr = Float.parseFloat(text_3.getText());
				if(thr < 0){
					rf.setThreshold(0);
					text_3.setText("0");
				}
				else if(thr > 1){
					rf.setThreshold(1);
					text_3.setText("1");
				}
				else
					rf.setThreshold(thr);
				if(flag > 0)
					rf.setVariableStyle(flag);
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(inputString.toCharArray());
				parser.setResolveBindings(true);
				CompilationUnit result = (CompilationUnit) parser.createAST(null);
				result.accept(rf);
				text_2.setText(result.toString());
			}
		});
		btnRefactoring.setText("Refactoring");
		btnRefactoring.setBounds(10, 463, 155, 27);
		
		Label lblCombineDuplicate = new Label(grpPreference, SWT.NONE);
		lblCombineDuplicate.setText("Duplicate - Threshold (0-1)");
		lblCombineDuplicate.setBounds(10, 108, 155, 17);
		
		text_3 = new Text(grpPreference, SWT.BORDER | SWT.CENTER);
		text_3.setText("0.5");
		text_3.setBounds(10, 131, 155, 23);
		
		Group grpResult = new Group(shlR, SWT.NONE);
		grpResult.setText("Result");
		grpResult.setBounds(198, 10, 576, 541);
		
		text_2 = new Text(grpResult, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text_2.setLocation(307, 20);
		text_2.setSize(245, 484);
		text_2.setEditable(false);
		
		text_1 = new Text(grpResult, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text_1.setLocation(22, 20);
		text_1.setSize(253, 484);
		text_1.setEditable(false);
		
		Label label = new Label(grpResult, SWT.NONE);
		label.setLocation(281, 249);
		label.setSize(18, 17);
		label.setText("=>");
	}
	
	public String readToString(String fileName) {  
        String encoding = "ISO-8859-1";  
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }  
	
	public void writeToFile(String fileName, String content) {
		Writer writer;
		try {
			writer = new FileWriter(fileName);
			writer.write(content);  
	        writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
}

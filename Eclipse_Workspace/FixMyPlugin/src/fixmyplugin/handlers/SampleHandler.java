package fixmyplugin.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import fixmyplugin.FixMyBugView;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	
	private boolean testing = true;
	
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}
	
	private String HARDCODED_PATH = "/Users/azureillusions/Desktop/FixMyBug/gs-rest-client/target/gs-client-0.1.0-jar-with-dependencies.jar";
	
	public String getCurrentFilePath() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = 
		        workbench == null ? null : workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = 
		        window == null ? null : window.getActivePage();

		IEditorPart editor = 
		        activePage == null ? null : activePage.getActiveEditor();
		IEditorInput input = 
		        editor == null ? null : editor.getEditorInput();
		IPath path = input instanceof FileEditorInput 
		        ? ((FileEditorInput)input).getPath()
		        : null;
		if (path != null) {
		    return path.toOSString();
		}
		return "";
	}
	
	public String getCurrentEditorContent() {
	    final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
	        .getActiveEditor();
	    if (!(editor instanceof ITextEditor)) return null;
	    ITextEditor ite = (ITextEditor)editor;
	    IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
	    return doc.get();
	}
	
	/**
	 * From https://usayadis.wordpress.com/2009/10/20/programmatically-query-current-text-selection/
	 * Can't wait to see what I have to import for this one.
	 */
	public ITextSelection getCurrentSelection() {
	   IEditorPart part = PlatformUI.getWorkbench()
			   						.getActiveWorkbenchWindow()
			   						.getActivePage()
			   						.getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			IDocumentProvider prov = editor.getDocumentProvider();
			IDocument doc = prov.getDocument(editor.getEditorInput());
			ISelection sel = editor.getSelectionProvider().getSelection();
			if (sel instanceof TextSelection) {
				ITextSelection textSel = (ITextSelection) sel;
				return textSel;
			}
		}
		return null;
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		ITextSelection highlighted = getCurrentSelection();
		String highlightedText = highlighted.getText();
		String out_string = "dummy test string";
		
		if (!this.testing) {
			// Run a java app in a separate system process
			Process proc;
			try {
				proc = Runtime.getRuntime().exec("java -jar " + HARDCODED_PATH + " "
	//											 + getCurrentFilePath()
												 + "/Users/azureillusions/Desktop/FixMyBug/gs-rest-client/Test.java"
												 + " error " + (highlighted.getStartLine() + 1)
												 + " " + (highlighted.getEndLine() + 1) + " fix");	
				System.out.println("hey");
				// Then retrieve the process output
				InputStream in = proc.getInputStream();
				InputStream err = proc.getErrorStream();
				out_string = IOUtils.toString(in);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	//			System.out.println("Crap");
			}
		}
		
		
		String display = highlightedText + "\n\nStarting line: " + (highlighted.getStartLine() + 1)
										 + "\nEnding line: " + (highlighted.getEndLine() + 1);
		
		
		String fix1, fix2, fix3, fix4;
		fix1 = "for (int i = 0; i < 10; i++) {\n\tlistName[i] = 0;\n}";
		fix2 = "x = 5; if(x == 5) { System.out.println(\"Test\"); }";
		fix3 = "this\n\tis\n\t\ta\n\t\tdummy\n\tfix";
		fix4 = "this\nis\na\nreally\nr\ne\na\nl\nl\ny\nbig\nfix";
		
		String[] fixes = out_string.split("~");
		if (testing) {
			fixes = new String[] {fix1, fix2, fix3, fix4};
		}
		fix1 = fixes[0];
		fix2 = fixes[1];
		fix3 = fixes[2];
		fix4 = fixes[3];
		
		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView("BugFixerView");
			FixMyBugView BFView = (FixMyBugView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("BugFixerView");
			BFView.update(fix1,fix2,fix3,fix4);
			BFView.setFocus();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		MessageDialog.openInformation(
//				window.getShell(),
//				"Fix My Bug",
//				out_string
//				);
		return null;
	}
}

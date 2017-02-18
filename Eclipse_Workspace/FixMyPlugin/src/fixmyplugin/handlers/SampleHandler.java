package fixmyplugin.handlers;

import client.SimpleClient;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
	
	// When toggled to true, calls a dummy method rather than the actual server.
	private boolean testing = false;
	
	
	/**
	 * The constructor.
	 */
	public SampleHandler() {}
	
	
	/**
	 * This method is run when the icon is clicked, or when the keyboard shortcut is hit.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// Gets the highlighted text from the window.
		// Note: window is potentially deleteable.
//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		ITextSelection highlighted = getCurrentSelection();
//		String highlightedText = highlighted.getText();
		
		// Initialize the string that will contain the output from the client
		String out_string = "testing~testing~testing~testing";
		
		// If we aren't testing, access the client and replace it.
		if (!this.testing) {
			SimpleClient client = new SimpleClient();
			out_string = client.prf();
		}
		
		// This WILL BE REPLACED, as the client will return a list.
		String[] fixes = out_string.split("~");
		
		// Displays the FixMyBugView, updates it, and sets the focus to it.
		try {
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().showView("BugFixerView");
			FixMyBugView BFView = (FixMyBugView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("BugFixerView");
			BFView.update(fixes[0], fixes[1], fixes[2], fixes[3]);
			BFView.setFocus();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * A getter for the file path. May or may not currently work.
	 * @return A string corresponding to the file path, on a mac.
	 */
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
	
//	public String getCurrentEditorContent() {
//	    final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//	        .getActiveEditor();
//	    if (!(editor instanceof ITextEditor)) return null;
//	    ITextEditor ite = (ITextEditor)editor;
//	    IDocument doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
//	    return doc.get();
//	}
	
	/**
	 * Grabs the highlighted text and returns it as an ITextSelection.
	 * From https://usayadis.wordpress.com/2009/10/20/programmatically-query-current-text-selection/
	 * Can't wait to see what I have to import for this one.
	 * @return an ITextSelection of the highlighted text.
	 */
	public ITextSelection getCurrentSelection() {
	   IEditorPart part = PlatformUI.getWorkbench()
			   						.getActiveWorkbenchWindow()
			   						.getActivePage()
			   						.getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
//			IDocumentProvider prov = editor.getDocumentProvider();
//			IDocument doc = prov.getDocument(editor.getEditorInput());
			ISelection sel = editor.getSelectionProvider().getSelection();
			if (sel instanceof TextSelection) {
				ITextSelection textSel = (ITextSelection) sel;
				return textSel;
			}
		}
		return null;
	}
}

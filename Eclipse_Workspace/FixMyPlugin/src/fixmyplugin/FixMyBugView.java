package fixmyplugin;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class FixMyBugView extends ViewPart {
	Composite root;
	ScrolledComposite scrolledComposite;
	Composite composite;

    public FixMyBugView() {}

	@Override
	public void createPartControl(Composite parent) {
		this.root = parent;
		// Tell the parent how to treat its contents.
		root.setLayout(new FillLayout());
		
		// Create the scrolled composite, make it vertical. Let it expand both directions.
		scrolledComposite = new ScrolledComposite(root, SWT.V_SCROLL);
		scrolledComposite.setLayout(new FillLayout(SWT.VERTICAL));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinSize(root.computeSize(root.getClientArea().width, SWT.DEFAULT ));
		// The listener changes the size appropriately when it's resized.
		scrolledComposite.addListener(SWT.Resize, event -> {
			  int width = scrolledComposite.getClientArea().width;
			  scrolledComposite.setMinSize(root.computeSize( width, SWT.DEFAULT ) );
		} );
		
		// Nest a composite in the scrolledComposite. Give it a grid. 
		composite = new Composite(scrolledComposite, SWT.NONE);
		GridLayout outerGrid = new GridLayout();
		outerGrid.numColumns = 1;
		composite.setLayout(outerGrid);
		
		Text unusedText = new Text(composite, SWT.CENTER | SWT.WRAP);
		GridData unusedData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
//		unusedData.grabExcessHorizontalSpace = true;
//		unusedData.grabExcessVerticalSpace = true;
//		unusedData.horizontalAlignment = SWT.FILL;
//		unusedData.verticalAlignment = SWT.FILL;
		unusedText.setLayoutData(unusedData);
		unusedText.setText("Highlight text and press the blue bug icon!");
		
		scrolledComposite.setContent(composite);
    }
    
    /*
     * Manually added. Changes the contents of the fix boxes to match the received,
     * harmonized fixes.
     */
    public void update(List<String> fixedCode) {
		
		// Eliminate all children of the root
    	for (Control child : composite.getChildren()) {
    		child.dispose();
    	}

    	for (String suggestedFix : fixedCode) {
    		addChildFix(composite, suggestedFix);
    	}
    	
    	int width = scrolledComposite.getClientArea().width;
    	scrolledComposite.setMinSize(root.computeSize( width, SWT.DEFAULT ) );
    	
    	scrolledComposite.setContent(composite);
    }
    
    /**
     * Add a fix "widget" to the display.
     */
    public void addChildFix(Composite parent, String suggestedFix) {
    	// Make the wrapper for a new fix suggestion.
		Group group = new Group(parent, SWT.NULL);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Give it an internal grid.
		GridLayout innerGrid = new GridLayout();
		innerGrid.numColumns = 2;
        group.setLayout(innerGrid);
        
        
        // Give it a button, and tell the button what to do when pressed.
        Button button = new Button(group, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        button.setText("Insert this fix");
        button.setToolTipText("Comments out the selected code and adds the replacement code after it.");
        
        // Make the text window. Prevent it from being manually edited, tell it
        // how to behave, and then set a listener to change its height when the
        // text is updated.
        GridData text_grid_data = new GridData(SWT.FILL, SWT.FILL, true, true);
        Text text = new Text(group, SWT.WRAP);
        text.setLayoutData(text_grid_data);
        text.setEditable(false);
        text.setText(suggestedFix);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
            	Point computeSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
                text_grid_data.minimumHeight = computeSize.y;
                text_grid_data.minimumWidth = computeSize.x;
                group.layout();
                parent.layout();
                scrolledComposite.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
        });
        
        button.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
            	try {               
            	    IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            	    if ( part instanceof ITextEditor ) {
            	        final ITextEditor editor = (ITextEditor)part;
            	        IDocumentProvider prov = editor.getDocumentProvider();
            	        IDocument doc = prov.getDocument( editor.getEditorInput() );
            	        ISelection sel = editor.getSelectionProvider().getSelection();
            	        if ( sel instanceof TextSelection ) {
            	            final TextSelection textSel = (TextSelection)sel;
            	            String newText = "/* [Pre-Fix] */\n" + 
            	            				 "//" + textSel.getText().replaceAll("\n", "\n//") + "\n" +
            	            			     "/* [Post-Fix] */\n" + 
            	            				  text.getText() + "\n";
            	            doc.replace( textSel.getOffset(), textSel.getLength(), newText );
            	        }
            	    }
            	} catch ( Exception ex ) {
            	    ex.printStackTrace();
            	}  
            }
        });
    }
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}

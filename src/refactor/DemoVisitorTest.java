package refactor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import refactor.ControlFlowAnalysis;
import refactor.JdtAstUtil;  
import refactor.DemoVisitor;
public class DemoVisitorTest {  
	public DemoVisitor visitor;
    public DemoVisitorTest(String source) {  
        CompilationUnit comp = JdtAstUtil.getCompilationUnit(source);  
        visitor = new DemoVisitor();  
        comp.accept(visitor);  
        printWarnings(comp,visitor.root);
    }
    public String newCode()
    {
         return visitor.root.getAstNode().toString();
    }
    public static void printAst(AstTreeNode visitorRoot)
    {
    	AstTreeNode root=visitorRoot;
    	if(root==null)
    		throw new IllegalArgumentException("invalid ast: null");
    	else
    	{
    		for(AstTreeNode child:root.getChildren())
    		{
    			System.out.println(child.getAstNode().toString());
    			printAst(child);
    		}
    	}
    }
    public void printWarnings(CompilationUnit comp,AstTreeNode root)
    {
    	ControlFlowAnalysis controlFlowAnalysis=new ControlFlowAnalysis();
        controlFlowAnalysis.detectDeadCode(comp,visitor.root);
    }
}

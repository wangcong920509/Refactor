package refactor;
import java.util.Stack;
import org.eclipse.jdt.core.dom.*;
import refactor.AstTreeNode;
public class DemoVisitor extends ASTVisitor {
	public AstTreeNode root;
	public Stack<AstTreeNode> stack=new Stack<AstTreeNode>();
	public void preVisit(ASTNode node)
    {
		AstTreeNode curNode=new AstTreeNode(node);
    	if(stack.isEmpty()==false)
    	{
    		AstTreeNode head=stack.peek();
    		head.addChild(curNode);
    	}
    	stack.push(curNode);
    }
    public void postVisit(ASTNode node)
    {
    	if(stack.size()==1)
    		root=stack.pop();
    	else
    		stack.pop();
    }
}
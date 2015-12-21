package refactor;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
public class AstTreeNode {
	private ASTNode node;
	private AstTreeNode parent;
	private List<AstTreeNode> children = new ArrayList<AstTreeNode>();
	public AstTreeNode(ASTNode node) {
		this.node = node;
	}
	public ASTNode getAstNode() {
		return node;
	}
	public List<AstTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<AstTreeNode> childrenValue) {
		for(AstTreeNode temp:childrenValue)
		{
			this.addChild(temp);
		}
	}
	public int countChildren() {
		return children.size();
	}
	public AstTreeNode getChild(int i) {
		return children.get(i);
	}
	public boolean hasChildren() {
		return countChildren() > 0;
	}
	// for chaining
	public AstTreeNode addChild(AstTreeNode child) {
		children.add(child);
		child.parent = this;
		return this;
	}
	public AstTreeNode getParent() {
		return parent;
	}
	public String getAstNodeType() {
		String type=node.getClass().getSimpleName();
		return (node == null) ? "NULL_AST" : type.substring(0, type.length());
	}
	
}
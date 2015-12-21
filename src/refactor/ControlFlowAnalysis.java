package refactor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.eclipse.jdt.core.dom.CompilationUnit;

import refactor.AstTreeNode;

public class ControlFlowAnalysis {
	Queue<String> warningList=new LinkedList<String>();
	
	public Queue<String> getWarningList()
	{
		return warningList;
	}
	
	public void clearList()
	{
		warningList.clear();
	}
	
	AstTreeNode selectionNode(AstTreeNode root){
		AstTreeNode selectRoot=new AstTreeNode(root.getAstNode());
		if(root.getAstNodeType().equals("IfStatement"))
		{
			selectRoot.addChild(new AstTreeNode(root.getChild(0).getAstNode()));
			selectRoot.addChild(new AstTreeNode(root.getChild(0).getAstNode()));
			
			if(root.countChildren()==2)
			{
				AstTreeNode temp=statementNode(root.getChild(1));
				selectRoot.getChild(0).setChildren(temp.getChildren());
			}
			
			if(root.countChildren()==3)
			{
				AstTreeNode temp=statementNode(root.getChild(1));
				selectRoot.getChild(0).setChildren(temp.getChildren());
				temp=statementNode(root.getChild(2));
				selectRoot.getChild(1).setChildren(temp.getChildren());
			}
		}
		return selectRoot;
	}
	
	AstTreeNode iterationNode(AstTreeNode root){
		AstTreeNode iterRoot=new AstTreeNode(root.getAstNode());
		if(root.getAstNodeType().equals("ForStatement")
				||root.getAstNodeType().equals("WhileStatement"))
		{
			AstTreeNode temp=statementNode(root.getChild(root.countChildren()-1));
			iterRoot.setChildren(temp.getChildren());
		}
		else
		{
			AstTreeNode temp=statementNode(root.getChild(0));
			iterRoot.setChildren(temp.getChildren());
		}
		return iterRoot;
	}
	
	AstTreeNode statementNode(AstTreeNode root){
		AstTreeNode stmtNode=new AstTreeNode(root.getAstNode());
		Stack<AstTreeNode> stack=new Stack<AstTreeNode>();
		stack.push(root);
		while(stack.isEmpty()!=true)
		{
			AstTreeNode oldNode=stack.pop();
			AstTreeNode tempNode=new AstTreeNode(oldNode.getAstNode());
			
			switch(tempNode.getAstNodeType())
			{
			case "VariableDeclarationStatement":
				stmtNode.addChild(tempNode);
				break;
			case "ExpressionStatement":
				stmtNode.addChild(tempNode);
				break;
			case "BreakStatement":
			case "ContinueStatement":
			case "ReturnStatement":
				stmtNode.addChild(tempNode);
				break;
			case "ForStatement":
			case "WhileStatement":
			case "DoStatement":
				stmtNode.addChild(iterationNode(oldNode));
				break;
			case "IfStatement":
				stmtNode.addChild(selectionNode(oldNode));
				break;
			case "Block":
				for(int i=oldNode.countChildren()-1;i>=0;i--)
				{
					stack.push(oldNode.getChild(i));
				}
				break;
			default:
				;
			}
		}
		return stmtNode;
	}
	
	private AstTreeNode generatePDG(AstTreeNode root){
		AstTreeNode pdgRoot=new AstTreeNode(root.getAstNode());
		for(int i=0;i<root.countChildren();i++)
		{
			if(root.getChild(i).getAstNodeType().equals("Block"))
			{
				AstTreeNode temp=statementNode(root.getChild(i));
				pdgRoot.setChildren(temp.getChildren());
				break;
			}
		}
		return pdgRoot;
	}
	
	public String getFilename()
    {
		
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName();
    }
	
	public int getLineNum(CompilationUnit comp,AstTreeNode node)
	{
		int lineNumber = comp.getLineNumber(node.getAstNode().getStartPosition());
		return lineNumber;
	}
	
	private int detectIfDeadCode(CompilationUnit comp,AstTreeNode ifNode,String[] location)
	{
		int countJumpStmt=0;
		int [] locationRecorder=new int[2];
		locationRecorder[0]=locationRecorder[1]=-1;
		for(int i=0;i<ifNode.countChildren();i++)
		{
			String[] loca=new String[1];
			int tempcount=countJumpStmt;
			boolean locaflag=false;
			Stack<AstTreeNode> stack2=new Stack<AstTreeNode>();
			stack2.push(ifNode.getChild(i));
			while(stack2.isEmpty()!=true)
			{
				AstTreeNode head=stack2.pop();
				if(head.countChildren()>0&&
						head.getAstNodeType().equals("ForStatement")==false&&
						head.getAstNodeType().equals("WhileStatement")==false&&
						head.getAstNodeType().equals("DoStatement")==false&&
						head.getAstNodeType().equals("IfStatement")==false)
				{
					for(int j=head.countChildren()-1;j>=0;j--)
					{
						stack2.push(head.getChild(j));
					}
				}
				if(countJumpStmt==tempcount&&
						(head.getAstNodeType().equals("BreakStatement")||
						head.getAstNodeType().equals("ContinueStatement")||
						head.getAstNodeType().equals("ReturnStatement")))
				{
					if(head.getAstNodeType().equals("ReturnStatement"))
						countJumpStmt+=3;
					else
						countJumpStmt++;
					locationRecorder[i]=getLineNum(comp,head);
					if(location[0]==null)
					{
						location[0]=String.valueOf(locationRecorder[i])+"  ";
					}
					else
					{
						location[0]=location[0]+String.valueOf(locationRecorder[i])+"  ";
					}
				}
				if(head.getAstNodeType().equals("IfStatement")==true)
				{
					if(head.countChildren()==2)
					{
						if(countJumpStmt==tempcount)
						{
							countJumpStmt=countJumpStmt+detectIfDeadCode(comp,head,loca);
							if(countJumpStmt==tempcount)
							{
								loca[0]="";
							}
						}
						else
						{
							String[] temploca=new String[1];
							detectIfDeadCode(comp,head,temploca);
						}
						if(countJumpStmt>tempcount&&locaflag==false)
						{
							if(location[0]==null)
							{
								location[0]=loca[0];
							}
							else
							{
								location[0]=location[0]+loca[0];
							}
							locaflag=true;
						}
					}
				}
			}
		}
		String filename=getFilename();
		if((countJumpStmt==2)||(countJumpStmt==4))
		{
			boolean flag1=false;
			AstTreeNode itertemp1=ifNode;
			while(itertemp1.getParent()!=null)
			{
				AstTreeNode tempParent1=itertemp1.getParent();
				if(tempParent1.getAstNodeType().equals("IfStatement"))
				{
					break;
				}
				for(int i=0;i<tempParent1.countChildren();i++)
				{
					if(tempParent1.getChild(i).equals(itertemp1))
					{
						if(i<tempParent1.countChildren()-1)
						{
							flag1=true;
							warningList.add("In file "+filename+" line:"+location[0]+"warning!����ת��������ֵܽڵ�Ϊ�����룡");
							for(int j=i+1;j<tempParent1.countChildren();j++)
							{
								tempParent1.getChild(j).getAstNode().delete();
							}
						}
						break;
					}
				}
				if(flag1==true||
						tempParent1.getAstNodeType().equals("ForStatement")==true||
						tempParent1.getAstNodeType().equals("WhileStatement")==true||
						tempParent1.getAstNodeType().equals("DoStatement")==true)
					break;
				itertemp1=tempParent1;
			}
		}
		if(countJumpStmt==6)
		{
			boolean flag2=false;
			AstTreeNode itertemp2=ifNode;
			while(itertemp2.getParent()!=null)
			{
				AstTreeNode tempParent2=itertemp2.getParent();
				if(tempParent2.getAstNodeType().equals("IfStatement"))
				{
					break;
				}
				for(int i=0;i<tempParent2.countChildren();i++)
				{
					if(tempParent2.getChild(i).equals(itertemp2))
					{
						if(i<tempParent2.countChildren()-1)
						{
							flag2=true;
							warningList.add("In file "+filename+" line:"+location[0]+"warning!����ת��������ֵܽڵ�Ϊ�����룡");
							for(int j=i+1;j<tempParent2.countChildren();j++)
							{
								tempParent2.getChild(j).getAstNode().delete();
							}
						}
						break;
					}
				}
				if(flag2==true||
						tempParent2.getAstNodeType().equals("MethodDeclaration")==true)
					break;
				itertemp2=tempParent2;
			}
		}
		if(countJumpStmt==2||countJumpStmt==4)
		{
			return 1;
		}
		else
		{
			if(countJumpStmt==6)
			{
				return 3;
			}
			else
			{
				return 0;
			}
		}
	}
	
	public AstTreeNode detectDeadCode(CompilationUnit comp,AstTreeNode root){
		Stack<AstTreeNode> stack=new Stack<AstTreeNode>();
		AstTreeNode pdg=new AstTreeNode(root.getAstNode());
		for(int i=root.countChildren()-1;i>=0;i--)
		{
			stack.push(root.getChild(i));
		}
		while(stack.isEmpty()==false)
		{
			AstTreeNode curRoot=stack.pop();
			Queue<AstTreeNode> queue=new LinkedList<AstTreeNode>();
			queue.add(curRoot);
			AstTreeNode tempPdg=new AstTreeNode(curRoot.getAstNode());
			String filename=getFilename();
			while(queue.isEmpty()!=true)
			{
				AstTreeNode node=queue.poll();
				if(node.countChildren()>0)
				{
					for(int i=0;i<node.countChildren();i++)
					{
						queue.add(node.getChild(i));
					}
				}
				
				if(node.getAstNodeType().equals("MethodDeclaration"))
				{
					AstTreeNode entry=generatePDG(node);
					tempPdg.addChild(entry);
					Queue<AstTreeNode> queue2=new LinkedList<AstTreeNode>();
					queue2.add(entry);
					
					while(queue2.isEmpty()!=true)
					{
						AstTreeNode tempNodeInFunc=queue2.poll();
						if(tempNodeInFunc.countChildren()>0)
						{
							for(int i=0;i<tempNodeInFunc.countChildren();i++)
							{
								queue2.add(tempNodeInFunc.getChild(i));
							}
						}
						AstTreeNode tempparent=null;
						if(tempNodeInFunc.getParent()!=null)
						{
							tempparent=tempNodeInFunc.getParent();
						}
						
						switch(tempNodeInFunc.getAstNodeType())
						{
						case "ContinueStatement":
						case "BreakStatement":
						case "ReturnStatement":
							for(int i=0;i<tempparent.countChildren();i++)
							{
								if(tempparent.getAstNodeType().equals("ForStatement")||
												tempparent.getAstNodeType().equals("WhileStatement")||
												tempparent.getAstNodeType().equals("DoStatement"))
								{
									if(tempparent.getChild(i).equals(tempNodeInFunc)==false&&
											(tempparent.getChild(i).getAstNodeType().equals("IfStatement")))
									{
										break;
									}
								}
								if(tempparent.getChild(i).equals(tempNodeInFunc))
								{
									if(i<tempparent.countChildren()-1&&
											tempparent.getChild(i+1).getAstNodeType().equals("LabeledStatement")==false)
									{
										warningList.add("In file "+filename+" line:"+String.valueOf(getLineNum(comp,tempNodeInFunc))+"  warning!��ת������ֵܽڵ�Ϊ�����룡");
										for(int j=i+1;j<tempparent.countChildren();j++)
										{
											tempparent.getChild(j).getAstNode().delete();
										}
									}
									break;
								}
							}
							break;
						case "IfStatement":
							boolean flag=false;
							AstTreeNode itertemp=tempNodeInFunc;
							while(itertemp.getParent()!=null)
							{
								AstTreeNode tempParent=itertemp.getParent();
								if(tempParent.getAstNodeType().equals("IfStatement"))
								{
									flag=true;
									break;
								}
								if(flag==true||
										tempParent.getAstNodeType().equals("MethodDeclaration")==true||
										tempParent.getAstNodeType().equals("ForStatement")==true||
										tempParent.getAstNodeType().equals("WhileStatement")==true||
										tempParent.getAstNodeType().equals("DoStatement")==true)
									break;
								itertemp=tempParent;
							}
							String[] location=new String[1];
							if(flag==false)
							{
								detectIfDeadCode(comp,tempNodeInFunc,location);
							}
							break;
						default:
								;
						}
					}
				}
			}
			pdg.addChild(tempPdg);
		}
		return pdg;
	}
}

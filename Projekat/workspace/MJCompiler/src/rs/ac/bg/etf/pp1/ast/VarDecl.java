// generated with ast extension for cup
// version 0.8
// 25/11/2021 12:58:50


package rs.ac.bg.etf.pp1.ast;

public class VarDecl implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private String varName;
    private OptionalBrackets OptionalBrackets;

    public VarDecl (String varName, OptionalBrackets OptionalBrackets) {
        this.varName=varName;
        this.OptionalBrackets=OptionalBrackets;
        if(OptionalBrackets!=null) OptionalBrackets.setParent(this);
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName=varName;
    }

    public OptionalBrackets getOptionalBrackets() {
        return OptionalBrackets;
    }

    public void setOptionalBrackets(OptionalBrackets OptionalBrackets) {
        this.OptionalBrackets=OptionalBrackets;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(OptionalBrackets!=null) OptionalBrackets.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(OptionalBrackets!=null) OptionalBrackets.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(OptionalBrackets!=null) OptionalBrackets.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarDecl(\n");

        buffer.append(" "+tab+varName);
        buffer.append("\n");

        if(OptionalBrackets!=null)
            buffer.append(OptionalBrackets.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarDecl]");
        return buffer.toString();
    }
}
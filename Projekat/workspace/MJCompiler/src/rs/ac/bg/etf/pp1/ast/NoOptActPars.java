// generated with ast extension for cup
// version 0.8
// 12/0/2022 22:19:23


package rs.ac.bg.etf.pp1.ast;

public class NoOptActPars extends OptActPars {

    public NoOptActPars () {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("NoOptActPars(\n");

        buffer.append(tab);
        buffer.append(") [NoOptActPars]");
        return buffer.toString();
    }
}

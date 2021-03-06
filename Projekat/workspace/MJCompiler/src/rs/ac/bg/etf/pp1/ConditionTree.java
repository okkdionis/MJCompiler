package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.ast.CondFact;
import rs.etf.pp1.mj.runtime.Code;

public class ConditionTree {
	
	private boolean isDoWhileStmt = false;			// Whether the condition is used in DO..WHILE or IF..ELSE statement
	
	private int ifStartAdr;
	private int elseStartAdr = -1;
	private int stmtEndAdr;
	private int condStartAddr;
	
	private List<ConditionFactor> currentCondFactors = new ArrayList<>();
	private List<ConditionTerm> currentCondTerms = new ArrayList<>();
	private SingleCondition condition;
	
	private List<Integer> breakAddrs = new ArrayList<>();
	private List<Integer> continueAddrs = new ArrayList<>();
	
	public ConditionTree(boolean isDoWhileStmt) {
		this.isDoWhileStmt = isDoWhileStmt;
	}
	
	public boolean isDoWhileStmt() {
		return isDoWhileStmt;
	}

	public void setDoWhileStmt(boolean isDoWhileStmt) {
		this.isDoWhileStmt = isDoWhileStmt;
	}

	public void addFactor(int adr, int relOp) {
		currentCondFactors.add(new ConditionFactor(adr, relOp));
	}
	
	public void addTerm() {
		currentCondTerms.add(new ConditionTerm(currentCondFactors));
		currentCondFactors = new ArrayList<>();
	}
	
	public void endCondition() {
		condition = new SingleCondition(currentCondTerms);
		currentCondTerms = new ArrayList<>();
	}
	
	public void setIfStartAdr() {
		ifStartAdr = Code.pc;
	}
	
	public void setElseStartAdr() {
		// If statement finished here, so jump over else statement
		Code.putJump(0);
		
		// Set else statement start address
		elseStartAdr = Code.pc;
	}
	
	public void setStmtEndAdr() {
		stmtEndAdr = Code.pc;
	}
	
	public void setCondStartAdr() {
		// Condition start address for do while loop
		condStartAddr = Code.pc;
	}
	
	public void addBreak() {
		if (isDoWhileStmt)
			breakAddrs.add(Code.pc - 2);		// Because the address is 2 bytes long
	}
	
	public void addContinue() {
		if (isDoWhileStmt)
			continueAddrs.add(Code.pc - 2);		// Because the address is 2 bytes long
	}
	
	private void patchAddr(int buffPos, int jmpAddr) {
		int pc = Code.pc;
		Code.pc = jmpAddr;
		Code.fixup(buffPos);
		Code.pc = pc;
	}
	
	public void fixCondition() {
		List<ConditionTerm> condTerms = condition.getConditionTerms();
	
		// If there is else part, fix the jump address over else statement
		if (elseStartAdr != -1)
			patchAddr(elseStartAdr - 2, stmtEndAdr);	// Because the address is 2 bytes long

		// If there are any break or continue statements, fix their jump addresses
		for (Integer addrStart: breakAddrs) {
			patchAddr(addrStart, stmtEndAdr);
		}
		
		for (Integer addrStart: continueAddrs) {
			patchAddr(addrStart, condStartAddr);
		}
		
		for (int i = 0; i < condTerms.size() - 1; i++) {
			fixTerm(condTerms.get(i), condTerms.get(i).getEndAddr(), ifStartAdr, false);
		}
		
		// If there is else part, jump to start of else statement, otherwise jump to if statement end
		int nextAdr = elseStartAdr != -1 ? elseStartAdr : stmtEndAdr;
		
		fixTerm(condTerms.get(condTerms.size() - 1), nextAdr, ifStartAdr, true);
	}
	
	private void fixTerm(ConditionTerm condTerm, int nextAdr, int stmtStartAdr, boolean lastCondTerm) {
		List<ConditionFactor> condFactors = condTerm.getConditionFactors();
		
		/*
		 *  For all the condition factors, except the last one
		 *  If the condition factor is false, whole condition term is false, so jump to the next condition term 
		 */
		for (int i = 0; i < condFactors.size() - 1; i++) {
			fixFactor(condFactors.get(i), nextAdr, false);
		}
		
		/*
		 * 	==========================================IF ELSE===============================================================
		 *  If we made it to the last condition factor, that means all previous factors were true
		 *  If it is a last condition term, jump over the then statement body if the last condition factor is false
		 *  If it is not a last condition term, jump to the then statement body if the last condition factor is true
		 *  
		 * 	=========================================DO WHILE===============================================================
		 * 	In case of DO..WHILE, we have to jump to statement start regardless of factor term being last or not
		 */
		if (lastCondTerm && !isDoWhileStmt)
			fixFactor(condFactors.get(condFactors.size() - 1), nextAdr, false);
		else
			fixFactor(condFactors.get(condFactors.size() - 1), stmtStartAdr, true);
	}
	
	private void fixFactor(ConditionFactor condFactor, int adr, boolean inverse) {
		// Save pc
		int pc = Code.pc;
		
		// Fix relOp
		if (inverse) {
			Code.pc = condFactor.getAddr();
			int cod = Code.buf[Code.pc];
			Code.put(Code.jcc + Code.inverse[condFactor.getRelOp()]);
		}
	
		// Fix jump address
		Code.pc = adr;
		Code.fixup(condFactor.getAddr() + 1);		// Because the first byte is the instruction (e.g. jeq)
		
		// Restore pc
		Code.pc = pc;
	}
	
	private static class SingleCondition {
		private List<ConditionTerm> conditionTerms;

		public SingleCondition(List<ConditionTerm> conditionTerms) {
			super();
			this.conditionTerms = conditionTerms;
		}

		public List<ConditionTerm> getConditionTerms() {
			return conditionTerms;
		}

		public void setConditionTerms(List<ConditionTerm> conditionTerms) {
			this.conditionTerms = conditionTerms;
		}
	}
	
	private static class ConditionTerm {
		private List<ConditionFactor> conditionFactors;

		public ConditionTerm(List<ConditionFactor> conditionFactors) {
			super();
			this.conditionFactors = conditionFactors;
		}

		public List<ConditionFactor> getConditionFactors() {
			return conditionFactors;
		}

		public void setConditionFactors(List<ConditionFactor> conditionFactors) {
			this.conditionFactors = conditionFactors;
		}
		
		public int getEndAddr() {
			return conditionFactors.get(conditionFactors.size() - 1).getAddr() + 3;	// Because jcc instruction is 3 bytes long
		}
	}
	
	private static class ConditionFactor {
		private int relOp;
		private int addr;
		
		public ConditionFactor(int addr, int relOp) {
			super();
			this.addr = addr;
			this.relOp = relOp;
		}
		
		public int getAddr() {
			return addr;
		}
		public void setAddr(int addr) {
			this.addr = addr;
		}

		public int getRelOp() {
			return relOp;
		}

		public void setRelOp(int relOp) {
			this.relOp = relOp;
		}
	}
}

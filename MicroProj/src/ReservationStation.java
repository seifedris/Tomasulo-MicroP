
public class ReservationStation {

	int size;
	RSCell[] rs;
	int type; // 0 for multiplier, 1 for adder, 2 for integer
	int latency;
	int tagNum = 0;
	Bus bus = Bus.getInstance();

	public ReservationStation(int size, int type, int latency) {
		this.size = size;
		this.latency = latency;
		this.type = type;
		rs = new RSCell[size];
		for (int i = 0; i < size; i++) {
			rs[i] = new RSCell();
		}

	}

	public void add(String tag, String op, int vj, int vk, String qj, String qk) {
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 0) {
				this.rs[i].tag = tag;
				this.rs[i].vj = vj;
				this.rs[i].vk = vk;
				this.rs[i].qj = qj;
				this.rs[i].qk = qk;
				this.rs[i].op = op;
				this.rs[i].busy = 1;
				
				break;

			}
		}
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public RSCell[] getRs() {
		return rs;
	}

	public void setRs(RSCell[] rs) {
		this.rs = rs;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public void print() {
		if (type == 0)
			System.out.println("Multiplier Buffer");
		else if (type == 1)
			System.out.println("Adder Buffer");
		else if (type == 2)
			System.out.println("Integer Buffer");
		for (int i = 0; i < size; i++) {
			System.out.print(rs[i].toString() + "\n");
		}

	}

	public boolean isEmpty() {
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 1) {
				return false;
			}
		}
		return true;
	}

	public void update() {
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 1 && rs[i].qj.equals("") && rs[i].qk.equals("")) {
				rs[i].executing = true;
			}
		}
	}



	public void fetchFromBus() {
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 1 && rs[i].qj.equals(bus.tag) &&!rs[i].qj.equals("")) {
				rs[i].qj = "";
				rs[i].vj = bus.value;
			}
			if (rs[i].busy == 1 && rs[i].qk.equals(bus.tag)&&!rs[i].qk.equals("")) {
				rs[i].qk = "";
				rs[i].vk = bus.value;
			}
		}
	}

	public boolean isFull() {
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 0) {
				return false;
			}
		}
		return true;
	}

	public String generateMulTag() {
		return "M" + (++tagNum);
	}

	public String generateAddTag() {
		if (type == 1)
			return "AD" + (++tagNum);
		return "AI" + (++tagNum);

	}

	public String generateIntTag() {
		return "I" + (++tagNum);
	}

	public String generateBranchTag() {
		return "B" + (++tagNum);
	}

	public boolean execute(boolean use_bus) {
		boolean bus_used = false;
		for (int i = 0; i < size; i++) {
			if (rs[i].busy == 1) {
				if (rs[i].executing == true) {
					if (type == 0 && rs[i].excCycles >= latency && !bus_used && use_bus) {
						bus.tag = rs[i].tag;
						if (rs[i].op.equals("MUL.D"))
							bus.value = rs[i].vk * rs[i].vj;
						if (rs[i].op.equals("DIV.D"))
							bus.value = rs[i].vj / rs[i].vk;
						rs[i] = new RSCell();
						bus_used = true;
	
					} else if (type == 1 && rs[i].excCycles >= latency && !bus_used && use_bus) {
						bus.tag = rs[i].tag;
						if (rs[i].op.equals("SUB.D"))
							bus.value = rs[i].vj - rs[i].vk;
						else if (rs[i].op.equals("ADD.D"))
							bus.value = rs[i].vk + rs[i].vj;
						rs[i] = new RSCell();
						bus_used = true;
						
					} else if (type == 2 && rs[i].excCycles >= latency && !bus_used && use_bus) {
						bus.tag = rs[i].tag;
						if (rs[i].op.equals("SUBI"))
							bus.value = rs[i].vj - rs[i].vk;
						else if (rs[i].op.equals("ADDI"))
							bus.value = rs[i].vj + rs[i].vk;
						else if (rs[i].tag.contains("B")) {
							if (rs[i].vj == 0) {
								bus.value = 0;
							} else {
								bus.value = 1;
							}
						}

						rs[i] = new RSCell();
						bus_used = true;

					} else if (rs[i].excCycles < latency)
						rs[i].excCycles++;
				}
			}
		}

		return bus_used;
	}

	class RSCell {
		String tag;
		int busy;
		String op;
		int vj;
		int vk;
		String qj;
		String qk;
		int instructionAddress;
		int a;
		boolean executing;
		int excCycles;

		public RSCell() {
			tag = "";
			busy = 0;
			op = "";
			vj = 0;
			vk = 0;
			qj = "";
			qk = "";
			instructionAddress = 0;
			a = 0;
			executing = false;
			excCycles = 0;
		}

		public RSCell(int busy, String op, int vj, int vk, String qj, String qk, int instructionAddress, int a) {
			if (type == 0) {
				tag = "M" + (++tagNum);
			} else if (type == 1) {
				tag = "A" + (++tagNum);
			} else if (type == 2) {
				tag = "I" + (++tagNum);
				if (op.equals("BNEZ"))
					tag = "B" + (++tagNum);
			}
			this.busy = busy;
			this.op = op;
			this.vj = vj;
			this.vk = vk;
			this.qj = qj;
			this.qk = qk;
			this.instructionAddress = instructionAddress;
			this.a = a;
		}

		public void setBusy(int busy) {
			this.busy = busy;
		}

		public void setOp(String op) {
			this.op = op;
		}

		public void setVj(int vj) {
			this.vj = vj;
		}

		public void setVk(int vk) {
			this.vk = vk;
		}

		public void setQj(String qj) {
			this.qj = qj;
		}

		public void setQk(String qk) {
			this.qk = qk;
		}

		public void setInstructionAddress(int instructionAddress) {
			this.instructionAddress = instructionAddress;
		}

		public void setA(int a) {
			this.a = a;
		}

		public int getBusy() {
			return busy;
		}

		public String getOp() {
			return op;
		}

		public int getVj() {
			return vj;
		}

		public int getVk() {
			return vk;
		}

		public String getQj() {
			return qj;
		}

		public String getQk() {
			return qk;
		}

		public int getInstructionAddress() {
			return instructionAddress;
		}

		public int getA() {
			return a;
		}

		public String toString() {
			return "tag: " + tag + " busy: " + busy + " op: " + op + " vj: " + vj + " vk: " + vk + " qj: " + qj
					+ " qk: " + qk + " instructionAddress: " + instructionAddress + " a: " + a + " excCycles: "
					+ excCycles;
		}

	}

}

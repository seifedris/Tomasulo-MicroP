public class StoreBuffer {

	int size;
	int latency;
	storeCell[] storeBuffer;
	static int tagNum = 0;
	Bus bus = Bus.getInstance();

	public StoreBuffer(int size, int latency) {
		this.size = size;
		this.latency = latency;
		storeBuffer = new storeCell[size];
		for (int i = 0; i != size; i++) {
			storeBuffer[i] = new storeCell();
		}
	}

	public void fetchFromBus() {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].busy == 1 && storeBuffer[i].Q.equals(bus.tag) && storeBuffer[i].Q != null
					&& storeBuffer[i].Q != "") {
				storeBuffer[i].Q = "";
				storeBuffer[i].V = bus.value;
			}
		}
	}

	public void add(String tag, int address, int V, String Q) {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].busy == 0) {
				storeBuffer[i].tag = tag;
				storeBuffer[i].address = address;
				storeBuffer[i].V = V;
				storeBuffer[i].Q = Q;
				storeBuffer[i].busy = 1;
				break;
			}
		}
	}

	public String generateTag() {
		return "S" + (++tagNum);
	}

	

	public boolean isFull() {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].busy == 0) {
				return false;
			}
		}
		return true;
	}

	public void print() {
		for (int i = 0; i < size; i++) {
			System.out.println("Store Buffer " + i + ":");
			System.out.print("Tag: " + storeBuffer[i].tag);
			System.out.print(" Busy: " + storeBuffer[i].busy);
			System.out.print(" Address: " + storeBuffer[i].address);
			System.out.print(" V: " + storeBuffer[i].V);
			System.out.print(" Q: " + storeBuffer[i].Q);
			
			System.out.print(" Executing: " + storeBuffer[i].executing);
			System.out.println(" excCycles: " + storeBuffer[i].excCycles);
		}
	}

	public void update() {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].Q.equals("") && storeBuffer[i].busy==1)
				storeBuffer[i].executing = true;
		}
	}

	public void execute() {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].busy == 1 && storeBuffer[i].executing == true) {

				if (storeBuffer[i].excCycles == latency) {
					Cache.getInstance().write(storeBuffer[i].address, storeBuffer[i].V);
					storeBuffer[i].busy = 0;

					storeBuffer[i] = new storeCell();
				} else
					storeBuffer[i].excCycles++;
			}
		}
	}

	public boolean isEmpty() {
		for (int i = 0; i < size; i++) {
			if (storeBuffer[i].busy == 1) {
				return false;
			}
		}
		return true;
	}

	class storeCell {
		String tag;
		public int address;
		public int V;
		public String Q;
		public int busy;
		public int excCycles;
		public boolean executing;

		public storeCell(int address, int V, String Q, int busy) {
			tag = "S" + (++tagNum);
			this.address = address;
			this.V = V;
			this.Q = Q;
			this.busy = busy;
			excCycles = 0;
			executing = false;
		}

		public storeCell() {
			this.tag = "";
			this.address = 0;
			this.V = 0;
			this.Q = "";
			this.busy = 0;
		}
	}

}

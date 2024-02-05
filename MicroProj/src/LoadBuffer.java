public class LoadBuffer {

	int size;
	loadCell[] loadBuffer;
	int latency;
	static int tagNum = 0;
	Bus bus = Bus.getInstance();

	public LoadBuffer(int size, int latency) {
		this.size = size;
		this.latency = latency;
		loadBuffer = new loadCell[size];
		for (int i = 0; i < size; i++) {
			loadBuffer[i] = new loadCell();
		}
	}

	public boolean isFull() {
		for (int i = 0; i < size; i++) {
			if (loadBuffer[i].busy == 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isEmpty() {
		for (int i = 0; i < size; i++) {
			if (loadBuffer[i].busy == 1) {
				return false;
			}
		}
		return true;
	}

	public void update() {
		for (int i = 0; i < size; i++) {
			if (loadBuffer[i].busy == 1) {
				loadBuffer[i].executing = true;
			}
		}
	}

	public void print() {
		for (int i = 0; i < size; i++) {
			System.out.print("Load Buffer " + i + ": ");
			System.out.print("Tag: " + loadBuffer[i].tag);
			// System.out.println("Register: " + loadBuffer[i].register);
			System.out.print(" Address: " + loadBuffer[i].address);
			// System.out.println("Value: " + loadBuffer[i].value);
			System.out.print(" Busy: " + loadBuffer[i].busy);
			System.out.print(" Executing: " + loadBuffer[i].executing);
			System.out.println(" # cycles execution: " + loadBuffer[i].excCycles);
		}
	}

	public boolean add(String tag, int address) {
		for (int i = 0; i < size; i++) {
			if (loadBuffer[i].busy == 0) {
				loadBuffer[i].busy = 1;
				loadBuffer[i].tag = tag;
				loadBuffer[i].address = address;
				return true;
			}
		}
		return false;
	}



	public String generateTag() {
		return "L" + (++tagNum);
	}

	public boolean execute(boolean use_bus) {
		boolean bus_used = false;
		for (int i = 0; i < size; i++) {
			if (loadBuffer[i].busy == 1 && loadBuffer[i].executing == true) {
				if (loadBuffer[i].excCycles >= latency && use_bus && !bus_used) {
					loadBuffer[i].value = Cache.getInstance().read((int) loadBuffer[i].address);
					
					bus.value = loadBuffer[i].value;
					bus.tag = loadBuffer[i].tag;
					
					bus_used = true;
					System.out.println("tag:"+loadBuffer[i].tag);
					loadBuffer[i] = new loadCell();
					
					
				} else if (loadBuffer[i].excCycles < latency)
					loadBuffer[i].excCycles++;

			}
		}
		return bus_used;
	}

	class loadCell {
		String tag;

		public Object address;
		public int value;
		public int busy;
		public int excCycles;
		public boolean executing;

		public loadCell() {
			tag = "";
			address = 0;
			value = 0;
			busy = 0;
			excCycles = 0;
			executing = false;
		}

		public loadCell(String tag, int register, int address, int value, int busy) {
			this.tag = tag;
			this.address = address;
			this.value = value;
			this.busy = busy;
		}

//        public void setRegister(int register) {
//            this.register = register;
//        }

		public void setAddress(int address) {
			this.address = address;
		}

	}

}

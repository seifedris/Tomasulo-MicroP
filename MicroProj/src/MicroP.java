import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MicroP {

	RegisterFile registerFile;

	ReservationStation FPAdderStation;
	ReservationStation FPMultiplierStation;
	ReservationStation IntAdderStation;
	boolean stalling = false;
	boolean fetched = false;
	LoadBuffer loadBuffer;
	StoreBuffer storeBuffer;
	Cache cache;
	Bus bus;
	ArrayList<String> instructions;
	boolean deadEnd=false;

	int fpAdderLatency;

	int fpMultiplierLatency;

	int loadLatency;

	int storeLatency;

	int cycle = 0;
	int PC = 0;

	public MicroP(int fpAdderSize, int fpMultiplierSize, int intAdderSize, int loadSize, int storeSize,
			int fpAdderLatency, int fpMultiplierLatency, int loadLatency, int storeLatency) {
		registerFile = RegisterFile.getInstance();
		instructions = new ArrayList<String>();
		cycle = 0;
		FPAdderStation = new ReservationStation(fpAdderSize, 1, fpAdderLatency);
		FPMultiplierStation = new ReservationStation(fpMultiplierSize, 0, fpMultiplierLatency);
		IntAdderStation = new ReservationStation(intAdderSize, 2, 1);
		loadBuffer = new LoadBuffer(loadSize, loadLatency);
		storeBuffer = new StoreBuffer(storeSize, storeLatency);
		cache = Cache.getInstance();
		bus = Bus.getInstance();

		// TODO: load instructions from file

	}

	public void loadInstructions() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("input.txt"));

		String inst = br.readLine();
		if (inst == null)
			return;

		String[] str = inst.split("\\\\s+|, ");

		while (inst != null) {
			str = inst.split(" ");
			if (str[0].contains("MUL.D") || str[0].contains("DIV.D") || str[1].contains("MUL.D")
					|| str[1].contains("DIV.D")) {
				instructions.add(FPMultiplierStation.generateMulTag() + " " + inst);
			} else if (str[0].contains("L.D") || str[1].contains("L.D")) {
				instructions.add(loadBuffer.generateTag() + " " + inst);
			} else if (str[0].contains("S.D") || str[1].contains("S.D")) {
				instructions.add(storeBuffer.generateTag() + " " + inst);
			} else if (str[0].contains("BNEZ") || str[1].contains("BNEZ")) {
				instructions.add(IntAdderStation.generateBranchTag() + " " + inst);
			} else if (str[0].contains("ADD.D") || str[0].contains("SUB.D") || str[1].contains("ADD.D")
					|| str[1].contains("SUB.D")) {
				instructions.add(FPAdderStation.generateAddTag() + " " + inst);
			} else if (str[0].contains("ADDI") || str[0].contains("SUBI") || str[1].contains("ADDI")
					|| str[1].contains("SUBI")) {
				instructions.add(IntAdderStation.generateAddTag() + " " + inst);
			}
			inst = br.readLine();
		}
		br.close();
	}

	public boolean fetchInstruction() {
		if (PC >= instructions.size())
			return false;
		int i = PC;
		int k = 0;
		String[] str = instructions.get(i).split("\\s+|, ");
		if (!(str[1].contains("L.D") || str[1].contains("S.D") || str[1].contains("BNEZ") || str[1].contains("MUL.D")
				|| str[1].contains("DIV.D") || str[1].contains("SUB.D") || str[1].contains("ADD.D")
				|| str[1].contains("SUBI") || str[1].contains("ADDI")))
			k = 1;

		if (instructions.get(i).contains("MUL.D") || instructions.get(i).contains("DIV.D")) {
			if (FPMultiplierStation.isFull())
				return false;
			else {
				Object a = registerFile.read(Integer.parseInt(str[k + 3].substring(1)));
				Object b = registerFile.read(Integer.parseInt(str[k + 4].substring(1)));
				boolean busyA = false;
				boolean busyB = false;
				Object obj1;
				Object obj2;
				if (a instanceof Integer && b instanceof Integer)
					FPMultiplierStation.add(str[0], str[k + 1], Integer.parseInt(a.toString()),
							Integer.parseInt(b.toString()), "", "");
				else if (a instanceof Integer && b instanceof String)
					FPMultiplierStation.add(str[0], str[k + 1], Integer.parseInt(a.toString()), 0, "", b.toString());
				else if (a instanceof String && b instanceof Integer)
					FPMultiplierStation.add(str[0], str[k + 1], 0, Integer.parseInt(b.toString()), (String) a, "");
				else if (a instanceof String && b instanceof String)
					FPMultiplierStation.add(str[0], str[k + 1], 0, 0, a.toString(), b.toString());
				registerFile.write(Integer.parseInt(str[k + 2].substring(1)), str[0]);
				System.out.println(str[0]);
			}
		}

		else if (instructions.get(i).contains("L.D")) {
			if (loadBuffer.isFull())
				return false;
			else {
				loadBuffer.add(str[0], Integer.parseInt(str[k + 3]));
				registerFile.write(Integer.parseInt(str[k + 2].substring(1)), str[0]);
			}

		} else if (instructions.get(i).contains("S.D")) {

			if (storeBuffer.isFull())
				return false;
			else {
				Object a = registerFile.read(Integer.parseInt(str[k + 2].substring(1)));
				if (a instanceof Integer)
					storeBuffer.add(str[0], Integer.parseInt(str[k + 3]), (int) a, "");
				else
					storeBuffer.add(str[0], Integer.parseInt(str[k + 3]), 0, (String) a);
			}

		} else if (instructions.get(i).contains("BNEZ")) {
			if (IntAdderStation.isFull())
				return false;
			else {
				stalling = true;
				Object a = registerFile.read(Integer.parseInt(str[k + 2].substring(1)));
				if (a instanceof Integer)
					IntAdderStation.add(str[0], "BNEZ", (int) a, 0, "", "");
				else {
					IntAdderStation.add(str[0], "BNEZ", 0, 0, (String) a, "");
				}
			}
		} else if (instructions.get(i).contains("ADD.D") || instructions.get(i).contains("SUB.D")) {
			if (FPAdderStation.isFull())
				return false;
			else {
				Object a = registerFile.read(Integer.parseInt(str[k + 3].substring(1)));
				Object b = registerFile.read(Integer.parseInt(str[k + 4].substring(1)));
				Object obj1;
				Object obj2;
				if (a instanceof Integer && b instanceof Integer)
					FPAdderStation.add(str[0], str[k + 1], (int) a, (int) b, "", "");
				else if (a instanceof Integer && b instanceof String)
					FPAdderStation.add(str[0], str[k + 1], (int) a, 0, "", (String) b);
				else if (a instanceof String && b instanceof Integer)
					FPAdderStation.add(str[0], str[k + 1], 0, (int) b, (String) a, "");
				else if (a instanceof String && b instanceof String)
					FPAdderStation.add(str[0], str[k + 1], 0, 0, (String) a, (String) b);
				registerFile.write(Integer.parseInt(str[k + 2].substring(1)), str[0]);
			}
		} else if (instructions.get(i).contains("ADDI") || instructions.get(i).contains("SUBI")) {
			if (IntAdderStation.isFull())
				return false;
			else {
				Object a = registerFile.read(Integer.parseInt(str[k + 2].substring(1)));
				try {
					a = Integer.parseInt((String) a.toString());
					IntAdderStation.add(str[0], str[k + 1], Integer.parseInt(a.toString()),
							Integer.parseInt(str[k + 4]), "", "");
				} catch (Exception e) {
					IntAdderStation.add(str[0], str[k + 1], 0, Integer.parseInt(str[k + 4]), (String) a, "");
				}

				registerFile.write(Integer.parseInt(str[k + 2].substring(1)), str[0]);
			}
		}

		System.out.println("Instruction being fetched: " + instructions.get(i));
		return true;
	}

	public void execute() throws IOException {
		boolean updated = false;
		boolean first = true;
		while (((!loadBuffer.isEmpty() || !storeBuffer.isEmpty() || !IntAdderStation.isEmpty()
				|| !FPMultiplierStation.isEmpty() || !FPAdderStation.isEmpty()) || first || PC < instructions.size())) {
			first = false;
			
			if(deadEnd)
				return;
			
			cycle++;
			System.out.println("\n\n\nCycle: " + cycle);
			if (!stalling)
				fetched = fetchInstruction();

			loadBuffer.update();
			storeBuffer.update();
			FPMultiplierStation.update();
			FPAdderStation.update();
			IntAdderStation.update();

			boolean usedl;
			boolean usedai;
			boolean usedad;

			usedl = loadBuffer.execute(true);
			if (!usedl)
				usedai = IntAdderStation.execute(true);
			else
				usedai = IntAdderStation.execute(false);
			if (!usedl && !usedai)
				usedad = FPAdderStation.execute(true);
			else
				usedad = FPAdderStation.execute(false);
			if (!usedad && !usedai && !usedl)
				FPMultiplierStation.execute(true);
			else
				FPMultiplierStation.execute(false);
			storeBuffer.execute();
			System.out.println("Stalling: " + stalling);

			storeBuffer.fetchFromBus();
			FPMultiplierStation.fetchFromBus();
			FPAdderStation.fetchFromBus();
			IntAdderStation.fetchFromBus();
			registerFile.fetchFromBus();
			System.out.print("\n");
			loadBuffer.print();
			System.out.print("\n");
			storeBuffer.print();
			System.out.print("\n");
			FPMultiplierStation.print();
			System.out.print("\n");
			FPAdderStation.print();
			System.out.print("\n");
			IntAdderStation.print();
			System.out.print("\n");
			registerFile.print();
			System.out.print("\n");
			cache.print();

			bus.print();

			if (bus.tag.contains("B"))
				stalling = false;

			if (fetched) {
				updatePC();
				
				
				

			}

		}

	}

	public boolean updatePC() throws IOException {
		if (bus.tag.contains("B") && bus.value == 1) {
			for (String inst : instructions) {
				if (inst.split("\\s+|, ")[0].contains(bus.tag)) {
					String str = "";
					if (inst.split("\\s+|, ")[1].equals("BNEZ"))
						str = inst.split("\\s+|, ")[3];
					else if(inst.split("\\s+|, ")[2].equals("BNEZ"))
						str = inst.split("\\s+|, ")[4];
					if(!str.equals("")) {
					for (int i = 0; i != instructions.size(); i++) {
						if (instructions.get(i).contains(str) && !instructions.get(i).contains(bus.tag)) {
							PC = i;
							System.out.print("PC " + PC);
							bus.tag="";
							instructions=new ArrayList<String>();
							this.loadInstructions();
							return true;

						}

					}}
					
				}
			}
			System.out.print("No such branch Address");
			deadEnd=true;
			return true;
			
		} else if (!stalling)
			PC++;
		return false;
	}

	public static void main(String[] args) throws IOException {
		// TODO code
		// TODO code
		// TODO code

		try (Scanner sc = new Scanner(System.in)) {
			System.out.println("Hello User");
			System.out.println("Enter the size of the load buffer : ");
			int loadSize = sc.nextInt();
			System.out.println("Enter the size of the store buffer : ");
			int storeSize = sc.nextInt();
			System.out.println("Enter the size of the FP Adder station : ");
			int fpAdderSize = sc.nextInt();
			System.out.println("Enter the size of the FP Multiplier station : ");
			int fpMultiplierSize = sc.nextInt();
			System.out.println("Enter the size of the Integer Adder station : ");
			int intAdderSize = sc.nextInt();

			// get latency for each instruction type
			System.out.println("Enter the latency of FP Adder : ");
			int fpAdderLatency = sc.nextInt();
			System.out.println("Enter the latency of FP Multiplier : ");
			int fpMultiplierLatency = sc.nextInt();
			System.out.println("Enter the latency of load : ");
			int loadLatency = sc.nextInt();
			System.out.println("Enter the latency of store : ");
			int storeLatency = sc.nextInt();
			MicroP microp = new MicroP(fpAdderSize, fpMultiplierSize, intAdderSize, loadSize, storeSize, fpAdderLatency,
					fpMultiplierLatency, loadLatency, storeLatency);

			int intAdderLatency = 1;
			int branchLatency = 1;
//			FileWriter wr = new FileWriter("input.txt");
//			wr.write("LOOP: L.D R0, 12 \n" + "DIV.D R4, R12, R4 \n" + "S.D R4, 10\n" + "SUBI R24, R24, 8\n"
//					+ "BNEZ R24, LOOP\n" + "ADD.D R4, R12, R4 \n" + "ADD.D R4, R12, R4 \n" + "SUB.D R4, R12, R4 \n");
//			wr.flush();
//			wr.close();
			microp.loadInstructions();

			microp.execute();


		}

		// allow user to enter the latency of each instruction

	}
}

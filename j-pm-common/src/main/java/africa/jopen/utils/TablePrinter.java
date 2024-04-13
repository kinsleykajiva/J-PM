package africa.jopen.utils;

public class TablePrinter {
	private final String[] header = {"id", "name", "version", "pid", "uptime", "status", "cpu", "mem", "user"};
	public   TablePrinter(String[][] data) {
		
		printTable(data);
	}
	
	public  void printTable(String[][] data) {
		// Calculate column widths
		int[] columnWidths = new int[header.length];
		for (int i = 0; i < header.length; i++) {
			columnWidths[i] = header[i].length();
			for (String[] row : data) {
				if (row[i].length() > columnWidths[i]) {
					columnWidths[i] = row[i].length();
				}
			}
		}
		
		// Print header
		printSeparator(columnWidths, "┌", "┬", "┐");
		printRow(header, columnWidths);
		printSeparator(columnWidths, "├", "┼", "┤");
		
		// Print data
		for (String[] row : data) {
			printRow(row, columnWidths);
		}
		
		// Print bottom border
		printSeparator(columnWidths, "└", "┴", "┘");
	}
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_RESET = "\u001B[0m";
	public  void printRow(String[] rowData, int[] columnWidths) {
		StringBuilder rowBuilder = new StringBuilder("│");
		for (int i = 0; i < rowData.length; i++) {
			if (rowData[i].equals("online")) {
				// Print in green if cell value is "online"
				rowBuilder.append(String.format("\u001B[32m %-" + columnWidths[i] + "s \u001B[0m│", rowData[i]));
			} else {
				rowBuilder.append(String.format(" %-" + columnWidths[i] + "s │", rowData[i]));
			}
		}
		System.out.println(rowBuilder);
	}
	
	public  void printSeparator(int[] columnWidths, String leftCorner, String middleCorner, String rightCorner) {
		StringBuilder separatorBuilder = new StringBuilder(leftCorner);
		for (int width : columnWidths) {
			separatorBuilder.append("─".repeat(width + 2)).append(middleCorner);
		}
		separatorBuilder.deleteCharAt(separatorBuilder.length() - 1); // Remove the last middle corner
		separatorBuilder.append(rightCorner);
		System.out.println(separatorBuilder);
	}
}

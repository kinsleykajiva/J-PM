package africa.jopen.utils;

public class TablePrinter {
	private String[] header = {"id", "name", "version", "pid", "uptime", "status", "cpu", "mem", "user"};
	public   TablePrinter(String[][] data) {
		//
		String[][] data1 = {
				{"1", "airtime-service", "0.39.3", "892556", "6D", "online", "0%", "67.0mb", "root"},
				{"6", "bulkemails-service", "0.39.3", "740232", "12D", "online", "0%", "66.8mb", "root"},
				{"2", "bulksms-service", "0.39.3", "624537", "17D", "online", "0%", "66.0mb", "root"},
				{"5", "social-media-service", "0.39.3", "1011112", "62m", "online", "0%", "64.1mb", "root"}
		};
		
		// Print table
		printTable(header, data);
	}
	
	public  void printTable(String[] header, String[][] data) {
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

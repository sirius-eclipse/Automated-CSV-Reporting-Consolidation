/*==============================================================================================
 *  Name: CSV Performance Test report consolidating automation utility
 *  Author: Barbuta Mihail-Gabriel - barbuta.mihail@ro.ibm.com
 *  Company: IBM Romania - Global Delivery Center Bucharest
 *  Date: 2011-05-15 / Revision: 1.5
 *  
 *  Developed during Yahoo! Open Hack, Bucharest, 14-15 May 2011
 *	
 *	Description:
 *	A class that can be used in Performance Test automation for consolidating CSV performance
 *		reports. It targets a long-time unsolved problem with test automation, the time wasted
 *		on putting together 48 or more test reports daily. This class will fetch all individual
 *		reports from a directory, parse the performance data, and output it into a single report
 *		that can be imported in a spreadsheet or publishing software.
 *==============================================================================================
 */

import java.io.*;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import au.com.bytecode.opencsv.CSVReader;	// Open-source, commercial-friendly licensed
import au.com.bytecode.opencsv.CSVWriter;	// 	libraries for handling CSV files

public class CSV_ReportingConsolidator {
	
	public static void main(String[] args) throws IOException {

		// Construct an array containing the list of files in the input folder
		String inputPath = "input/"; 				// Set the directory containing the CSV files
		String outputPath = "output/";				// Set the output directory for the consolidated report
		String outputFile = "Consolidated_CSV_Report.csv";
		File folder = new File(inputPath);			// Load the selected path
		File[] listOfFiles = folder.listFiles(); 	// Retrieve the list of files from the directory
		
		// Serialize the reference headers to write the output CSV header
		CSVReader referenceReader = new CSVReader(new FileReader("reference/example_fields.csv"));
		String [] referenceHeaders = referenceReader.readNext();
		CSVWriter writer = new CSVWriter(new FileWriter(outputPath+outputFile), ',', CSVWriter.NO_QUOTE_CHARACTER);
		
		System.out.println("-- CSV parser initiated, found " +listOfFiles.length+ " input files.\n");
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
			if (listOfFiles[i].isFile()) {
				String filename = listOfFiles[i].getName();		// Retrieve the file name
				
				if (!filename.endsWith("csv"))	{				// Check if the file has a CSV extension
					System.out.println("EE | Fatal error: The input path contains non-csv files: " +filename+ 
									   ".\n Please remove them and try again.");
					writer.close();
					System.exit(1);								// Exit if non-CSV files are found
				}
				
				String filePath=String.valueOf(inputPath+filename);	// Combine the path with the filename
				File file = new File(filePath);
				CSVReader csvFile = new CSVReader(new FileReader(filePath));
				String [] nextLine;		// CSV line data container
				int rowIterator = 0;	// Used to loop between rows
				int colIterator = 0;	// Used to loop between columns
				int rowCount = 0;		// Used to count the total number of rows
				int pageCount = 0;
				int f = 0;
				
				String [] pageName = new String[100];	// Holder for Page names
				double [] individualPRT = new double[100];	// Holder for Page Response Times
				String PTrun = "";		// Name of Performance Test Run
				String startTime = "";	// Test start time

				double PRT = 0;			// Average Page Response Time
		        double PRd = 0;			// Page Response Time Standard Deviation
		        double ERT = 0;			// Average Element Response Time
		        double ERd = 0;			// Element Response Time Standard Deviation
		        double MRT = 0;			// Maximum Page Response Time
		        double mRT = 0;			// Minimum Page Response Time
				int elapsedTime = 0;	// Test Elapsed Time
				int completedUsers = 0;	// Number of Completed Users
				int TPA = 0;			// Total Page Attempts
				int TPH = 0;			// Total Page Hits
				int TEA = 0;			// Total Element Attempts
				int TEH = 0;			// Total Element Hits
				
				// Fetch the total row count:
				FileReader fr = new FileReader(file);
		        LineNumberReader ln = new LineNumberReader(fr);
		        while (ln.readLine() != null)	{	rowCount++;		}
		        ln.close();				// Close the file reader
		        
		        // Fetch test identification data:
		        nextLine = csvFile.readNext();
		        PTrun = nextLine[1];	// Name of Performance Test Run
		        nextLine = csvFile.readNext();
		        startTime =nextLine[1];	// Performance Test Start Time
		        
		        // Skip 9 uninteresting rows:
		        while (rowIterator < 9) {
					nextLine = csvFile.readNext();
					rowIterator++;
				}
		        
		        // Check if there are VP fails (adds another column)
		        if (nextLine[9].equals("Total Page VPs Error For Run"))	{
		        	f = 2;
		        }
		        else if (nextLine[8].equals("Total Page VPs Failed For Run") || nextLine[8].equals("Total Page VPs Error For Run"))	{
		        	f = 1;
		        }
		        else	{
		        	f = 0;
		        }
		        
		        
		        // Read the page titles:
		        while (colIterator != -1)	{
		        	pageName[colIterator] = nextLine[colIterator+18+f];
		        	if((pageName[colIterator].equals(pageName[0])) && colIterator > 0)	{
		        		pageCount = colIterator;
		        		pageName[colIterator] = null;
		        		colIterator = -1;	// Detects when the page titles start to repeat
		        	}
		        	else	{
		        		colIterator++;
		        	}
		        }
		        
		        // Retrieve non-continuous performance data, auto-detect gaps, auto-convert in seconds where needed
		        nextLine = csvFile.readNext();
		        nextLine = csvFile.readNext();
		        while (rowIterator < rowCount-3) {
		        	if (nextLine.length > 1)	{	if (nextLine[0].length() != 0)		{ elapsedTime = Integer.parseInt(nextLine[0])/1000;	}}
		        	if (nextLine.length > 5)	{	if (nextLine[5].length() != 0)		{ completedUsers = Integer.parseInt(nextLine[5]);	}}
		        	if (nextLine.length > 8+f)	{	if (nextLine[8+f].length() != 0)	{ TPA = (int)Double.parseDouble(nextLine[8+f]);		}}
		        	if (nextLine.length > 9+f)  {	if (nextLine[9+f].length() != 0)	{ TPH = (int)Double.parseDouble(nextLine[9+f]);		}}
		        	if (nextLine.length > 14+f) {	if (nextLine[14+f].length() != 0)	{ TEA = (int)Double.parseDouble(nextLine[14+f]);	}}
		        	if (nextLine.length > 15+f) {	if (nextLine[15+f].length() != 0)	{ TEH = (int)Double.parseDouble(nextLine[15+f]);	}}
		        	if (nextLine.length > 10+f) {	if (nextLine[10+f].length() != 0)	{ PRT = Double.parseDouble(nextLine[10+f])/1000;	}}
		        	if (nextLine.length > 11+f) {	if (nextLine[11+f].length() != 0)	{ PRd = Double.parseDouble(nextLine[11+f])/1000;	}}
		        	if (nextLine.length > 16+f) {	if (nextLine[16+f].length() != 0)	{ ERT = Double.parseDouble(nextLine[16+f])/1000;	}}
		        	if (nextLine.length > 17+f) {	if (nextLine[17+f].length() != 0)	{ ERd = Double.parseDouble(nextLine[17+f])/1000;	}}
		        	if (nextLine.length > 12+f) {	if (nextLine[12+f].length() != 0)	{ MRT = Double.parseDouble(nextLine[12+f])/1000;	}}
		        	if (nextLine.length > 13+f) {	if (nextLine[13+f].length() != 0)	{ mRT = Double.parseDouble(nextLine[13+f])/1000;	}}
		        	
					nextLine = csvFile.readNext();
					rowIterator++;
				}
				
				// Convert the elapsed time from seconds to HH:MM:SS format
				int hours = elapsedTime / 3600,
				remainder = elapsedTime % 3600,
				minutes = remainder / 60,
				seconds = remainder % 60;
				String eTime  = (hours < 10 ? "0" : "") + hours 
						+ ":" + (minutes < 10 ? "0" : "") + minutes
						+ ":" + (seconds < 10 ? "0" : "") + seconds;
				
				csvFile.close();	// File recycled to reset the line parser
				CSVReader csvFile2 = new CSVReader(new FileReader(filePath));
				
				// Reset iterators to allow re-usage:
		        rowIterator = 0;
				colIterator = 0;
				
				// Skip first 13 rows:
				while (rowIterator < 13) {
					nextLine = csvFile2.readNext();
					rowIterator++;
				}
				
				// Dynamically retrieve individual page response times in seconds, correlate with page names:
				while (rowIterator < rowCount)	{
					while (colIterator < pageCount)	{
						if (nextLine.length > 18+f)	{
							if (nextLine[colIterator+18+f].length() != 0)	{
								individualPRT[colIterator] = Double.parseDouble(nextLine[colIterator+18+f])/1000;
							}
						}
						colIterator++;
					}
		 			nextLine = csvFile2.readNext();
					rowIterator++;
					colIterator = 0;
				}
				
				csvFile2.close();	// Final file closing
				
				// Reset iterators to allow re-usage:
		        rowIterator = 0;
				colIterator = 0;
				
				// Display statistics in console, enable only for debugging purposes:
				/*
				System.out.println(" Elapsed Time: " + elapsedTime
									+ "\n Completed Users: " + completedUsers
									+ "\n Total Page Attempts: " + TPA
									+ "\n Total Page Hits: " + TPH
									+ "\n Average Response Time For All Pages For Run: " + PRT
									+ "\n Response Time Standard Deviation For All Pages For Run: " + PRd
									+ "\n Maximum Response Time For All Pages For Run: " + MRT
									+ "\n Minimum Response Time For All Pages For Run: " + mRT
									+ "\n Total Page Element Attempts: " + TEA
									+ "\n Total Page Element Hits: " + TEH
									+ "\n Average Response Time For All Page Elements For Run: " + ERT
									+ "\n Response Time Standard Deviation For All Page Elements For Run: " + ERd
									+ "\n");
				
				// Display individual page response times in console:
				while (colIterator < 9)	{
					System.out.println("Page " + Page[colIterator] + " - Response Time: " + PagePRT[colIterator]);
					colIterator++;
				}
				*/
				
				// Serialize individual Page Response Times into CSV values
				StringBuffer individualPRTList = new StringBuffer();
				if(individualPRT.length > 0){
					individualPRTList.append(String.valueOf(individualPRT[0]));
					for(int k=1;k<pageCount;k++)	{
						individualPRTList.append(",");
						individualPRTList.append(String.valueOf(individualPRT[k]));
					}
				}
				
				// Serialize all retrieved performance parameters:
				String[] entries = {PTrun,startTime,
						String.valueOf(completedUsers),
						eTime,
						String.valueOf(TPA),
						String.valueOf(TPH),
						String.valueOf(PRT),
						String.valueOf(PRd),
						String.valueOf(MRT),
						String.valueOf(mRT),
						String.valueOf(TEA),
						String.valueOf(TEH),
						String.valueOf(ERT),
						String.valueOf(ERd),"",
						individualPRTList.toString(),
						};
				
				// Define header and write it to the first CSV row
				Object[] headerConcatenator = ArrayUtils.addAll(referenceHeaders,pageName);
				String[] header = new String[referenceHeaders.length+pageCount];
				header = Arrays.copyOf(headerConcatenator, header.length, String[].class);
				
				if(i==0)	{
					writer.writeNext(header);	// Write CSV header
				}
				writer.writeNext(entries);		// Write performance parameters in CSV format
				System.out.println("== Processed: " + filename + " ===========================");
			}
		}
		writer.close();							// Close the CSV writer
		System.out.println(  "\n-- Done processing " + listOfFiles.length + " files."
							+"\n-- The consolidated report has been saved to " + outputPath + outputFile);
	}
}
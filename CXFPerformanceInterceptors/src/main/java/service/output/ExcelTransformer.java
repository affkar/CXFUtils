package service.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;

import service.performance.CollatorDaemon;
import service.performance.Reportable;
import service.performance.ReportablePerformance;
import service.performance.ReportablePerformanceAndThroughputInfo;

public class ExcelTransformer {
	
	private String workBookDirectory;
	private String workBookTemplate = "workbook-template.xls";
	private String workBookPrefix = "workbook";

	public ExcelTransformer(String workBookDirectory) {
		this.workBookDirectory=workBookDirectory;
	}
	
	public void toExcel() throws IOException{
		toExcel("");
	}
	
	public void toExcel(String workBookKey) throws IOException {

		FileInputStream fis = new FileInputStream(getSlashed(workBookDirectory)+workBookTemplate);
		POIFSFileSystem poifs = new POIFSFileSystem(fis);
		Workbook wb = new HSSFWorkbook(poifs);
		CellStyle defaultMergedCellStyle = wb.createCellStyle();
		defaultMergedCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		defaultMergedCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		CellStyle defaultCellStyle = wb.createCellStyle();

		Sheet sheet1 = getOrCreateSheet(wb, "sheet1");
		CellSequencer sequence = new CellSequencer();

		Collection<ReportablePerformanceAndThroughputInfo> info = CollatorDaemon
				.getInstance().getReportableInfo();
		for (Iterator iterator = info.iterator(); iterator.hasNext();) {
			ReportablePerformanceAndThroughputInfo reportablePerformanceAndThroughputInfo = (ReportablePerformanceAndThroughputInfo) iterator
					.next();

			for (Entry<Integer, ReportablePerformance> entry : reportablePerformanceAndThroughputInfo
					.getReportablePerformance().entrySet()) {
				createCell(sheet1, sequence.getRowIndex(),
						sequence.getColumnIndex(),
						reportablePerformanceAndThroughputInfo
								.getServiceAndOperation().getService(),
						defaultCellStyle);
				createCell(sheet1, sequence.getRowIndex(), sequence
						.nextColumn().getColumnIndex(),
						reportablePerformanceAndThroughputInfo
								.getServiceAndOperation().getOperation(),
						defaultCellStyle);
				createCell(sheet1, sequence.getRowIndex(), sequence
						.nextColumn().getColumnIndex(),
						entry.getKey().toString(),
						defaultCellStyle);
				createNumericCell(sheet1, sequence.getRowIndex(), sequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getRequestsIn(), defaultCellStyle);
				createNumericCell(sheet1, sequence.getRowIndex(), sequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getResponsesOut(), defaultCellStyle);
				createNumericCell(sheet1, sequence.getRowIndex(), sequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getFaultsOut(), defaultCellStyle);
				addReportableCells(defaultCellStyle, sheet1, sequence, entry
						.getValue().getResponseTimesInMs());
				addReportableCells(defaultCellStyle, sheet1, sequence, entry
						.getValue().getFaultOutTimesInMs());
				addReportableCells(defaultCellStyle, sheet1, sequence, entry
						.getValue().getRequestPayloadSizes());
				addReportableCells(defaultCellStyle, sheet1, sequence, entry
						.getValue().getResponsePayloadSizes());
				addReportableCells(defaultCellStyle, sheet1, sequence, entry
						.getValue().getFaultPayloadSizes());
				sequence.nextRow();
			}
		}
		fis.close();
		//optimise if needed.
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String outputFile = getSlashed(workBookDirectory)+workBookPrefix+"-"+ workBookKey +"-"+df.format(new Date(System.currentTimeMillis()))+".xls";
		OutputStream fos = new FileOutputStream(new File(outputFile));
		wb.write(fos);
		fos.flush();
		fos.close();
	}

	private String getSlashed(String workBookDirectory2) {
		if(workBookDirectory2==null || workBookDirectory2.equals("")){
			return "";
		}else{
			char lastChar = workBookDirectory2.charAt(workBookDirectory2.length()-1);
			if(lastChar=='/'){
				return workBookDirectory2;
			}else if(lastChar=='\\'){
				return workBookDirectory2;
			}else{
				return workBookDirectory2+"/";
			}
		}
	}

	private void addReportableCells(CellStyle defaultCellStyle, Sheet sheet1,
			CellSequencer sequence, Reportable reportable) {
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMinimum(),
				defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMedian(),
				defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getLine90Percent(),
				defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMaximum(),
				defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getAverage(),
				defaultCellStyle);
	}

	private Sheet getOrCreateSheet(Workbook wb, String string) {
		Sheet sheet = null;
		if ((sheet = wb.getSheet(string)) != null) {
			return sheet;
		}
		return wb.createSheet(string);
	}

	public void createMergedCell(Sheet sheet, int fromRowIndex,
			int fromColumnIndex, int toRowIndex, int toColumnIndex,
			String value, CellStyle cellStyle) {
		createCell(sheet, fromRowIndex, fromColumnIndex, value, cellStyle);
		sheet.addMergedRegion(new CellRangeAddress(fromRowIndex, toRowIndex,
				fromColumnIndex, toColumnIndex));
	}

	

	public void createCell(Sheet sheet, int rowIndex, int columnIndex,
			String value, CellStyle cellStyle) {
		CellUtil.createCell(CellUtil.getRow(rowIndex, sheet), columnIndex,
				value, cellStyle);
	}
	
	public void createNumericCell(Sheet sheet, int rowIndex, int columnIndex,
			Number value, CellStyle cellStyle) {
		CellUtil.getRow(rowIndex, sheet).createCell(columnIndex).setCellValue(value.doubleValue());;
	}
	
	


	public static void main(String[] args) throws IOException {
		new ExcelTransformer("").toExcel();
	}

}

class CellSequencer {
	private int rowIndex = 2;
	private int columnIndex;

	CellSequencer nextRow() {
		rowIndex++;
		columnIndex = 0;
		return this;
	}

	CellSequencer nextColumn() {
		columnIndex++;
		return this;
	}

	int getRowIndex() {
		return rowIndex;
	}

	int getColumnIndex() {
		return columnIndex;
	}

}
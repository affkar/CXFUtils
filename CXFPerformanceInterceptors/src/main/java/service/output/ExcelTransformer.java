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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import service.performance.CollatorDaemon;
import service.performance.Reportable;
import service.performance.ReportablePerformance;
import service.performance.ReportablePerformanceAndThroughputInfo;

public class ExcelTransformer {

	private String workBookDirectory;
	private String workBookTemplate = "workbook-template.xlsx";
	private String workBookPrefix = "workbook";

	public ExcelTransformer(String workBookDirectory) {
		this.workBookDirectory = workBookDirectory;
	}

	public void toExcel() throws IOException, InvalidFormatException {
		toExcel("");
	}

	public void toExcel(String workBookKey) throws IOException, InvalidFormatException {

		FileInputStream fis = new FileInputStream(getSlashed(workBookDirectory)
				+ workBookTemplate);
		OPCPackage pfk =OPCPackage.open(fis);
		Workbook wb = new XSSFWorkbook(pfk);
		CellStyle defaultMergedCellStyle = wb.createCellStyle();
		defaultMergedCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		defaultMergedCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		CellStyle defaultCellStyle = wb.createCellStyle();

		Sheet statisticsSheet = getOrCreateSheet(wb, "statistics");
		CellSequencer statisticsSequence = new CellSequencer(2);

		Sheet payloadsSheet = getOrCreateSheet(wb, "payloads");
		CellSequencer payloadsSequence = new CellSequencer(1);
		
		Collection<ReportablePerformanceAndThroughputInfo> info = CollatorDaemon
				.getInstance().getReportableInfo();
		for (Iterator iterator = info.iterator(); iterator.hasNext();) {
			ReportablePerformanceAndThroughputInfo reportablePerformanceAndThroughputInfo = (ReportablePerformanceAndThroughputInfo) iterator
					.next();

			for (Entry<Integer, ReportablePerformance> entry : reportablePerformanceAndThroughputInfo
					.getReportablePerformance().entrySet()) {
				createCell(statisticsSheet, statisticsSequence.getRowIndex(),
						statisticsSequence.getColumnIndex(),
						reportablePerformanceAndThroughputInfo
								.getServiceAndOperation().getService(),
						defaultCellStyle);
				createCell(statisticsSheet, statisticsSequence.getRowIndex(), statisticsSequence
						.nextColumn().getColumnIndex(),
						reportablePerformanceAndThroughputInfo
								.getServiceAndOperation().getOperation(),
						defaultCellStyle);
				createCell(statisticsSheet, statisticsSequence.getRowIndex(), statisticsSequence
						.nextColumn().getColumnIndex(), entry.getKey()
						.toString(), defaultCellStyle);
				createNumericCell(statisticsSheet, statisticsSequence.getRowIndex(), statisticsSequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getRequestsIn(), defaultCellStyle);
				createNumericCell(statisticsSheet, statisticsSequence.getRowIndex(), statisticsSequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getResponsesOut(), defaultCellStyle);
				createNumericCell(statisticsSheet, statisticsSequence.getRowIndex(), statisticsSequence
						.nextColumn().getColumnIndex(), entry.getValue()
						.getFaultsOut(), defaultCellStyle);
				addReportableCells(defaultCellStyle, statisticsSheet, statisticsSequence, entry
						.getValue().getResponseTimesInMs());
				addReportableCells(defaultCellStyle, statisticsSheet, statisticsSequence, entry
						.getValue().getFaultOutTimesInMs());
				addReportableCells(defaultCellStyle, statisticsSheet, statisticsSequence, entry
						.getValue().getRequestPayloadSizes());
				addReportableCells(defaultCellStyle, statisticsSheet, statisticsSequence, entry
						.getValue().getResponsePayloadSizes());
				addReportableCells(defaultCellStyle, statisticsSheet, statisticsSequence, entry
						.getValue().getFaultPayloadSizes());
				for (Iterator iterator2 = entry.getValue().getExchanges()
						.keySet().iterator(); iterator2.hasNext();) {
					Integer next=(Integer)iterator2.next();
					createNumericCell(payloadsSheet, payloadsSequence.getRowIndex(), payloadsSequence
							.getColumnIndex(), next, defaultCellStyle);
					createCell(payloadsSheet, payloadsSequence.getRowIndex(), payloadsSequence
							.nextColumn().getColumnIndex(),
							reportablePerformanceAndThroughputInfo
									.getServiceAndOperation().getOperation(),
							defaultCellStyle);
					createCell(payloadsSheet, payloadsSequence.getRowIndex(), payloadsSequence
							.nextColumn().getColumnIndex(), entry.getValue()
							.getExchanges().get(next)
							.getRequestPayload(), defaultCellStyle);
					createCell(payloadsSheet, payloadsSequence.getRowIndex(), payloadsSequence
							.nextColumn().getColumnIndex(), entry.getValue()
							.getExchanges().get(next)
							.getResponsePayload(), defaultCellStyle);
					payloadsSequence.nextRow();
				}
				statisticsSequence.nextRow();
			}
		}
		fis.close();
		// optimise if needed.
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String outputFile = getSlashed(workBookDirectory) + workBookPrefix
				+ "-" + workBookKey + "-"
				+ df.format(new Date(System.currentTimeMillis())) + ".xlsx";
		OutputStream fos = new FileOutputStream(new File(outputFile));
		wb.write(fos);
		fos.flush();
		fos.close();
		pfk.close();
	}

	private String getSlashed(String workBookDirectory2) {
		if (workBookDirectory2 == null || workBookDirectory2.equals("")) {
			return "";
		} else {
			char lastChar = workBookDirectory2.charAt(workBookDirectory2
					.length() - 1);
			if (lastChar == '/') {
				return workBookDirectory2;
			} else if (lastChar == '\\') {
				return workBookDirectory2;
			} else {
				return workBookDirectory2 + "/";
			}
		}
	}

	private void addReportableCells(CellStyle defaultCellStyle, Sheet sheet1,
			CellSequencer sequence, Reportable reportable) {
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMinimum(), defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMedian(), defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getLine90Percent(),
				defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getMaximum(), defaultCellStyle);
		createNumericCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getAverage(), defaultCellStyle);
		createCell(sheet1, sequence.getRowIndex(), sequence.nextColumn()
				.getColumnIndex(), reportable.getRawData().toString(),
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
		CellUtil.getRow(rowIndex, sheet).createCell(columnIndex)
				.setCellValue(value.doubleValue());
		;
	}

	public static void main(String[] args) throws IOException, InvalidFormatException {
		ExcelTransformer excelGuy = new ExcelTransformer(".");
		FileInputStream fis = new FileInputStream(excelGuy.workBookTemplate);
		OPCPackage pfk =OPCPackage.open(fis);
		Workbook wb = new XSSFWorkbook(pfk);
		Sheet sheet1 = excelGuy.getOrCreateSheet(wb, "sheet1");
		for (int i = 2; i < 1000; i++) {
			for (int j = 0; j < 1000; j++){
				excelGuy.createCell(sheet1, i, j, Integer.valueOf(j).toString(), null);
			}
		};
		fis.close();
		// optimise if needed.
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String outputFile = excelGuy.workBookPrefix
				+ "-" + "" + "-"
				+ df.format(new Date(System.currentTimeMillis())) + ".xlsx";
		OutputStream fos = new FileOutputStream(new File(outputFile));
		wb.write(fos);
		fos.flush();
		fos.close();
		pfk.close();
	}

}

class CellSequencer {
	private int rowIndex ;
	private int columnIndex;

	public CellSequencer(int startRowIndex) {
		rowIndex=startRowIndex;
	}
	
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
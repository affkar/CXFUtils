package service.output;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelTransformer {

	public void toExcel() throws IOException{
		Workbook wb=new HSSFWorkbook();
		Sheet sheet1 = wb.createSheet("Sheet1");
		
		Row row = sheet1.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue(1);
		CreationHelper createHelper = wb.getCreationHelper();
		
		
		
		
		FileOutputStream fos=new FileOutputStream("workbook.xls");
		wb.write(fos);
		fos.close();
		
		
	}
	
	public void createCell(Sheet sheet, int rowIndex, int columnIndex){
		
	}
	
	public static void main(String[] args) throws IOException {
		new ExcelTransformer().toExcel();
	}
	
}

package com.numpy.scraping.team17.utils;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelReader {

	private static final String INGREDIENTS_WORK_SHEET = "Diabetes-Hypothyroidism-Hyperte";
	private static final String SRC_TEST_RESOURCES_TEST_DATA = "src/test/resources/TestData/";

	public static void main(String[] args) {
		String excelFilePath = SRC_TEST_RESOURCES_TEST_DATA + "IngredientsAndComorbidities.xlsx";
		String sheetName = INGREDIENTS_WORK_SHEET;

		try (FileInputStream inputStream = new FileInputStream(excelFilePath)) {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheet(sheetName);

			// Assuming column index is 0 (first column)
			int columnIndex = 4;

			List<String> columnValues = getColumnValues(sheet, columnIndex);

			// Display the result
			System.out.println("Column Values: " + columnValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> getColumnValues(Sheet sheet, int columnIndex) {
		List<String> columnValues = new ArrayList<>();

		Iterator<Row> rowIterator = sheet.iterator();
		int rowsCount = 0;
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			if (rowsCount < 2) {
				// Don't process top 2 rows
			} else {
				Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String cellValue = getCellValueAsString(cell);
				if (cellValue.trim().length() > 0) {
					columnValues.add(cellValue);
				}
			}
			rowsCount++;
		}

		return columnValues;
	}

	private static String getCellValueAsString(Cell cell) {
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return cell.getCellFormula();
		default:
			return "";
		}
	}
}

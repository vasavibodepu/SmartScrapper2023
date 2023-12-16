package com.numpy.scraping.team17.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.numpy.scraping.team17.model.Recipe;

public class ExcelWriter {

    public static void writeRecipesToExcel(List<Recipe> recipes, String worksheetName, String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(worksheetName);

        createHeaderRow(sheet);

        int rowCount = 1;
        for (Recipe recipe : recipes) {
            Row row = sheet.createRow(rowCount++);
            writeRecipeToRow(recipe, row);
        }

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        String[] columns = {
                "Recipe ID",
                "Recipe Name",
                "Recipe URL",
                "Recipe Category",
                "Food Category",
                "Ingredients",
                "Preparation Time",
                "Cooking Time",
                "Preparation Method",
                "Nutrient Values",
                "Targeted Morbid Conditions"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }
    }

    private static void writeRecipeToRow(Recipe recipe, Row row) {
        row.createCell(0).setCellValue(recipe.getRecipeID());
        row.createCell(1).setCellValue(recipe.getRecipeName());
        row.createCell(2).setCellValue(recipe.getRecipeURL());
        row.createCell(3).setCellValue(recipe.getRecipeCategory());
        row.createCell(4).setCellValue(recipe.getFoodCategory());
        row.createCell(5).setCellValue(recipe.getIngredients());
        row.createCell(6).setCellValue(recipe.getPreparationTime());
        row.createCell(7).setCellValue(recipe.getCookingTime());
        row.createCell(8).setCellValue(recipe.getPreparationmethod());
        row.createCell(9).setCellValue(recipe.getNutrientValues());
        row.createCell(10).setCellValue(recipe.getTargettedMorbidConditions());
    }
    
//
//    public static void main(String[] args) {
//        // Example usage:
//    	Recipe recipe = new Recipe();
//    	recipe.setRecipeID("Test1");
//        List<Recipe> recipes = new ArrayList<Recipe>();
//        recipes.add(recipe);
//
//        String filePath = "src/test/resources/TestData/Hypothyroidism_All.xlsx";
//        writeRecipesToExcel(recipes,"Hypothyroidism_All", filePath);
//    }
}

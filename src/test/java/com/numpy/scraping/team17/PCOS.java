package com.numpy.scraping.team17;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class PCOS extends BaseRecipeExtractor {

	/**
	 * Export Recipes list for PCOS
	 * @throws InterruptedException
	 */
	@Test
	public void scrapDiabeticsRecipes() throws InterruptedException {

		// load the recipes page that has all recipes by category
		loadRecipiesPage();

		// find and click the link for morbidity (e.g., PCOS) from diet recipes page
		WebElement pcosReceipeLink = driver
				.findElement(By.xpath("//*[@title=\"Click here to see all recipes under PCOS\"]"));
		pcosReceipeLink.click();

		// Export all recipes for morbidity
		exportRecipes("PCOS",6,7);
	}
	
	/**
	 * 
	 */
	private void loadRecipiesPage() {
		// find and click the link recipes
		WebElement headerRecipesLink = driver.findElement(By.xpath("//div[normalize-space()='RECIPES']"));
		headerRecipesLink.click();
	}
}

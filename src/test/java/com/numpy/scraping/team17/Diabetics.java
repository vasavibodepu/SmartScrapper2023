package com.numpy.scraping.team17;

import org.testng.annotations.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;



/**
 * 
 * @author vbodepu Scrape Diabetics Recipes from TarlaDalal.com
 *
 */
public class Diabetics extends BaseRecipeExtractor {

	/**
	 * Export Recipes list for Diabetics
	 * @throws InterruptedException
	 */
	@Test
	public void scrapDiabeticsRecipes() throws InterruptedException {

		// load the recipes page that has all recipes by category
		loadRecipiesPage();

		// find and click the link for morbidity (e.g., hypothyroidism) from diet recipes page
		WebElement morbidityLink = driver
				.findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht46']"));
		morbidityLink.click();

		// Export all recipes for morbidity
		exportRecipes("Diabetics", 0, 1);
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

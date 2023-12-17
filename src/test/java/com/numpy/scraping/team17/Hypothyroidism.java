package com.numpy.scraping.team17;

import org.testng.annotations.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;



/**
 * 
 * @author vbodepu Scrape Hypothyroidism Recipes from TarlaDalal.com
 *
 */
public class Hypothyroidism extends BaseRecipeExtractor {

	/**
	 * Export Recipes list for Hypothyroidism
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void scrapHypothyroidismRecipes() throws InterruptedException {

		// load the recipes page that has all recipes by category
		loadRecipiesPage();

		// find and click the link for morbidity (e.g., hypothyroidism) from
		// diet recipes page
		WebElement hypothyroidismRecipesLink = driver
				.findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht211']"));
		hypothyroidismRecipesLink.click();

		// Export all recipes for morbidity
		exportRecipes("Hypothyroidism", 2, 3);
	}

	/**
	 * 
	 */
	private void loadRecipiesPage() {
		// find and click the link recipes
		WebElement homePageRecipesLink = driver.findElement(By.xpath("//div[normalize-space()='RECIPES']"));
		homePageRecipesLink.click();
	}
}

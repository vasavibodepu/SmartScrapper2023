package com.numpy.scraping.team17;
import org.testng.annotations.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Hypertension extends BaseRecipeExtractor{

	
	@Test
	public void scrapHypertensionRecipes() throws InterruptedException {

		// load the recipes page that has all recipes by category
		loadRecipiesPage();

		// find and click the link for morbidity (e.g., hypothyroidism) from
		// diet recipes page
		WebElement highbloodpreasureRecipesLink = driver
				.findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht152']"));
		highbloodpreasureRecipesLink.click();

		// Export all recipes for morbidity
		exportRecipes("Hypertension", 4, 5);
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